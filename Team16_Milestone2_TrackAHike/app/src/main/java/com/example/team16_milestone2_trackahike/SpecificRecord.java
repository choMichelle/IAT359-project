package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class SpecificRecord extends Activity implements View.OnClickListener {
    private TextView recordNameText, recordedTimeText, recordedStepsText, recordedCategoryText,
            recordedDistanceText;
    private String recordID;
    private Button deleteButton;
    private MyDatabase db;
    private MyHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_record);

        db = new MyDatabase(this);

        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        recordNameText = (TextView) findViewById(R.id.currentRecordNameText);
        recordedTimeText = (TextView) findViewById(R.id.recordedTimeText);
        recordedStepsText = (TextView) findViewById(R.id.recordedStepsText);
        recordedCategoryText = (TextView) findViewById(R.id.recordedCategoryText);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String selectedRecord = extras.getString("recordName");

            Cursor cursor = db.getData();

            int index0 = cursor.getColumnIndex(Constants.UID);
            int index1 = cursor.getColumnIndex(Constants.NAME);
            int index2 = cursor.getColumnIndex(Constants.TIME);
            int index3 = cursor.getColumnIndex(Constants.STEPS);
            int index4 = cursor.getColumnIndex(Constants.CATEGORY);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String recordName = cursor.getString(index1);
                if (recordName.equals(selectedRecord)) {
                    recordID = cursor.getString(index0);
                    String recordSteps = cursor.getString(index3);
                    String recordCategory = cursor.getString(index4);

                    recordNameText.setText(recordName);
                    recordedStepsText.setText(recordSteps);
                    recordedCategoryText.setText(recordCategory);

                    try {
                        int recordTime = Integer.parseInt(cursor.getString(index2));
                        int hrs = recordTime / 3600;
                        int mins = (recordTime % 3600) / 60;
                        int secs = recordTime % 60;
                        recordedTimeText.setText(hrs + ":" + mins + ":" + secs);
                    }
                    catch (NumberFormatException e){
                        Log.e("Parse time int: ", "could not parse error: " + e);
                    }

                    break;
                }
                cursor.moveToNext();
            }

        }
    }

    @Override
    public void onClick(View v) {
        db.deleteRecord(recordID);
        Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, AllRecords.class);
        startActivity(i);

    }
}
