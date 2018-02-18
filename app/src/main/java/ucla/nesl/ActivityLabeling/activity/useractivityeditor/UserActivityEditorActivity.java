package ucla.nesl.ActivityLabeling.activity.useractivityeditor;

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

import ucla.nesl.ActivityLabeling.R;
import ucla.nesl.ActivityLabeling.activity.main.MainActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivity;
import ucla.nesl.ActivityLabeling.storage.UserActivityStorageManager;
import ucla.nesl.ActivityLabeling.uiwidget.NothingSelectedSpinnerAdapter;
import ucla.nesl.ActivityLabeling.utils.Utils;


public class UserActivityEditorActivity extends AppCompatActivity {

    private static final String TAG = UserActivityEditorActivity.class.getSimpleName();

    public static final String ACTIVITY_INFO = "Activity_Info";

    private long mStartTime;
    private String mMicroLocation;
    private String mType;

    private ArrayList<String> mUsrUlocs;
    private ArrayList<String> mUsrActTypes;

    private UserActivityStorageManager mStoreManager;

    private Location mCurLoc;


    //region Section: Activity life cycle
    // =============================================================================================
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

        mStoreManager = new UserActivityStorageManager(this);
        mUsrActTypes = mStoreManager.loadUsrActTypes();
        mUsrUlocs = mStoreManager.loadUsrUlocs();

        mStartTime = Calendar.getInstance().getTime().getTime();

        prepareTextViews();
        prepareSpinner(R.id.MicrolocsSp, mUsrUlocs, "Select a location",
                microlocationItemSelectedListener);
        prepareSpinner(R.id.ActivityTypesSp, mUsrActTypes, "Select an activity",
                userActivityItemSelectedListener);
        prepareButtons();
    }
    //endregion

    //region Section: TextView UI
    // =============================================================================================
    private void prepareTextViews() {
        TextView startTimeTV = findViewById(R.id.StartTimeValTV);
        startTimeTV.setText(Utils.timeToString(mStartTime));

        TextView startLocTV = findViewById(R.id.LocValTV);
        startLocTV.setText(Utils.locationToString(mCurLoc));
    }
    //endregion

    //region Section: Spinner UI and event listeners
    // =============================================================================================
    private void prepareSpinner(int spinnerID, final List<String> items, String hintMessage,
                                PositionProcessedItemSelectedListener itemSelectedListener) {
        Spinner sp = findViewById(spinnerID);

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        NothingSelectedSpinnerAdapter spinnerAdapter = new NothingSelectedSpinnerAdapter(
                adapter, hintMessage, this);

        sp.setAdapter(spinnerAdapter);

        sp.setOnItemSelectedListener(itemSelectedListener);
    }

    private abstract class PositionProcessedItemSelectedListener
            implements AdapterView.OnItemSelectedListener {

        protected abstract void processItemPosition(int index);

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            processItemPosition((int) id);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private PositionProcessedItemSelectedListener microlocationItemSelectedListener
            = new PositionProcessedItemSelectedListener() {
        @Override
        protected void processItemPosition(int index) {
            mMicroLocation = mUsrUlocs.get(index);
        }
    };

    private PositionProcessedItemSelectedListener userActivityItemSelectedListener
            = new PositionProcessedItemSelectedListener() {
        @Override
        protected void processItemPosition(int index) {
            mType = mUsrActTypes.get(index);
        }
    };
    //endregion

    //region Section: Button UI and event listeners
    // =============================================================================================
    private void prepareButtons() {
        Button ulocCustomizeBtn = findViewById(R.id.ulocAddBtn);
        ulocCustomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pop up custom dialog");
                openCustomDialog(mUsrUlocs);
            }
        });

        Button typeCustomizeBtn = findViewById(R.id.typeAddBtn);
        typeCustomizeBtn.setOnClickListener(new View.OnClickListener() {
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

                finish();
            }
        });
    }
    //endregion

    //region Section: Dialogs for label customization
    // =============================================================================================
    private void openCustomDialog(ArrayList<String> items) {
        CustomDialog customDialog = CustomDialog.newInstance(items);
        customDialog.show(getSupportFragmentManager(), "Custom Dialog");
    }
    //endregion
}
