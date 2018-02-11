package ucla.nesl.ActivityLabeling;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    static final String CURRENT_LOCATION = "Current Location";

    private static final String TAG = MainActivity.class.getSimpleName();


    /**
     *  Constants for Activity Result Code
     */
    private static final int ACTIVITY_EDITOR_RESULT_REQUEST_CODE = 0;

    /**
     * Constants for Permission Request Code
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    /**
     * Keys for storing activity state in the Bundle.
     */
    private static final String KEY_ACTIVITY_LIST = "ActivityList";
    private static final String KEY_LAST_KNOWN_LOCATION = "LastKnownLocation";


    /**
     * Stores activity records
     */
    private ArrayList<ActivityDetail> actsList  = null;


    /**
     * Create an ArrayAdapter from List
     */
    private ActivityDetailListAdapter mActivityListAdapter;


    // UI Widgets.
    private ListView mActivitiesListView;
    private FloatingActionButton mAddActivityFab;
    private TextView mStorageStatTextView;
    private Button mStartLocationUpdateButton;
    private Button mStopLocationUpdateButton;


    // The BroadcastReceiver used to listen from broadcasts from the service.
    //private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mActivityListAdapter.updateService(mService);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    private Location mCurrentLocation;

    private ActivityStorageManager mStoreManager;


    private static int numOfSavedActivities = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Locate the UI widgets.
        mActivitiesListView = findViewById(R.id.ActivitiesListView);
        mAddActivityFab = findViewById(R.id.fab);
        mStorageStatTextView = findViewById(R.id.StorageStatsTextView);
        mStartLocationUpdateButton = findViewById(R.id.StartLocationUpdateBtn);
        mStopLocationUpdateButton = findViewById(R.id.StopLocationUpdateBtn);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Instantiate the UI widgets
        mStoreManager = new ActivityStorageManager(this);
        if (actsList == null) {
            actsList = new ArrayList<>();
            //display saved records within 24 hours
            actsList = mStoreManager.getActivityLogs();
            numOfSavedActivities = mStoreManager.getNumberOfStoredActivities();
        }

        mActivityListAdapter = new ActivityDetailListAdapter( MainActivity.this, actsList, mStoreManager, mService);
        mActivitiesListView.setAdapter(mActivityListAdapter);
        mAddActivityFab.setImageResource(R.drawable.plus_sign);
        mAddActivityFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Create a new activity log");
                mCurrentLocation = mService.getCurrentLocation();
                Intent intent = new Intent(getApplicationContext(), ActivityEditor.class);
                intent.putExtra(CURRENT_LOCATION, mCurrentLocation);
                startActivityForResult(intent, ACTIVITY_EDITOR_RESULT_REQUEST_CODE);
            }
        });

        mStorageStatTextView.setText("Number of activities recorded: " + String.valueOf(numOfSavedActivities));

        if (!checkPermissions()) {
            requestPermissions();
        }

        mStartLocationUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    mService.requestLocationUpdates();
                    mService.sendActivityUpdatesRequest();
                } else {
                    requestPermissions();
                }
            }
        });

        mStopLocationUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.removeLocationUpdates();
                mService.removeActivityUpdates();
            }
        });
        boolean requestLocationUpdates = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(SharedPreferenceHelper.KEY_REQUESTING_LOCATION_UPDATES, false);
        setButtonsState(requestLocationUpdates);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        //Save all unfinished activities to storage
        mStoreManager.saveOngoingActivities(actsList);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            Log.i(TAG, "Restore saved activities");
            if (savedInstanceState.keySet().contains(KEY_ACTIVITY_LIST)) {
                actsList = savedInstanceState.getParcelableArrayList(KEY_ACTIVITY_LIST);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_KNOWN_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LAST_KNOWN_LOCATION);
            }
        }
    }

    private boolean checkPermissions() {
        int fineLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int writePermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                writePermissionState == PackageManager.PERMISSION_GRANTED &&
                readPermissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, PERMISSIONS_REQUEST_CODE
        );
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if(grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted.");
                    //startLocationUpdates();
                    bindService(new Intent(this, LocationService.class), mServiceConnection,
                            Context.BIND_AUTO_CREATE);
                    //mService.requestLocationUpdates();
                } else {
                    // TODO: Handle permission denied case.
                    // Permission denied.
                    Log.i(TAG, "Permission denied.");
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case ACTIVITY_EDITOR_RESULT_REQUEST_CODE:
                if (resultCode  == RESULT_OK) {
                    Log.i(TAG, "Received results from EditorActivity");
                    actsList.add((ActivityDetail) data.getParcelableExtra(ActivityEditor.ACTIVITY_INFO));
                    mActivityListAdapter.notifyDataSetChanged();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        // Save the user's current activities list state
        savedInstanceState.putParcelableArrayList(KEY_ACTIVITY_LIST, actsList);
        savedInstanceState.putParcelable(KEY_LAST_KNOWN_LOCATION, mCurrentLocation);
        Log.i(TAG, "OnSaveInstanceState");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mStartLocationUpdateButton.setEnabled(false);
            mStopLocationUpdateButton.setEnabled(true);
        } else {
            mStartLocationUpdateButton.setEnabled(true);
            mStopLocationUpdateButton.setEnabled(false);
        }
    }

    public void incrementNumOfStoredActivities() {
        numOfSavedActivities++;
        mStorageStatTextView.setText(String.valueOf("Number of activities recorded: " + numOfSavedActivities));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update the buttons state depending on whether location updates are being requested.
        Log.i(TAG, "Location Update request changed");
        if (key.equals(SharedPreferenceHelper.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(
                    SharedPreferenceHelper.KEY_REQUESTING_LOCATION_UPDATES, false));
        }
    }
}