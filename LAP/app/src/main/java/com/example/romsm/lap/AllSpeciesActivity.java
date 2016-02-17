package com.example.romsm.lap;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AllSpeciesActivity extends AppCompatActivity {
    private UserAccount user;
    ArrayList<TreeSpecies> speciesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_species_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");

        new GetSpeciesListTask().execute();
    }

    private ArrayList<TreeSpecies> jsonToArrayList(String jsonString) throws JSONException {
        ArrayList<TreeSpecies> speciesList = new ArrayList<TreeSpecies>();
        JSONArray data = new JSONArray(jsonString);

        for(int i=0; i < data.length() ; i++) {
            JSONObject json_data = data.getJSONObject(i);
            int id = json_data.getInt("id");
            String name = json_data.getString("name");
            String sciName = json_data.getString("scientific_name");
            String desc = json_data.getString("description");
            String imageURL = json_data.getString("image");

            speciesList.add(new TreeSpecies(name, sciName, desc, id, imageURL));
        }
        return speciesList;
    }

    private void setupListView(){
        final ListView lv = (ListView)findViewById(R.id.speciesList);
        TreeSpeciesAdapter adapter = new TreeSpeciesAdapter(this, speciesList);
        lv.setAdapter(adapter);

    }

    public class GetSpeciesListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return getSpeciesList();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setupListView();
            } else {
                Toast.makeText(AllSpeciesActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

        private Boolean getSpeciesList(){
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://isitso.pythonanywhere.com/species/");
                conn = (HttpURLConnection) url.openConnection();
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
                    Log.d(Constants.TAG, "speciesJSON: " + speciesListData);
                    speciesList = jsonToArrayList(speciesListData);
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
            }catch(JSONException e){
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
