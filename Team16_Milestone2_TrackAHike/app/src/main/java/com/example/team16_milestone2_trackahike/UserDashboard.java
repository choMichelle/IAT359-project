package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserDashboard extends Activity implements View.OnClickListener {

    private Button trackButton, settingsButton, allRecordsButton;

    public static final String DEFAULT= "";

    private TextView welcomeText;

    private ImageView imageViewCaptured;

    //views to hold last recorded exercise session
    public TextView recordNameText, recordGroupText;
    public ImageView previewPhoto;

    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);

        db = new MyDatabase(this);

        welcomeText = (TextView) findViewById(R.id.welcomeText);

        //get navigation buttons
        trackButton = (Button) findViewById(R.id.trackButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        //set click listeners for navigation buttons
        trackButton.setOnClickListener(this::gotoTracking);
        settingsButton.setOnClickListener(this::gotoSettings);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        //get image view that will hold user's profile picture
        imageViewCaptured = (ImageView) findViewById(R.id.imageViewCapturedImg);

        //get views to hold brief info about last exercise session
        recordNameText = (TextView) findViewById(R.id.previewRecNameText);
        recordGroupText = (TextView) findViewById(R.id.previewGroupNameText);
        previewPhoto = (ImageView) findViewById(R.id.previewPhotoView);

        //retrieve info about last exercise session
        Cursor cursor = db.getData(); //get all record data

        cursor.moveToLast(); //go to last exercise session

        //get relevant indices for the exercise session
        int index0 = cursor.getColumnIndex(Constants.UID);
        int index1 = cursor.getColumnIndex(Constants.NAME);
        int index2 = cursor.getColumnIndex(Constants.CATEGORY);

        String recordID = cursor.getString(index0);
        String recName = cursor.getString(index1);
        String groupName = cursor.getString(index2);

        recordNameText.setText(recName);
        recordGroupText.setText(groupName);

        //get a photo for the record preview
        Cursor photoCursor = db.getPhotos(recordID);
        int photoBytesIndex = photoCursor.getColumnIndex(Constants.PHOTO_CONTENT);

        photoCursor.moveToFirst(); //go to the first image
        if (photoCursor != null && photoCursor.getCount() > 0) { //check if there are any photos
            byte[] photoBytes = photoCursor.getBlob(photoBytesIndex); //get photo byte array
            Bitmap photoBitmap = Utility.toBitmap(photoBytes); //convert byte array to bitmap;
            previewPhoto.setImageBitmap(photoBitmap);
        }
        else {
            //do nothing
        }


    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        String nameText = sharedPrefs.getString("name", DEFAULT);

        welcomeText.setText(nameText);
        if(sharedPrefs.contains("imageViewCapturedImg"))
        {

            String encodedImage = sharedPrefs.getString("imageViewCapturedImg",null);

            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);

            Bitmap bitmapImage = Utility.toBitmap(b);
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