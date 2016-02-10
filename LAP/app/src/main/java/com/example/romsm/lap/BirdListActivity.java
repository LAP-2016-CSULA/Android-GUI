package com.example.romsm.lap;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
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

public class BirdListActivity extends AppCompatActivity {
    UserAccount user;
    ArrayList<BirdSpecies> birdList = new ArrayList<>();
    Button enterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bird_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");

        enterBtn = (Button)findViewById(R.id.enter_bird_button);
        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(BirdListActivity.this, MapsActivity.class);
                mapIntent.putExtra("userTokens", user);
                startActivity(mapIntent);
                finish();
            }
        });

        new GetBirdListTask().execute();
    }

    private ArrayList<BirdSpecies> jsonToArrayList(String jsonString) throws JSONException {
        ArrayList<BirdSpecies> birdList = new ArrayList<>();
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
            if (type_id == 2) {//type 2 = bird
                birdList.add(new BirdSpecies(name, sciName, desc, id, imageURL));
            }

        }
        return birdList;
    }

    private void setupListView() {
        final ListView lv = (ListView) findViewById(R.id.birdListView);
        BirdListAdapter adapter = new BirdListAdapter(this, birdList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {

                SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
                CheckedTextView ctv =(CheckedTextView)v.findViewById(R.id.speciesName);
                ctv.toggle();
                Log.d(Constants.TAG, "Clicked Position := " + position + " Value: " + sparseBooleanArray.get(position));
            }
        });
    }

    public void toggle(View v)
    {
        CheckedTextView cv = (CheckedTextView)v;
        cv.toggle();
    }

    public class GetBirdListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://isitso.pythonanywhere.com/species/?type_name=bird");
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
                    String birdListData = sb.toString();
                    birdList = jsonToArrayList(birdListData);
                    Log.d(Constants.TAG, "bird JSON: " + birdListData);
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
                setupListView();
            } else {
                Toast.makeText(BirdListActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }
    }

}
