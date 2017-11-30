package com.valpiok.NaviPark.payment.persistence;

/**
 * Created by SERHIO on 14.09.2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.valpiok.NaviPark.payment.model.Transaction;
import com.valpiok.NaviPark.payment.model.TransactionDetails;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class TransactionsDataSource {

    private SQLiteDatabase database;
    private TransactionsSQLiteHelper dbHelper;
    private String[] allColumns = { TransactionsSQLiteHelper.COLUMN_ID,
            TransactionsSQLiteHelper.COLUMN_MERCHANT_ID,
            TransactionsSQLiteHelper.COLUMN_REFERENCE_NUMBER,
            TransactionsSQLiteHelper.COLUMN_CURRENCY,
            TransactionsSQLiteHelper.COLUMN_AMOUNT,
            TransactionsSQLiteHelper.COLUMN_STATUS,
            TransactionsSQLiteHelper.COLUMN_TIMESTAMP};

    public TransactionsDataSource(Context context) {
        dbHelper = new TransactionsSQLiteHelper(context);
    }

    public void open() throws SQLException {
        if(database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        dbHelper.close();
    }

    public void saveTransaction(TransactionDetails transactionDetails) {
        ContentValues values = new ContentValues();
        values.put(TransactionsSQLiteHelper.COLUMN_MERCHANT_ID , transactionDetails.getMerchantId());
        values.put(TransactionsSQLiteHelper.COLUMN_REFERENCE_NUMBER , transactionDetails.getRefrenceNumber());
        values.put(TransactionsSQLiteHelper.COLUMN_CURRENCY , transactionDetails.getCurrency());
        values.put(TransactionsSQLiteHelper.COLUMN_AMOUNT , transactionDetails.getAmount());
        values.put(TransactionsSQLiteHelper.COLUMN_STATUS , transactionDetails.getStatus());


        database.insert(TransactionsSQLiteHelper.TABLE_TRANSACTIONS, null, values);
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        Cursor cursor = database.query(TransactionsSQLiteHelper.TABLE_TRANSACTIONS,
                allColumns, null, null, null, null, TransactionsSQLiteHelper.COLUMN_TIMESTAMP + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Transaction transaction = cursorToTransaction(cursor);
            transactions.add(transaction);
            cursor.moveToNext();
        }
        cursor.close();
        return transactions;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setMerchantId(cursor.getLong(1));
        transaction.setReference_number(cursor.getString(2));
        transaction.setCurrency(cursor.getString(3));
        transaction.setAmount(cursor.getLong(4));
        transaction.setStatus(cursor.getString(5));
        transaction.setTimestamp(Timestamp.valueOf(cursor.getString(6)).getTime());
        return transaction;
    }

}