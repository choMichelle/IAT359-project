package com.example.team16_milestone2_trackahike;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//handle populating recycler view with items
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    public ArrayList<String> list; //holds dataset
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
        String recID = list.get(position).toString(); //is uid of the record
        holder.recordID = recID;
        Cursor cursor = holder.db.getData(); //get all record data

        int index0 = cursor.getColumnIndex(Constants.UID);

        cursor.moveToLast();
        while (!cursor.isBeforeFirst()) {
            String checkRecordUID = cursor.getString(index0); //get UID of current record
            if (recID.equals(checkRecordUID)) { //check if current record UID and UID from dataset match

                int index1 = cursor.getColumnIndex(Constants.NAME);
                int index2 = cursor.getColumnIndex(Constants.CATEGORY);

                String recName = cursor.getString(index1);
                String groupName = cursor.getString(index2);

                holder.recordNameText.setText(recName);
                holder.recordGroupText.setText(groupName);

                //get a photo for the record preview
                Cursor photoCursor = holder.db.getPhotos(recID);
                int photoBytesIndex = photoCursor.getColumnIndex(Constants.PHOTO_CONTENT);

                photoCursor.moveToFirst(); //go to the first image
                if (photoCursor != null && photoCursor.getCount() > 0) { //check if there are any photos
                    byte[] photoBytes = photoCursor.getBlob(photoBytesIndex); //get photo byte array
                    Bitmap photoBitmap = Utility.toBitmap(photoBytes); //convert byte array to bitmap;
                    holder.previewPhoto.setImageBitmap(photoBitmap);
                }
                else {
                    //do nothing
                }
            }

            cursor.moveToPrevious();

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout layout;
        public TextView recordNameText, recordGroupText;
        public ImageView previewPhoto;
        public String recordID;
        private MyDatabase db;
        Context context;

        public MyViewHolder(View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
            recordNameText = (TextView) itemView.findViewById(R.id.previewRecNameText);
            recordGroupText = (TextView) itemView.findViewById(R.id.previewGroupNameText);
            previewPhoto = (ImageView) itemView.findViewById(R.id.previewPhotoView);

            itemView.setOnClickListener(this);
            context = itemView.getContext();

            db = new MyDatabase(context);
        }

        //redirect to specific record activity on clicking a record
        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, SpecificRecord.class);
            i.putExtra("recordID", recordID);
            context.startActivity(i);
        }
    }
}
