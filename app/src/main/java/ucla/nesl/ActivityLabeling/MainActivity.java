package ucla.nesl.ActivityLabeling;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ucla.nesl.ActivityLabeling.storage.UserActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivityStorageManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACTIVITY_EDITOR_RESULT_REQUEST_CODE = 0;

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    public static final String INTENT_KEY_CURRENT_LOCATION = "Current Location";

    // Keys for storing activity state in the Bundle.
    private static final String KEY_ACTIVITY_LIST = "ActivityList";
    private static final String KEY_LAST_KNOWN_LOCATION = "LastKnownLocation";

    private static final String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    // UI Widgets.
    private ListView mActivitiesListView;
    private FloatingActionButton mAddActivityFab;
    private TextView mStorageStatTextView;
    private Button mStartLocationUpdateButton;
    private Button mStopLocationUpdateButton;

    // UI helper
    private UserActivityListAdapter mActivityListAdapter;

    // A reference to the service used to get location updates.
    private SensorDataProcessingService mService = null;

    private UserActivityStorageManager mStoreManager;

    private int numSavedActivities;
    private ArrayList<UserActivity> actsList;


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

        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    // ==== UI Option menu =========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    // ==== Main procedure initialization ==========================================================
    private void startProcedure() {
        // Instantiate the UI widgets
        mStoreManager = new UserActivityStorageManager(this);

        //display saved records within 24 hours
        actsList = mStoreManager.getRecentActivities();
        numSavedActivities = mStoreManager.getNumTotalUserActivities();

        mActivityListAdapter = new UserActivityListAdapter(this, actsList, mStoreManager, mService);
        mActivitiesListView.setAdapter(mActivityListAdapter);
        mAddActivityFab.setImageResource(R.drawable.plus_sign);

        mStorageStatTextView.setText("Number of activities recorded: " + numSavedActivities);

        attachButtonClickEventListeners();

        startService(new Intent(this, SensorDataProcessingService.class));
    }

    // ==== Activity transition ====================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_EDITOR_RESULT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Received results from EditorActivity");
                    UserActivity newActivity = data.getParcelableExtra(UserActivityEditorActivity.ACTIVITY_INFO);
                    actsList.add(newActivity);
                    mActivityListAdapter.notifyDataSetChanged();

                    numSavedActivities++;
                    mStorageStatTextView.setText("Number of activities recorded: " + numSavedActivities);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ==== Service connection =====================================================================
    //TODO: need to re-examine the service connection object
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            SensorDataProcessingService.LocalBinder binder = (SensorDataProcessingService.LocalBinder) service;
            mService = binder.getService();
            mActivityListAdapter.updateService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    // ==== Button event registration and behavior definition ======================================
    private void attachButtonClickEventListeners() {
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

        mAddActivityFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Create a new activity log");
                Intent intent = new Intent(MainActivity.this, UserActivityEditorActivity.class);
                intent.putExtra(INTENT_KEY_CURRENT_LOCATION, mService.getCurrentLocation());
                startActivityForResult(intent, ACTIVITY_EDITOR_RESULT_REQUEST_CODE);
            }
        });
    }

    //TODO: not properly use this method
    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mStartLocationUpdateButton.setEnabled(false);
            mStopLocationUpdateButton.setEnabled(true);
        } else {
            mStartLocationUpdateButton.setEnabled(true);
            mStopLocationUpdateButton.setEnabled(false);
        }
    }

    // ==== Permission related =====================================================================
    private boolean checkPermissions() {
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length <= 0) {
                    // The user interaction was interrupted because the permission request is
                    // cancelled
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Permission denied.
                    Log.i(TAG, "Permission denied.");
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Permission granted.");

                } else {
                    //startLocationUpdates();
                    //bindService(new Intent(this, LocationService.class), mServiceConnection,
                    //        Context.BIND_AUTO_CREATE);
                    //mService.requestLocationUpdates();
                    startProcedure();
                }
                break;
        }
    }

}