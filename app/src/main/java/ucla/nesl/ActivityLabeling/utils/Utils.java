package ucla.nesl.ActivityLabeling.utils;

import android.location.Location;
import android.text.format.DateFormat;

import java.util.Date;
import java.util.Locale;

/**
 * Created by zxxia on 12/24/17.
 *
 * Provide methods cover
 *   - Common objects to strings
 *   - String related utility
 */

public class Utils {

    public static final long INVALID_TIME = -1L;
    public static final double INVALID_LOCATION_VAL = -1000.0;

    //region Section: Objects to strings
    // =============================================================================================
    public static String timeToString(long timeMs) {
        if (timeMs == INVALID_TIME) {
            return "Not Available";
        }
        return DateFormat.format("HH:mm:ss MM/dd/yyyy", new Date(timeMs)).toString();
    }

    public static String locationToString(Location location) {
        if (location == null) {
            return locationToString(INVALID_LOCATION_VAL, INVALID_LOCATION_VAL);
        } else {
            return locationToString(location.getLatitude(), location.getLongitude());
        }
    }

    public static String locationToString(double latitude, double longitude) {
        if (latitude == INVALID_LOCATION_VAL || longitude == INVALID_LOCATION_VAL) {
            return "Unknown Location";
        } else {
            return String.format(Locale.getDefault(), "(%.6f, %.6f)", latitude, longitude);
        }
    }
    //endregion

    //region Section: String utilities
    // =============================================================================================
    public static String stringJoin(CharSequence delimiter, CharSequence... args) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (CharSequence cs : args) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(cs);
        }
        return builder.toString();
    }
    //endregion
}
