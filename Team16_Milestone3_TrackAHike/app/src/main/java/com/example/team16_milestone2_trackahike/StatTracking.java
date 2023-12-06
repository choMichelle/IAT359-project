package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


//starts, pauses, and saves tracking data and photos
public class StatTracking extends Activity implements View.OnClickListener, SensorEventListener {

    //variables to hold/track stats
    private int seconds = 0; //holds recorded time in seconds
    private int totalSteps; //holds number of steps taken
    private double totalDistance; //holds distance travelled (calculated)
    private double caloriesBurned; //holds calories burned (calculated)
    private double moveSpeed; //holds movement speed (calculated)

    //variables to control/track when the tracker is running
    private boolean isRunning = false; //checks if the tracker is currently active
    private boolean wasRunning = false; //checks if the tracker was previously active
    private boolean recordStarted = false; //checks if the tracker was started once after activity was opened

    private TextView timeText, stepsText, distanceText, caloriesText, speedText; //views to display tracked stats
    private ImageView imgView0, imgView1, imgView2,
                    imgView3, imgView4, imgView5; //views to hold captured photos
    private ImageView[] imgViews; //holds all imageviews for easy retrieval
    private static final int REQUEST_CODE = 1; //request code for camera intent
    private static int img_id = 0; //used to get the correct imageview to fill
    private int totalImgs = 0; //used to help save photos when more than 6 photos are being taken
    private String img_placeholder_path = "@drawable/no_image"; //path to placeholder image
    private Drawable img_placeholder; //placeholder for 'empty' imageviews
    private Button startBtn, pauseBtn, saveBtn, resetBtn; //stat tracking buttons
    private Button cameraBtn; //button to open camera
    private Button settingsButton, dashboardButton, allRecordsButton; //bottom bar of buttons
    private EditText sessionName, sessionCategory; //user inputted names for the session


    //step sensor variables
    private SensorManager mSensorManager;
    private Sensor mStepCounter;

    //tone generator for sound feedback
    private final ToneGenerator mToneGen = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

    private MyDatabase db; //database

    //used to check for and request step detector and camera permissions
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_tracking);

        db = new MyDatabase(this); //initialize db

        //get navigation buttons
        settingsButton = (Button) findViewById(R.id.settingsButton);
        dashboardButton = (Button) findViewById(R.id.homeButton);
        allRecordsButton = (Button) findViewById(R.id.allRecButton);

        //set click listeners for navigation buttons
        settingsButton.setOnClickListener(this::gotoSettings);
        dashboardButton.setOnClickListener(this::gotoHome);
        allRecordsButton.setOnClickListener(this::gotoRecords);

        //get stat tracking views
        sessionName = (EditText) findViewById(R.id.currentSessionNameEdit);
        sessionCategory = (EditText) findViewById(R.id.currentSessionCategoryEdit);
        timeText = (TextView) findViewById(R.id.timeTextView);
        stepsText = (TextView) findViewById(R.id.stepsTextView);
        distanceText = (TextView) findViewById(R.id.distanceTextView);
        caloriesText = (TextView) findViewById(R.id.caloriesTextView);
        speedText = (TextView) findViewById(R.id.speedTextView);

        //set key listeners for session name and group name edittexts
        sessionName.setOnKeyListener(onSoftKeyboardDonePress);
        sessionCategory.setOnKeyListener(onSoftKeyboardDonePress);

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

        //add each imageview to an array
        imgViews = new ImageView[]{imgView0, imgView1, imgView2, imgView3, imgView4, imgView5};

        //get the placeholder image resource
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
            //set name, group name text
            sessionName.setText(savedInstanceState.getString("name"));
            sessionCategory.setText(savedInstanceState.getString("category"));

            //set stats variables
            seconds = savedInstanceState.getInt("seconds");
            totalSteps = savedInstanceState.getInt("steps");
            totalDistance = savedInstanceState.getDouble("distance");
            caloriesBurned = savedInstanceState.getDouble("calories");
            moveSpeed = savedInstanceState.getDouble("speed");

            //set tracker state variables
            isRunning = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            recordStarted = savedInstanceState.getBoolean("recordStarted");

            //set photo variables
            img_id = savedInstanceState.getInt("imgID");
            totalImgs = savedInstanceState.getInt("totalImgs");

            //reload temporarily saved photos
            if (img_id > 0 || totalImgs == 6) {
                if (totalImgs == 6) { //for when 7+ photos were taken
                    for (int i = 0; i < totalImgs; i++) {
                        //retrieve photo byte array
                        byte[] photoByteArray = savedInstanceState.getByteArray("photo" + i);
                        Bitmap photoBitmap = Utility.toBitmap(photoByteArray); //convert to bitmap

                        imgViews[i].setImageBitmap(photoBitmap); //set photo in imageview
                    }
                }
                else {
                    for (int i = 0; i < img_id; i++) {
                        //retrieve photo byte array
                        byte[] photoByteArray = savedInstanceState.getByteArray("photo" + i);
                        Bitmap photoBitmap = Utility.toBitmap(photoByteArray); //convert to bitmap

                        imgViews[i].setImageBitmap(photoBitmap); //set photo in imageview
                    }
                }
            }

            //format and set text for timer
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;
            timeText.setText(hrs + ":" + mins + ":" + secs);

            //set text for the number of steps taken
            stepsText.setText(String.valueOf(totalSteps));

            //set text for distance travelled
            distanceText.setText(totalDistance + " km");

            //set text for calories burned
            caloriesText.setText(caloriesBurned + " kcal");

            //set text for speed
            speedText.setText(moveSpeed + " km/s");
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
        //save name, group name
        savedInstanceState.putString("name", sessionName.getText().toString().trim());
        savedInstanceState.putString("category", sessionCategory.getText().toString().trim());

        //save stats values
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putInt("steps", totalSteps);
        savedInstanceState.putDouble("distance", totalDistance);
        savedInstanceState.putDouble("calories", caloriesBurned);
        savedInstanceState.putDouble("speed", moveSpeed);

        //save tracker state variables
        savedInstanceState.putBoolean("running", isRunning);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
        savedInstanceState.putBoolean("recordStarted", recordStarted);

        //save photo state variables
        savedInstanceState.putInt("imgID", img_id);
        savedInstanceState.putInt("totalImgs", totalImgs);

        //prepare photos to be temporarily saved (convert bitmaps to byte arrays)
        if (img_id > 0 || totalImgs == 6) {

            //loop through all photos to be saved, for when 7+ photos have been taken (some overwritten)
            if (totalImgs == 6) {
                for (int i = 0; i < totalImgs; i++) {
                    ImageView currentPhoto = imgViews[i];
                    Bitmap photoBitmap = ((BitmapDrawable) currentPhoto.getDrawable()).getBitmap();
                    byte[] photoBytes = Utility.toBytes(photoBitmap);

                    savedInstanceState.putByteArray("photo" + i, photoBytes); //save photo
                }
            } else {
                //loop through all photos to be saved
                for (int i = 0; i < img_id; i++) {
                    ImageView currentPhoto = imgViews[i];
                    Bitmap photoBitmap = ((BitmapDrawable) currentPhoto.getDrawable()).getBitmap();
                    byte[] photoBytes = Utility.toBytes(photoBitmap);

                    savedInstanceState.putByteArray("photo" + i, photoBytes); //save photo
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); //unregister step detector

        //set tracking state (used to keep tracking if device orientation is changed)
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //register step detector
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
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        isRunning = false;
        startBtn.setText("Resume tracking");
    }

    //on save button press, save session stats and photos
    private void saveSession(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback

        //get the tracking variables to save
        String name = sessionName.getText().toString().trim(); //name of session
        String category = sessionCategory.getText().toString().toLowerCase().trim(); //group name
        String time = String.valueOf(seconds); //time in seconds
        String steps = String.valueOf(totalSteps); //steps taken
        String distance = String.valueOf(totalDistance); //distance travelled (calculated)
        String calories = String.valueOf(caloriesBurned); //calories burned (calculated)
        String speed = String.valueOf(moveSpeed); //movement speed (calculated)

        //boolean to check if data should be saved
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
            //save stats data to db - records table
            long recordID = db.insertData(name, time, steps, distance, calories, speed, category);

            //check if a photo has been taken
            //(ex. img_id would = 1 if 1 photo has been taken)
            if (img_id > 0 || totalImgs == 6) {
                if (totalImgs == 6) { //for when 7+ photos are taken (overwritten some)
                    for (int i = 0; i < totalImgs; i++) { //loop through all photos to be saved
                        //retrieve photo and convert bitmap to byte array
                        ImageView currentPhoto = imgViews[i];
                        BitmapDrawable photoBitmap = (BitmapDrawable) currentPhoto.getDrawable();
                        byte[] photoBytes = Utility.toBytes(photoBitmap.getBitmap());

                        db.insertPhotos(photoBytes, String.valueOf(recordID)); //save photo to db
                    }
                }
                else {
                    for (int i = 0; i < img_id; i++) { //loop through all photos to be saved
                        //retrieve photo and convert bitmap to byte array
                        ImageView currentPhoto = imgViews[i];
                        BitmapDrawable photoBitmap = (BitmapDrawable) currentPhoto.getDrawable();
                        byte[] photoBytes = Utility.toBytes(photoBitmap.getBitmap());

                        db.insertPhotos(photoBytes, String.valueOf(recordID)); //save photo to db
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

    //on reset button press, reset all variables and views
    private void resetTracking(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback

        //reset name, group name text
        sessionName.setText("");
        sessionCategory.setText("");

        //reset stats variables and text
        seconds = 0;
        timeText.setText("0:0:0");
        totalSteps = 0;
        stepsText.setText("0");
        totalDistance = 0;
        distanceText.setText("0 km");
        caloriesBurned = 0;
        caloriesText.setText("0 kcal");
        moveSpeed = 0;
        speedText.setText("0 km/s");

        //reset tracker state variables
        isRunning = false;
        recordStarted = false;

        //reset start button text
        startBtn.setText("Start tracking");

        //reset photo variables
        img_id = 0;
        totalImgs = 0;

        //loop through all photo imageviews and replace with placeholders
        for (ImageView i : imgViews) {
            i.setImageDrawable(img_placeholder);
        }

    }

    //run timer, format and set current time
    public void countTime() {

        while (isRunning) {
            //format timer
            int hrs = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            int secs = seconds % 60;

            //calculate other stats
            calculateStats(seconds, totalSteps);

            //update stats text on UI thread
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeText.setText(hrs + ":" + mins + ":" + secs);
                    distanceText.setText(String.format("%.2f", totalDistance) + " km");
                    caloriesText.setText(String.format("%.3f", caloriesBurned) + " kcal");
                    speedText.setText(String.format("%.3f", moveSpeed) + " km/s");
                }
            });

            //sleep to update timer/calculate every second
            SystemClock.sleep(1000);
            seconds++;
        }

    }

    //on start tracking button press, start tracking
    public void startTiming(View view) {
        if (!isRunning) {
            Thread timerThread = new Thread(new timeTrackingThread());
            isRunning = true;
            timerThread.start(); //start separate thread for timer and calculations
            startBtn.setText("Tracking...");
            recordStarted = true;
            mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        }
    }

    //separate thread to handle time tracking function
    private class timeTrackingThread implements Runnable {
        @Override
        public void run() {
            countTime();
        }
    }

    //calculate distance travelled, calories burned, movement speed
    public void calculateStats(int time, int steps) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        int savedStepLength = sharedPrefs.getInt("stepLength", 0); //get user's step length
        String savedKcalBurn = sharedPrefs.getString("kcalBurnPerStep", ""); //get user's kcal burn rate

        double stepMetres;
        double kcalBurnRate;

        //check if there is user data for step length and kcal burn rate
        if (savedStepLength == 0 || savedKcalBurn.equals("")) {
            stepMetres = 0; //set step length to 0
            kcalBurnRate = 0; //set kcal rate to 0
        }
        else {
            stepMetres = Utility.convertInchToMetre(savedStepLength); //convert step length to metres
            kcalBurnRate = Double.parseDouble(savedKcalBurn); //parse kcal burn rate to double
        }

        totalDistance = steps * stepMetres / 1000; //calculate distance in km
        caloriesBurned = steps * kcalBurnRate; //calculate calories burned in kcal
        moveSpeed = totalDistance / time; //calculate movement speed in km/s

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

    //on take photo button press, move to device's camera app
    public void openCamera(View view) {
        if (isRunning) {
            mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, REQUEST_CODE);
        }
        else {
            Toast.makeText(this, "Please start tracking first", Toast.LENGTH_SHORT).show();
        }
    }

    //receive result from device's camera app
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //check if a photo was taken
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            if (img_id < 6) {
                ImageView view = imgViews[img_id]; //retrieve an imageview
                view.setImageBitmap(photo); //set the photo to the imageview
                img_id++;
            }
            if (img_id == 6) {
                img_id = 0; //reset id to point at first photo - will overwrite old photos
                totalImgs = 6; //used to ensure all photos are processed and saved
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    //get out of name/group name edit text after done typing
    private View.OnKeyListener onSoftKeyboardDonePress = new View.OnKeyListener() {
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            //check if the enter key is tapped
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                //unfocus from edit text after hitting enter
                sessionName.clearFocus();
                sessionCategory.clearFocus();
            }
            return false;
        }
    };

    //check if required permissions are granted, return boolean
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
