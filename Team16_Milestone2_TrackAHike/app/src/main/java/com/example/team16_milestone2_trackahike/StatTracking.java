package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StatTracking extends Activity implements View.OnClickListener {

    private int seconds = 0;
    private Button startBtn, pauseBtn, saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        startBtn = (Button) findViewById(R.id.startButton);
        pauseBtn = (Button) findViewById(R.id.pauseButton);
        saveBtn = (Button) findViewById(R.id.saveButton);

        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);


        //TODO - add buttons and onClick functions
    }

    public void startTracking() {

    }

    @Override
    public void onClick(View v) {

    }
}
