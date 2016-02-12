package com.example.romsm.lap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class UpdateActivity extends AppCompatActivity {
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
                Intent questionsIntent = new Intent(UpdateActivity.this, TreeQuestionsActivity.class);
                questionsIntent.putExtra("tree", tree);
                questionsIntent.putExtra("booleant", true);
                startActivity(questionsIntent);
            }
        });
    }

}