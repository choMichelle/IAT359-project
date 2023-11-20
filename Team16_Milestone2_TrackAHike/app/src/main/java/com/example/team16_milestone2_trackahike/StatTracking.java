package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
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

public class StatTracking extends Activity implements View.OnClickListener, SensorEventListener {

    private int seconds = 0;
    private int totalSteps, previousSteps;
    private boolean isRunning = false;
    private boolean wasRunning = false;
    private boolean recordStarted = false;
    private TextView timeText, stepsText;
    private Button startBtn, pauseBtn, saveBtn, resetBtn;
    private EditText sessionName, sessionCategory;
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private MyDatabase db;
    Context context;

    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACTIVITY_RECOGNITION"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        db = new MyDatabase(this);

        timeText = (TextView) findViewById(R.id.timeTextView);
        stepsText = (TextView) findViewById(R.id.stepsTextView);
        sessionName = (EditText) findViewById(R.id.currentSessionNameEdit);
        sessionCategory = (EditText) findViewById(R.id.currentSessionCategoryEdit);

        startBtn = (Button) findViewById(R.id.startButton);
        pauseBtn = (Button) findViewById(R.id.pauseButton);
        saveBtn = (Button) findViewById(R.id.saveButton);
        resetBtn = (Button) findViewById(R.id.resetButton);

        startBtn.setOnClickListener(this::startTiming);
        pauseBtn.setOnClickListener(this::pauseTracking);
        saveBtn.setOnClickListener(this::saveSession);
        resetBtn.setOnClickListener(this::resetTracking);

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
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStepCounter, mSensorManager.SENSOR_DELAY_UI);
        if (wasRunning) {
            startTiming(startBtn);
            isRunning = true;
        }
        if (recordStarted && !wasRunning) {
            startBtn.setText("Resume tracking");
        }
    }

    private void pauseTracking(View view) {
        isRunning = false;
        startBtn.setText("Resume tracking");
    }

    private void saveSession(View view) {
        String name = sessionName.getText().toString();
        String time = String.valueOf(seconds);
        String steps = String.valueOf(totalSteps);
        String category = sessionCategory.getText().toString();

        if (name.equals("")) {
            Toast.makeText(this, "Session not named. Please set a name.", Toast.LENGTH_SHORT).show();
        }
        else if (!name.equals("") && category.equals("")){
            category = "no group";

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

    public void countTime() {

        while (isRunning) {
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeText.setText(hrs + ":" + mins + ":" + secs);
                }
            });
            SystemClock.sleep(1000);
            seconds++;
        }

    }

    public void startTiming(View view) {
        if (!isRunning) {
            Thread timerThread = new Thread(new timeTrackingThread());
            isRunning = true;
            timerThread.start();
            startBtn.setText("Tracking...");
            recordStarted = true;
        }
    }

    private class timeTrackingThread implements Runnable {
        @Override
        public void run() {
            countTime();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (isRunning) {
            int currStep = (int) event.values[0];
            totalSteps += currStep;
            Log.i("steps count: ", String.valueOf(totalSteps));
            stepsText.setText(String.valueOf(totalSteps));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
