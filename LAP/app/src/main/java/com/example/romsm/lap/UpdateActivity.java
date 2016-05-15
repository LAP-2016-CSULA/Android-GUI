package com.example.romsm.lap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateActivity extends AppCompatActivity {
    private TextView treeName, treeSciName, treeDesc;
    private ImageView treeImage;
    private ProgressBar progress;
    private UserAccount user;
    private int treeID;
    private TreeSpecies tree;
    float scale=1f;
    ScaleGestureDetector scaleGDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        //Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);

        treeName = (TextView)findViewById(R.id.nameText);
        treeSciName = (TextView)findViewById(R.id.sciNameText);
        treeDesc = (TextView) findViewById(R.id.descriptionText);
        progress = (ProgressBar) findViewById(R.id.treeInfoProgress);
        treeImage = (ImageView) findViewById(R.id.treeImage);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        treeID = intent.getIntExtra("treeID", 0);
        scaleGDetector=new ScaleGestureDetector(this, new ScaleListener());
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

    @Override

    public boolean onTouchEvent(MotionEvent ev) {

        scaleGDetector.onTouchEvent(ev);

        return true;

    }
    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener{

        public boolean onScaleBegin(ScaleGestureDetector sgd){



            return true;



        }

        public void onScaleEnd(ScaleGestureDetector sgd){



        }

        public boolean onScale(ScaleGestureDetector sgd){

            // Multiply scale factor

            scale*= sgd.getScaleFactor();

            // Scale or zoom the imageview

            treeImage.setScaleX(scale);

            treeImage.setScaleY(scale);

            Log.i("Main",String.valueOf(scale));

            return true;

        }

    }

    private void setUpInfo(){
        treeName.setText(tree.getName());
        treeSciName.setText(tree.getScientificName());
        treeDesc.setText(tree.getDescription());

        Picasso.with(getApplicationContext())
                .load(tree.getImageURL())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(treeImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        toggleProgress(false);
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getApplicationContext())
                                .load(tree.getImageURL())
                                .into(treeImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        toggleProgress(false);
                                    }

                                    @Override
                                    public void onError() {
                                        toggleProgress(false);
                                        Log.d(Constants.TAG, "Could not fetch image");
                                    }
                                });
                    }
                });

        //Picasso img = Picasso.with(this);
        //img.setIndicatorsEnabled(true);
        /*img.load(tree.getImageURL()).resize(500, 500).centerInside().into(treeImage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                toggleProgress(false);
            }

            @Override
            public void onError() {
                toggleProgress(false);
            }
        });*/

        Button continueButton = (Button) findViewById(R.id.btnContinue);
        if (user.getIsGuest()){
            continueButton.setEnabled(false);
            continueButton.setText("Sign-in to update");
        }
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

        ImageButton deleteButton = (ImageButton) findViewById(R.id.btDelete);
        if (user.getIsGuest()){
            continueButton.setEnabled(false);
            continueButton.setText("Sign-in to update");
            deleteButton.setEnabled(false);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        if (user.getIsSuperUser()){
                            new DeleteTreeTask().execute();
                        }
                        else if (!user.getIsGuest()) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("message/rfc822");
                            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"laphenology@gmail.com"});
                            i.putExtra(Intent.EXTRA_SUBJECT, "LAP - Delete Tree Request");
                            i.putExtra(Intent.EXTRA_TEXT, "Request to Delete tree ID: " + treeID);
                            try {
                                startActivity(Intent.createChooser(i, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(UpdateActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                if (user.getIsSuperUser()){
                    builder.setMessage(R.string.delete_tree)
                            .setTitle(R.string.app_name);
                }
                else{
                    builder.setMessage(R.string.request_delete_tree)
                            .setTitle(R.string.app_name);
                }

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public class GetTreeInfoTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            MapDbHelper db = new MapDbHelper(getApplicationContext());
            tree = db.getTreeInfo(treeID);
            db.close();
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

    public class DeleteTreeTask extends AsyncTask<Void, Void, Boolean> {

        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(Constants.POST_TREE_URL + treeID + "/?access_token="+ user.getAccessToken());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setDoOutput(false);
                conn.setDoInput(true);

                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "delete tree status " +" : " + status);

                if (status == 204) {
                    return true;
                }
            }catch (MalformedURLException e){
                Log.i(Constants.TAG, "Malformed Url");
                e.printStackTrace();
                return false;
            }catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                e.printStackTrace();
                return false;
            }finally {
                if (conn != null)
                    conn.disconnect();
            }

            return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(UpdateActivity.this, "Tree Deleted", Toast.LENGTH_LONG).show();
                Intent mapIntent = new Intent(UpdateActivity.this, MapsActivity.class);
                mapIntent.putExtra("userTokens", user);
                startActivity(mapIntent);
                finish();
            } else {
                Toast.makeText(UpdateActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

    }

}