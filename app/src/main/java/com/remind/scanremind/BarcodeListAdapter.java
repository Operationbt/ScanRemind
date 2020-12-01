package com.remind.scanremind;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;


public class BarcodeListAdapter extends BaseAdapter {
    private ArrayList<BarcodeData> bList;
    public BarcodeListAdapter() {
    }
    public BarcodeListAdapter(ArrayList<BarcodeData> blist){
        this.bList = blist;
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
        final Context context = parent.getContext();
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_list_item, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.imageView_pic);
        TextView name = convertView.findViewById(R.id.textView_name);
        TextView number = convertView.findViewById(R.id.textView_dday);

        try{
            image.setImageBitmap(BitmapFactory.decodeFile(bList.get(position).getImageSrc()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //image.setImageResource(bList.get(position).getImageSrc());
        name.setText(bList.get(position).getName());

        long dday = getDday(bList.get(position).getDday());
        String op = null;
        if(dday >= 0) {
            op = "-";
            number.setText("D" + op + dday);
        } else{
            op = "+";
            number.setText("D" + op + (-dday));
        }

        //기간 임박한 항목의 배경색을 짙게 해주기
        //convertView.setBackgroundColor(0xFF4CAF50); //Meterial 500 green
        convertView.setBackgroundColor(0xFFFFFFF);
        convertView.findViewById(R.id.textView_dday).setBackgroundColor(0xFF4CAF50);
        SharedPreferences sharedPreferences = context.getSharedPreferences("daily alarm", MODE_PRIVATE);
        long ddayThreshold = sharedPreferences.getInt("ddayThreshold", 1);
        if(dday <= ddayThreshold) {
            //convertView.setBackgroundColor(Color.RED);
            convertView.findViewById(R.id.textView_dday).setBackgroundColor(Color.RED);
            //ddayThreshold값을 3분할 해서 노랑-주황-빨강 차등 해보기
            //Meterial 500 Y->R
            //FFFFC107
            //FFFF9800
            //FFFF5722
        }



        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(BarcodeData item) {
        //item.setImageSrc(null);
        bList.add(item);
    }

    private long getDday(String ddayStr) {
        // D-day 설정
        final int ONE_DAY = 24 * 60 * 60 * 1000;
        String[] temp = ddayStr.split("-");
        int year = Integer.parseInt(temp[0]);
        int month = Integer.parseInt(temp[1]) - 1; //DatePicker가 월은 1작은 값으로 반환해서
        int day = Integer.parseInt(temp[2]);

        Calendar ddayCalendar = Calendar.getInstance();
        ddayCalendar.set(year, month, day);

        // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
        final long dday = ddayCalendar.getTimeInMillis() / ONE_DAY;
        final long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;
        long result = dday - today;

        return result;
    }
}
