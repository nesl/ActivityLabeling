package ucla.nesl.ActivityLabeling;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


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
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 4;


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;


    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


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


    // Variables Required by Location Services

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;


    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;


    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;


    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;


    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;



    private Location mCurrentLocation;

    private ActivityStorageManager m_store;



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
        m_store = new ActivityStorageManager(this);
        if (actsList == null) {
            actsList = new ArrayList<>();
            //display saved records within 24 hours
            //actsList = m_store.getActivityLogs();
        }
        mActivityListAdapter = new ActivityDetailListAdapter(this, actsList, m_store);
        mActivitiesListView.setAdapter(mActivityListAdapter);
        mAddActivityFab.setImageResource(R.drawable.plus_sign);
        mAddActivityFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Create a new activity log");
                Intent intent = new Intent(getApplicationContext(), ActivityEditor.class);
                intent.putExtra(Constants.CURRENT_LOCATION, mCurrentLocation);
                startActivityForResult(intent, ACTIVITY_EDITOR_RESULT_REQUEST_CODE);
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION);
        }

        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        //Save all unfinished activities to storage
        m_store.saveActivities(actsList);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        try {
                            //noinspection MissingPermission
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                            Log.i(TAG, "Request Location Update");

                        } catch (java.lang.SecurityException ex) {
                            Log.i(TAG, "fail to request location update, ignore", ex);
                        } catch (IllegalArgumentException ex) {
                            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
                        }


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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
                    startLocationUpdates();
                } else {
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
                    actsList.add((ActivityDetail) data.getParcelableExtra(Constants.ACTIVITY_INFO));
                    mActivityListAdapter.notifyDataSetChanged();
                }
                break;
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_CANCELED) {
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        Log.i(TAG, "Create LocationCallback");
        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();

                // Update all activities's location except those already stopped by user
                for(int i = 0; i < actsList.size(); i++) {
                    if (actsList.get(i).m_end == -1) {
                        actsList.get(i).m_latitude = mCurrentLocation.getLatitude();
                        actsList.get(i).m_longitude = mCurrentLocation.getLongitude();
                    }
                }
                mActivityListAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), mCurrentLocation.toString(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "Received Location Update");
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()) {
                    Toast.makeText(getApplicationContext(), "Current Location cannot be determined.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }


    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
}