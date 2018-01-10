package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.content.res.Resources;
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
}
