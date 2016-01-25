package com.example.romsm.lap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;

public class TreeInfoActivity extends AppCompatActivity {
    private TextView treeName, treeSciName, treeDesc;
    private ImageView treeImage;
    private UserAccount user;
    private TreeSpecies tree;
    private double l1;
    private double l2;
    private ProgressBar imageProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_info);

        Intent intent = getIntent();

        user = (UserAccount) intent.getSerializableExtra("userTokens");
        tree = (TreeSpecies) intent.getSerializableExtra("tree");
        l1 = intent.getDoubleExtra("lat", 0);
        l2 = intent.getDoubleExtra("long", 0);

        imageProgress = (ProgressBar) findViewById(R.id.tree_image_progress);

        treeName = (TextView)findViewById(R.id.nameText);
        treeSciName = (TextView)findViewById(R.id.sciNameText);
        treeDesc = (TextView) findViewById(R.id.descriptionText);

        treeName.setText(tree.getName());
        treeSciName.setText(tree.getScientificName());
        treeDesc.setText(tree.getDescription());

        treeImage = (ImageView) findViewById(R.id.treeImage);
        imageProgress.setVisibility(View.VISIBLE);
        treeImage.setVisibility(View.INVISIBLE);
        new DownloadImageTask(treeImage)
                .execute(tree.getImageURL());

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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.i(Constants.TAG, "image Exception");
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            imageProgress.setVisibility(View.INVISIBLE);
            treeImage.setVisibility(View.VISIBLE);
        }
    }
}
