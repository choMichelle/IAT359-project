package com.example.team16_milestone2_trackahike;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserDashboard extends AppCompatActivity implements View.OnClickListener {

    private Button trackButton, settingsButton, allRecordsButton;

    public static final String DEFAULT= "";

    private TextView welcomeText;

    private ImageView imageViewCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);
        welcomeText = (TextView) findViewById(R.id.welcomeText);

        trackButton = (Button) findViewById(R.id.trackButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        trackButton.setOnClickListener(this::gotoTracking);
        settingsButton.setOnClickListener(this::gotoSettings);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        imageViewCaptured = findViewById(R.id.imageViewCapturedImg);




    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String nameText = sharedPrefs.getString("name", DEFAULT);
        String savedHeight = sharedPrefs.getString("height", DEFAULT);

        welcomeText.setText(nameText);
        if(sharedPrefs.contains("imageViewCapturedImg"))
        {

            String encodedImage = sharedPrefs.getString("imageViewCapturedImg",null);

            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);

            Bitmap bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.length);
            imageViewCaptured.setImageBitmap(bitmapImage);
        }

    }
    public void gotoTracking(View view) {
        Intent i = new Intent(this, StatTracking.class);
        startActivity(i);
    }

    public void gotoSettings(View view){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void gotoRecords(View view) {
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }

    @Override
    public void onClick(View view) {

    }
}