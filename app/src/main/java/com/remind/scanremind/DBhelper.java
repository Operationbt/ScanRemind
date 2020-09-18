package com.remind.scanremind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBhelper extends SQLiteOpenHelper {


    public DBhelper(Context context) {
        super(context, BarcodeDataContract.TEST_DATABASE_NAME, null, BarcodeDataContract.TEST_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBhelper", "onCreate");
        db.execSQL(BarcodeDataContract.BarcodeTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BarcodeDataContract.BarcodeTable.DELETE_TABLE);
        onCreate(db);
    }
}
