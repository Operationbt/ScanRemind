package com.remind.scanremind;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//바코드로 스캔하거나 사진으로 직접 추가한 항목의 정보를 입력하는 액티비티
public class SetScanInfoActivity extends AppCompatActivity {
    // Millisecond 형태의 하루(24 시간)
    private final int ONE_DAY = 24 * 60 * 60 * 1000;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BARCODE_SCAN = 49374; //zxing 바코드 스캔 CaptureActivity requestCode = c0de
    //static final String IMAGE_PATH = ""
    private BarcodeData barcodeData = new BarcodeData();
    private String mode = null;
    private Bitmap thumbImage = null;
    private int list_index = -1;
    private boolean isImageChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_scan_info);

        final ImageView imageView = findViewById(R.id.imageView_setThumb);
        final TextView noticeText = findViewById(R.id.textView_Notice);
        final EditText editText = findViewById(R.id.editText_name);
        final TextView ddayText = findViewById(R.id.textView_dday);
        final Button saveButton = findViewById(R.id.buttonSave);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                barcodeData.setName(s.toString());
            }
        });

        noticeText.setVisibility(View.GONE);
        //메인액티비티에서 수정/추가 정보를 인텐트로 받아옴
        Intent intent = new Intent(this.getIntent());
        mode = intent.getStringExtra("mode");
        if(mode.equals("add")) {
            saveButton.setText("저장");
        }
        if(mode.equals("edit")) {
            list_index = intent.getIntExtra("index", -1);
            barcodeData = (BarcodeData) intent.getSerializableExtra("item");
            if(!(barcodeData.getImageSrc() == null || barcodeData.getImageSrc().isEmpty())) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(barcodeData.getImageSrc()));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            editText.setText(barcodeData.getName());
            ddayText.setText(getDday(barcodeData.getDday()) + "일 남았습니다");
            saveButton.setText("수정");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_BARCODE_SCAN : //바코드 스캔 결과
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() != null) {
                        //Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();
                        barcodeData.setNumber(result.getContents()); //데이터 객체 스캔된 바코드 넘버로 초기화
                        //Toast.makeText(getApplicationContext(), "검색 중", Toast.LENGTH_SHORT).show();
                        // AsyncTask를 통해 HttpURLConnection 수행.
                        NetworkTask networkTask = new NetworkTask(barcodeData.getNumber());
                        networkTask.execute();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE : //일반 촬영 결과
                if(resultCode == RESULT_OK) {
                    this.isImageChanged = true;
                    Bundle extras = data.getExtras();
                    thumbImage = (Bitmap) extras.get("data");
                    ImageView imageView = findViewById(R.id.imageView_setThumb);
                    imageView.setImageBitmap(thumbImage);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    TextView noticeText = findViewById(R.id.textView_Notice);
                    noticeText.setVisibility(View.GONE);
                }
                break;
        }
    }

    //사진 촬영 클릭 이벤트
    public void selCameraModeOnClick(View view) {
        //권한 확인
        int permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permissonCheck == PackageManager.PERMISSION_DENIED) {
            //Toast.makeText(getApplicationContext(), "카메라 권한 없음", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        if(permissonCheck == PackageManager.PERMISSION_GRANTED) {
            //인터넷 연결 되있으면
            if(CheckNetworkStatus.getConnectivityStatus(getApplicationContext()) != CheckNetworkStatus.TYPE_NOT_CONNECTED) {
                //바코드 촬영 or 일반 사진 촬영 중 택 1 다이얼로그 띄워야함
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                View dlgView = getLayoutInflater().inflate(R.layout.layout_dialog_camera_sel, null);
                ImageView selCamera = dlgView.findViewById(R.id.imageView_camera);
                ImageView selBarcode = dlgView.findViewById(R.id.imageView_barcode);
                alert.setView(dlgView);

                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(true); //다이얼로그 바깥 회색 배경 누르면 빠져나오기 허용

                selCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { //일반 촬영을 선택한 경우
                        //일반 사진 촬영 엑티비티 호출
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                        //Toast.makeText(getApplicationContext(), "카메라", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });
                selBarcode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { //바코드 촬영을 선택한 경우
                        //바코드 스캔하는 CaptureActivity로 이동
                        IntentIntegrator integrator = new IntentIntegrator(SetScanInfoActivity.this);
                        integrator.setCaptureActivity(CaptureActivity.class);
                        integrator.setOrientationLocked(false);
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                        integrator.setPrompt("SCAN CODE");
                        integrator.initiateScan(); //->onActivityResult case REQUEST_BARCODE_SCAN로 이동

                        //Toast.makeText(getApplicationContext(), "바코드", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            } else { //인터넷 연결 안되있으면
                //일반 사진 촬영 인텐트 호출
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
        if(mode.equals("add")) {   //추가 버튼 눌러서 왔을 때


        } else if(mode.equals("edit")) {   //리스트에서 수정 눌러서 왔을 때

        }
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

        //dday = getDday(year, month, day); 알림 로직 구현할때 dday수치보다 날짜 형식이 더 용이한듯
        barcodeData.setDday(year_string + "-" + month_string + "-" + day_string);

        //textview에는 dday 수치를 보여줘야함
        TextView textDday = findViewById(R.id.textView_dday);
        textDday.setText(getDday(year, month, day) + "일 남았습니다");
    }

    private String getDday(int a_year, int a_monthOfYear, int a_dayOfMonth) {
        final Calendar ddayCalendar = Calendar.getInstance();
        ddayCalendar.set(a_year, a_monthOfYear, a_dayOfMonth); //년월일을 Calendar로 조립

        // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
        final long dday = ddayCalendar.getTimeInMillis() / ONE_DAY;
        final long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;
        long result = dday - today;

        return Long.toString(result);
    }
    private String getDday(String ddayStr) {
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

        return Long.toString(result);
    }

    //완성된 BarcodeData를 MainActivity에 전달
    public void setSavebtnOnclick(View v) {
        if(barcodeData.getName() == null || barcodeData.getName().isEmpty()) {
            Toast.makeText(getApplicationContext(), "이름을 입력해주세요", Toast.LENGTH_LONG).show();
            return;
        } else if(barcodeData.getDday() == null) {
            Toast.makeText(getApplicationContext(), "기간을 설정해주세요", Toast.LENGTH_LONG).show();
            return;
        }//return condition

        //make intent
        Intent intent = new Intent(SetScanInfoActivity.this, MainActivity.class);
        if(mode.equals("add")) {
            barcodeData.setRegDate(nowTimetoString());
            barcodeData.setItemNum(System.currentTimeMillis()); //생성될 아이템의 기본키 값 시간으로 사용
        } else if(mode.equals("edit")) {
            intent.putExtra("index", this.list_index);
        }
        //비트맵 유효하면 thumbImage를 파일로 저장하고 경로 획득
        if(thumbImage != null) {
            if(isImageChanged) { //바코드로 사진 얻었거나 일반 촬영으로 사진 얻은 경우
                barcodeData.setImageSrc(saveBitmap(thumbImage, Long.toString(barcodeData.getItemNum())));
            }
        }

        intent.putExtra("barcodedata", barcodeData);
        setResult(1, intent);
        finish();
    }
    private String saveBitmap(Bitmap bitmap, String name) {
        //내부저장소 캐시 경로를 받아옵니다.
        File storage = getCacheDir();
        //저장할 파일 이름
        String fileName = name + ".png";
        //storage 에 파일 인스턴스를 생성합니다.
        File tempFile = new File(storage, fileName);
        try {
            // 자동으로 빈 파일을 생성합니다.
            tempFile.createNewFile();

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(tempFile);

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            // 스트림 사용후 닫아줍니다.
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("MyTag","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MyTag","IOException : " + e.getMessage());
        }

        return tempFile.getAbsolutePath();
    }

    public static String nowTimetoString() {
        long now = System.currentTimeMillis();
        java.util.Date date = new java.util.Date(now);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(date);
        return result;
    }

    public class NetworkTask extends AsyncTask<Void, Void, BarcodeData> {
        private String number;
        private ProgressDialog progressDialog;

        public NetworkTask(String number) {
            this.number = number;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //검색 완료 후 다음 작업 할 수 있도록 한다.
            progressDialog = new ProgressDialog(SetScanInfoActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("검색 중입니다.");
            progressDialog.show();
        }

        @Override
        protected BarcodeData doInBackground(Void... params) {
            String name = null; // 요청 결과를 저장할 변수.
            String imgSrc = null;
            try {
                Document doc = null;
                //전체 html문서 획득
                doc = Jsoup.connect("http://www.koreannet.or.kr/home/hpisSrchGtin.gs1?gtin=" + number).get();
                //파싱
                Elements title = doc.select(".productTit"); //class명이 productTit인 것을 선택
                if(!title.text().isEmpty()) {
                    name = title.text().substring(14);    //13자리 GTIN번호와 공백 하나 제거
                    Log.d("doInBackground", "name:" + name);
                }
                imgSrc = doc.select("#detailImage").attr("src"); //id가 detailImage 것에서 src속성 선택
                if(imgSrc.contains("no_img"))   //대표사진 없는 물품인 경우
                    imgSrc = null;
                //barcodeData.setImageSrc(imgSrc);
                Log.d("doInBackground", "imgSrc:" + imgSrc);

                if(imgSrc != null) {
                    URL url = new URL(imgSrc);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    thumbImage = BitmapFactory.decodeStream(bis);
                    isImageChanged = true;
                    bis.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarcodeData(-1, number, name, imgSrc, null, null); //아직 저장버튼 누른건 아니므로 itemNum = -1
        }


        @Override
        protected void onPostExecute(BarcodeData data) {
            super.onPostExecute(data);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            Log.d("onPostExecute", "onPostExecute: " + data);
            EditText editText = findViewById(R.id.editText_name);
            TextView noticeText = findViewById(R.id.textView_Notice);
            if(data.getName() != null) {
                noticeText.setVisibility(View.GONE);
                editText.setText(data.getName());
                barcodeData.setName(data.getName()); //검색된 품목명으로 초기화
                //대표 이미지를 썸네일로 설정
                if(data.getImageSrc() != null) {
                    ImageView imageView = findViewById(R.id.imageView_setThumb);
                    imageView.setImageBitmap(thumbImage);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            } else {
                noticeText.setText("검색된 상품이 없습니다!\n일반 촬영 모드를 선택해주세요");
                noticeText.setVisibility(View.VISIBLE);
            }
            progressDialog.dismiss(); //검색중 다이얼로그 해제
        }
    }
}
