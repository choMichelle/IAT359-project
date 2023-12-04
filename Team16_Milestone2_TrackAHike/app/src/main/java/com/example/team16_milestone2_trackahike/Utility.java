package com.example.team16_milestone2_trackahike;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

//holds useful functions for converting photos for save/display purposes
public class Utility {

    //convert from bitmap to byte array (for saving the photo)
    public static byte[] toBytes(Bitmap bitmap) {
        byte[] result = new byte[0];
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            stream.close();
            result = stream.toByteArray();
        }
        catch (IOException e) {
            System.out.println("Could not convert to byte array.");
        }
        return result;

    }

    //convert from byte array to bitmap (for preparing to display the photo)
    public static Bitmap toBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public int strideCalculator (int gender, int height){

        int stride;
        if (gender==0&&height<162){
            stride = 26;
        } else if (gender==0&&height>162) {
            stride = 28;
        }else if (gender==1||gender==3 &&height<175) {
            stride = 28;
        }else  {
            stride = 30;
        }
        return stride;
    }


}
