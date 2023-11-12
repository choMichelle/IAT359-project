package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class Settings extends Activity implements View.OnClickListener {

    Button buttonStartCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        buttonStartCamera = findViewById(R.id.StartCamera);
        buttonStartCamera.setOnClickListener(this);

    }

    public void onClick(View view) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }
}
