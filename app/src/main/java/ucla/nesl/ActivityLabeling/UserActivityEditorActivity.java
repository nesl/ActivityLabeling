package ucla.nesl.ActivityLabeling;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ucla.nesl.ActivityLabeling.activity.main.MainActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivityStorageManager;
import ucla.nesl.ActivityLabeling.utils.Utils;


public class UserActivityEditorActivity extends AppCompatActivity {

    private static final String TAG = UserActivityEditorActivity.class.getSimpleName();

    public static final String ACTIVITY_INFO = "Activity_Info";

    /**
     * Keys for storing activity state in the Bundle.
     */
    private static final String KEY_USR_ULOC_LIST = "User MicroLocation Items";
    private static final String KEY_USR_ACT_TYPE_LIST = "User Activity Type Items";

    private long mStartTime;
    private String mMicroLocation;
    private String mType;

    private ArrayList<String> mUsrUlocs;
    private ArrayList<String> mUsrActTypes;

    private UserActivityStorageManager mStoreManager;

    private Location mCurLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Please input activity information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mCurLoc = getIntent().getExtras().getParcelable(MainActivity.INTENT_KEY_CURRENT_LOCATION);

        updateValuesFromBundle(savedInstanceState);
        mStoreManager = new UserActivityStorageManager(this);
        mUsrActTypes = mStoreManager.loadUsrActTpyes();
        mUsrUlocs = mStoreManager.loadUsrUlocs();

        prepareStartTime();
        prepareStartLocation();
        prepareSpinner(R.id.MicrolocsSp, mUsrUlocs);
        prepareSpinner(R.id.ActivityTypesSp, mUsrActTypes);

        prepareBtns();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the user's current activities list state
        savedInstanceState.putStringArrayList(KEY_USR_ULOC_LIST, mUsrUlocs);
        savedInstanceState.putStringArrayList(KEY_USR_ACT_TYPE_LIST, mUsrActTypes);
        Log.i(TAG, "OnSaveInstanceState");
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state

            if (savedInstanceState.keySet().contains(KEY_USR_ULOC_LIST)) {
                mUsrUlocs = savedInstanceState.getStringArrayList(KEY_USR_ULOC_LIST);
            }
            if (savedInstanceState.keySet().contains(KEY_USR_ACT_TYPE_LIST)) {
                mUsrActTypes = savedInstanceState.getStringArrayList(KEY_USR_ACT_TYPE_LIST);
            }
        }
    }


    private void prepareStartTime() {
        mStartTime = Calendar.getInstance().getTime().getTime();
        String formattedTime = Utils.timeToString(mStartTime);
        TextView startTimeTV = findViewById(R.id.StartTimeValTV);
        startTimeTV.setText(formattedTime);
    }

    private void prepareStartLocation() {
        TextView startLocTV = findViewById(R.id.LocValTV);
        startLocTV.setText(Utils.locToString(mCurLoc));
    }

    private void prepareSpinner(final int spinnerID, List<String> items) {
        Spinner sp = findViewById(spinnerID);
        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                stringArrayID, android.R.layout.simple_spinner_item);*/
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item , items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                switch (parent.getId()) {
                    case R.id.MicrolocsSp:
                        if (!selection.equalsIgnoreCase("Select a microlocation")) {
                            mMicroLocation = selection;
                        }
                        break;
                    case R.id.ActivityTypesSp:
                        if (!selection.equalsIgnoreCase("Select an activity type")) {
                            mType = selection;
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void prepareBtns() {
        Button ulocCustomizeBtn = findViewById(R.id.ulocAddBtn);
        ulocCustomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pop up custom dialog");
                openCustomDialog(mUsrUlocs);
            }
        });

        Button typeCutomizeBtn = findViewById(R.id.typeAddBtn);
        typeCutomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pop up custom dialog");
                openCustomDialog(mUsrActTypes);
            }
        });

        Button saveBtn = findViewById(R.id.SaveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update labels of micro-location and activity spinners
                mStoreManager.saveUsrUloc(mUsrUlocs);
                mStoreManager.saveUserActType(mUsrActTypes);

                // Create a UserActivity entry
                EditText description = findViewById(R.id.DescriptionET);

                UserActivity act = new UserActivity();
                act.startTimeMs = mStartTime;
                act.setStartLocation(mCurLoc);
                act.microLocationLabel = mMicroLocation;
                act.type = mType;
                act.description = description.getText().toString();

                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.putExtra(ACTIVITY_INFO, act);
                setResult(RESULT_OK, myIntent);
                finish(); // TODO: do we need this line?
            }
        });
    }

    private void openCustomDialog(ArrayList<String> items) {
        CustomDialog customDialog = CustomDialog.newInstance(items);
        customDialog.show(getSupportFragmentManager(), "Custom Dialog");
    }
}
