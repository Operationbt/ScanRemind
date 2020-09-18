package com.remind.scanremind;

import android.provider.BaseColumns;

public final class BarcodeDataContract {

    public static final  int    DATABASE_VERSION   = 1;
    public static final  int    TEST_DATABASE_VERSION   = 1;
    public static final  String DATABASE_NAME      = "Barcodes.db";
    public static final String TEST_DATABASE_NAME = "Test.db";
    private static final String TEXT_TYPE          = " TEXT";
    private static final String INTEGER_TYPE          = " INTEGER";
    private static final String COMMA_SEP          = ",";
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private BarcodeDataContract() {}

    /* Inner class that defines the table contents */
    public static class BarcodeTable implements BaseColumns {
        public static final String TABLE_NAME = "barcode_tb";
        public static final String TEST_TABLE_NAME = "test_tb";
        public static final String COLUMN_NAME_BNUMBER = "number";
        public static final String COLUMN_NAME_BNAME = "name";
        public static final String COLUMN_NAME_IMGID = "imageID";
        public static final String COLUMN_NAME_REGDATE = "regDate"; //현재로선 이걸 기본키처럼...
        public static final String COLUMN_NAME_DDAY = "dday";

        //쿼리
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TEST_TABLE_NAME + " (" +
                COLUMN_NAME_BNUMBER + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_BNAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_IMGID + INTEGER_TYPE + COMMA_SEP +
                COLUMN_NAME_REGDATE + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_DDAY + TEXT_TYPE + " )";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TEST_TABLE_NAME;
    }
}
