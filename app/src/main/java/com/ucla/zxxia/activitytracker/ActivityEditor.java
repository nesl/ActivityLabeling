package com.ucla.zxxia.activitytracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ActivityEditor extends AppCompatActivity {

    private Date mStartTime = Calendar.getInstance().getTime();
    private String mLocation = "";
    private String mMicroLocation = "";
    private String mType = "";
    // Acquire a reference to the system Location Manager
    private LocationManager mLocationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("EditorActivity", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Please input activity information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);



        prepareStartTime();
        prepareLocation();
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
                        -1 , mLocation, mMicroLocation, mType, description.getText().toString()));
                setResult(RESULT_OK, myIntent);
                finish();//finishing activity
            }
        });
    }


    private void prepareStartTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
        String formattedTime = df.format(mStartTime);
        TextView startTimeTV = findViewById(R.id.StartTimeValTV);
        startTimeTV.setText(formattedTime);
    }

    private void prepareLocation() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSEnabled) {
                Log.i("ActivityEditor", "Please enable gps provider.");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            Location location = null;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION);

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                        Constants.PERMISSIONS_REQUEST_CODE_INTERNET);
                return;
            }
            if (isGPSEnabled) {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.i("ActivityEditor", "Obtain location from gps provider.");
            } else {
                Toast.makeText(this, "Fail to obtain last Known Location", Toast.LENGTH_LONG).show();
            }
            if (location != null) {
                mLocation = "(" + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude())+")";
                System.out.println(mLocation);
                TextView locationTv = findViewById(R.id.LocValTV);
                locationTv.setText(mLocation);
            }
        } else {
            Toast.makeText(this, "Fail to obtain location service", Toast.LENGTH_LONG).show();
        }
    }

    private void prepareSpinner(final int spinnerID, int stringArrayID) {
        Spinner sp = (Spinner) findViewById(spinnerID);
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


    @Override
    protected void onStart() {
        Log.i("EditorActivity", "OnStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i("EditorActivity", "OnResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("EditorActivity", "OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("EditorActivity", "OnStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("EditorActivity", "OnDestroy");
        super.onDestroy();
    }
}
