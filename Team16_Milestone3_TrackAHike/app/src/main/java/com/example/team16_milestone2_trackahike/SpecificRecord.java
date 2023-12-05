package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//shows details of a single record
//also shows associated photos, if any
public class SpecificRecord extends Activity implements View.OnClickListener {

    private TextView recordNameText, recordedTimeText, recordedStepsText, recordedCategoryText,
            recordedDistanceText, recordedCaloriesText, recordedSpeedText; //views to hold record details
    private String recordID; //holds ID of record being viewed
    private Button deleteButton; //delete record button
    private Button settingsButton, dashboardButton, allRecordsButton; //navigation buttons
    private ImageView imgView0, imgView1, imgView2,
            imgView3, imgView4, imgView5; //views to display saved photos
    private ImageView[] imgViews; //holds all imageviews for easy retrieval
    private MyDatabase db; //database

    //tone generator for sound feedback
    private final ToneGenerator mToneGen = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_record);

        //get navigation buttons
        settingsButton = (Button) findViewById(R.id.settingsButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        //set click listeners for navigation buttons
        settingsButton.setOnClickListener(this::gotoSettings);
        dashboardButton.setOnClickListener(this::gotoHome);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        db = new MyDatabase(this); //initialize db

        //set up delete record button
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        //get views to hold record details
        recordNameText = (TextView) findViewById(R.id.currentRecordNameText);
        recordedTimeText = (TextView) findViewById(R.id.recordedTimeText);
        recordedStepsText = (TextView) findViewById(R.id.recordedStepsText);
        recordedCategoryText = (TextView) findViewById(R.id.recordedCategoryText);
        recordedDistanceText = (TextView) findViewById(R.id.recordedDistanceText);
        recordedCaloriesText = (TextView) findViewById(R.id.recordedCaloriesText);
        recordedSpeedText = (TextView) findViewById(R.id.recordedSpeedText);

        //get views to display photos
        imgView0 = (ImageView) findViewById(R.id.photoGalleryView0);
        imgView1 = (ImageView) findViewById(R.id.photoGalleryView1);
        imgView2 = (ImageView) findViewById(R.id.photoGalleryView2);
        imgView3 = (ImageView) findViewById(R.id.photoGalleryView3);
        imgView4 = (ImageView) findViewById(R.id.photoGalleryView4);
        imgView5 = (ImageView) findViewById(R.id.photoGalleryView5);

        //add each image view to an array
        imgViews = new ImageView[]{imgView0, imgView1, imgView2, imgView3, imgView4, imgView5};

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String selectedRecord = extras.getString("recordID"); //get id of selected record

            Cursor cursor = db.getData();

            //get column indices
            int index0 = cursor.getColumnIndex(Constants.UID);
            int index1 = cursor.getColumnIndex(Constants.NAME);
            int index2 = cursor.getColumnIndex(Constants.TIME);
            int index3 = cursor.getColumnIndex(Constants.STEPS);
            int index4 = cursor.getColumnIndex(Constants.CATEGORY);
            int index5 = cursor.getColumnIndex(Constants.DISTANCE);
            int index6 = cursor.getColumnIndex(Constants.CALORIES);
            int index7 = cursor.getColumnIndex(Constants.SPEED);

            //loop through records in the db
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                //get the name of each record
                recordID = cursor.getString(index0);

                //check if the record name matches the selected record's name
                if (recordID.equals(selectedRecord)) {

                    //get and set the record's data
                    recordID = cursor.getString(index0);
                    String recordName = cursor.getString(index1);
                    String recordSteps = cursor.getString(index3);
                    String recordCategory = cursor.getString(index4);

                    recordNameText.setText(recordName);
                    recordedStepsText.setText(recordSteps);
                    recordedCategoryText.setText(recordCategory);

                    //format and set recorded time text
                    try {
                        int recordTime = Integer.parseInt(cursor.getString(index2));
                        int hrs = recordTime / 3600;
                        int mins = (recordTime % 3600) / 60;
                        int secs = recordTime % 60;
                        recordedTimeText.setText(hrs + ":" + mins + ":" + secs);
                    }
                    catch (NumberFormatException e){
                        Log.e("Parse time int: ", "could not parse error: " + e);
                    }

                    //format and set distance text
                    double recordDistance = Double.parseDouble(cursor.getString(index5));
                    recordedDistanceText.setText(String.format("%.2f", recordDistance) + " km");

                    //format and set calories text
                    double recordCalories = Double.parseDouble(cursor.getString(index6));
                    recordedCaloriesText.setText(String.format("%.3f", recordCalories) + " kcal");

                    //format and set speed text
                    double recordSpeed = Double.parseDouble(cursor.getString(index7));
                    recordedSpeedText.setText(String.format("%.3f", recordSpeed) + " km/s");

                    //get the photo data for the selected record via its id
                    Cursor photoCursor = db.getPhotos(recordID);
                    int photoBytesIndex = photoCursor.getColumnIndex(Constants.PHOTO_CONTENT);
                    int photoCount = 0; //counts the photos, used to decide which imageView to use

                    photoCursor.moveToFirst();
                    while (!photoCursor.isAfterLast()) {
                        byte[] photoBytes = photoCursor.getBlob(photoBytesIndex); //get photo byte array
                        Bitmap photoBitmap = Utility.toBitmap(photoBytes); //convert byte array to bitmap;
                        ImageView currentView = imgViews[photoCount]; //get an imageView
                        currentView.setImageBitmap(photoBitmap); //set the photo

                        photoCount++;
                        photoCursor.moveToNext();
                    }
                    if (photoCount < 6) { //if the number of photos to display < number of imageViews
                        for (int i = photoCount; i < 6; i++) { //loop through empty imageViews
                            ImageView currView = imgViews[i]; //get the current imageView
                            currView.setVisibility(View.GONE); //get rid of the imageView
                        }
                    }
                    break; //exit loop if correct record is found
                }
                cursor.moveToNext();
            }
        }
    }

    //on clicking delete record button
    @Override
    public void onClick(View v) {
        db.deleteRecord(recordID);
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }

    //navigate to settings activity
    public void gotoSettings(View view){
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    //navigate to dashboard activity
    public void gotoHome(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }

    //navigate to all records activity
    public void gotoRecords(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }
}
