package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AllRecords extends Activity {
    private RecyclerView mRecycler;
    private CustomAdapter mCustomAdapter;
    private MyDatabase db;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> recordsArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_records);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);

        db = new MyDatabase(this);

        Cursor cursor = db.getData();
        cursor.moveToLast();

        while (!cursor.isBeforeFirst()) {
            int index1 = cursor.getColumnIndex(Constants.NAME);
            String recordName = cursor.getString(index1);
            recordsArray.add(recordName);
            cursor.moveToPrevious();
        }

        for (String s : recordsArray) {
            Log.i("records: ", s.toString());
        }

        mCustomAdapter = new CustomAdapter(recordsArray);
        mRecycler.setAdapter(mCustomAdapter);

        layoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(layoutManager);

    }
}
