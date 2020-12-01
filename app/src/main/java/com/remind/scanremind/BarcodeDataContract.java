package com.remind.scanremind;

import android.provider.BaseColumns;

public final class BarcodeDataContract {
    //public static final String TEST_DATABASE_NAME = "Test.db";
    private static final String TEXT_TYPE          = " TEXT";
    private static final String INTEGER_TYPE          = " INTEGER";
    private static final String COMMA_SEP          = ",";
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private BarcodeDataContract() {}

    /* Inner class that defines the table contents */
    public static class BarcodeTable implements BaseColumns {
        public static final String TABLE_NAME = "barcode_tb";
        //public static final String TEST_TABLE_NAME = "test_tb";
        public static final String COLUMN_NAME_ITEMNUM = "itemNum";
        public static final String COLUMN_NAME_BNUMBER = "number";
        public static final String COLUMN_NAME_BNAME = "name";
        public static final String COLUMN_NAME_IMGSRC = "imageSrc";
        public static final String COLUMN_NAME_REGDATE = "regDate";
        public static final String COLUMN_NAME_DDAY = "dday";
    }
    //쿼리
    //itemNum은 long값인데 SQLite에서 모든 1,2,3,4,6,8바이트 정수형을 INTEGER로 관리함
    //하지만 DB로부터 값을 가져와 쓸 때는 cursor.getLong()으로 가져옴에 주의
    public static final String CREATE_TABLE =
            "CREATE TABLE " + BarcodeTable.TABLE_NAME + " (" +
                    BarcodeTable.COLUMN_NAME_ITEMNUM + " INTEGER PRIMARY KEY," +
                    BarcodeTable.COLUMN_NAME_BNUMBER + TEXT_TYPE + COMMA_SEP +
                    BarcodeTable.COLUMN_NAME_BNAME + TEXT_TYPE + COMMA_SEP +
                    BarcodeTable.COLUMN_NAME_IMGSRC + TEXT_TYPE + COMMA_SEP +
                    BarcodeTable.COLUMN_NAME_REGDATE + TEXT_TYPE + COMMA_SEP +
                    BarcodeTable.COLUMN_NAME_DDAY + TEXT_TYPE + " )";

    public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + BarcodeTable.TABLE_NAME;

}
