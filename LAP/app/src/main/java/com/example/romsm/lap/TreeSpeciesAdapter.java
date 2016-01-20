package com.example.romsm.lap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TreeSpeciesAdapter extends ArrayAdapter<TreeSpecies> {
    public TreeSpeciesAdapter(Context context, ArrayList<TreeSpecies> species) {
        super(context, 0, species);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TreeSpecies species = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tree_species, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.speciesName);
        TextView sciName = (TextView) convertView.findViewById(R.id.speciesSciName);
        // Populate the data into the template view using the data object
        name.setText(species.getName());
        sciName.setText(species.getScientificName());
        // Return the completed view to render on screen
        return convertView;
    }
}
