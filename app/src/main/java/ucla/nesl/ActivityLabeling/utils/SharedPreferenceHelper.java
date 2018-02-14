package ucla.nesl.ActivityLabeling.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by timestring on 2/2/18.
 *
 * A helper class which allows activities or services to easily access shared preferences globally.
 */

public class SharedPreferenceHelper {
    //TODO: make the scope of the following keys private
    public static final String KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED = "notification_location_changed";
    public static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";
    public static final String KEY_LOCATION_MINIMUM_DISPLACEMENT = "location_minimum_displacement";
    public static final String KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED = "notification_motion_changed";
    public static final String KEY_ACTIVITY_DETECTION_INTERVAL = "activity_detection_interval";

    private SharedPreferences mSharedPreferences;


    public SharedPreferenceHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getSendingNotificationOnLocationChanged() {
        return mSharedPreferences.getBoolean(KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED, false);
    }

    public void setSendingNotificationOnLocationChanged(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED, value).apply();
    }

    public long getLocationUpdateInterval() {
        return mSharedPreferences.getLong(KEY_LOCATION_UPDATE_INTERVAL, 60000L);
    }

    public void setLocationUpdateInterval(long value) {
        mSharedPreferences.edit().putLong(KEY_LOCATION_UPDATE_INTERVAL, value).apply();
    }

    public float getLocationMinimumDisplacement() {
        return mSharedPreferences.getFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, 50.f);
    }

    public void setLocationMinimumDisplacement(float value) {
        mSharedPreferences.edit().putFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, value).apply();
    }

    public boolean getSendingNotificationOnMotionChanged() {
        return mSharedPreferences.getBoolean(KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED, false);
    }

    public void getSendingNotificationOnMotionChanged(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED, value).apply();
    }

    public long getActivityDetetionInterval() {
        return mSharedPreferences.getLong(KEY_ACTIVITY_DETECTION_INTERVAL, 60000L);
    }

    public void setActivityDetectionInterval(long value) {
        mSharedPreferences.edit().putLong(KEY_ACTIVITY_DETECTION_INTERVAL, value).apply();
    }

}
