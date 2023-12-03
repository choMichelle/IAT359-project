package com.example.team16_milestone2_trackahike;

public class Constants {
    public static final String DATABASE_NAME = "recordsdatabase";

    //constants for the exercise sessions table
    public static final String TABLE_NAME = "recordstable";
    public static final String UID = "_id";
    public static final String NAME = "name";
    public static final String TIME = "Time";
    public static final String STEPS = "Steps";
    public static final String CATEGORY = "Category";

    //constants for the photos table, each record holds 1 photo
    public static final String PHOTOS_TABLE_NAME = "photostable";
    public static final String PHOTO_ID = "_photoID";
    public static final String PHOTO_CONTENT = "photoBytes";
    public static final String RECORD_ID = "recordID"; //foreign key to link to individual records

    public static final int DATABASE_VERSION = 6;
}
