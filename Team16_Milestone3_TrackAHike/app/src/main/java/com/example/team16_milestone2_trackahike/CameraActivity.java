package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraProvider;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//allows user to open their device's camera to take photo for their profile picture
public class CameraActivity extends Activity implements View.OnClickListener {

    private Button buttonCaptureShow, backButton; //navigation buttons
    private ImageView imageViewCaptured; //view to hold photo
    private static final int REQUEST_CODE = 1; //request code for camera implicit intent

    //variables for camera access permission
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    //tone generator for sound feedback
    private final ToneGenerator mToneGen = new ToneGenerator(AudioManager.STREAM_MUSIC,100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //get the back button (says 'settings') and set click listener
        backButton = (Button) findViewById(R.id.settingsButton);
        backButton.setOnClickListener(this::gotoSettings);

        //get open camera button and set click listener
        buttonCaptureShow = findViewById(R.id.buttonCaptureShow);
        buttonCaptureShow.setOnClickListener(this);

        //get view to hold photo
        imageViewCaptured = findViewById(R.id.imageViewCapturedImg);

        //check for permissions
        if (allPermissionsGranted()) {
            //do nothing
        } else { //directly request permissions if not granted
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    //on clicking open camera button, opens device's camera
    @Override
    public void onClick(View view) {
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, REQUEST_CODE);
    }

    //navigate to settings activity
    public void gotoSettings(View view){
        mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP); //beep sound feedback
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    //get result, if any, from the device camera activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) { //check if a photo was taken
            Bitmap photo = (Bitmap) data.getExtras().get("data"); //get the photo
            imageViewCaptured.setImageBitmap(photo); //set the photo in the imageview

            //prepare to save the photo into sharedprefs
            SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();

            byte[] compressImage = Utility.toBytes(photo); //compress image into byte[]

            //encode photo into a string
            String sEncodedImage = Base64.encodeToString(compressImage, Base64.DEFAULT);

            //save the photo string into sharedprefs
            editor.putString("imageViewCapturedImg",sEncodedImage);
            editor.commit();
        }

    }

    //check for required permissions
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

