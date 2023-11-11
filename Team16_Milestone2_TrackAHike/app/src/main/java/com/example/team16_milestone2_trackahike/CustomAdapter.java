package com.example.team16_milestone2_trackahike;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    public ArrayList<String> list;

    public CustomAdapter(ArrayList<String> list) { this.list = list; }

    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomAdapter.MyViewHolder holder, int position) {
        String item = list.get(position);
        holder.recordNameText.setText(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout layout;
        public TextView recordNameText;
        Context context;

        public MyViewHolder(View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
            recordNameText = (TextView) itemView.findViewById(R.id.recordNameTextView);

            itemView.setOnClickListener(this);
            context = itemView.getContext();
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, SpecificRecord.class);
            //TODO - add data to identify record to display
            context.startActivity(i);
        }
    }
}
