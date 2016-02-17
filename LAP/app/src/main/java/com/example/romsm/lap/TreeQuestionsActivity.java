package com.example.romsm.lap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TreeQuestionsActivity extends AppCompatActivity {
    private UserAccount user;
    private TreeSpecies tree;
    private static final int ACTIVITY_START_CAMERA=1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    double l1, l2;
    boolean t,f;
    Button btnSubmit;
    Button btnPhoto;
    ProgressBar progress;
    ListView lvQuestions;
    ImageView imageView;
    String mCurrentPhotoPath;
    //questionsList will hold a list of all the questions that are retrieved from the server
    ArrayList<String> questionsList = new ArrayList<String>();
    //answers will hold the answers to the above questions (probably better to use a map<String, Boolean> instead of two lists)
    List<Boolean> answers = new ArrayList<>();
    //questions will hold an object of TreeQuestions which we'll use to retrieve the question's ID depending if it's true or false
    List<TreeQuestion> questions = new ArrayList<>();

    //id of tree that is about to be added (is used when submiting dailyUpdate)
    int treeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_questions_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        l1 = intent.getDoubleExtra("lat", 0);
        l2 = intent.getDoubleExtra("long", 0);
        t = intent.getBooleanExtra("booleant", false);
        f = intent.getBooleanExtra("booleanf", true);
        tree= (TreeSpecies) intent.getSerializableExtra("tree");
        Log.d(Constants.TAG, "lat: " + l1 + " Long: " + l2);
        Log.d(Constants.TAG, "t =" + t + " f= " + f);
        imageView= (ImageView)findViewById(R.id.imageView);
        btnSubmit = (Button) findViewById(R.id.enter_button);
        btnPhoto = (Button) findViewById(R.id.photo_button);
        lvQuestions = (ListView) findViewById(R.id.treeQuestionsList);
        progress = (ProgressBar)findViewById(R.id.question_progress);

        if(t && !user.getIsSuperUser()){ //if not admin don't show photo button
            btnPhoto.setVisibility(View.INVISIBLE);
        }

        //will retrieve questions from server and add them to our listView
        new GetQuestionsListTask().execute();
    }


    private ArrayList<String> jsonToArrayList(String jsonString) throws JSONException {
        ArrayList<String> questionsList = new ArrayList<String>();
        JSONArray data = new JSONArray(jsonString);

        for (int i = 0; i < data.length(); i++) {
            JSONObject json_data = data.getJSONObject(i);
            int id = json_data.getInt("id");
            String text = json_data.getString("text");

            JSONArray choices = json_data.getJSONArray("choices");
            int trueId;
            int falseId;
            if(choices.getJSONObject(0).getBoolean("value")){
                trueId = choices.getJSONObject(0).getInt("id");
                falseId = choices.getJSONObject(1).getInt("id");
            }else{
                trueId = choices.getJSONObject(1).getInt("id");
                falseId = choices.getJSONObject(0).getInt("id");
            }

            //Log.d(Constants.TAG,"TRUE ID = " + trueId + " FALSE ID = " + falseId + "TEXT = " + text);
            questions.add(new TreeQuestion(trueId, falseId, id, text));
            questionsList.add(text);
        }
        return questionsList;
    }

    private void setupListView() {
        setupButton();
        final ListView lv = (ListView) findViewById(R.id.treeQuestionsList);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, questionsList));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {
                //Object o = lv.getItemAtPosition(position);
                //Log.d(Constants.TAG, o.toString());

                //CheckedTextView textView = (CheckedTextView)v;
                //textView.setChecked(!textView.isChecked()

                SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
                Log.d(Constants.TAG, "Clicked Position := " + position + " Value: " + sparseBooleanArray.get(position));
            }
        });
    }

    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        lvQuestions.setVisibility(show ? View.GONE : View.VISIBLE);
        btnPhoto.setVisibility(show ? View.GONE : View.VISIBLE);
        btnSubmit.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    private void setupButton() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selection();
                if(mCurrentPhotoPath == null){
                    Toast.makeText(getApplicationContext(), "Please submit a picture of the tree before you move on",
                            Toast.LENGTH_LONG).show();
                }

                else if (f == false) {
                    showProgress(true);
                    new UploadTreeTask().execute(); //adds tree and then adds the dailyUpdate -> Goes to bird list activity
                    //new DbInsertTask().execute();
                }
                else {
                    showProgress(true);
                    treeID = tree.getId();
                    new UploadDailyTask().execute();
                }
            }
        });
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    //startActivityForResult(takePictureIntent, ACTIVITY_START_CAMERA);
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        // Error occurred while creating the File
                        Log.i(Constants.TAG, "IO Exception");
                        e.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TREE_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(Constants.TAG, mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_START_CAMERA:
                if (requestCode == ACTIVITY_START_CAMERA && resultCode == RESULT_OK & null != data) {

                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    //to generate random file name
                    String fileName = "tempimg.jpg";

                    try {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        //captured image set in imageview
                        imageView.setImageBitmap(photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public class GetQuestionsListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return getQuestionsList();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setupListView();
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

        private Boolean getQuestionsList() {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://isitso.pythonanywhere.com/questions/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("client_id", Constants.CLIENT_ID);
                conn.setRequestProperty("client_secret", Constants.CLIENT_SECRET);
                conn.setRequestProperty("Authorization", "Bearer " + user.getAccessToken());
                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "species status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String speciesListData = sb.toString();
                    questionsList = jsonToArrayList(speciesListData);
                    Log.d(Constants.TAG, "speciesJSON: " + speciesListData);
                    return true;
                }
            } catch (MalformedURLException e) {
                Log.i(Constants.TAG, "Malformed Url");
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                Log.i(Constants.TAG, "JSON Exception");
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }
    }

    public void selection (){
        final ListView lv = (ListView)findViewById(R.id.treeQuestionsList);
        int size = questionsList.size();

        //checks if position is true/false and adds to answers list
        for (int i = 0; i < size; i++) {
            answers.add(lv.getCheckedItemPositions().get(i));

            Log.i(Constants.TAG, "answers: " + answers.get(i));
        }

    }

    public class UploadTreeTask extends AsyncTask<Void, Void, Boolean> {

        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(Constants.POST_TREE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("long", String.valueOf(l2))
                        .appendQueryParameter("lat", String.valueOf(l1))
                        .appendQueryParameter("landmark", null)
                        .appendQueryParameter("species", String.valueOf(tree.getId()))
                        .appendQueryParameter("changed_by", String.valueOf(1));
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "add tree status" + status);

                if (status == 201) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();
                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String response= sb.toString();
                    Log.d(Constants.TAG, "add tree response: " + response);

                    //get newly added tree's id from response json (to be used when sending daily updates)
                    JSONObject responseJSON = new JSONObject(response);
                    treeID = responseJSON.getInt("id");

                    return true;
                }
            } catch (MalformedURLException e) {
                Log.i(Constants.TAG, "Malformed Url");
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                Log.i(Constants.TAG, "JSON Exception");
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                new UploadDailyTask().execute();
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }


    public class UploadDailyTask extends AsyncTask<Void, Void, Boolean> {

        int[] answerID = new int[questionsList.size()];
        String charset = "UTF-8";
        File treeFile;

        private void reduceImageSize(){
            Bitmap bitmapImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(mCurrentPhotoPath);
                scaled.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            //checks each question's answer and retrieves it's appropriate ID then adds it to answerID array
            for(int i = 0 ; i < questionsList.size() ; i++ ){
                if(answers.get(i)){
                    answerID[i] = questions.get(i).getTrueID();
                }
                else{
                    answerID[i] = questions.get(i).getFalseID();
                }
            }

            try {
                MultipartUtility multipart = new MultipartUtility(Constants.POST_DAILY_UPDATE_URL, charset);

                reduceImageSize();
                treeFile = new File(mCurrentPhotoPath);
                multipart.addFilePart("image", treeFile);

                multipart.addFormField("tree", String.valueOf(treeID));
                multipart.addFormField("changed_by", "1");
                for(int id : answerID){
                    multipart.addFormField("choices", String.valueOf(id));
                }
                List<String> response = multipart.finish();

                Log.d(Constants.TAG, "add daily updates response: ");
                for (String line : response) {
                    Log.d(Constants.TAG, line);
                }

                if(!response.isEmpty()){
                    return true;
                }
            } catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
         protected void onPostExecute(final Boolean success) {
            showProgress(false);
            if (success) {
                Intent birdIntent = new Intent(TreeQuestionsActivity.this, BirdListActivity.class);
                birdIntent.putExtra("userTokens", user);
                startActivity(birdIntent);
                finish();
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }
}
