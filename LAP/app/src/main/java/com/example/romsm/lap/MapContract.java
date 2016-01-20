package com.example.romsm.lap;


import android.provider.BaseColumns;

public class MapContract {
    public MapContract() {}

    /* Inner class that defines the table contents */
    public static abstract class MapEntry implements BaseColumns {
        public static final String TABLE_NAME = "map_entry";
        public static final String TYPE = "type";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }
}
