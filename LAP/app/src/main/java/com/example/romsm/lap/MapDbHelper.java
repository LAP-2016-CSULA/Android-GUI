package com.example.romsm.lap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MapDbHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MapCoord.db";

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    String[] projection = {
            MapContract.MapEntry._ID,
            MapContract.MapEntry.TYPE,
            MapContract.MapEntry.LATITUDE,
            MapContract.MapEntry.LONGITUDE,};

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    MapContract.MapEntry.TABLE_NAME + " (" +
                    MapContract.MapEntry._ID + " INTEGER PRIMARY KEY, " +
                    MapContract.MapEntry.TYPE + " TEXT NOT NULL, " +
                    MapContract.MapEntry.LATITUDE + " TEXT NOT NULL, " +
                    MapContract.MapEntry.LONGITUDE + " TEXT NOT NULL " + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MapContract.MapEntry.TABLE_NAME;

    public MapDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertMapEntry(String type, String l1, String l2  ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MapContract.MapEntry.TYPE, type);
        cv.put(MapContract.MapEntry.LATITUDE, l1);
        cv.put(MapContract.MapEntry.LONGITUDE, l2);

        db.insert(MapContract.MapEntry.TABLE_NAME, null, cv);
    }

    public Cursor getAllRows() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(MapContract.MapEntry.TABLE_NAME, projection, null, null, null, null, null);
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + MapContract.MapEntry.TABLE_NAME);
    }
}
