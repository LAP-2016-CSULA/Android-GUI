package com.example.romsm.lap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

public class TreeInfoActivity extends AppCompatActivity {
    private TextView treeName, treeSciName, treeDesc;
    private ImageView treeImage;
    private UserAccount user;
    private TreeSpecies tree;
    private double l1;
    private double l2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_info);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();

        user = (UserAccount) intent.getSerializableExtra("userTokens");
        tree = (TreeSpecies) intent.getSerializableExtra("tree");
        l1 = intent.getDoubleExtra("lat", 0);
        l2 = intent.getDoubleExtra("long", 0);

        treeName = (TextView)findViewById(R.id.nameText);
        treeSciName = (TextView)findViewById(R.id.sciNameText);
        treeDesc = (TextView) findViewById(R.id.descriptionText);

        treeName.setText(tree.getName());
        treeSciName.setText(tree.getScientificName());
        treeDesc.setText(tree.getDescription());

        treeImage = (ImageView) findViewById(R.id.treeImage);
        Picasso img = Picasso.with(this);
        img.setIndicatorsEnabled(true);
        img.load(tree.getImageURL()).into(treeImage);

        Button continueButton = (Button) findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionsIntent = new Intent(TreeInfoActivity.this, TreeQuestionsActivity.class);
                questionsIntent.putExtra("userTokens", user);
                questionsIntent.putExtra("tree", tree);
                questionsIntent.putExtra("lat",l1);
                questionsIntent.putExtra("long", l2);
                startActivity(questionsIntent);
            }
        });
    }

}
