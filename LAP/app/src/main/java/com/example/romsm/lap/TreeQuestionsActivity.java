package com.example.romsm.lap;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class TreeQuestionsActivity extends AppCompatActivity {
    private UserAccount user;
    private TreeSpecies tree;
    double l1, l2;
    ArrayList<String> questionsList = new ArrayList<String>();
    Button btnSubmit;
    List<Observation> entries = new ArrayList<>();

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
        tree= (TreeSpecies) intent.getSerializableExtra("tree");
        Log.d(Constants.TAG, "lat: " + l1 + " Long: " + l2);

        btnSubmit = (Button) findViewById(R.id.enter_button);

        new GetQuestionsListTask().execute();
    }

    JSONArray jsonArray = new JSONArray(entries);



    private ArrayList<String> jsonToArrayList(String jsonString) throws JSONException {
        ArrayList<String> questionsList = new ArrayList<String>();
        JSONArray data = new JSONArray(jsonString);

        for (int i = 0; i < data.length(); i++) {
            JSONObject json_data = data.getJSONObject(i);
            int id = json_data.getInt("id");
            String text = json_data.getString("text");
            questionsList.add(text);
        }
        return questionsList;
    }
    JSONArray jsArray = new JSONArray(entries);

    private void setupListView() {
        setupButton();
        final ListView lv = (ListView) findViewById(R.id.treeQuestionsList);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, questionsList));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {
                Object o = lv.getItemAtPosition(position);
                Log.d(Constants.TAG, o.toString());

                //CheckedTextView textView = (CheckedTextView)v;
                //textView.setChecked(!textView.isChecked());

                SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
                Log.d(Constants.TAG, "Clicked Position := " + position + " Value: " + sparseBooleanArray.get(position));
            }
        });
    }

    private void setupButton() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection();
               new UploadTask().execute();
                new DbInsertTask().execute();
            }
        });
    }

    public class GetQuestionsListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return getSpeciesList();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setupListView();
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

        private Boolean getSpeciesList() {
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
        Intent intent = getIntent();
        int id2 = intent.getIntExtra("id2", 0);
        SparseBooleanArray checked = lv.getCheckedItemPositions();
        int size = questionsList.size();

        for (int i = 0; i < size; i++) {

            entries.add(new Observation(id2, lv.getCheckedItemPositions().get(i)));

            Log.i(Constants.TAG,"ID: "+id2+"entries:"+ entries.get(i).answers + " -------------------------------------------------------");
        }

    }
    public class UploadTask extends AsyncTask<Void, Void, Boolean> {


        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                URL url = new URL("http://isitso.pythonanywhere.com/trees/");
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
                Log.d(Constants.TAG, "upload status----------------------------------------------------- " + status);

                if (status == 201) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }

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
    }
    public class DbInsertTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String type = "tree";
            MapDbHelper dbHelper = new MapDbHelper(getApplicationContext());
            //dbHelper.clearTable();
            dbHelper.insertMapEntry(type, String.valueOf(l1),String.valueOf(l2));
            dbHelper.close();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Intent mapIntent = new Intent(TreeQuestionsActivity.this, MapsActivity.class);
                mapIntent.putExtra("userTokens", user);
                startActivity(mapIntent);
                finish();
            } else {
                Toast.makeText(TreeQuestionsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }
}
