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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ActivityEditor extends AppCompatActivity {

    private static final String TAG = ActivityEditor.class.getSimpleName();

    private Date mStartTime;
    private String mLocation = "";
    private String mMicroLocation = "";
    private String mType = "";

    //private LocationService mLocationSerivce;
    //private boolean mBound = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Please input activity information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mStartTime = Calendar.getInstance().getTime();

        prepareStartTime();
        prepareSpinner(R.id.MicrolocsSp, R.array.microlocations_array);
        prepareSpinner(R.id.ActivityTypesSp, R.array.activityTypes_array);

        // Send user activity information back to MainActivity
        Button saveBtn = findViewById(R.id.SaveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                EditText description;
                description = findViewById(R.id.DescriptionET);

                myIntent.putExtra(Constants.ACTIVITY_INFO, new ActivityDetail(mStartTime.getTime(),
                        -1 , -1, -1, mMicroLocation, mType, description.getText().toString()));
                setResult(RESULT_OK, myIntent);
                finish();//finishing activity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocationService
        /*Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.i("ActivityEditor", "bindService in start");*/
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbindService(mConnection);
        //mBound = false;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    /*private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mLocationSerivce = binder.getService();

            Location curLocation = mLocationSerivce.getLocation();
            TextView locView = findViewById(R.id.LocValTV);
            mLocation =  "(" + Double.toString(curLocation.getLatitude()) + ", " + Double.toString(curLocation.getLongitude())+")";
            locView.setText(mLocation);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };*/


    private void prepareStartTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
        String formattedTime = df.format(mStartTime);
        TextView startTimeTV = findViewById(R.id.StartTimeValTV);
        startTimeTV.setText(formattedTime);
    }


    private void prepareSpinner(final int spinnerID, int stringArrayID) {
        Spinner sp = findViewById(spinnerID);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                stringArrayID, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getId()) {
                    case R.id.MicrolocsSp:
                        mMicroLocation = (String) parent.getItemAtPosition(position);
                        break;
                    case R.id.ActivityTypesSp:
                        mType = (String) parent.getItemAtPosition(position);
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
}
