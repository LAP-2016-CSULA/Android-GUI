package com.example.romsm.lap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TreeQuestionsActivity extends AppCompatActivity {
    private UserAccount user;
    private TreeSpecies tree;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private boolean isBirdList = false;
    double l1, l2;
    boolean t,f;
    Button btnSubmit;
    ImageButton btnPhoto;
    ImageButton btnBird;
    ProgressBar progress;
    ListView lvQuestions;
    ListView lvBirds;
    ImageView imageView;
    TextView titleQuestion;
    String mCurrentPhotoPath;
    //questionsList will hold a list of all the questions that are retrieved from the server
    //ArrayList<String> questionsList = new ArrayList<String>();
    //answers will hold the answers to the above questions (probably better to use a map<String, Boolean> instead of two lists)
    //List<Boolean> answers = new ArrayList<>();
    HashMap<TreeQuestion, Boolean> questionMap = new HashMap<>();
    //questions will hold an object of TreeQuestions which we'll use to retrieve the question's ID depending if it's true or false
    //List<TreeQuestion> questions = new ArrayList<>();
    //birdMap has all species of birds as key and true/false as value
    HashMap<BirdSpecies, Boolean> birdMap = new HashMap<>();

    ResponseReceiver receiver;

    //id of tree that is about to be added (is used when submiting dailyUpdate)
    int treeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_questions_list);

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);
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
        btnPhoto = (ImageButton) findViewById(R.id.photo_button);
        btnBird = (ImageButton) findViewById(R.id.bird_button);
        lvQuestions = (ListView) findViewById(R.id.treeQuestionsList);
        lvBirds = (ListView) findViewById(R.id.birdList);
        progress = (ProgressBar)findViewById(R.id.question_progress);
        titleQuestion = (TextView) findViewById(R.id.titleTextView);
        if (!user.getIsGuest() && !user.getIsSuperUser() && !user.getIsStaff()){
            btnSubmit.setEnabled(true);
        }
        else if (t && user.getIsSuperUser()){
            btnSubmit.setEnabled(true);
        }
        else{
            btnSubmit.setEnabled(false);
        }

        if(t && !user.getIsSuperUser()){ //if not admin don't show photo button
            btnPhoto.setVisibility(View.INVISIBLE);
        }

        //will retrieve questions from server and add them to our listView
        new GetBirdListTask().execute();
        new GetQuestionsListTask().execute();
    }


    private void jsonToArrayList(String jsonString) throws JSONException {
        //ArrayList<String> questionsList = new ArrayList<String>();
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
            }
            else{
                trueId = choices.getJSONObject(1).getInt("id");
                falseId = choices.getJSONObject(0).getInt("id");
            }

            //Log.d(Constants.TAG, "TRUE ID = " + trueId + " FALSE ID = " + falseId + "TEXT = " + text);
            //questions.add(new TreeQuestion(trueId, falseId, id, text));
            //questionsList.add(text);
            questionMap.put(new TreeQuestion(trueId, falseId, id, text), false);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){

        }

    }
    @Override
    protected void onPause(){
        super.onPause();
        try{
            unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){

        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }
    private void setupListView() {
        setupButton();
        final ListView lv = (ListView) findViewById(R.id.treeQuestionsList);
        final ArrayList<TreeQuestion> questionsList = new ArrayList<>();
        for(TreeQuestion qKey : questionMap.keySet()){
            qKey.setIsSelected(false);
            questionsList.add(qKey);
        }
        QuestionListAdapter questionAdapter = new QuestionListAdapter(this, questionsList);
        lv.setAdapter(questionAdapter);
        //lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, questionsList));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {
                SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
                //CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.questionView);
                //ctv.toggle();
                questionsList.get(position).toggleSelected();
                lv.invalidateViews();
                questionMap.put(questionsList.get(position), sparseBooleanArray.get(position));
                Log.d(Constants.TAG, "Clicked Position := " + position + " Value: " + sparseBooleanArray.get(position));
            }
        });
    }

    private void setupBirdListView(){
        final ListView blv = (ListView) findViewById(R.id.birdList);
        final ArrayList<BirdSpecies> birdList = new ArrayList<>();
        for(BirdSpecies birdKey : birdMap.keySet()){
            birdKey.setIsSelected(false);
            birdList.add(birdKey);
        }

        BirdListAdapter birdAdapter = new BirdListAdapter(this, birdList);
        blv.setAdapter(birdAdapter);
        blv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        blv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {
                SparseBooleanArray sparseBooleanArray = blv.getCheckedItemPositions();
                birdList.get(position).toggleSelected();
                blv.invalidateViews();
                //CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.speciesName);
                //ctv.toggle();
                //Log.d(Constants.TAG, birdList.get(position).getName()+ " Clicked Position := " + position + " Value: " + sparseBooleanArray.get(position));
                birdMap.put(birdList.get(position), sparseBooleanArray.get(position));
                Log.d(Constants.TAG, birdList.get(position).getName()+ " Clicked Position := " + birdMap.get(birdList.get(position)));
            }
        });
    }

    public void toggle(View v)
    {
        CheckedTextView cv = (CheckedTextView)v;
        cv.toggle();
    }

    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        lvQuestions.setVisibility(show ? View.GONE : View.VISIBLE);
        lvBirds.setVisibility(show ? View.GONE : View.VISIBLE);
        btnPhoto.setVisibility(show ? View.GONE : View.VISIBLE);
        btnSubmit.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    private void setupButton() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //selection();
                /*if (mCurrentPhotoPath == null && (user.getIsSuperUser() || user.getIsStaff()) ) {
                    Toast.makeText(getApplicationContext(), "Please submit a picture of the tree before you move on",
                            Toast.LENGTH_LONG).show();
                    return;
                }*/

                if (f == false) {
                    showProgress(true);
                    Intent UploadIntent = new Intent(TreeQuestionsActivity.this, UploadTreeQuestionIntentService.class);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_UPDATE, false);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_LAT, l1);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_LONG, l2);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_SPECIES, tree.getId());
                    //UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_CHNG, 1);

                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_IMG, mCurrentPhotoPath);

                    int[] answerID = new int[questionMap.size()];
                    int qIter = 0;
                    for (Map.Entry<TreeQuestion, Boolean> entry : questionMap.entrySet()){
                        if(entry.getValue()){
                            answerID[qIter] = entry.getKey().getTrueID();
                        }
                        else{
                            answerID[qIter] = entry.getKey().getFalseID();
                        }
                        qIter ++;
                    }

                    int birdsID[] = new int[birdMap.size()];
                    int birdIter = 0;
                    for (Map.Entry<BirdSpecies, Boolean> entry : birdMap.entrySet())
                    {
                        if (entry.getValue()){
                            birdsID[birdIter] = entry.getKey().getId();
                        }
                        else{
                            birdsID[birdIter] = -1;
                        }
                        birdIter++;
                    }
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_CHOICES, answerID);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_BIRDS, birdsID);
                    startService(UploadIntent);

                } else {
                    showProgress(true);
                    treeID = tree.getId();
                    Intent UploadIntent = new Intent(TreeQuestionsActivity.this, UploadTreeQuestionIntentService.class);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_UPDATE, true);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_IMG, mCurrentPhotoPath);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_TREE, treeID);
                    int[] answerID = new int[questionMap.size()];
                    int qIter = 0;
                    for (Map.Entry<TreeQuestion, Boolean> entry : questionMap.entrySet()){
                        if(entry.getValue()){
                            answerID[qIter] = entry.getKey().getTrueID();
                        }
                        else{
                            answerID[qIter] = entry.getKey().getFalseID();
                        }
                        qIter ++;
                    }
                    int birdsID[] = new int[birdMap.size()];
                    int birdIter = 0;
                    for (Map.Entry<BirdSpecies, Boolean> entry : birdMap.entrySet())
                    {
                        if (entry.getValue()){
                            birdsID[birdIter] = entry.getKey().getId();
                        }
                        else{
                            birdsID[birdIter] = -1;
                        }
                        birdIter++;
                    }
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_CHOICES, answerID);
                    UploadIntent.putExtra(UploadTreeQuestionIntentService.PARAM_IN_BIRDS, birdsID);
                    startService(UploadIntent);
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
        btnBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isBirdList){
                    lvQuestions.setVisibility(View.INVISIBLE);
                    lvBirds.setVisibility(View.VISIBLE);
                    btnBird.setImageResource(R.drawable.ic_tree_48dp);
                    titleQuestion.setText(R.string.bird_seen_title);
                }
                else{
                    lvQuestions.setVisibility(View.VISIBLE);
                    lvBirds.setVisibility(View.INVISIBLE);
                    btnBird.setImageResource(R.drawable.ic_bird_48dp);
                    titleQuestion.setText(R.string.tree_questions_title);
                }
                isBirdList = !isBirdList;
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
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    btnSubmit.setEnabled(true);
                    btnPhoto.setImageResource(R.drawable.ic_camera_checked);
                    Log.d(Constants.TAG, "PICTURE TAKEN");
                }
                else{
                    File deleted = new File(mCurrentPhotoPath);
                    boolean d = deleted.delete();
                    Log.d(Constants.TAG, "PICTURE NOT TAKEN . image deleted?: " + d);
                    btnPhoto.setImageResource(R.drawable.ic_camera_warning);
                    btnSubmit.setEnabled(false);
                    if (t && user.getIsSuperUser()) {
                        btnSubmit.setEnabled(true);
                    }
                }
                break;

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
                URL url = new URL(Constants.GET_QUESTIONS_LIST_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("client_id", Constants.CLIENT_ID);
                conn.setRequestProperty("client_secret", Constants.CLIENT_SECRET);
                conn.setRequestProperty("Authorization", "Bearer " + user.getAccessToken());
                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "questions status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String questionsListData = sb.toString();
                    jsonToArrayList(questionsListData);
                    Log.d(Constants.TAG, "questionsJSON: " + questionsListData);
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

    /*
    public void selection (){
        final ListView lv = (ListView)findViewById(R.id.treeQuestionsList);
        int size = questionsList.size();

        //checks if position is true/false and adds to answers list
        for (int i = 0; i < size; i++) {
            answers.add(lv.getCheckedItemPositions().get(i));

            Log.i(Constants.TAG, "answers: " + answers.get(i));
        }

    }*/

    public class GetBirdListTask extends AsyncTask<Void, Void, Boolean> {
        String jsonResponse;
        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Constants.GET_BIRDS_LIST_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("client_id", Constants.CLIENT_ID);
                conn.setRequestProperty("client_secret", Constants.CLIENT_SECRET);
                conn.setRequestProperty("Authorization", "Bearer " + user.getAccessToken());
                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "bird status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    jsonResponse = sb.toString();
                    Log.d(Constants.TAG, "bird JSON: " + jsonResponse);
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
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setUpBirdMap(jsonResponse);
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setUpBirdMap(String jsonString){
        try {
            JSONArray data = new JSONArray(jsonString);

            for (int i = 0; i < data.length(); i++) {
                JSONObject json_data = data.getJSONObject(i);
                JSONObject type = json_data.getJSONObject("type");
                int type_id = type.getInt("id");
                String type_name = type.getString("name");
                int id = json_data.getInt("id");
                String name = json_data.getString("name");
                String sciName = json_data.getString("scientific_name");
                String desc = json_data.getString("description");
                String imageURL = json_data.getString("image");

                birdMap.put(new BirdSpecies(name, sciName, desc, id, imageURL), false);
            }
        }catch (JSONException e){
            Log.i(Constants.TAG, "JSON Exception");
            e.printStackTrace();
        }
        setupBirdListView();
    }


    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.example.lap.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(UploadTreeQuestionIntentService.PARAM_OUT_MSG);
            Toast.makeText(TreeQuestionsActivity.this, text, Toast.LENGTH_LONG).show();
            //showProgress(false);
            Intent mapIntent = new Intent(TreeQuestionsActivity.this, MapsActivity.class);
            mapIntent.putExtra("userTokens", user);
            startActivity(mapIntent);
            finish();
        }
    }
}
