package com.remind.scanremind;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String USER_NAME = "tester";
    TextView txt_barcodeNum;
    TextView txt_SQLResult;
    ListView lstView_barcode;
    ArrayList<BarcodeData> bList = new ArrayList<>();
    ArrayAdapter<BarcodeData> adapter;

    private DatabaseReference bListDAO;
    private ChildEventListener bListDAOListener;
    DBhelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.InitializeFirebaseDB();
        this.InitializeDB();
        this.InitializeView();
        this.InitializeBarcodeList();   //DB->리스트구조


    }
    //SQLiteDB 초기화
    private void InitializeDB() {
        dbHelper = new DBhelper(this);
    }
    public static String nowTimetoString() {
        long now = System.currentTimeMillis();
        java.util.Date date = new java.util.Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = sdf.format(date);
        return result;
    }
    //DB에 바코드데이터 삽입
    private void insert(BarcodeData b) {
        // Gets the data repository in write mode
        String getTime = nowTimetoString();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNUMBER, b.getNumber());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNAME, b.getName());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_IMGID, b.getImageID());
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE, getTime);
        values.put(BarcodeDataContract.BarcodeTable.COLUMN_NAME_DDAY, "1");
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(BarcodeDataContract.BarcodeTable.TEST_TABLE_NAME, null, values);

        txt_SQLResult.append("\nInsert Success");
    }

    //DB에서 데이터 리스트로 반환
    private ArrayList<BarcodeData> selectList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<BarcodeData> selList = new ArrayList<>();
        String sql = "SELECT * FROM " + BarcodeDataContract.BarcodeTable.TEST_TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {

            String number;
            String name;
            int imageID;
            String regDate;
            String dday;
            while (cursor.moveToNext()) {
                number = cursor.getString(0);
                name = cursor.getString(1);
                imageID = cursor.getInt(2);
                regDate = cursor.getString(3);
                dday = cursor.getString(4);

                txt_SQLResult.append(String.format("\nnumber = %s, name = %s, img = %s, date = %s, dday=%s",
                        number, name, imageID, regDate, dday));
                selList.add(new BarcodeData(number, name, imageID, regDate, dday));
            }
        } else {
            txt_SQLResult.append("\n조회결과가 없습니다.");
        }
        cursor.close();

        return selList;
    }

    //DB 전체 삭제
    private void delete() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM " + BarcodeDataContract.BarcodeTable.TEST_TABLE_NAME + ";";
        db.execSQL(sql);
        txt_SQLResult.append("\nDelete Success");
    }

    //DB 선택 삭제
    private void delete(BarcodeData b) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM " + BarcodeDataContract.BarcodeTable.TEST_TABLE_NAME + " WHERE " +
                BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE + " = '" + b.getRegDate() + "';";
        db.execSQL(sql);
        txt_SQLResult.append("\nSel Delete Success");
    }

    //SQL실행 결과로 바코드 데이터 생성
    private BarcodeData createFromResultSet(ResultSet rs) throws SQLException {
        String number = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNUMBER);
        String name = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_BNAME);
        int imageID = rs.getInt(BarcodeDataContract.BarcodeTable.COLUMN_NAME_IMGID);
        String regDate = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_REGDATE);
        String dday = rs.getString(BarcodeDataContract.BarcodeTable.COLUMN_NAME_DDAY);

        BarcodeData b = new BarcodeData(number, name, imageID, regDate, dday);

        return b;
    }


    //파이어베이스 인스턴스 초기화, 이벤트 초기화
    private void InitializeFirebaseDB() {
        bListDAO = FirebaseDatabase.getInstance().getReference();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot l : dataSnapshot.getChildren()) {
                    for (DataSnapshot e : l.getChildren()) {
                        bList.add(e.getValue(BarcodeData.class));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        bListDAO.addListenerForSingleValueEvent(listener);

    }

    public void InitializeView() {
        txt_barcodeNum = findViewById(R.id.txtBarcodeNum);

        //SQL테스트용 텍스트박스
        txt_SQLResult = findViewById(R.id.txtSQL);
        txt_SQLResult.setMovementMethod(new ScrollingMovementMethod());

        //리스트 뷰 초기화
        lstView_barcode = findViewById(R.id.lstView_barcode);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bList);
        lstView_barcode.setAdapter(adapter);
        //클릭 시 발생하는 이벤트
        lstView_barcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        adapter.getItem(position).getNumber(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        //롱클릭 시 리스트에서 원소 삭제
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
                        //setBarcodeListFireBase();
                        adapter.notifyDataSetChanged();
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

        adapter.notifyDataSetChanged();

    }
    public void InitializeBarcodeList() {
        bList.addAll(selectList());
        adapter.notifyDataSetChanged();
    }
    public void setBarcodeListFireBase() {

        bListDAO.child(USER_NAME).setValue(bList);

    }



    //바코드 리스트에 데이터 추가
    //지금은 스캔한 바코드 넘버만 넘긴다
    public void addBarcodeList(String number) {
        //todo-바코드 넘버를 토대로 제품이름, 사진, dDay 검색해서 얻어와야함
        //bList.add(new BarcodeData(number, name, img, nowTimetoString(), dday));
        bList.add(new BarcodeData(number, "샘플", 1, nowTimetoString(), null));
        Log.d("addList", bList.toString());
        //setBarcodeListFireBase();
        insert(bList.get(bList.size() - 1));
        adapter.notifyDataSetChanged();
    }


    //스캔하고 메인액티비티로 넘어올 때 이벤트
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                //Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();
                txt_barcodeNum.setText(result.getContents());
                addBarcodeList(result.getContents());
            }
            else {
                Toast.makeText(getApplicationContext(), "No Result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void btnScan(View v) {
        scanMethod();
    }

    public void btnSelect(View v) {
        selectList();
    }

    public void btnDelete(View v) {
        delete();
        bList.clear();
        adapter.notifyDataSetChanged();
    }

    public void scanMethod() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("SCAN CODE");
        integrator.initiateScan();
    }

    //testButton
    public void btnTest(View v) {
        Intent intent = new Intent(MainActivity.this, SetScanInfoActivity.class);
        intent.putExtra("bbb", "hello");
        startActivity(intent);
    }


}
