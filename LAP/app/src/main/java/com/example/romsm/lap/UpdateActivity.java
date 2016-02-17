package com.example.romsm.lap;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class UpdateActivity extends AppCompatActivity {
    private TextView treeName, treeSciName, treeDesc;
    private ImageView treeImage;
    private ProgressBar progress;
    private UserAccount user;
    private int treeID;
    private TreeSpecies tree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_info);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        treeName = (TextView)findViewById(R.id.nameText);
        treeSciName = (TextView)findViewById(R.id.sciNameText);
        treeDesc = (TextView) findViewById(R.id.descriptionText);
        progress = (ProgressBar) findViewById(R.id.treeInfoProgress);
        treeImage = (ImageView) findViewById(R.id.treeImage);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        treeID = intent.getIntExtra("treeID", 0);

        toggleProgress(true);
        new GetTreeInfoTask().execute();
    }

    private void toggleProgress(Boolean toggle){
        if (toggle){
            progress.setVisibility(View.VISIBLE);
            treeImage.setVisibility(View.GONE);
        }
        else{
            progress.setVisibility(View.GONE);
            treeImage.setVisibility(View.VISIBLE);
        }
    }

    private void setUpInfo(){
        treeName.setText(tree.getName());
        treeSciName.setText(tree.getScientificName());
        treeDesc.setText(tree.getDescription());

        Picasso img = Picasso.with(this);
        img.setIndicatorsEnabled(true);
        img.load(tree.getImageURL()).resize(500, 500).centerInside().rotate(90f).into(treeImage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                toggleProgress(false);
            }

            @Override
            public void onError() {
                toggleProgress(false);
            }
        });

        Button continueButton = (Button) findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionsIntent = new Intent(UpdateActivity.this, TreeQuestionsActivity.class);
                questionsIntent.putExtra("userTokens", user);
                questionsIntent.putExtra("tree", tree);
                questionsIntent.putExtra("booleant", true);
                startActivity(questionsIntent);
            }
        });
    }

    public class GetTreeInfoTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            MapDbHelper db = new MapDbHelper(getApplicationContext());
            tree = db.getTreeInfo(treeID);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setUpInfo();
            } else {
                Toast.makeText(UpdateActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

    }

}