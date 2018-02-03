package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.google.android.gms.location.DetectedActivity;

import java.util.Date;
import java.util.Locale;

/**
 * Created by zxxia on 12/24/17.
 */

public class Utils {

    static final long INVALID_TIME = -1L;
    static final double INVALID_LOCATION_VAL = -1000.0;

    static String timeToString(long timeMs) {
        if (timeMs == INVALID_TIME) {
            return "Not Available";
        }
        return DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(timeMs)).toString();
    }


    static String locToString(Location location) {
        if (location == null) {
            return locToString(INVALID_LOCATION_VAL, INVALID_LOCATION_VAL);
        } else {
            return locToString(location.getLatitude(), location.getLongitude());
        }
    }

    static String locToString(double latitude, double longitude) {
        if (latitude == INVALID_LOCATION_VAL || longitude == INVALID_LOCATION_VAL) {
            return "Unknown Location";
        } else {
            return String.format(Locale.getDefault(), "(%.6f, %.6f)", latitude, longitude);
        }
    }

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In a vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On a bicycle";
            case DetectedActivity.ON_FOOT:
                return "On foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown activity";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unidentifiable activity: " + detectedActivityType;
        }
    }



    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    /*static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }*/

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    /*static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }*/


    static boolean locationChangeNotification(Context context) {location
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
