package com.remind.scanremind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SetScanInfoActivity extends AppCompatActivity {
    // Millisecond 형태의 하루(24 시간)
    private final int ONE_DAY = 24 * 60 * 60 * 1000;
    //바코드 정보 필드
    //number, name, img, nowTimetoString(), dday
    String number = null;
    String name = null;
    int imageID = 1;
    String regDate = null;
    String dday = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_scan_info);

        //바코드 스캔하는 CaptureActivity로 이동
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("SCAN CODE");
        integrator.initiateScan();

        //메인액티비티에서 수정/추가 정보를 인텐트로 받아옴
        Intent intent = new Intent(this.getIntent());
        String s = intent.getStringExtra("mode");
        TextView textView = findViewById(R.id.textView);
        textView.setText(s);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //바코드 스캔 정보
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                //Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();

                TextView textView2 = findViewById(R.id.textViewBarnum);
                textView2.setText(result.getContents());
                number = result.getContents();  //스캔된 바코드 넘버
            }
            else {
                Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setDateBtnOnClick(View view) {
        DialogFragment newFragment = new SelDateFragment();
        newFragment.show(getSupportFragmentManager(), "dateSel");
    }

    public void processDatePickerResult(int year, int month, int day){
        //달력에서 선택한 날짜로 dday를 구해서 출력
        String month_string = Integer.toString(month+1);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        String dateMessage = (month_string + "/" + day_string + "/" + year_string);

        Toast.makeText(this,"Date: "+dateMessage,Toast.LENGTH_SHORT).show();

        dday = getDday(year, month, day);
        TextView textDday = findViewById(R.id.textView3);
        textDday.setText(dday);
    }

    private String getDday(int a_year, int a_monthOfYear, int a_dayOfMonth) {
        // D-day 설정
        final Calendar ddayCalendar = Calendar.getInstance();
        ddayCalendar.set(a_year, a_monthOfYear, a_dayOfMonth);

        // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
        final long dday = ddayCalendar.getTimeInMillis() / ONE_DAY;
        final long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;
        long result = dday - today;

        return Long.toString(result);
    }


    public void setSavebtnOnclick(View v) {
        Intent intent = new Intent(SetScanInfoActivity.this, MainActivity.class);
        //number, name, img, nowTimetoString(), dday
        intent.putExtra("number", number);
        intent.putExtra("name", "mask");
        intent.putExtra("img", nowTimetoString());
        intent.putExtra("dday", dday);

        setResult(1, intent);
        finish();
    }

    public static String nowTimetoString() {
        long now = System.currentTimeMillis();
        java.util.Date date = new java.util.Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = sdf.format(date);
        return result;
    }
}
