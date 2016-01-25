package com.example.romsm.lap;

/**
 * Created by mike on 1/19/16.
 */
public class Observation {

    int id;
    // double latitude;
    // double longitude;
    boolean answers;


    public Observation(int id, Boolean answers) {
        this.id = id;
        //  this.latitude=latitude;
        //  this.longitude=longitude;
        this.answers = answers;

    }

    public boolean isAnswers() {
        return answers;
    }

    public void setAnswers(boolean answers) {
        this.answers = answers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    // public double getLongitude() {
    //      return longitude;
    //  }

    // public void setLongitude(double longitude) {
    //      this.longitude = longitude;
    //  }

    // public double getLatitude() {
    //      return latitude;
    //  }

    //  public void setLatitude(double latitude) {
    //     this.latitude = latitude;
    //  }

}
