package com.example.romsm.lap;

import java.io.Serializable;

public class TreeSpecies implements Serializable {
    private String name, scientificName, description, imageURL;
    private int id;

    public TreeSpecies(String name , String scientificName, String descrition, int id, String imageURL){
        this.name = name;
        this.scientificName = scientificName;
        this.description = descrition;
        this.id = id;
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getId() {
        return id;
    }
}
