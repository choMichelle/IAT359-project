package com.example.team16_milestone2_trackahike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

//used to interact with data in the database
public class MyDatabase {

    private SQLiteDatabase db;
    private final MyHelper helper;
    private Context context;

    public MyDatabase (Context c) {
        context = c;
        helper = new MyHelper(context);

    }

    //add record into db (records table)
    public long insertData(String name, String seconds, String steps, String distance,
                           String calories, String speed, String category) {
        db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.NAME, name);
        contentValues.put(Constants.TIME, seconds);
        contentValues.put(Constants.STEPS, steps);
        contentValues.put(Constants.DISTANCE, distance);
        contentValues.put(Constants.CALORIES, calories);
        contentValues.put(Constants.SPEED, speed);
        contentValues.put(Constants.CATEGORY, category);
        long id = db.insert(Constants.TABLE_NAME, null, contentValues);
        return id;
    }

    //add photos into db (photos table)
    public long insertPhotos(byte[] photo, String recordID) {
        db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.PHOTO_CONTENT, photo);
        contentValues.put(Constants.RECORD_ID, recordID);
        long photoID = db.insert(Constants.PHOTOS_TABLE_NAME, null, contentValues);
        return photoID;
    }

    //get cursor for all exercise session data
    public Cursor getData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.UID, Constants.NAME, Constants.TIME, Constants.STEPS,
                Constants.DISTANCE, Constants.CALORIES, Constants.SPEED, Constants.CATEGORY};
        Cursor cursor = db.query(Constants.TABLE_NAME, columns, null, null, null, null, null);
        return cursor;
    }

    //return filtered exercise records IDs
    public ArrayList<String> getFilteredData(String category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.UID, Constants.CATEGORY};

        String selection = Constants.CATEGORY + "='" + category + "'";
        Cursor cursor = db.query(Constants.TABLE_NAME, columns, selection, null, null, null, null);

        ArrayList<String> filteredResults = new ArrayList<String>();

        cursor.moveToLast();
        while (!cursor.isBeforeFirst()) {
            int index1 = cursor.getColumnIndex(Constants.UID);
            String recID = cursor.getString(index1);
            filteredResults.add(recID);
            cursor.moveToPrevious();
        }
        return filteredResults;
    }

    //get all photos related to the selected exercise record
    public Cursor getPhotos(String recordID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.PHOTO_CONTENT, Constants.RECORD_ID};

        String selection = Constants.RECORD_ID + "='" + recordID + "'";
        Cursor cursor = db.query(Constants.PHOTOS_TABLE_NAME, columns, selection, null, null, null, null);

        return cursor;
    }

    //delete selected record from db
    public void deleteRecord(String currentRecordID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, "_id=?", new String[]{currentRecordID});
        db.delete(Constants.PHOTOS_TABLE_NAME, "recordID=?", new String[]{currentRecordID});
        db.close();
    }
}
