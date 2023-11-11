package com.example.team16_milestone2_trackahike;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

public class AllRecords extends Activity {
    private RecyclerView mRecycler;
    private CustomAdapter mCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_records);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);

        //TODO - set up adapter and dataset
    }
}
