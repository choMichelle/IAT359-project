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

//TODO - **consider** making the stat tracking a service so it runs even while phone screen off
//unless that's not how that works and i'm trolling
public class StatTracking extends Activity implements View.OnClickListener, SensorEventListener {

    private int seconds = 0;
    private int totalSteps, previousSteps;
    private boolean isRunning = false;
    private boolean wasRunning = false;
    private TextView timeText, stepsText;
    private Button startBtn, pauseBtn, saveBtn, resetBtn;
    private EditText sessionName;
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

        startBtn = (Button) findViewById(R.id.startButton);
        pauseBtn = (Button) findViewById(R.id.pauseButton);
        saveBtn = (Button) findViewById(R.id.saveButton);
        resetBtn = (Button) findViewById(R.id.resetButton);

        startBtn.setOnClickListener(this::startTiming);
        //TODO - disable start button while isRunning = true (to prevent double click)
        //TODO - change text per state: "tracking...", "resume tracking" (after hitting pause)

        pauseBtn.setOnClickListener(this::pauseTracking);
        saveBtn.setOnClickListener(this::saveSession);
        resetBtn.setOnClickListener(this::resetTracking);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            Log.i("step counter feature exists", "true");
        }


        if (savedInstanceState != null) {

            //Get the state of the stopwatch prior to the activity being destroyed
            sessionName.setText(savedInstanceState.getString("name"));
            seconds = savedInstanceState.getInt("seconds");
            isRunning = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            totalSteps = savedInstanceState.getInt("steps");

            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timeText.setText(hrs + ":" + mins + ":" + secs);
            stepsText.setText(String.valueOf(totalSteps));
        }

        if (allPermissionsGranted()) {
            Log.i("Activity rec perms: ", "Active");
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //may need to request physical activity permission at runtime
        //can be manually activated via: app notif and settings -> permission manager
        // -> physical activity -> turning on for the app

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            Log.i("Activity recog permission: ", "not granted");
//        }
//
//        ActivityCompat.requestPermissions(this,
//                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
//                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);

//        if (ContextCompat.checkSelfPermission(
//                context, Manifest.permission.ACTIVITY_RECOGNITION) ==
//                PackageManager.PERMISSION_GRANTED) {
//            // You can use the API that requires the permission.
//            Log.i("Activity rec permission: ", "granted");
//        }  else {
//            // You can directly ask for the permission.
//            // The registered ActivityResultCallback gets the result of this request.
//            requestPermissionLauncher.launch(
//                    Manifest.permission.ACTIVITY_RECOGNITION);
//        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("name", sessionName.getText().toString());
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", isRunning);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
        savedInstanceState.putInt("steps", totalSteps);
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
            isRunning = true;
            startTiming(startBtn);
        }
    }

    private void pauseTracking(View view) {
        isRunning = false;
    }

    private void saveSession(View view) {
        String name = sessionName.getText().toString();
        String time = String.valueOf(seconds);
        String steps = String.valueOf(totalSteps);
        if (name.equals("")) {
            Toast.makeText(this, "Session not named. Please set a name.", Toast.LENGTH_SHORT).show();
        }
        else {
            long id = db.insertData(name, time, steps);
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
        seconds = 0;
        timeText.setText("0:0:0");
//        previousSteps = totalSteps;
        totalSteps = 0;
        stepsText.setText("0");
        isRunning = false;
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
        Thread timerThread = new Thread(new timeTrackingThread());
        isRunning = true;
        timerThread.start();
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
//            totalSteps = (int) event.values[0];
//            int currSteps = totalSteps - previousSteps;
//            Log.i("steps count: ", String.valueOf(currSteps));
//            stepsText.setText(String.valueOf(currSteps));
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
