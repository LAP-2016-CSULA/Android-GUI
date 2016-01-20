package com.example.romsm.lap;

public class TreeSpecies {
    private String name, scientificName, descrition;
    private int id;

    public TreeSpecies(String name , String scientificName, String descrition, int id){
        this.name = name;
        this.scientificName = scientificName;
        this.descrition = descrition;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescrition() {
        return descrition;
    }

    public int getId() {
        return id;
    }
}
