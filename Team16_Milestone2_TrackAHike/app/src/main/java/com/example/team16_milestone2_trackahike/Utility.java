package com.example.team16_milestone2_trackahike;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

//holds useful functions for use in any activity
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

    //calculate the user's step length (in inches), used in settings (for calculating stats during tracking)
    //rough step length values adapted from: https://marathonhandbook.com/average-stride-length/
    public static int stepLengthCalculator(String gender, int height){

        int stepLen;
        if (gender.equals("female") && height < 162){ //height < ~5ft 4in
            stepLen = 26;
        } else if (gender.equals("female") && height > 162) { //height > ~5ft 4in
            stepLen = 28;
        } else if ((gender.equals("male") || gender.equals("other")) && height < 175) { //height < ~5ft 7in
            stepLen = 28;
        } else { //gender = male or other, height > ~5ft 7in
            stepLen = 30;
        }
        return stepLen;
    }

    //convert user's step length from inches to metres, used in settings (for calculating stats during tracking)
    public static double convertInchToMetre(int stepInches) {
        return stepInches / 39.37;
    }

    //calculates calories burned per step based on user's height and weight
    //rough kcal per step adapted from: https://www.verywellfit.com/pedometer-steps-to-calories-converter-3882595
    public static double calcKcalPerStep(int height, int weight) {
        double kcalPerStep;

        if (height > 180) { //height > ~5ft 9in
            if (weight > 100) {
                kcalPerStep = 0.00007;
            }
            else if (weight > 70 && weight < 100) {
                kcalPerStep = 0.00005;
            }
            else { //weight < 70kg
                kcalPerStep = 0.000035;
            }
        }
        else if (height > 165 && height < 180){ //height between ~5ft 4in and ~5ft 9in
            if (weight > 100) {
                kcalPerStep = 0.000065;
            }
            else if (weight > 70 && weight < 100) {
                kcalPerStep = 0.000048;
            }
            else { //weight < 70kg
                kcalPerStep = 0.000032;
            }
        }
        else { //height < ~5ft 4in
            if (weight > 100) {
                kcalPerStep = 0.00006;
            }
            else if (weight > 70 && weight < 100) {
                kcalPerStep = 0.000043;
            }
            else { //weight < 70kg
                kcalPerStep = 0.00003;
            }
        }

        return kcalPerStep;
    }


}
