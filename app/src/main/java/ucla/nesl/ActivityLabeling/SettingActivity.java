package ucla.nesl.ActivityLabeling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
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

        locUpdateIntervalET.setText(String.valueOf(Utils.locationUpdateInterval(this)));
        locMinDisplacementET.setText(String.valueOf(Utils.locationMinimumDisplacement(this)));
        locNotificationSW.setChecked(Utils.locationChangeNotification(this));

        actDetectionIntervalET.setText(String.valueOf(Utils.activityDetetionInterval(this)));
        actNotificationSW.setChecked(Utils.activityChangeNotification(this));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long locUpdateInterval = stringToLong(locUpdateIntervalET);
                long actUpdateInterval = stringToLong(actDetectionIntervalET);
                float minDisplacement = stringToFloat(locMinDisplacementET);
                if (locUpdateInterval < 0 || actUpdateInterval < 0 || minDisplacement < 0 ) {
                    return;
                }
                Utils.setLocationUpdateInterval(getApplicationContext(), locUpdateInterval);
                Utils.setLocationMinimumDisplacement(getApplicationContext(), minDisplacement);
                Utils.setLocationChangeNotification(getApplicationContext(), locNotificationSW.isChecked());
                Utils.setActivityDetectionInterval(getApplicationContext(), actUpdateInterval);
                Utils.setActivityChangeNotification(getApplicationContext(), actNotificationSW.isChecked());
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
