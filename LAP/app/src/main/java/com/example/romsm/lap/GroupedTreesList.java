package com.example.romsm.lap;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.romsm.lap.model.MarkerGroup;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupedTreesList extends AppCompatActivity {
    private UserAccount user;
    ArrayList ids = new ArrayList<>();
    ArrayList<TreeSpecies> treesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_species_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        ids = intent.getIntegerArrayListExtra("groupTreeList");
        new SetUpTreeList().execute();
    }

    private void setupListView(){
        final ListView lv = (ListView)findViewById(R.id.speciesList);
        TreeSpeciesAdapter adapter = new TreeSpeciesAdapter(this, treesList);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                TreeSpecies tree = (TreeSpecies) lv.getItemAtPosition(position);
                Intent infoIntent = new Intent(GroupedTreesList.this, TreeInfoActivity.class);
                infoIntent.putExtra("userTokens", user);
                infoIntent.putExtra("tree", tree);
                startActivity(infoIntent);
            }
        });
    }

    public class SetUpTreeList extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            MapDbHelper db = new MapDbHelper(getApplicationContext());
            for(int id : (ArrayList<Integer>)ids){
                TreeSpecies tree = db.getTreeInfoFromID(id);
                if (tree != null){
                    treesList.add(tree);
                }
            }
            db.close();
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setupListView();
            } else {
                Toast.makeText(GroupedTreesList.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();
            }
        }

    }
}
