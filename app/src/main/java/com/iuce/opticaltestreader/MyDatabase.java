package com.iuce.opticaltestreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "answer_database";
    private static final String TABLE_NAME = "answer_table";
    private static final int DATABASE_VERSION = 1;

    private static final String ID = "_id";
    private static final String ANSWER_COUNT = "_count";
    private static final String ANSWER_ = "";

    public MyDatabase(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
