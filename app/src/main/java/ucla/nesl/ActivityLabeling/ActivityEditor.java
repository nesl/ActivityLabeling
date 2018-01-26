package ucla.nesl.ActivityLabeling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class ActivityEditor extends AppCompatActivity {

    private static final String TAG = ActivityEditor.class.getSimpleName();
    static final String ACTIVITY_INFO = "Activity Info";

    /**
     * Keys for storing activity state in the Bundle.
     */
    private static final String KEY_USR_ULOC_LIST = "User MicroLocation Items";
    private static final String KEY_USR_ACT_TYPE_LIST = "User Activity Type Items";

    private Date mStartTime;
    private String mMicroLocation;
    private String mType;

    private ArrayList<String> mUsrUlocs;
    private ArrayList<String> mUsrActTypes;

    private ActivityStorageManager mStoreManager;

    Location mCurLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Please input activity information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mCurLoc = getIntent().getExtras().getParcelable(MainActivity.CURRENT_LOCATION);


        updateValuesFromBundle(savedInstanceState);
        mStoreManager = new ActivityStorageManager(this);
        if (mUsrActTypes == null) {
            mUsrActTypes = mStoreManager.loadUsrActTpyes();
        }
        if (mUsrUlocs == null) {
            mUsrUlocs = mStoreManager.loadUsrUlocs();
        }
        if (mUsrUlocs.size() == 0) {
            String[] ulocArray = getResources().getStringArray(R.array.microlocations_array);
            mUsrUlocs = new ArrayList<>(Arrays.asList(ulocArray));
        }
        if (mUsrActTypes.size() == 0) {
            String[] typeArray = getResources().getStringArray(R.array.activityTypes_array);
            mUsrActTypes = new ArrayList<>(Arrays.asList(typeArray));
        }

        prepareStartTime();
        prepareStartLocation();
        prepareSpinner(R.id.MicrolocsSp, mUsrUlocs);
        prepareSpinner(R.id.ActivityTypesSp, mUsrActTypes);

        prepareBtns();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "OnStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStoreManager.saveUsrUloc(mUsrUlocs);
        mStoreManager.saveUserActType(mUsrActTypes);
    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
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
        mStartTime = Calendar.getInstance().getTime();
        String formattedTime = Utils.timeToString(mStartTime.getTime());
        TextView startTimeTV = findViewById(R.id.StartTimeValTV);
        startTimeTV.setText(formattedTime);
    }

    private void prepareStartLocation() {
        String content;
        if (mCurLoc == null) {
            content = Utils.locToString(-1, -1);
        } else {
            content = Utils.locToString(mCurLoc.getLatitude(), mCurLoc.getLongitude());
        }
        TextView startLocTV = findViewById(R.id.LocValTV);
        startLocTV.setText(content);
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
        Button typeCutomizeBtn = findViewById(R.id.typeAddBtn);
        // Send user activity information back to MainActivity
        Button saveBtn = findViewById(R.id.SaveBtn);

        ulocCustomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pop up custom dialog");
                openCustomDialog(mUsrUlocs);
            }
        });

        typeCutomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pop up custom dialog");
                openCustomDialog(mUsrActTypes);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                EditText description;
                description = findViewById(R.id.DescriptionET);

                ActivityDetail act = new ActivityDetail();
                act.setStartTime(mStartTime.getTime());
                act.setStartLocation(mCurLoc);
                act.setMicrolocation(mMicroLocation);
                act.setActType(mType);
                act.setDescription(description.getText().toString());
                myIntent.putExtra(ACTIVITY_INFO, act);
                setResult(RESULT_OK, myIntent);
                finish();//finishing activity
            }
        });
    }

    private void openCustomDialog(ArrayList<String> items) {
        CustomDialog customDialog = CustomDialog.newInstance(items);
        customDialog.show(getSupportFragmentManager(), "Custom Dialog");
    }
}
