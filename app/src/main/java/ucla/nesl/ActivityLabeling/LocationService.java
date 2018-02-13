package ucla.nesl.ActivityLabeling;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import ucla.nesl.ActivityLabeling.utils.SharedPreferenceHelper;

public class LocationService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = LocationService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_0";

    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10000;


    private static final float MINIMUM_DISPLACEMENT_IN_METERS = 50;
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
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_FOREGROUND_SERVICE_ID = 12345;
    private static final int NOTIFICATION_LOCATION_CHANGED_ID = 12346;
    private static final int NOTIFICATION_ACTIVITY_CHANGED_ID = 12347;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;


    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;




    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;


    private final IBinder mBinder = new LocalBinder();

    /**
     * The entry point for interacting with activity recognition.
     */
    private ActivityRecognitionClient mActivityRecognitionClient;

    private DetectedActivity mLastDetectedActivity = null;

    private DetectedActivityReceiver mReceiver;

    private SharedPreferenceHelper preferenceHelper;
    private SharedPreferences mSharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();


        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mReceiver = new DetectedActivityReceiver();

        preferenceHelper = new SharedPreferenceHelper(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mNotificationManager.cancel(NOTIFICATION_LOCATION_CHANGED_ID);
        mNotificationManager.cancel(NOTIFICATION_ACTIVITY_CHANGED_ID);
        mServiceHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter(DetectedActivitiesIntentService.ACTION_BROADCAST));


        mNotificationManager.cancel(NOTIFICATION_LOCATION_CHANGED_ID);
        mNotificationManager.cancel(NOTIFICATION_ACTIVITY_CHANGED_ID);
        // Tells the system to not try to recreate the service after it has been killed
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUbind");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.

        //TODO: check requesting state
        if (!mChangingConfiguration) {
            Log.i(TAG, "Starting foreground service");
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
            startForeground(NOTIFICATION_FOREGROUND_SERVICE_ID, getNotification(NOTIFICATION_FOREGROUND_SERVICE_ID));
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }


    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification(int notificationID) {
        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        String title;
        switch (notificationID) {
            case NOTIFICATION_FOREGROUND_SERVICE_ID:
                title = "Location and Google Activity Service";
                break;
            case NOTIFICATION_LOCATION_CHANGED_ID:
                title = "Location Changed";
                break;
            case NOTIFICATION_ACTIVITY_CHANGED_ID:
                title = "Google Detected Activity Changed";
                break;
            default:
                title = "";
                break;
        }
        NotificationCompat.Builder builder;
        if (notificationID == NOTIFICATION_FOREGROUND_SERVICE_ID) {
            CharSequence text = "Monitoring location and user activity";
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentText(text)
                    .setContentTitle(title)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(activityPendingIntent);
        } else {
            CharSequence text = "You might want to update your activity information";
            builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentText(text)
                    .setContentTitle(title)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(activityPendingIntent);
        }


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }



    // ==== Location callback and location request =================================================
    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            mLocation = locationResult.getLastLocation();

            boolean isForeground = serviceIsRunningInForeground(LocationService.this);
            boolean locationChangeNotification = preferenceHelper.getLocationChangeNotification();
            if (isForeground && locationChangeNotification) {
                mNotificationManager.notify(NOTIFICATION_LOCATION_CHANGED_ID,
                        getNotification(NOTIFICATION_LOCATION_CHANGED_ID));
            }
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

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(preferenceHelper.getLocationUpdateInterval());

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        //long fastestInterval = interval / 2;
        //mLocationRequest.setFastestInterval(fastestInterval);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(preferenceHelper.getLocationMinimumDisplacement());
    }

    /**
     * Makes a request for location updates.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");

        startService(new Intent(getApplicationContext(), LocationService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }


    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    private class DetectedActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DetectedActivity detectedActivity = intent.getParcelableExtra(DetectedActivitiesIntentService.EXTRA_DETECTED_ACTIVITY);

            if (detectedActivity != null && mLastDetectedActivity != null) {
                if (detectedActivity.getType() != mLastDetectedActivity.getType()) {
                    Log.i(TAG, "Different detected activities should send out notification.");

                    boolean isForeground = serviceIsRunningInForeground(LocationService.this);
                    boolean locationChangeNotification = preferenceHelper.getLocationChangeNotification();
                    if (isForeground && locationChangeNotification) {
                        mNotificationManager.notify(NOTIFICATION_ACTIVITY_CHANGED_ID, getNotification(NOTIFICATION_ACTIVITY_CHANGED_ID));
                    }
                } else {
                    Log.i(TAG, "Same detected activities.");
                }
            }
            mLastDetectedActivity = detectedActivity;
        }
    }

    public Location getCurrentLocation() {
        return mLocation;
    }


    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    public void sendActivityUpdatesRequest() {

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(), "activity update request enabled",
                        Toast.LENGTH_SHORT)
                        .show();
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


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO: comments here, not sure what's the intention here
        if (key.equals(SharedPreferenceHelper.KEY_LOCATION_UPDATE_INTERVAL) ||
                key.equals(SharedPreferenceHelper.KEY_LOCATION_MINIMUM_DISPLACEMENT)) {
            Log.i(TAG, "Location Setting Changed");
            createLocationRequest();

            removeLocationUpdates();
            requestLocationUpdates();
        } else if (key.equals(SharedPreferenceHelper.KEY_ACTIVITY_DETECTION_INTERVAL)) {
            Log.i(TAG, "Activity Setting Changed");
            removeActivityUpdates();
            sendActivityUpdatesRequest();
        }
    }
}