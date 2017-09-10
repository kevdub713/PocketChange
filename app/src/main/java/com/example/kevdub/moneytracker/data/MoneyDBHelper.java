package com.example.kevdub.moneytracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.kevdub.moneytracker.data.MoneyContract.MoneyEntry;

/**
 * Created by kevinwang on 7/31/17.
 */

public class MoneyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneytracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";

    private static final String CREATE_TABLE = "CREATE TABLE " + MoneyEntry.TABLE_NAME + " (" +
            MoneyEntry._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            MoneyEntry.COLUMN_DATE + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
            MoneyEntry.COLUMN_FLOW + INTEGER_TYPE + " NOT NULL" + COMMA_SEP +
            MoneyEntry.COLUMN_NAME + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
            MoneyEntry.COLUMN_AMOUNT + INTEGER_TYPE + " NOT NULL DEFAULT 0" + COMMA_SEP +
            MoneyEntry.COLUMN_TAG + TEXT_TYPE +
            ");";

    private static final String DELETE_ENTRIES = "DROP IF TABLE EXISTS " + MoneyEntry.TABLE_NAME;

    /** Constructor */
    public MoneyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
    }
}
