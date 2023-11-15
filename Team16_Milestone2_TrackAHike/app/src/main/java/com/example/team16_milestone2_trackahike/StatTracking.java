package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

//TODO - **consider** making the stat tracking a service so it runs even while phone screen off
//unless that's not how that works and i'm trolling
public class StatTracking extends Activity implements View.OnClickListener {

    private int seconds = 0;
    private boolean isRunning = false;
    private boolean wasRunning = false;
    private TextView timeText;
    private Button startBtn, pauseBtn, saveBtn, resetBtn;
    private EditText sessionName;
    MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        timeText = (TextView) findViewById(R.id.timeTextView);
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

        if (savedInstanceState != null) {

            //Get the state of the stopwatch prior to the activity being destroyed
            sessionName.setText(savedInstanceState.getString("name"));
            seconds = savedInstanceState.getInt("seconds");
            isRunning = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");

            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timeText.setText(hrs + ":" + mins + ":" + secs);
        }

        db = new MyDatabase(this);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("name", sessionName.getText().toString());
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", isRunning);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (name.equals("")) {
            Toast.makeText(this, "Session not named. Please set a name.", Toast.LENGTH_SHORT).show();
        }
        else {
            long id = db.insertData(name, time);
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
    public void onClick(View v) {

    }
}
