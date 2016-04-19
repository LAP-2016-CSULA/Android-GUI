package com.example.romsm.lap.model;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MarkerGroup {
    private HashMap<Marker, Integer> markerMap;
    private int groupSize;
    private double latitude;
    private double longitude;

    public MarkerGroup(Marker m, int i){
        markerMap = new HashMap<>();
        groupSize = 0;
        latitude = m.getPosition().latitude;
        longitude = m.getPosition().longitude;
        addMaker(m, i);
    }

    public void addMaker(Marker m, int i){
        markerMap.put(m, i);
        groupSize++;
    }

    public HashMap<Marker, Integer> getMarkerMap(){
        return markerMap;
    }

    public int getGroupSize(){
        return groupSize;
    }

    public boolean contains(Marker m){
        Iterator it = markerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(m == (Marker)pair.getKey()){
                return true;
            }
        }
        return false;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
