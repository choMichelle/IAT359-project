package com.example.team16_milestone2_trackahike;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
//code was taken from the camera lecture code
    //testing functionality for now, will edit this code to fit our functionality
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private CameraProvider cameraProvider;

    private Button buttonCaptureSave, buttonCaptureShow;
    private ImageView imageViewCaptured;



    private static final int img_id = 1;

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        buttonCaptureShow = findViewById(R.id.buttonCaptureShow);

        imageViewCaptured = findViewById(R.id.imageViewCapturedImg);


        buttonCaptureShow.setOnClickListener(this);



        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    //bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        //cameraProvider.unbindAll();

       // Preview preview = new Preview.Builder()
               // .build();

       // CameraSelector cameraSelector = new CameraSelector.Builder()
               // .requireLensFacing(CameraSelector.LENS_FACING_BACK)
               // .build();


       // imageCapture = new ImageCapture.Builder()
               // .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
               // .build();
       // preview.setSurfaceProvider(previewView.getSurfaceProvider());
       // cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    //}

    @Override
    public void onClick(View view) {
       // int id = view.getId();
       // if (id == R.id.buttonCaptureSave) {
           // capturePhoto();
            //figure out how to save photo in saved pref





            Toast.makeText(this, "Profile Picture has been updated", Toast.LENGTH_LONG).show();

       // } else if (id == R.id.buttonCaptureShow) {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, img_id);
       // }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        imageViewCaptured.setImageBitmap(photo);

        SharedPreferences sharedPrefs = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] compressImage = baos.toByteArray();
        String sEncodedImage = Base64.encodeToString(compressImage, Base64.DEFAULT);

        editor.putString("imageViewCapturedImg",sEncodedImage);

        editor.commit();


    }

    /*private void capturePhoto() {
        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");


        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CameraActivity.this, "Saving...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

