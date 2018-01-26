package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.google.android.gms.location.DetectedActivity;

import java.util.Date;
import java.util.Locale;

/**
 * Created by zxxia on 12/24/17.
 */

public class Utils {
    static String timeToString(long t) {
        if (t == -1) {
            return "Not Available";
        }
        return DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(t)).toString();
    }

    static String locToString(double latitude, double longitude) {
        if (latitude == -1 || longitude == -1) {
            return "Unknown Location";
        } else {
            return "(" + String.valueOf(latitude) + ", " + String.valueOf(longitude) +")";
        }
    }


    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    static final String KEY_LOCATION_CHANGE_NOTIFICATION = "location_change_notification";
    static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";
    static final String KEY_LOCATION_MINIMUM_DISPLACEMENT = "location_minimum_displacement";
    static final String KEY_ACTIVITY_DETECTION_INTERVAL = "activity_detection_interval";
    static final String KEY_ACTIVITY_CHANGE_NOTIFICATION = "activity_change_notification";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }


    static boolean locationChangeNotification(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_CHANGE_NOTIFICATION, false);
    }
    static void setLocationChangeNotification(Context context, boolean locationChangeNotification) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_CHANGE_NOTIFICATION, locationChangeNotification)
                .apply();
    }


    static long locationUpdateInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_LOCATION_UPDATE_INTERVAL, 60000);
    }
    static void setLocationUpdateInterval(Context context, long locationUpdateInterval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_LOCATION_UPDATE_INTERVAL, locationUpdateInterval)
                .apply();
    }


    static float locationMinimumDisplacement(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, 50);
    }
    static void setLocationMinimumDisplacement(Context context, float locationMinimumDisplacement) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, locationMinimumDisplacement)
                .apply();
    }



    static long activityDetetionInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_ACTIVITY_DETECTION_INTERVAL, 60000);
    }
    static void setActivityDetectionInterval(Context context, long activityDetectionInterval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_ACTIVITY_DETECTION_INTERVAL, activityDetectionInterval)
                .apply();
    }

    static boolean activityChangeNotification(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ACTIVITY_CHANGE_NOTIFICATION, false);
    }
    static void setActivityChangeNotification(Context context, boolean activityChangeNotification) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_ACTIVITY_CHANGE_NOTIFICATION, activityChangeNotification)
                .apply();
    }

}
