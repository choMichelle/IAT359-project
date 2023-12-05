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

//allows user to edit personal metrics (which are used to calculate some tracked stats)
//and change their profile picture
public class Settings extends Activity implements View.OnClickListener {

    private Button buttonStartCamera; //button to open camera (for taking a profile picture)
    private EditText firstName, age, height, weight; //text input fields for user info
    private Button submitButton; //button to save user info
    private Button trackButton, allRecordsButton, dashboardButton; //navigation buttons

    //change gender radio buttons
    private RadioGroup Gender;
    private RadioButton Male, Female, Other;

    public static final String DEFAULT = ""; //default value for retrieving some string data from sharedprefs
    private ImageView imageViewCaptured; //view to hold profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //getting the input fields for name, age, gender, and height
        firstName = (EditText) findViewById(R.id.firstName);
        age = (EditText) findViewById(R.id.age);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);

        Gender = (RadioGroup) findViewById(R.id.Gender);
        Male = (RadioButton) findViewById(R.id.Male);
        Female = (RadioButton) findViewById(R.id.Female);
        Other = (RadioButton) findViewById(R.id.Other);

        //set up submit button for saving user information
        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this::submit);

        //set up button for starting the camera code
        buttonStartCamera = (Button) findViewById(R.id.StartCamera);
        buttonStartCamera.setOnClickListener(this);

        //get view to hold profile picture
        imageViewCaptured = (ImageView) findViewById(R.id.imageViewCapturedImg);

        //get saved user info from sharedprefs
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String savedName = sharedPrefs.getString("name", DEFAULT);
        int savedAge = sharedPrefs.getInt("age", 20);
        String savedGender = sharedPrefs.getString("gender", DEFAULT);
        int heightNumber = sharedPrefs.getInt("height", 160);
        int weightNumber = sharedPrefs.getInt("weight", 150);

        //set user info fields with existing data, if any
        firstName.setText(savedName); //set name
        age.setText(String.valueOf(savedAge)); //set age

        //set gender button
        if (savedGender.equals("male")) {
            Male.setChecked(true);
        } else if (savedGender.equals("female")) {
            Female.setChecked(true);
        } else if (savedGender.equals("other")) {
            Other.setChecked(true);
        }

        //set height field
        height.setText(String.valueOf(heightNumber));

        //set weight field
        weight.setText(String.valueOf(weightNumber));

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

        //check if a profile picture has been saved to sharedprefs
        if (sharedPrefs.contains("imageViewCapturedImg")) {
            //retrieve the encoded photo
            String encodedImage = sharedPrefs.getString("imageViewCapturedImg", null);

            //decode the photo and convert to bitmap
            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmapImage = Utility.toBitmap(b);

            imageViewCaptured.setImageBitmap(bitmapImage); //set photo to the imageview
        }

    }

    //on clicking change profile picture button
    public void onClick(View view) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }

    //on clicking the submit button, save to preferences
    public void submit(View view) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        //get saved gender from sharedprefs, if any
        String savedGender = sharedPrefs.getString("gender", DEFAULT);

        //retrieve personal metrics inputs
        String ageInput = age.getText().toString();
        String heightInput = height.getText().toString();
        String weightInput = weight.getText().toString();

        //check if age, height, weight, and gender input fields were filled (name is optional)
        if (ageInput.trim().equals("") || heightInput.trim().equals("") ||
                weightInput.trim().equals("") || savedGender.equals(DEFAULT)) {
            Toast.makeText(this, "Sorry you did not fill in all the fields", Toast.LENGTH_SHORT).show();
        } else {
            editor.putString("name", firstName.getText().toString()); //add name to sharedprefs

            //parse age, height, weight, and add to sharedprefs
            int ageNumber = Integer.parseInt(ageInput);
            editor.putInt("age", ageNumber);

            int heightNumber = Integer.parseInt(heightInput);
            editor.putInt("height", heightNumber);

            int weightNumber = Integer.parseInt(weightInput);
            editor.putInt("weight", weightNumber);

            //use gender and height to calculate step length, add to sharedprefs
            int calculatedStepLength = Utility.stepLengthCalculator(savedGender, heightNumber);
            editor.putInt("stepLength", calculatedStepLength);

            //use height and weight to calculate kcal burned per step, add to shared prefs
            double calculatedKcalBurn = Utility.calcKcalPerStep(heightNumber, weightNumber);
            editor.putString("kcalBurnPerStep", String.valueOf(calculatedKcalBurn));

            editor.commit(); //save to sharedprefs
            Toast.makeText(this, "Settings have been saved", Toast.LENGTH_SHORT).show();
        }

    }

    //on clicking a gender radio button
    public void btnClicked(View view) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        //check which gender button was clicked, save to sharedprefs
        if (Male.isChecked()) {
            editor.putString("gender", "male");
        } else if (Female.isChecked()) {
            editor.putString("gender", "female");
        } else if (Other.isChecked()) {
            editor.putString("gender", "other");
        }
        editor.commit();
    }

    //navigate to tracking activity
    public void gotoTracking(View view) {
        Intent i = new Intent(this, StatTracking.class);
        startActivity(i);
    }

    //navigate to all records activity
    public void gotoRecords(View view) {
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }

    //navigate to dashboard
    public void gotoHome(View view) {
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }

}