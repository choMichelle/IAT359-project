package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity implements View.OnClickListener {

    Button buttonStartCamera;
    EditText firstName, age, gender,height;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        firstName = (EditText)findViewById(R.id.firstName);
        age = (EditText)findViewById(R.id.age);
        gender = (EditText)findViewById(R.id.gender);
        height = (EditText)findViewById(R.id.height);
        submitButton = findViewById(R.id.submitButton);
        //setting up the edit text for name, age, gender, and height

        buttonStartCamera = findViewById(R.id.StartCamera);
        buttonStartCamera.setOnClickListener(this);
        //button for starting the camera code

    }

    public void onClick(View view) {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }
    public void submit (View view){
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("name", firstName.getText().toString());
        editor.putString("age", age.getText().toString());
        editor.putString("gender", gender.getText().toString());
        Toast.makeText(this, "User Settings Have Been Updated", Toast.LENGTH_LONG).show();
        //displays toast when the user clicks the button
        editor.commit();

    }
}
