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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    Button buttonStartCamera;
    EditText firstName, age, gender,height;
    Button submitButton;

    public static final String DEFAULT= "";

    private Bitmap bitmapImage;
    private ImageView imageViewCaptured;
    private TextView imgText;



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


        imageViewCaptured = findViewById(R.id.imageViewCapturedImg);




    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        if(sharedPrefs.contains("imageViewCapturedImg"))
        {

            String encodedImage = sharedPrefs.getString("imageViewCapturedImg",null);

            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);

            Bitmap bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.length);
            imageViewCaptured.setImageBitmap(bitmapImage);
        }

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
        editor.putString("height", height.getText().toString());

        Toast.makeText(this, "User Settings Have Been Updated", Toast.LENGTH_LONG).show();


        //displays toast when the user clicks the button
        editor.commit();





    }
}
