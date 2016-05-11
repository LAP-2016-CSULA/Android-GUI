package com.example.romsm.lap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
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
        ImageView treeImg = (ImageView) convertView.findViewById(R.id.speciesImg);
        // Populate the data into the template view using the data object
        name.setText(species.getName());

        Picasso img = Picasso.with(getContext());
        //img.setIndicatorsEnabled(true);
        img.load(species.getImageURL()).resize(150,180).centerCrop().into(treeImg);

        // Return the completed view to render on screen
        return convertView;
    }

}
