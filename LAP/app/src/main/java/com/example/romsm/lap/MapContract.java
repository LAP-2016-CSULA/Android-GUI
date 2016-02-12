package com.example.romsm.lap;


import android.provider.BaseColumns;

public class MapContract {
    public MapContract() {}

    /* Inner class that defines the table contents */
    public static abstract class MapEntry implements BaseColumns {
        public static final String TABLE_NAME = "map_entry";
        public static final String TREE_ID = "tree_id";
        public static final String TREE_NAME = "tree_name";
        public static final String TREE_SCI_NAME= "tree_sci_name";
        public static final String TREE_DESC = "tree_desc";
        public static final String TREE_IMAGE_URL = "tree_image_url";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }
}
