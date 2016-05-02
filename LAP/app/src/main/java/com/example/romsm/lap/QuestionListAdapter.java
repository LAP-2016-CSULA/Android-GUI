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

public class QuestionListAdapter extends ArrayAdapter<String> {
    public QuestionListAdapter(Context context, ArrayList<String> questions) {
        super(context, 0, questions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String question = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tree_questions, parent, false);
        }
        // Lookup view for data population
        TextView questionView = (TextView) convertView.findViewById(R.id.questionView);
        questionView.setText(question);
        // Return the completed view to render on screen
        return convertView;
    }
}
