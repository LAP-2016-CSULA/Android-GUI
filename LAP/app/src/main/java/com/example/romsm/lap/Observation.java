package com.example.romsm.lap;

/**
 * Created by mike on 1/19/16.
 */
public class Observation {

    int species;
    double latitude;
    double longitude;
    public Observation(int species,double latitude,double longitude){
        this.species=species;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getSpecies() {
        return species;
    }

    public void setSpecies(int species) {
        this.species = species;
    }
}
