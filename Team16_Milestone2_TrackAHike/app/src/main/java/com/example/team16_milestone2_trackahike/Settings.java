package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends Activity implements View.OnClickListener {

    private Button buttonStartCamera;
    private EditText firstName, age, height;
    private Button submitButton;

    private Button trackButton, allRecordsButton, dashboardButton;

    private RadioGroup Gender;
    private RadioButton Male, Female, Other;

    public static final String DEFAULT = "";


    private ImageView imageViewCaptured;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        firstName = (EditText) findViewById(R.id.firstName);
        age = (EditText) findViewById(R.id.age);
        height = (EditText) findViewById(R.id.height);

        Gender = (RadioGroup) findViewById(R.id.Gender);
        Male = (RadioButton) findViewById(R.id.Male);
        Female = (RadioButton) findViewById(R.id.Female);
        Other = (RadioButton) findViewById(R.id.Other);
        submitButton = (Button) findViewById(R.id.submitButton);
        //setting up the edit text for name, age, gender, and height

        buttonStartCamera = (Button) findViewById(R.id.StartCamera);
        buttonStartCamera.setOnClickListener(this);
        //button for starting the camera code

        imageViewCaptured = (ImageView) findViewById(R.id.imageViewCapturedImg);

        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int genderSP = sharedPrefs.getInt("genderSP", 4);
        int heightNumber = sharedPrefs.getInt("height", 160);

        if (genderSP == 1) {
            Male.setChecked(true);

        } else if (genderSP == 0) {
            Female.setChecked(true);
        } else if (genderSP == 2) {
            Other.setChecked(true);
        }

        //get navigation buttons
        trackButton = (Button) findViewById(R.id.trackButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);

        //set click listeners for navigation buttons
        trackButton.setOnClickListener(this::gotoTracking);
        allRecordsButton.setOnClickListener(this::gotoRecords);
        dashboardButton.setOnClickListener(this::gotoHome);

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        if (sharedPrefs.contains("imageViewCapturedImg")) {

            String encodedImage = sharedPrefs.getString("imageViewCapturedImg", null);

            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);

            Bitmap bitmapImage = Utility.toBitmap(b);
            imageViewCaptured.setImageBitmap(bitmapImage);
        }

    }

    public void onClick(View view) {

        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }


    public void submit(View view) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("name", firstName.getText().toString());

        String ageInput = age.getText().toString();
        String heightInput = height.getText().toString();


        if (ageInput == null || ageInput.trim().equals("")) {
            Toast.makeText(this, "Sorry you did not fill in all the fields", Toast.LENGTH_SHORT).show();
        } else if (heightInput == null || heightInput.trim().equals("")) {
            Toast.makeText(this, "Sorry you did not fill in all the fields", Toast.LENGTH_SHORT).show();
        } else {
            int ageNumber = Integer.parseInt(age.getText().toString());
            editor.putInt("age", ageNumber);
            int heightNumber = Integer.parseInt(height.getText().toString());
            editor.putInt("height", heightNumber);
            Toast.makeText(this, "Settings have been saved", Toast.LENGTH_SHORT).show();
            editor.commit();
        }

        }

    public void btnClicked(View view) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (Male.isChecked()) {
            editor.putInt("genderSP", 1);
        } else if (Female.isChecked()) {
            editor.putInt("genderSP", 0);
        } else if (Other.isChecked()) {
            editor.putInt("genderSP", 3);

        }
        editor.commit();
    }

    public void gotoTracking(View view) {
        Intent i = new Intent(this, StatTracking.class);
        startActivity(i);
    }

    public void gotoRecords(View view) {
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }

    public void gotoHome(View view) {
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }

}