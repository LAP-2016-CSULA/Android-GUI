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
            MapContract.MapEntry.TREE_ID,
            MapContract.MapEntry.TREE_NAME,
            MapContract.MapEntry.TREE_SCI_NAME,
            MapContract.MapEntry.TREE_DESC,
            MapContract.MapEntry.TREE_IMAGE_URL,
            MapContract.MapEntry.LATITUDE,
            MapContract.MapEntry.LONGITUDE,};

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    MapContract.MapEntry.TABLE_NAME + " (" +
                    MapContract.MapEntry._ID + " INTEGER PRIMARY KEY, " +
                    MapContract.MapEntry.TREE_ID + " INTEGER NOT NULL, " +
                    MapContract.MapEntry.TREE_NAME + " TEXT NOT NULL, " +
                    MapContract.MapEntry.TREE_SCI_NAME + " TEXT NOT NULL, " +
                    MapContract.MapEntry.TREE_DESC + " TEXT NOT NULL, " +
                    MapContract.MapEntry.TREE_IMAGE_URL + " TEXT NOT NULL, " +
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

    public void insertMapEntry(int treeID, String treeName, String treeSciName, String treeDesc, String treeURL, String l1, String l2  ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MapContract.MapEntry.TREE_ID, treeID);
        cv.put(MapContract.MapEntry.TREE_NAME, treeName);
        cv.put(MapContract.MapEntry.TREE_SCI_NAME, treeSciName);
        cv.put(MapContract.MapEntry.TREE_DESC, treeDesc);
        cv.put(MapContract.MapEntry.TREE_IMAGE_URL, treeURL);
        cv.put(MapContract.MapEntry.LATITUDE, l1);
        cv.put(MapContract.MapEntry.LONGITUDE, l2);

        db.insert(MapContract.MapEntry.TABLE_NAME, null, cv);
    }

    public Cursor getAllRows() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(MapContract.MapEntry.TABLE_NAME, projection, null, null, null, null, null);
    }

    public int getTreeID(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(MapContract.MapEntry.TABLE_NAME, new String[]{MapContract.MapEntry.TREE_ID}, MapContract.MapEntry._ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }

        return cursor.getInt(0);
    }

    public TreeSpecies getTreeInfo(int treeID){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(MapContract.MapEntry.TABLE_NAME, new String[]{ MapContract.MapEntry.TREE_NAME, MapContract.MapEntry.TREE_SCI_NAME, MapContract.MapEntry.TREE_DESC, MapContract.MapEntry.TREE_IMAGE_URL }, MapContract.MapEntry.TREE_ID + "=?", new String[]{String.valueOf(treeID)}, null, null, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                return new TreeSpecies(cursor.getString(0),cursor.getString(1),cursor.getString(2),treeID,cursor.getString(3));
            }
        }
        return null;
    }

    public TreeSpecies getTreeInfoFromID(int _id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(MapContract.MapEntry.TABLE_NAME, new String[]{ MapContract.MapEntry.TREE_NAME, MapContract.MapEntry.TREE_SCI_NAME, MapContract.MapEntry.TREE_DESC, MapContract.MapEntry.TREE_ID, MapContract.MapEntry.TREE_IMAGE_URL }, MapContract.MapEntry._ID + "=?", new String[]{String.valueOf(_id)}, null, null, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                return new TreeSpecies(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getInt(3),cursor.getString(4));
            }
        }
        return null;
    }

    public void deleteFromTreeID(int treeID){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + MapContract.MapEntry.TABLE_NAME + "where "+ MapContract.MapEntry.TREE_ID + " = " + treeID);
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + MapContract.MapEntry.TABLE_NAME);
    }
}
