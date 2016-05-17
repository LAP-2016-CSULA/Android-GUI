package com.example.romsm.lap;

import java.io.Serializable;

public class BirdSpecies {
    private String name, scientificName, description, imageURL;
    private int id;
    private boolean isSelected;
    public BirdSpecies(String name , String scientificName, String descrition, int id, String imageURL){
        this.name = name;
        this.scientificName = scientificName;
        this.description = descrition;
        this.id = id;
        this.imageURL = imageURL;
        this.isSelected = false;
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

    public boolean getIsSelected(){ return isSelected; }

    public void setIsSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

    public void toggleSelected(){
        isSelected = !isSelected;
    }
}
