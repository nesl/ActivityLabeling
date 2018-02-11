package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by timestring on 2/2/18.
 */

public class SharedPreferenceHelper {
    //TODO: make the scope of the following keys private
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    static final String KEY_LOCATION_CHANGE_NOTIFICATION = "location_change_notification";
    static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";
    static final String KEY_LOCATION_MINIMUM_DISPLACEMENT = "location_minimum_displacement";
    static final String KEY_ACTIVITY_DETECTION_INTERVAL = "activity_detection_interval";
    static final String KEY_ACTIVITY_CHANGE_NOTIFICATION = "activity_change_notification";

    private SharedPreferences mSharedPreferences;

    SharedPreferenceHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    boolean getRequestingLocationUpdates() {
        return mSharedPreferences.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    void setRequestingLocationUpdates(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES, value).apply();
    }

    boolean getLocationChangeNotification() {
        return mSharedPreferences.getBoolean(KEY_LOCATION_CHANGE_NOTIFICATION, false);
    }

    void setLocationChangeNotification(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_LOCATION_CHANGE_NOTIFICATION, value).apply();
    }

    long getLocationUpdateInterval() {
        return mSharedPreferences.getLong(KEY_LOCATION_UPDATE_INTERVAL, 60000L);
    }

    void setLocationUpdateInterval(long value) {
        mSharedPreferences.edit().putLong(KEY_LOCATION_UPDATE_INTERVAL, value).apply();
    }

    float getLocationMinimumDisplacement() {
        return mSharedPreferences.getFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, 50.f);
    }

    void setLocationMinimumDisplacement(float value) {
        mSharedPreferences.edit().putFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, value).apply();
    }

    long getActivityDetetionInterval() {
        return mSharedPreferences.getLong(KEY_ACTIVITY_DETECTION_INTERVAL, 60000L);
    }

    void setActivityDetectionInterval(long value) {
        mSharedPreferences.edit().putLong(KEY_ACTIVITY_DETECTION_INTERVAL, value).apply();
    }

    boolean getActivityChangeNotification() {
        return mSharedPreferences.getBoolean(KEY_ACTIVITY_CHANGE_NOTIFICATION, false);
    }

    void setActivityChangeNotification(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_ACTIVITY_CHANGE_NOTIFICATION, value).apply();
    }
}
