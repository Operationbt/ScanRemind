package com.remind.scanremind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
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

        //메인액티비티에서 수정/추가 정보를 인텐트로 받아옴
        Intent intent = new Intent(this.getIntent());
        String s = intent.getStringExtra("mode");
        TextView textView = findViewById(R.id.textView);
        textView.setText(s);

        if(s.equals("add")) {
            //바코드 스캔하는 CaptureActivity로 이동
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("SCAN CODE");
            integrator.initiateScan();

            if(number != null) {
                // URL 설정.
                Toast.makeText(getApplicationContext(), "검색 중", Toast.LENGTH_SHORT).show();
                String url = "http://www.koreannet.or.kr/home/hpisSrchGtin.gs1?gtin=" + number;

                // AsyncTask를 통해 HttpURLConnection 수행.
                NetworkTask networkTask = new NetworkTask(number);
                networkTask.execute();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 49374 :    //zxing 바코드 스캔 CaptureActivity requestCode = c0de
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() != null) {
                        //Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();

                        TextView textView2 = findViewById(R.id.textView_barNum);
                        textView2.setText(result.getContents());
                        number = result.getContents();  //스캔된 바코드 넘버

                        //Toast.makeText(getApplicationContext(), "검색 중", Toast.LENGTH_SHORT).show();
                        // AsyncTask를 통해 HttpURLConnection 수행.
                        NetworkTask networkTask = new NetworkTask(number);
                        networkTask.execute();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        //바코드 스캔 정보

    }

    //dday설정 달력, 일수 클릭 이벤트
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
        TextView textDday = findViewById(R.id.textView_dday);
        textDday.setText(dday + "일 후에 알림이 울립니다.");
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
        intent.putExtra("name", name);
        intent.putExtra("img", nowTimetoString());
        intent.putExtra("date", nowTimetoString());
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

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String number;
        private ProgressDialog progressDialog;

        public NetworkTask(String number) {
            this.number = number;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SetScanInfoActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시 기다려 주세요.");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = ""; // 요청 결과를 저장할 변수.
//            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
//            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.
//
//            return parseBarcodeData(result);

            try {
                Document doc = null;
                doc = Jsoup.connect("http://www.koreannet.or.kr/home/hpisSrchGtin.gs1?gtin=" + number).get();
                Elements title = doc.select(".productTit"); //class명이 productTit인 것을 선택
                result = title.text().substring(14);    //13자리 GTIN번호와 공백 하나 제거

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            Log.d("onPostExecute", "onPostExecute: " + s);
            TextView textViewName = findViewById(R.id.textView_name);
            textViewName.setText(s);
            name = s; //검색된 품목명으로 초기화

            progressDialog.dismiss();
        }
    }
}
