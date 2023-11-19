package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class SpecificRecord extends Activity {
    private TextView recordNameText, recordedTimeText, recordedStepsText, recordedDistanceText;
    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_record);

        db = new MyDatabase(this);

        recordNameText = (TextView) findViewById(R.id.currentRecordNameText);
        recordedTimeText = (TextView) findViewById(R.id.recordedTimeText);
        recordedStepsText = (TextView) findViewById(R.id.recordedStepsText);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String selectedRecord = extras.getString("recordName");

            Cursor cursor = db.getData();

            int index1 = cursor.getColumnIndex(Constants.NAME);
            int index2 = cursor.getColumnIndex(Constants.TIME);
            int index3 = cursor.getColumnIndex(Constants.STEPS);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String recordName = cursor.getString(index1);
                if (recordName.equals(selectedRecord)) {
                    String recordSteps = cursor.getString(index3);

                    recordNameText.setText(recordName);
                    recordedStepsText.setText(recordSteps);

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
}
