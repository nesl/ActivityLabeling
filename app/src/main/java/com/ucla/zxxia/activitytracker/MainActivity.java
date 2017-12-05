package com.ucla.zxxia.activitytracker;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {



    static final String ACTIVITY_LIST = "ActivityList";
    // Create a List from String Array elements
    private ArrayList<ActivityDetail> actsList  = new ArrayList<>();
    // Create an ArrayAdapter from List
    ActivityDetailListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            Log.i("MainActivity", "restore saved state");
            actsList = savedInstanceState.getParcelableArrayList(ACTIVITY_LIST);
        } else {
            // Probably initialize members with default values for a new instance
            Log.i("MainActivity", "create a new list");
        }


        final ListView actsListView = findViewById(R.id.ActivitiesListView);
        adapter = new ActivityDetailListAdapter(this, actsList);
        actsListView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.plus_sign);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MainActivity", "floating button pressed");
                Intent intent = new Intent(getApplicationContext(), ActivityEditor.class);
                startActivityForResult(intent, Constants.ACTIVITY_EDITOR_RESULT_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == Constants.ACTIVITY_EDITOR_RESULT_REQUEST_CODE  && resultCode  == RESULT_OK) {
                Log.i("MainActivity", "Received results from EditorActivity");
                actsList.add((ActivityDetail) data.getParcelableExtra(Constants.ACTIVITY_INFO));
                adapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onStart() {
        Log.i("MainActivity", "OnStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i("MainActivity", "OnResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.i("MainActivity", "OnRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.i("MainActivity", "OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("MainActivity", "OnStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("MainActivity", "OnDestroy");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        // Save the user's current activities list state
        savedInstanceState.putParcelableArrayList(ACTIVITY_LIST, actsList);
        Log.i("MainActivity", "OnSaveInstanceState");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
