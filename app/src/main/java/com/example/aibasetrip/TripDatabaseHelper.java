package com.example.aibasetrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TripDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "tripPlanner.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "trips";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_JSON = "trip_json";

    public TripDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_JSON + " TEXT)";
        db.execSQL(createTable);
        db.execSQL("CREATE TABLE Trip (id INTEGER PRIMARY KEY AUTOINCREMENT, total_cost TEXT)");
        db.execSQL("CREATE TABLE Itinerary (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, day TEXT, date TEXT, title TEXT, description TEXT, location TEXT, cost TEXT, image_url TEXT)");
        db.execSQL("CREATE TABLE Hotel (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, name TEXT, cost TEXT, image_url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS Trip");
        db.execSQL("DROP TABLE IF EXISTS Itinerary");
        db.execSQL("DROP TABLE IF EXISTS Hotel");
        onCreate(db);
    }

    // Insert trip
    public boolean insertTrip(String name, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_JSON, json);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    // Get all trips
    public Cursor getAllTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);
    }
}
