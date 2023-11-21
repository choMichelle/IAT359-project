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

//handle populating recycler view with items
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    public ArrayList<String> list;

    public CustomAdapter(ArrayList<String> list) { this.list = list; }

    //inflate recycler view with layout from row.xml
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    //get items from the dataset and set text
    @Override
    public void onBindViewHolder(CustomAdapter.MyViewHolder holder, int position) {
        String item = list.get(position).toString();
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

        //redirect to specific record activity on clicking a record
        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, SpecificRecord.class);
            i.putExtra("recordName", recordNameText.getText().toString());
            context.startActivity(i);
        }
    }
}
