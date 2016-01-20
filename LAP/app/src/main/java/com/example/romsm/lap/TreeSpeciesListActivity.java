package com.example.romsm.lap;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class TreeSpeciesListActivity extends AppCompatActivity {
    private UserAccount user;
    private double l1, l2;
    ArrayList<TreeSpecies> speciesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_species_list);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        l1= intent.getDoubleExtra("lat", 0);
        l2= intent.getDoubleExtra("long", 0);

        new GetSpeciesListTask().execute();
    }

    private ArrayList<TreeSpecies> jsonToArrayList(String jsonString) throws JSONException{
        ArrayList<TreeSpecies> speciesList = new ArrayList<TreeSpecies>();
        JSONArray data = new JSONArray(jsonString);

        for(int i=0; i < data.length() ; i++) {
            JSONObject json_data = data.getJSONObject(i);
            int type = json_data.getInt("type");
            int id = json_data.getInt("id");
            String name = json_data.getString("name");
            String sciName = json_data.getString("scientific_name");
            String desc = json_data.getString("description");
            if(type == 1){ //type 1 = tree
                speciesList.add(new TreeSpecies(name, sciName, desc, id));
            }

        }
        return speciesList;
    }

    private void setupListView(){
        final ListView lv = (ListView)findViewById(R.id.speciesList);
        //lv.setAdapter(new ArrayAdapter<TreeSpecies>(this, android.R.layout.simple_list_item_1, speciesList));
        TreeSpeciesAdapter adapter = new TreeSpeciesAdapter(this, speciesList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object o = lv.getItemAtPosition(position);
                Log.d(Constants.TAG, o.toString());

                Intent questionsIntent = new Intent(TreeSpeciesListActivity.this, TreeQuestionsActivity.class);
                questionsIntent.putExtra("userTokens", user);
                questionsIntent.putExtra("lat",l1);
                questionsIntent.putExtra("long", l2);
                startActivity(questionsIntent);
            }
        });
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
                Toast.makeText(TreeSpeciesListActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
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
                    speciesList = jsonToArrayList(speciesListData);
                    Log.d(Constants.TAG, "speciesJSON: " + speciesListData);
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
}
