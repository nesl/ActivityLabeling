package ucla.nesl.ActivityLabeling;


import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String CURRENT_LOCATION = "Current Location";

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     *  Constants for Activity Result Code
     */
    private static final int ACTIVITY_EDITOR_RESULT_REQUEST_CODE = 0;

    /**
     * Constants for Permission Request Code
     */
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_CODE_EXTERNAL_STORAGE = 3;

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
            mService.requestLocationUpdates();
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

    /**
     * The entry point for interacting with activity recognition.
     */
    private ActivityRecognitionClient mActivityRecognitionClient;


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

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Instantiate the UI widgets
        mStoreManager = new ActivityStorageManager(this);
        if (actsList == null) {
            actsList = new ArrayList<>();
            //display saved records within 24 hours
            actsList = mStoreManager.getActivityLogs();
        }
        mActivityListAdapter = new ActivityDetailListAdapter(this, actsList, mStoreManager, mService);
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

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        sendActivityUpdatesRequest();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION);
        } else {
            bindService(new Intent(this, LocationService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }


        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE_EXTERNAL_STORAGE);
        }

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        //LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
          //      new IntentFilter(LocationService.ACTION_BROADCAST));
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

    private boolean checkLocationPermission() {
        int fineLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        int writePermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return writePermissionState == PackageManager.PERMISSION_GRANTED &&
                readPermissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION:
                if(grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location Access Permission granted.");
                    //startLocationUpdates();
                    bindService(new Intent(this, LocationService.class), mServiceConnection,
                            Context.BIND_AUTO_CREATE);
                    //mService.requestLocationUpdates();
                } else {
                    // TODO: Handle permission denied case. Currently, get stuck at this block.
                    // Permission denied.
                    Log.i(TAG, "Location Access Permission denied.");
                    Toast.makeText(this, "Location Access Permission denied.", Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSIONS_REQUEST_CODE_EXTERNAL_STORAGE:

                if(grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "External Storage Access Permission granted.");
                } else {
                    // TODO: Handle permission denied case. Currently, get stuck at this block.
                    // Finish the activity? or other logic
                    // Permission denied.
                    Log.i(TAG, "External Storage Access Permission denied.");
                    Toast.makeText(this, "External Storage Access Permission denied.", Toast.LENGTH_LONG).show();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    private void sendActivityUpdatesRequest() {

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                /*Toast.makeText(getApplicationContext(), "activity update request enabled",
                        Toast.LENGTH_SHORT)
                        .show();*/
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.w(TAG, "activity update request failed");
                Toast.makeText(getApplicationContext(),
                        "activity update request failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Removes activity recognition updates using
     * {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. Registers success and
     * failure callbacks.
     */
    public void removeActivityUpdates() {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "activity update remove success",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to enable activity recognition.");
                Toast.makeText(getApplicationContext(), "activity update remove failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}