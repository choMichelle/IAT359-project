package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//main activity
//shows user's profile picture, if available
//and shows user's newest saved exercise session
public class UserDashboard extends Activity implements View.OnClickListener {

    private Button trackButton, settingsButton, allRecordsButton; //navigation buttons
    public static final String DEFAULT= ""; //default string used for retrieving sharedprefs data
    private TextView welcomeText; //holds user's name in the welcome banner, if name was inputted
    private ImageView imageViewCaptured; //holds user's profile picture

    //views to hold last recorded exercise session
    private LinearLayout previewSession; //full container of the exercise session preview
    private TextView recordNameText, recordGroupText; //session name and group name
    private ImageView previewPhoto; //photo from the session
    private String recordID; //id of the session

    private String img_placeholder_path = "@drawable/no_image"; //path to placeholder image
    private Drawable img_placeholder; //placeholder for 'empty' imageviews

    private MyDatabase db; //database

    //tone generator for sound feedback
    private final ToneGenerator mToneGen = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);

        db = new MyDatabase(this); //initialize database

        welcomeText = (TextView) findViewById(R.id.welcomeText); //get view to hold user's name

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

        //get the placeholder image resource
        int imageResource = getResources().getIdentifier(img_placeholder_path, null, getPackageName());
        img_placeholder = getResources().getDrawable(imageResource);

        //get views to hold brief info about last exercise session
        previewSession = (LinearLayout) findViewById(R.id.previewSession);
        recordNameText = (TextView) findViewById(R.id.previewRecNameText);
        recordGroupText = (TextView) findViewById(R.id.previewGroupNameText);
        previewPhoto = (ImageView) findViewById(R.id.previewPhotoView);

        //set click listener for the last session preview container
        previewSession.setOnClickListener(this::accessLastRecord);

        //retrieve info about last exercise session
        Cursor cursor = db.getData(); //get all record data

        cursor.moveToLast(); //go to last exercise session
        if (cursor != null && cursor.getCount() > 0) { //check if there are any records
            //get relevant indices for the exercise session
            int index0 = cursor.getColumnIndex(Constants.UID);
            int index1 = cursor.getColumnIndex(Constants.NAME);
            int index2 = cursor.getColumnIndex(Constants.CATEGORY);

            //get saved data for the exercise session
            recordID = cursor.getString(index0);
            String recName = cursor.getString(index1);
            String groupName = cursor.getString(index2);

            //set name and group name text
            recordNameText.setText(recName);
            recordGroupText.setText(groupName);

            //get a photo for the record preview
            Cursor photoCursor = db.getPhotos(recordID);
            int photoBytesIndex = photoCursor.getColumnIndex(Constants.PHOTO_CONTENT);

            photoCursor.moveToFirst(); //go to the first image
            if (photoCursor != null && photoCursor.getCount() > 0) { //check if there are any photos
                byte[] photoBytes = photoCursor.getBlob(photoBytesIndex); //get photo byte array
                Bitmap photoBitmap = Utility.toBitmap(photoBytes); //convert byte array to bitmap;
                previewPhoto.setImageBitmap(photoBitmap); //set photo to imageview
            }
            else {
                previewPhoto.setImageDrawable(img_placeholder);
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);

        //get user's name and set text
        String nameText = sharedPrefs.getString("name", DEFAULT);
        welcomeText.setText(nameText);

        //check if there is a photo saved in sharedprefs
        if(sharedPrefs.contains("imageViewCapturedImg"))
        {
            //retrieve the encoded photo
            String encodedImage = sharedPrefs.getString("imageViewCapturedImg",null);

            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT); //decode the photo
            Bitmap bitmapImage = Utility.toBitmap(b); //convert the photo byte array to bitmap

            imageViewCaptured.setImageBitmap(bitmapImage); //set photo to the imageview
        }

    }

    //on click, go to the specific record shown in the preview of the last record
    public void accessLastRecord(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, SpecificRecord.class);
        i.putExtra("recordID", recordID);
        startActivity(i);
    }

    //navigate to tracking activity
    public void gotoTracking(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, StatTracking.class);
        startActivity(i);
    }

    //navigate to settings activity
    public void gotoSettings(View view){
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    //navigate to all records activity
    public void gotoRecords(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }

    @Override
    public void onClick(View view) {

    }
}