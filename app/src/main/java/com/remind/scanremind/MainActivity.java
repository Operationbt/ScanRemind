package com.remind.scanremind;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ActivityInfoCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Millisecond 형태의 하루(24 시간)
    private final int ONE_DAY = 24 * 60 * 60 * 1000;
    public static final String USER_NAME = "tester";
    static final int REQUEST_ADD_ITEM = 1;
    static final int REQUEST_EDIT_ITEM = 2;
    ListView lstView_barcode;
    ArrayList<BarcodeData> bList = new ArrayList<>();
    //ArrayAdapter<BarcodeData> adapter;
    BarcodeListAdapter bListAdapter;

    private DatabaseReference bListDAO;
    private ChildEventListener bListDAOListener;
    DBhelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //권한 확인
        int permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permissonCheck == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "카메라 권한이 없으면 기능 이용에 제한이 있을 수 있습니다", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }


        //this.InitializeFirebaseDB();
        this.InitializeDB();
        this.InitializeView();
        this.InitializeBarcodeList();   //DB->리스트구조

        refreshDdayItemCount(bList);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bListAdapter.notifyDataSetChanged();
        refreshDdayItemCount(bList);
    }

    //SQLiteDB 초기화
    private void InitializeDB() {
        dbHelper = new DBhelper(this);
    }
    public static String nowTimetoString() {
        long now = System.currentTimeMillis();
        java.util.Date date = new java.util.Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(date);
        return result;
    }
    //DB에 바코드데이터 삽입
    private void insert(BarcodeData b) {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM, b.getItemNum());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNUMBER, b.getNumber());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNAME, b.getName());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_IMGSRC, b.getImageSrc());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE, b.getRegDate());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_DDAY, b.getDday());
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(BarcodeDataContract.BarcodeTable.TABLE_NAME, null, values);
        Log.d("insert in DB", "data:" + b.toString());
    }
    //DB 바코드데이터 수정
    private void edit(BarcodeData b) {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Which row to update, based on the title
        String selection = BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM + " LIKE ?";
        String[] selectionArgs = { Long.toString(b.getItemNum()) };

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM, b.getItemNum());
        //values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNUMBER, b.getNumber());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNAME, b.getName());
        //values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_IMGSRC, b.getImageSrc());
        //values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE, b.getRegDate());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_DDAY, b.getDday());

        db.update(BarcodeDataContract.BarcodeTable.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    //DB에서 데이터 리스트로 반환
    private ArrayList<BarcodeData> selectList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<BarcodeData> selList = new ArrayList<>();
        String sql = "SELECT * FROM " + BarcodeDataContract.BarcodeTable.TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            long itemNum;
            String number;
            String name;
            String imageSrc;
            String regDate;
            String dday;
            while (cursor.moveToNext()) {
                itemNum = cursor.getLong(0);
                number = cursor.getString(1);
                name = cursor.getString(2);
                imageSrc = cursor.getString(3);
                regDate = cursor.getString(4);
                dday = cursor.getString(5);
                selList.add(new BarcodeData(itemNum, number, name, imageSrc, regDate, dday));
            }
        }
        cursor.close();

        return selList;
    }

    //DB 전체 삭제
    private void deleteAll() {
        deleteAllImageFiles();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM " + BarcodeDataContract.BarcodeTable.TABLE_NAME + ";";
        db.execSQL(sql);
    }

    //DB 선택 삭제
    private void delete(BarcodeData b) {
        deleteImageFile(b.getImageSrc());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM " + BarcodeDataContract.BarcodeTable.TABLE_NAME + " WHERE " +
                BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM + " = '" + b.getItemNum() + "';";
        db.execSQL(sql);
    }
//    private void delete(int itemNum) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        String sql = "DELETE FROM " + BarcodeDataContract.BarcodeTable.TABLE_NAME + " WHERE " +
//                BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM + " = '" + itemNum + "';";
//        db.execSQL(sql);
//    }
    private void deleteImageFile(String path) {
        if(path == null || path.isEmpty())
            return;
        File file = new File(path);
        file.delete();
    }
    private void deleteAllImageFiles() {
        try{
            File file = getCacheDir();
            File[] flist = file.listFiles();
            for(int i = 0 ; i < flist.length ; i++)
            {
                String fname = flist[i].getName();
                if(fname.contains(".png"))
                {
                    flist[i].delete();
                }
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "파일 삭제 실패 ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //SQL실행 결과로 데이터 생성
    private BarcodeData createFromResultSet(ResultSet rs) throws SQLException {
        long itemNum = rs.getInt(BarcodeDataContract.BarcodeTable.COLUMN_NAME_ITEMNUM);
        String number = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNUMBER);
        String name = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNAME);
        String imageSrc = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_IMGSRC);
        String regDate = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE);
        String dday = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_DDAY);

        BarcodeData b = new BarcodeData(itemNum, number, name, imageSrc, regDate, dday);
        return b;
    }

    public void InitializeView() {
        //리스트 뷰 초기화
        lstView_barcode = findViewById(R.id.lstView_barcode);
        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bList);
        //lstView_barcode.setAdapter(adapter);
        bListAdapter = new BarcodeListAdapter(bList); //어댑터 아이템은 bList를 가져가도록 초기화
        lstView_barcode.setAdapter(bListAdapter);

        //리스트 클릭 시 발생하는 이벤트
        lstView_barcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //SetScanInfoActivity로 이동해서 수정
                Intent intent = new Intent(MainActivity.this, SetScanInfoActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("item", bList.get(position));
                intent.putExtra("index", position);
                startActivityForResult(intent, 2);
            }
        });

        //리스트 롱클릭 시 발생하는 이벤트
        lstView_barcode.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDlg = new AlertDialog.Builder(view.getContext());
                alertDlg.setMessage(bList.get(position).getName() + " 삭제할까요?");
                alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DB에서 롱클릭 원소 찾아서 정보 삭제
                        delete(bList.get(position));
                        //리스트에서도 원소 삭제
                        bList.remove(position);
                        bListAdapter.notifyDataSetChanged();
                        //임박 품목 갯수 수정
                        refreshDdayItemCount(bList);
                    }
                });
                alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDlg.show();
                return true;
            }
        });
        Log.d("InitializeView", bList.toString());

        bListAdapter.notifyDataSetChanged();

    }
    //DB에 저장된 데이터를 bList로 옮기고 리스트뷰 갱신
    public void InitializeBarcodeList() {
        bList.addAll(selectList());
        bListAdapter.notifyDataSetChanged();
    }
    public void setBarcodeListFireBase() {

        bListDAO.child(USER_NAME).setValue(bList);

    }

    //바코드 리스트에 데이터 추가
    public void addBarcodeList(BarcodeData data) {
        bList.add(data);
        bListAdapter.notifyDataSetChanged();
        insert(bList.get(bList.size() - 1));
    }
    //바코드 리스트 데이터 수정
    public void editBarcodeList(int index, BarcodeData data) {
        bList.set(index, data);
        bListAdapter.notifyDataSetChanged();
        edit(data);
    }

    //스캔하고 메인액티비티로 넘어올 때 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ADD_ITEM : //SetScanInfoActivity's add result
                if(resultCode == 1) {
//                    addBarcodeList(data.getStringExtra("number"),
//                            data.getStringExtra("name"),
//                            data.getStringExtra("img"),
//                            data.getStringExtra("date"),
//                            data.getStringExtra("dday"));
                    addBarcodeList((BarcodeData) data.getSerializableExtra("barcodedata"));
                } else {
                    //Toast.makeText(MainActivity.this, "add Failed", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_EDIT_ITEM : //SetScanInfoActivity's edit result
                if(resultCode == 1) {
                    editBarcodeList(data.getIntExtra("index", -1),
                            (BarcodeData) data.getSerializableExtra("barcodedata"));
                } else {
                    //Toast.makeText(MainActivity.this, "edit Failed", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //추가 버튼 눌러서 SetScanInfoActivity로 이동
    public void btnScan(View v) {
        Intent intent = new Intent(MainActivity.this, SetScanInfoActivity.class);
        intent.putExtra("mode", "add");
        startActivityForResult(intent, REQUEST_ADD_ITEM);
        //startActivity(intent);
    }

    //리스트 중에서 dday임박한 항목들의 갯수 반환
    public int getImminentCount(ArrayList<BarcodeData> list) {
        //memo BarcodeListAdapter에 notifyDataSetChanged 오버라이드 하고 로직 개선할 수 있지 않을까
        int count = 0;
        int ddayThreshold = 1;
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        ddayThreshold = sharedPreferences.getInt("ddayThreshold", 1);

        long dday;
        long today = Calendar.getInstance().getTimeInMillis() / ONE_DAY;

        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");

        for(BarcodeData b : list) {
            Calendar ddayCalendar = Calendar.getInstance();
            try {
                ddayCalendar.setTime(fm.parse(b.getDday()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dday = ddayCalendar.getTimeInMillis() / ONE_DAY;

            if(dday - today <= ddayThreshold) {
                count++;
            }
        }
        //  Preference에 값 저장
        SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
        editor.putInt("thresholdCount", count);
        editor.apply();

        return count;
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

    public void refreshDdayItemCount(ArrayList<BarcodeData> list) {
        int num = getImminentCount(list);
        TextView textView = findViewById(R.id.textView_ddaynum);
        if(num == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText("임박한 물품 " + num + "개");
        }
    }

    public void btnDelete(View v) {
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("전체 삭제할까요?");
        alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAll();
                bList.clear();
                bListAdapter.notifyDataSetChanged();
                refreshDdayItemCount(bList);
            }
        });
        alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDlg.show();
    }

    public void btnSetAlarmTime(View v){
        Intent intent = new Intent(MainActivity.this, SetAlarmTimeActivity.class);
        startActivity(intent);
    }

    //testButton
    public void btnTest(View v) {

    }

}
