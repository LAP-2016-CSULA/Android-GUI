package com.example.romsm.lap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        tree = (TreeSpecies) intent.getSerializableExtra("tree");
        l1 = intent.getDoubleExtra("lat", 0);
        l2 = intent.getDoubleExtra("long", 0);

        treeName = (TextView)findViewById(R.id.nameText);
        treeSciName = (TextView)findViewById(R.id.sciNameText);
        treeImage = (ImageView) findViewById(R.id.treeImage);
        treeDesc = (TextView) findViewById(R.id.descriptionText);

        treeName.setText(tree.getName());
        treeSciName.setText(tree.getScientificName());
        treeDesc.setText(tree.getDescrition());

        Button continueButton = (Button) findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionsIntent = new Intent(TreeInfoActivity.this, TreeQuestionsActivity.class);
                questionsIntent.putExtra("userTokens", user);
                questionsIntent.putExtra("lat",l1);
                questionsIntent.putExtra("long", l2);
                startActivity(questionsIntent);
            }
        });
    }
}
