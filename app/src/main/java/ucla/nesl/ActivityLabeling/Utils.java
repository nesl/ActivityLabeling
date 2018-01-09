package ucla.nesl.ActivityLabeling;

import android.text.format.DateFormat;

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
}
