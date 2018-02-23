package ucla.nesl.ActivityLabeling.service.sensordataprocessing;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import ucla.nesl.ActivityLabeling.notification.NotificationHelper;
import ucla.nesl.ActivityLabeling.service.sensordataprocessing.motionactivity.MotionActivityCallback;
import ucla.nesl.ActivityLabeling.service.sensordataprocessing.motionactivity.MotionActivityDataCollector;
import ucla.nesl.ActivityLabeling.utils.SharedPreferenceHelper;
import ucla.nesl.ActivityLabeling.utils.ToastShortcut;


/**
 * Created by zxxia.
 *
 * We treat SensorDataProcessingService as both a foreground service and a bound service. We declare
 * it as a foreground service to collect data even when the activities disappear. We declare it as
 * a bound service because some activities have to get information from the service. In summary,
 * this service provides the following capabilities:
 *   - Collecting location data
 *   - Collecting motion activity data
 *   - Sending notifications upon location or motion activity changes
 */

public class SensorDataProcessingService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SensorDataProcessingService.class.getSimpleName();

    // Binder
    private final IBinder mBinder = new LocalBinder();

    // Service information
    private long serviceCreatedTimestampMs;

    // Notification related
    private NotificationHelper notificationHelper;
    private ToastShortcut toastHelper;

    // Location collection proxy and location status
    private AggressiveLocationDataCollector locationCollector;
    private Location currentLocation;

    // Motion activity collection proxy and activity status
    private MotionActivityDataCollector motionActivityCollector;
    private DetectedActivity mLastDetectedActivity = null;

    // Shared preference
    private SharedPreferenceHelper preferenceHelper;
    private SharedPreferences mSharedPreferences;


    //region Section: Service life cycle - treat as a foreground service
    // =============================================================================================
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // Service basic properties
        serviceCreatedTimestampMs = System.currentTimeMillis();

        // Acquire application properties/preferences
        preferenceHelper = new SharedPreferenceHelper(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Initialize helpers
        notificationHelper = new NotificationHelper(this);
        toastHelper = new ToastShortcut(this);

        // Initialize location data source
        locationCollector = new AggressiveLocationDataCollector(this, mLocationCallback);
        locationCollector.updateParameters(
                preferenceHelper.getLocationUpdateIntervalMsec(),
                preferenceHelper.getLocationMinimumDisplacementMeter()
        );
        locationCollector.start();

        // Initialize motion activity data source
        motionActivityCollector = new MotionActivityDataCollector(this, motionActivityCallback);
        motionActivityCollector.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        //notificationHelper.cancelNotification(NotificationHelper.Type.ACTIVITY_CHANGED);
        //notificationHelper.cancelNotification(NotificationHelper.Type.LOCATION_CHANGED);
        // Tells the system to not try to recreate the service after it has been killed

        // Declare itself as a foreground service
        notificationHelper.serviceNotifyStartingForeground(
                this, NotificationHelper.Type.FOREGROUND_SERVICE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //TODO: re-examine this method
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        locationCollector.stop();

        notificationHelper.cancelNotification(NotificationHelper.Type.ACTIVITY_CHANGED);
        notificationHelper.cancelNotification(NotificationHelper.Type.LOCATION_CHANGED);

        super.onDestroy();
    }
    //endregion

    //region Section: Service life cycle - binder part, and binder class
    // =============================================================================================
    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        //mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUbind");

        /*
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.


        //TODO: check requesting state
        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service");

            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }

            notificationHelper.serviceNotifyStartingForeground(
                    this, NotificationHelper.Type.FOREGROUND_SERVICE);
        }
        */
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public SensorDataProcessingService getService() {
            return SensorDataProcessingService.this;
        }
    }
    //endregion

    //region Section: Location callback and location request
    // =============================================================================================
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            currentLocation = locationResult.getLastLocation();
            notificationHelper.sendNotification(NotificationHelper.Type.LOCATION_CHANGED);
            Log.i(TAG, "Received Location Update");
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            if (!locationAvailability.isLocationAvailable()) {
                toastHelper.showLong("Current Location cannot be determined.");
            }
        }
    };
    //endregion

    //region Section: Motion activity callback
    // =============================================================================================
    private MotionActivityCallback motionActivityCallback = new MotionActivityCallback() {
        @Override
        public void onMotionActivityResult(ActivityRecognitionResult result) {
            DetectedActivity detectedActivity = result.getMostProbableActivity();

            if (detectedActivity != null && mLastDetectedActivity != null) {
                if (detectedActivity.getType() != mLastDetectedActivity.getType()) {
                    if (preferenceHelper.getSendingNotificationOnLocationChanged()) {
                        notificationHelper.sendNotification(NotificationHelper.Type.ACTIVITY_CHANGED);
                    }
                }
            }
            mLastDetectedActivity = detectedActivity;
        }
    };
    //endregion

    //region Section: Shared preference monitoring
    // =============================================================================================
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferenceHelper.KEY_LOCATION_UPDATE_INTERVAL) ||
                key.equals(SharedPreferenceHelper.KEY_LOCATION_MINIMUM_DISPLACEMENT)) {
            Log.i(TAG, "Location Setting Changed");
            locationCollector.updateParameters(
                    preferenceHelper.getLocationUpdateIntervalMsec(),
                    preferenceHelper.getLocationMinimumDisplacementMeter()
            );
        } else if (key.equals(SharedPreferenceHelper.KEY_ACTIVITY_DETECTION_INTERVAL)) {
            Log.i(TAG, "Activity Setting Changed");
            //removeActivityUpdates();
            //sendActivityUpdatesRequest();
        }
    }
    //endregion

    //region Section: Public facing methods to fetch sensing data
    // =============================================================================================
    public long getCreatedTimestampMs() {
        return serviceCreatedTimestampMs;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public DetectedActivity getCurrentMotionActivity() {
        return mLastDetectedActivity;
    }
    //endregion

}