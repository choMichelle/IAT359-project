package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

//starts, pauses, and saves tracking data
public class StatTracking extends Activity implements View.OnClickListener, SensorEventListener {

    private int seconds = 0;
    private int totalSteps;
    private boolean isRunning = false;
    private boolean wasRunning = false;
    private boolean recordStarted = false;
    private TextView timeText, stepsText;
    private Button startBtn, pauseBtn, saveBtn, resetBtn;
    private Button deleteButton, settingsButton, dashboardButton, allRecordsButton;
    private EditText sessionName, sessionCategory;
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private MyDatabase db;

    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACTIVITY_RECOGNITION"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        //move between activities buttons
        settingsButton = (Button) findViewById(R.id.settingsButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        settingsButton.setOnClickListener(this::gotoSettings);
        dashboardButton.setOnClickListener(this::gotoHome);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        db = new MyDatabase(this);

        //stat tracking views
        timeText = (TextView) findViewById(R.id.timeTextView);
        stepsText = (TextView) findViewById(R.id.stepsTextView);
        sessionName = (EditText) findViewById(R.id.currentSessionNameEdit);
        sessionCategory = (EditText) findViewById(R.id.currentSessionCategoryEdit);

        //stat tracking buttons
        startBtn = (Button) findViewById(R.id.startButton);
        pauseBtn = (Button) findViewById(R.id.pauseButton);
        saveBtn = (Button) findViewById(R.id.saveButton);
        resetBtn = (Button) findViewById(R.id.resetButton);

        startBtn.setOnClickListener(this::startTiming);
        pauseBtn.setOnClickListener(this::pauseTracking);
        saveBtn.setOnClickListener(this::saveSession);
        resetBtn.setOnClickListener(this::resetTracking);

        //get step detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        if (savedInstanceState != null) {

            //Get and set the state of the activity prior to the activity being destroyed
            sessionName.setText(savedInstanceState.getString("name"));
            seconds = savedInstanceState.getInt("seconds");
            isRunning = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            totalSteps = savedInstanceState.getInt("steps");
            sessionCategory.setText(savedInstanceState.getString("category"));
            recordStarted = savedInstanceState.getBoolean("recordStarted");

            //format and set text for timer
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timeText.setText(hrs + ":" + mins + ":" + secs);

            stepsText.setText(String.valueOf(totalSteps));
        }

        //check if activity recognition permissions are granted
        if (allPermissionsGranted()) {
            Log.i("Activity rec perms: ", "Active");
        } else {
            //if not, directly request permission
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("name", sessionName.getText().toString());
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", isRunning);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
        savedInstanceState.putInt("steps", totalSteps);
        savedInstanceState.putString("category", sessionCategory.getText().toString());
        savedInstanceState.putBoolean("recordStarted", recordStarted);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

        //set tracking state (used to keep tracking if device orientation is changed)
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStepCounter, mSensorManager.SENSOR_DELAY_UI);

        //start tracking stats again (used to keep tracking if device orientation is changed)
        if (wasRunning) {
            startTiming(startBtn);
            isRunning = true;
        }

        //set start button text if tracking was started then paused
        if (recordStarted && !wasRunning) {
            startBtn.setText("Resume tracking");
        }
    }

    //on pause button press
    private void pauseTracking(View view) {
        isRunning = false;
        startBtn.setText("Resume tracking");
    }

    //on save button press, save session stats and details
    private void saveSession(View view) {
        String name = sessionName.getText().toString();
        String time = String.valueOf(seconds);
        String steps = String.valueOf(totalSteps);
        String category = sessionCategory.getText().toString();

        if (name.equals("")) {
            Toast.makeText(this, "Session not named. Please set a name.", Toast.LENGTH_SHORT).show();
        }
        else if (!name.equals("") && category.equals("")){
            category = "no group"; //set group name to a default value if not set

            long id = db.insertData(name, time, steps, category);
            if (id < 0)
            {
                Toast.makeText(this, "add to db fail", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "add to db success", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            long id = db.insertData(name, time, steps, category);
            if (id < 0)
            {
                Toast.makeText(this, "add to db fail", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "add to db success", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetTracking(View view) {
        sessionName.setText("");
        sessionCategory.setText("");
        seconds = 0;
        timeText.setText("0:0:0");
        totalSteps = 0;
        stepsText.setText("0");
        isRunning = false;
        recordStarted = false;
        startBtn.setText("Start tracking");
    }

    //run timer, format and set current time
    public void countTime() {

        while (isRunning) {
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;

            //update timer text on UI thread
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeText.setText(hrs + ":" + mins + ":" + secs);
                }
            });

            //update timer every second
            SystemClock.sleep(1000);
            seconds++;
        }

    }

    //on start tracking button press, start tracking
    public void startTiming(View view) {
        if (!isRunning) {
            Thread timerThread = new Thread(new timeTrackingThread());
            isRunning = true;
            timerThread.start(); //start separate thread for timer
            startBtn.setText("Tracking...");
            recordStarted = true;
        }
    }

    //separate thread to handle time tracking function
    private class timeTrackingThread implements Runnable {
        @Override
        public void run() {
            countTime();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //get step detector values while tracking, update text
        if (isRunning) {
            int currStep = (int) event.values[0];
            totalSteps += currStep;
            stepsText.setText(String.valueOf(totalSteps));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    //check if required permissions are granted, return boolean
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void gotoSettings(View view){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void gotoHome(View view) {
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }

    public void gotoRecords(View view) {
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }
}
