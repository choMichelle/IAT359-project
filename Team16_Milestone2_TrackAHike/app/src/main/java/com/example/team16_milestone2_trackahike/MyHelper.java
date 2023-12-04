package com.example.team16_milestone2_trackahike;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

//used to create/update database schema
public class MyHelper extends SQLiteOpenHelper {

    Context context;

    //creates table to hold exercise stats records
    private static final String CREATE_RECORDS_TABLE =
            "CREATE TABLE "+
                    Constants.TABLE_NAME + " (" +
                    Constants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.NAME + " TEXT, " +
                    Constants.TIME + " TEXT, " +
                    Constants.STEPS + " TEXT, " +
                    Constants.DISTANCE + " TEXT, " +
                    Constants.CALORIES + " TEXT, " +
                    Constants.SPEED + " TEXT, " +
                    Constants.CATEGORY + " TEXT);" ;

    //creates table to hold photos related to each exercise record
    private static final String CREATE_PHOTOS_TABLE =
            "CREATE TABLE "+
                    Constants.PHOTOS_TABLE_NAME + " (" +
                    Constants.PHOTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.PHOTO_CONTENT + " BLOB, " +
                    Constants.RECORD_ID + " TEXT);" ;

    //drop the records table
    private static final String DROP_RECORDS_TABLE = "DROP TABLE IF EXISTS " + Constants.TABLE_NAME;

    //drop the photos table
    private static final String DROP_PHOTOS_TABLE = "DROP TABLE IF EXISTS " + Constants.PHOTOS_TABLE_NAME;

    public MyHelper(Context context){
        super (context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_RECORDS_TABLE);
            db.execSQL(CREATE_PHOTOS_TABLE);
            Toast.makeText(context, "onCreate() called", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "exception onCreate() db", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DROP_RECORDS_TABLE);
            db.execSQL(DROP_PHOTOS_TABLE);
            onCreate(db);
            Toast.makeText(context, "onUpgrade called", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "exception onUpgrade() db", Toast.LENGTH_LONG).show();
        }
    }

}
