package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

//starts, pauses, and saves tracking data
public class StatTracking extends Activity implements View.OnClickListener, SensorEventListener {

    //variables to hold/track stats
    private int seconds = 0; //holds recorded time in seconds
    private int totalSteps; //holds number of steps taken

    //variables to control/track when the tracker is running
    private boolean isRunning = false; //checks if the tracker is currently active
    private boolean wasRunning = false; //checks if the tracker was previously active
    private boolean recordStarted = false; //checks if the tracker was started once after activity was opened

    private TextView timeText, stepsText; //views to display tracked stats
    private ImageView imgView0, imgView1, imgView2,
                    imgView3, imgView4, imgView5; //views to hold captured photos
    private ImageView[] imgViews; //holds all imageviews for easy retrieval
    private static int img_id = 0; //used to get the correct imageview to fill
    private String img_placeholder_path = "@drawable/doge"; //TODO - replace
    private Drawable img_placeholder;
    private Button startBtn, pauseBtn, saveBtn, resetBtn; //stat tracking buttons
    private Button cameraBtn; //button to open camera
    private Button settingsButton, dashboardButton, allRecordsButton; //bottom bar of buttons
    private EditText sessionName, sessionCategory; //user inputted names for the session


    //step sensor variables
    private SensorManager mSensorManager;
    private Sensor mStepCounter;

    private MyDatabase db; //database

    //used to check for and request step detector and camera permissions
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACTIVITY_RECOGNITION", "android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        //instantiate db
        db = new MyDatabase(this);

        //get move between activities buttons (bottom bar of buttons)
        settingsButton = (Button) findViewById(R.id.settingsButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        //set click listeners for bottom bar of buttons
        settingsButton.setOnClickListener(this::gotoSettings);
        dashboardButton.setOnClickListener(this::gotoHome);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        //get stat tracking views
        timeText = (TextView) findViewById(R.id.timeTextView);
        stepsText = (TextView) findViewById(R.id.stepsTextView);
        sessionName = (EditText) findViewById(R.id.currentSessionNameEdit);
        sessionCategory = (EditText) findViewById(R.id.currentSessionCategoryEdit);

        //get stat tracking buttons
        startBtn = (Button) findViewById(R.id.startButton);
        pauseBtn = (Button) findViewById(R.id.pauseButton);
        saveBtn = (Button) findViewById(R.id.saveButton);
        resetBtn = (Button) findViewById(R.id.resetButton);

        //set click listeners for stat tracking buttons
        startBtn.setOnClickListener(this::startTiming);
        pauseBtn.setOnClickListener(this::pauseTracking);
        saveBtn.setOnClickListener(this::saveSession);
        resetBtn.setOnClickListener(this::resetTracking);

        //get views to hold captured photos
        imgView0 = (ImageView) findViewById(R.id.imageView0);
        imgView1 = (ImageView) findViewById(R.id.imageView1);
        imgView2 = (ImageView) findViewById(R.id.imageView2);
        imgView3 = (ImageView) findViewById(R.id.imageView3);
        imgView4 = (ImageView) findViewById(R.id.imageView4);
        imgView5 = (ImageView) findViewById(R.id.imageView5);

        //add each image view to an array
        imgViews = new ImageView[]{imgView0, imgView1, imgView2, imgView3, imgView4, imgView5};

        //get the placeholder image
        int imageResource = getResources().getIdentifier(img_placeholder_path, null, getPackageName());
        img_placeholder = getResources().getDrawable(imageResource);

        //get camera button and set click listener
        cameraBtn = (Button) findViewById(R.id.cameraButton);
        cameraBtn.setOnClickListener(this::openCamera);

        //get step detector, if it exists
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        //check if there is saved data about the activity
        if (savedInstanceState != null) {

            //get and set the state of the activity prior to the activity being destroyed
            sessionName.setText(savedInstanceState.getString("name"));
            seconds = savedInstanceState.getInt("seconds");
            isRunning = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            totalSteps = savedInstanceState.getInt("steps");
            sessionCategory.setText(savedInstanceState.getString("category"));
            recordStarted = savedInstanceState.getBoolean("recordStarted");

            //format and set text for timer
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timeText.setText(hrs + ":" + mins + ":" + secs);

            //set text for the number of steps taken
            stepsText.setText(String.valueOf(totalSteps));
        }

        //check if required permissions are granted
        if (allPermissionsGranted()) {
            Log.i("Activity rec perms: ", "Active");
        } else {
            //if not, directly request permission
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("name", sessionName.getText().toString());
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", isRunning);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
        savedInstanceState.putInt("steps", totalSteps);
        savedInstanceState.putString("category", sessionCategory.getText().toString());
        savedInstanceState.putBoolean("recordStarted", recordStarted);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

        //set tracking state (used to keep tracking if device orientation is changed)
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStepCounter, mSensorManager.SENSOR_DELAY_UI);

        //start tracking stats again (used to keep tracking if device orientation is changed)
        if (wasRunning) {
            startTiming(startBtn);
            isRunning = true;
        }

        //set start button text if tracking was started then paused
        if (recordStarted && !wasRunning) {
            startBtn.setText("Resume tracking");
        }
    }

    //on pause button press
    private void pauseTracking(View view) {
        isRunning = false;
        startBtn.setText("Resume tracking");
    }

    //on save button press, save session stats and details
    private void saveSession(View view) {
        //get the variables to save
        String name = sessionName.getText().toString(); //name of session
        String time = String.valueOf(seconds); //time in seconds
        String steps = String.valueOf(totalSteps); //steps taken
        String category = sessionCategory.getText().toString(); //group name

        //boolean to check if data can be saved
        boolean canSave;

        //check if the conditions to save are met
        if (name.equals("")) { //check if the session is named
            Toast.makeText(this, "Session not named. Please set a name.", Toast.LENGTH_SHORT).show();
            canSave = false;
        }
        else if (!name.equals("") && category.equals("")){ //if the session is named but the group is not
            category = "no group"; //set group name to a default value if not set
            canSave = true;
        }
        else { //if the session and the group names are set
            canSave = true;
        }

        //save data
        if (canSave) {
            long recordID = db.insertData(name, time, steps, category); //save stats data to db, records table

            if (img_id > 0) { //check if a photo has been taken (ex. img_id would =1 if 1 photo has been taken)
                for (int i = 0; i < img_id; i++) { //loop through all photos to be saved
                    ImageView currentPhoto = imgViews[i];
                    BitmapDrawable photoBitmap = (BitmapDrawable) currentPhoto.getDrawable();
                    byte[] photoBytes = Utility.toBytes(photoBitmap.getBitmap());
                    long photoID = db.insertPhotos(photoBytes, String.valueOf(recordID));

                    //toast result - success/failure to add to db
                    if (photoID < 0)
                    {
                        Toast.makeText(this, "photos add to db fail", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(this, "photos add to db success", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            //toast result - success/failure to add to db
            if (recordID < 0)
            {
                Toast.makeText(this, "add to db fail", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "add to db success", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void resetTracking(View view) {
        sessionName.setText("");
        sessionCategory.setText("");
        seconds = 0;
        timeText.setText("0:0:0");
        totalSteps = 0;
        stepsText.setText("0");
        isRunning = false;
        recordStarted = false;
        startBtn.setText("Start tracking");
        img_id = 0;

        //loop through all image views and replace with placeholders
        for (ImageView i : imgViews) {
            i.setImageDrawable(img_placeholder);
        }

    }

    //run timer, format and set current time
    public void countTime() {

        while (isRunning) {
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;

            //update timer text on UI thread
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeText.setText(hrs + ":" + mins + ":" + secs);
                }
            });

            //update timer every second
            SystemClock.sleep(1000);
            seconds++;
        }

    }

    //on start tracking button press, start tracking
    public void startTiming(View view) {
        if (!isRunning) {
            Thread timerThread = new Thread(new timeTrackingThread());
            isRunning = true;
            timerThread.start(); //start separate thread for timer
            startBtn.setText("Tracking...");
            recordStarted = true;
        }
    }

    //separate thread to handle time tracking function
    private class timeTrackingThread implements Runnable {
        @Override
        public void run() {
            countTime();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //get step detector values while tracking, update text
        if (isRunning) {
            int currStep = (int) event.values[0];
            totalSteps += currStep;
            stepsText.setText(String.valueOf(totalSteps));
        }

    }

    //on button press, move to device's camera app
    public void openCamera(View view) {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, img_id);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        if (img_id < 6) {
            ImageView view = imgViews[img_id];
            view.setImageBitmap(photo);
            img_id++;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    //check if required permissions are granted, return boolean
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void gotoSettings(View view){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void gotoHome(View view) {
        Intent i = new Intent(this, UserDashboard.class);
        startActivity(i);
    }

    public void gotoRecords(View view) {
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);
    }
}
