package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//shows all of the user's saved records from newest to oldest
//allows user to filter by group name
public class AllRecords extends Activity {

    //variables used in creating the recyclerview
    private RecyclerView mRecycler;
    private CustomAdapter mCustomAdapter;
    private LinearLayoutManager layoutManager;

    private MyDatabase db; //database
    private EditText filterEntry; //input field for filtering
    private Button trackButton, settingsButton, dashboardButton; //navigation buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_records);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView); //get recyclerview

        //get navigation buttons
        trackButton = (Button) findViewById(R.id.trackButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);

        //set on click listeners for the navigation buttons
        trackButton.setOnClickListener(this::gotoTracking);
        settingsButton.setOnClickListener(this::gotoSettings);
        dashboardButton.setOnClickListener(this::gotoHome);

        filterEntry = (EditText) findViewById(R.id.filterEntry); //get user inputted filter criteria

        db = new MyDatabase(this); //initialize database

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //filter the records
            String filterGroup = extras.getString("group");
            ArrayList<String> filteredRecords = db.getFilteredData(filterGroup);

            //create adapter with filtered dataset
            mCustomAdapter = new CustomAdapter(filteredRecords);
        }
        else {
            //get all record data (starting from newest)
            Cursor cursor = db.getData();
            cursor.moveToLast();

            ArrayList<String> recordsArray = new ArrayList<String>();

            while (!cursor.isBeforeFirst()) {
                int index1 = cursor.getColumnIndex(Constants.UID);
                String recordUID = cursor.getString(index1); //retrieve record uid
                recordsArray.add(recordUID); //add uid to dataset

                cursor.moveToPrevious();
            }

            //create adapter with full dataset
            mCustomAdapter = new CustomAdapter(recordsArray);
        }

        //set up recyclerview
        mRecycler.setAdapter(mCustomAdapter);
        layoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(layoutManager);

    }

    //filter the shown records on button click
    public void filterRecords(View view) {
        //get user input text
        String groupNameQuery = filterEntry.getText().toString();

        //start allrecords activity (this activity) again with the filter criteria
        Intent i = new Intent(this, AllRecords.class);
        i.putExtra("group", groupNameQuery);
        startActivity(i);
    }

    //navigate to tracking activity
    public void gotoTracking(View view) {
        Intent i = new Intent(this, StatTracking.class);
        startActivity(i);
    }

    //navigate to settings activity
    public void gotoSettings(View view){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    //navigate to dashboard
    public void gotoHome(View view) {
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }
}
