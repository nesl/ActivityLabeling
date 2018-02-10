package ucla.nesl.ActivityLabeling;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    // UI widgets
    private EditText locUpdateIntervalET;
    private EditText locMinDisplacementET;
    private Switch locNotificationSW;
    private EditText actDetectionIntervalET;
    private Switch actNotificationSW;
    private Button saveBtn;
    private Button cancelBtn;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Instantiate UI Widgets
        locUpdateIntervalET = findViewById(R.id.LocationUpdateIntervalEditText);
        locMinDisplacementET = findViewById(R.id.LocationMinimumDisplacementEditText);
        locNotificationSW = findViewById(R.id.LocationNotificationSwitch);
        actDetectionIntervalET = findViewById(R.id.ActivityDetectionIntervalEditText);
        actNotificationSW = findViewById(R.id.ActivityNotificationSwitch);
        saveBtn = findViewById(R.id.SettingsSaveBtn);
        cancelBtn = findViewById(R.id.SettingsCancelBtn);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        locUpdateIntervalET.setText(String.valueOf(
                mSharedPreferences.getLong(PreferenceKey.KEY_LOCATION_UPDATE_INTERVAL, 60000L)));
        locMinDisplacementET.setText(String.valueOf(
                mSharedPreferences.getFloat(PreferenceKey.KEY_LOCATION_MINIMUM_DISPLACEMENT, 50.f)));
        locNotificationSW.setChecked(
                mSharedPreferences.getBoolean(PreferenceKey.KEY_LOCATION_CHANGE_NOTIFICATION, false));
        actDetectionIntervalET.setText(String.valueOf(
                mSharedPreferences.getLong(PreferenceKey.KEY_ACTIVITY_DETECTION_INTERVAL, 60000L)));
        actNotificationSW.setChecked(
                mSharedPreferences.getBoolean(PreferenceKey.KEY_ACTIVITY_CHANGE_NOTIFICATION, false));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: let's do in-place check instead of checking in the very end.
                long locUpdateInterval = stringToLong(locUpdateIntervalET);
                long actUpdateInterval = stringToLong(actDetectionIntervalET);
                float minDisplacement = stringToFloat(locMinDisplacementET);
                if (locUpdateInterval < 0 || actUpdateInterval < 0 || minDisplacement < 0 ) {
                    return;
                }

                mSharedPreferences.edit()
                        .putLong(PreferenceKey.KEY_LOCATION_UPDATE_INTERVAL, locUpdateInterval)
                        .putFloat(PreferenceKey.KEY_LOCATION_MINIMUM_DISPLACEMENT, minDisplacement)
                        .putBoolean(PreferenceKey.KEY_LOCATION_CHANGE_NOTIFICATION, locNotificationSW.isChecked())
                        .putLong(PreferenceKey.KEY_ACTIVITY_DETECTION_INTERVAL, actUpdateInterval)
                        .putBoolean(PreferenceKey.KEY_ACTIVITY_CHANGE_NOTIFICATION, actNotificationSW.isChecked())
                        .apply();

                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
                finish();
            }
        });
    }

    private long stringToLong(EditText editText) {
        long result = -1;
        try {
            result = Long.valueOf(editText.getText().toString());
        } catch (NumberFormatException ex) {
            String str = "";
            if(editText == locUpdateIntervalET) {
                str = "Location Update Interval";
            } else if (editText == actDetectionIntervalET) {
                str = "Activity Detection Interval";
            }
            Toast.makeText(getApplicationContext(), "Invalid number format for " + str, Toast.LENGTH_LONG).show();
        }
        return result;
    }

    private float stringToFloat(EditText editText) {
        float result = -1;
        try {
            result = Float.valueOf(editText.getText().toString());
        } catch (NumberFormatException ex) {
            String str = "";
            if(editText == locMinDisplacementET) {
                str = "Location Minimum Displacement";
                Toast.makeText(getApplicationContext(), "Invalid number format for " + str, Toast.LENGTH_LONG).show();
            }
        }
        return result;
    }
}
