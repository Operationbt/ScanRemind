package com.remind.scanremind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class BarcodeListAdapter extends BaseAdapter {

    SQLiteDatabase db;
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<BarcodeData> bList;

    public BarcodeListAdapter(Context mContext, ArrayList<BarcodeData> bList) {
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.bList = bList;
    }

    @Override
    public int getCount() {
        return bList.size();
    }

    @Override
    public BarcodeData getItem(int position) {
        return bList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.barcode_list, null);

        ImageView image = view.findViewById(R.id.imageViewImage);
        TextView name = view.findViewById(R.id.textViewName);
        TextView number = view.findViewById(R.id.textViewNum);

        image.setImageResource(bList.get(position).getImageID());
        name.setText(bList.get(position).getName());
        number.setText(bList.get(position).getNumber());

        return view;
    }
}
