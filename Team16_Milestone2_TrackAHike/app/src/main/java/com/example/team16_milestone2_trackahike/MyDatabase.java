package com.example.team16_milestone2_trackahike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MyDatabase {

    private SQLiteDatabase db;
    private final MyHelper helper;
    private Context context;

    public MyDatabase (Context c) {
        context = c;
        helper = new MyHelper(context);

    }

    public long insertData(String name, String seconds, String steps, String category) {
        db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.NAME, name);
        contentValues.put(Constants.TIME, seconds);
        contentValues.put(Constants.STEPS, steps);
        contentValues.put(Constants.CATEGORY, category);
        long id = db.insert(Constants.TABLE_NAME, null, contentValues);
        return id;
    }

    public Cursor getData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.UID, Constants.NAME, Constants.TIME, Constants.STEPS, Constants.CATEGORY};
        Cursor cursor = db.query(Constants.TABLE_NAME, columns, null, null, null, null, null);
        return cursor;
    }

    public ArrayList<String> getFilteredData(String category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.NAME, Constants.TIME, Constants.STEPS, Constants.CATEGORY};

        String selection = Constants.CATEGORY + "='" + category + "'";
        Cursor cursor = db.query(Constants.TABLE_NAME, columns, selection, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        ArrayList<String> filteredResults = new ArrayList<String>();

        cursor.moveToLast();
        while (!cursor.isBeforeFirst()) {
            int index1 = cursor.getColumnIndex(Constants.NAME);
            String recName = cursor.getString(index1);
            filteredResults.add(recName);
            cursor.moveToPrevious();
        }
        return filteredResults;
    }

    public void deleteRecord(String currentRecordID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, "_id=?", new String[]{currentRecordID});
        db.close();
    }
}
