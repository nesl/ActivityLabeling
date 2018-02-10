package ucla.nesl.ActivityLabeling;

import android.location.Location;
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
}
