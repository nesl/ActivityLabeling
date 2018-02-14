package ucla.nesl.ActivityLabeling.activity.setting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import ucla.nesl.ActivityLabeling.R;
import ucla.nesl.ActivityLabeling.edittextmonitor.EditTextMonitor;
import ucla.nesl.ActivityLabeling.utils.SharedPreferenceHelper;

public class SettingActivity extends AppCompatActivity {

    // UI widgets
    private EditText locUpdateIntervalET;
    private EditText locMinDisplacementET;
    private Switch locNotificationSW;
    private EditText actDetectionIntervalET;
    private Switch actNotificationSW;
    private Button saveBtn;
    private Button cancelBtn;

    private EditTextMonitor editMonitor;
    private SharedPreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Acquire UI Widgets
        locUpdateIntervalET = findViewById(R.id.LocationUpdateIntervalEditText);
        locMinDisplacementET = findViewById(R.id.LocationMinimumDisplacementEditText);
        locNotificationSW = findViewById(R.id.LocationNotificationSwitch);
        actDetectionIntervalET = findViewById(R.id.ActivityDetectionIntervalEditText);
        actNotificationSW = findViewById(R.id.ActivityNotificationSwitch);
        saveBtn = findViewById(R.id.SettingsSaveBtn);
        cancelBtn = findViewById(R.id.SettingsCancelBtn);

        // Provide Widget content
        preferenceHelper = new SharedPreferenceHelper(this);

        locUpdateIntervalET.setText(String.valueOf(preferenceHelper.getLocationUpdateInterval()));
        locMinDisplacementET.setText(String.valueOf(preferenceHelper.getLocationMinimumDisplacement()));
        locNotificationSW.setChecked(preferenceHelper.getSendingNotificationOnLocationChanged());
        actDetectionIntervalET.setText(String.valueOf(preferenceHelper.getActivityDetetionInterval()));
        actNotificationSW.setChecked(preferenceHelper.getSendingNotificationOnMotionChanged());

        // Attach on-change event listeners to all EditTexts
        editMonitor = new EditTextMonitor();
        editMonitor.registerEditText(locUpdateIntervalET, 0.001, Double.MAX_VALUE,
                "Please enter a valid time interval");
        editMonitor.registerEditText(locMinDisplacementET, 0., Double.MAX_VALUE,
                "Please enter a valid distance");
        editMonitor.registerEditText(actDetectionIntervalET, 0.001, Double.MAX_VALUE,
                "Please enter a valid time interval");

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editMonitor.areAllEditTextValid()) {
                    Toast.makeText(SettingActivity.this, "Please fix the errors",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                preferenceHelper.setLocationUpdateInterval(
                        (long)(editMonitor.getEditTextValue(locUpdateIntervalET) * 1000.));
                preferenceHelper.setLocationMinimumDisplacement(
                        (float) editMonitor.getEditTextValue(locMinDisplacementET));
                preferenceHelper.setSendingNotificationOnLocationChanged(
                        locNotificationSW.isChecked());
                preferenceHelper.setActivityDetectionInterval(
                        (long)(editMonitor.getEditTextValue(actDetectionIntervalET) * 1000.));
                preferenceHelper.getSendingNotificationOnMotionChanged(
                        actNotificationSW.isChecked());

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

}
