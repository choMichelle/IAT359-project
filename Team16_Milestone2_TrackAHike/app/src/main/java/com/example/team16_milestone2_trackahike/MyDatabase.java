package com.example.team16_milestone2_trackahike;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MyDatabase {

    private SQLiteDatabase db;
    private final MyHelper helper;
    private Context context;

    public MyDatabase (Context c) {
        context = c;
        helper = new MyHelper(context);

    }

    public long insertData(String name, String seconds, String steps) {
        db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.NAME, name);
        contentValues.put(Constants.TIME, seconds);
        contentValues.put(Constants.STEPS, steps);
        long id = db.insert(Constants.TABLE_NAME, null, contentValues);
        return id;
    }
}
