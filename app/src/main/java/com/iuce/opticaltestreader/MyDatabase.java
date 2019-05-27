package com.iuce.opticaltestreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "answer_database";
    private static final String TABLE_NAME = "answer_table";
    private static final int DATABASE_VERSION = 1;

    private static final String ID = "_id";
    private static final String QUESTION_NUMBER = "_questionNumber";
    private static final String QUESTION_INDEX = "_questionIndex";

    public MyDatabase(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QUESTION_NUMBER + " TEXT NOT NULL, "
                + QUESTION_INDEX + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void QuestionAdd(String number, String index){
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(QUESTION_NUMBER,number);
            cv.put(QUESTION_INDEX,index);

            long id = db.insert(TABLE_NAME,null,cv);
            if (id > 0){
                Log.i("tag","Islem Basarili");
            }
            else
                Log.e("tag","Islem Basarisiz");
        }catch (Exception e){

        }
        db.close();
    }

    public List<String> showQuestion(){
        List<String> question = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        try {
            String[] coulumns = {ID,QUESTION_NUMBER,QUESTION_INDEX};
            Cursor cursor = database.query(TABLE_NAME,coulumns,null,null,null,null,null);
            while (cursor.moveToNext()){
                question.add(cursor.getInt(0)
                        + " - "
                        + cursor.getString(1)
                        + " - "
                        + cursor.getString(2));
            }
        }catch (Exception e){

        }
        database.close();
        return question;
    }
}
