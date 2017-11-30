package com.valpiok.NaviPark.payment.persistence;

/**
 * Created by SERHIO on 14.09.2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TransactionsSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MERCHANT_ID = "merchant_id";
    public static final String COLUMN_REFERENCE_NUMBER = "reference_number";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String DATABASE_NAME = "transactions_history.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table " + TABLE_TRANSACTIONS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_MERCHANT_ID + " integer not null, " +
            COLUMN_REFERENCE_NUMBER + " text not null, " +
            COLUMN_CURRENCY + " text not null, " +
            COLUMN_STATUS + " text not null, " +
            COLUMN_AMOUNT + " integer not null, " +
            COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
            ");";

    public TransactionsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TransactionsSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }
}