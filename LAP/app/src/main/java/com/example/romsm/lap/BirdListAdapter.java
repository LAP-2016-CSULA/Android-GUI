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

public class BirdListAdapter extends ArrayAdapter<BirdSpecies> {
    public BirdListAdapter(Context context, ArrayList<BirdSpecies> bird) {
        super(context, 0, bird);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BirdSpecies bird = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bird_species, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.speciesName);
        ImageView treeImg = (ImageView) convertView.findViewById(R.id.speciesImg);
        // Populate the data into the template view using the data object
        name.setText(bird.getName());

        Picasso img = Picasso.with(getContext());
        //img.setIndicatorsEnabled(true);
        img.load(bird.getImageURL()).resize(180,150).centerCrop().into(treeImg);

        // Return the completed view to render on screen
        return convertView;
    }
}
