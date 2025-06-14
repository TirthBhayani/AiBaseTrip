package com.example.aibasetrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "trip_db";
    public static final String TABLE_NAME = "trips";

    public HistoryDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, trip_json TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
    public boolean insertTrip(String name, String tripJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("trip_json", tripJson);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // returns true if insert is successful
    }

}
