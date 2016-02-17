package com.example.romsm.lap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.HashMap;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private UserAccount user;
    private ImageView treeImage;

    private HashMap<Marker, Integer> markerIDs;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        Log.i(Constants.TAG, user.getAccessToken()+"");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void setUpDbMarkers(){
        markerIDs = new HashMap<>();
        MapDbHelper dbHelper = new MapDbHelper(getApplicationContext());
        cursor = dbHelper.getAllRows();

        cursor.moveToFirst();
        //Log.d(Constants.TAG, "cursor is after last? : " + cursor.isAfterLast());
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MapContract.MapEntry._ID));
            String latitude = cursor.getString(cursor.getColumnIndexOrThrow(MapContract.MapEntry.LATITUDE));
            String longitude = cursor.getString(cursor.getColumnIndexOrThrow(MapContract.MapEntry.LONGITUDE));
            Double l1 = Double.parseDouble(latitude);
            Double l2 = Double.parseDouble(longitude);
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MapContract.MapEntry.TREE_NAME));
            String sciName = cursor.getString(cursor.getColumnIndexOrThrow(MapContract.MapEntry.TREE_SCI_NAME));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(MapContract.MapEntry.TREE_DESC));


            //Log.d(Constants.TAG, latitude + " " + longitude);
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(l1, l2))
                    .title(name)
                    .snippet(desc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tree_48dp)));

            markerIDs.put(m,id);

            cursor.moveToNext();
        }
        cursor.close();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        //setUpDbMarkers();

        new RetrieveTreesTask().execute();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                    final LatLng location = latLng;
                    //Show dialog asking user if they want to add a tree
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button

                            Intent listIntent = new Intent(MapsActivity.this, TreeSpeciesListActivity.class);
                            listIntent.putExtra("userTokens", user);
                            double l1 = location.latitude;
                            double l2 = location.longitude;
                            listIntent.putExtra("lat", l1);
                            listIntent.putExtra("long", l2);
                            startActivity(listIntent);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.setMessage(R.string.add_tree_here)
                            .setTitle(R.string.app_name);
                    AlertDialog dialog = builder.create();
                    dialog.show();
            }
        });


        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                //   using info class to test

                MapDbHelper dbHelper = new MapDbHelper(getApplicationContext());
                int treeID = dbHelper.getTreeID(markerIDs.get(marker));
                //Log.d(Constants.TAG, "id: " + markerIDs.get(marker)+ " tree id: " + treeID);
                Intent intent = new Intent(MapsActivity.this, UpdateActivity.class);
                intent.putExtra("userTokens", user);
                intent.putExtra("treeID", treeID);
                startActivity(intent);
            }
        });
        {
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app
            mMap.setMyLocationEnabled(true);
            setUpMap();
        }
    }

    private void setUpMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location mylocation = locationManager.getLastKnownLocation(provider);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (mylocation != null) {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            double latitude = mylocation.getLatitude();
            double longitude = mylocation.getLongitude();
            LatLng latlng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }



    // Initiating Menu XML file (menu.xml)
    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_buttons, menu);
        this.menu = menu;
        MenuItem usernameItem = menu.findItem(R.id.action_user);
        String group = "Student";
        if(user.getIsStaff()){
            group = "Staff";
        }
        if(user.getIsSuperUser()){
            group = "Admin";
        }
        usernameItem.setTitle(user.getUsername()+" : " + group);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new UserLogoutTask().execute();
                return true;

            case R.id.action_search:
                Intent speciesIntent = new Intent(MapsActivity.this, AllSpeciesActivity.class);
                speciesIntent.putExtra("userTokens", user);
                startActivity(speciesIntent);
                return true;
            case R.id.action_info:
                startActivity(new Intent(MapsActivity.this, Information.class));
                return true;

            case R.id.action_user:
                //debug: remove trees
                MapDbHelper dbHelper = new MapDbHelper(getApplicationContext());
                dbHelper.clearTable();
                dbHelper.close();
                //restart activity
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Represents an asynchronous logout task used to revoke the access token and logout
     * the user.
     */
    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            return revokeToken(user.getAccessToken());
        }

        protected Boolean revokeToken(String token){
            try {
                URL url = new URL(Constants.REVOKE_TOKEN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("token", token)
                        .appendQueryParameter("client_id", Constants.CLIENT_ID)
                        .appendQueryParameter("client_secret", Constants.CLIENT_SECRET);
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
                Log.d(Constants.TAG, "logout status " + token +" : " + status);

                if (status == 200) {
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

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(MapsActivity.this, "Successfully logged out", Toast.LENGTH_LONG).show();

                //clear tokens from shared preferences
                SharedPreferences settings = getSharedPreferences("tokens", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();

                Intent LoginIntent = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(LoginIntent);
                finish();
            } else {
                Toast.makeText(MapsActivity.this, "Something went wrong. Try Again", Toast.LENGTH_LONG).show();

            }
        }
    }

    /**
     * Represents an asynchronous task used to get trees from server
     */
    public class RetrieveTreesTask extends AsyncTask<Void, Void, Boolean> {

        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            return retriveTree();
        }

        protected Boolean retriveTree(){
            try {
                URL url = new URL(Constants.POST_TREE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "retrieve tree status " +" : " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String treesJSON = sb.toString();
                    //Log.d(Constants.TAG, "treesJSON: " + treesJSON);
                    jsonToDB(treesJSON);
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
            }catch (JSONException e) {
                Log.i(Constants.TAG, "JSON Exception");
                e.printStackTrace();
                return false;
            }finally {
                if (conn != null)
                    conn.disconnect();
            }

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            return false;
        }

        private void jsonToDB(String jsonString) throws JSONException {
            JSONArray data = new JSONArray(jsonString);

            MapDbHelper dbHelper = new MapDbHelper(getApplicationContext());
            dbHelper.clearTable();

            for(int i=0; i < data.length() ; i++) {
                JSONObject json_data = data.getJSONObject(i);

                int id = json_data.getInt("id");

                JSONObject species = json_data.getJSONObject("species");
                String name = species.getString("name");
                String sciName = species.getString("scientific_name");
                String desc = species.getString("description");

                double lng = json_data.getDouble("long");
                double lat = json_data.getDouble("lat");

                String imageURL = json_data.getString("image");

                dbHelper.insertMapEntry(id, name, sciName, desc, imageURL, String.valueOf(lat),String.valueOf(lng));
            }
            dbHelper.close();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setUpDbMarkers();
            } else {
                Toast.makeText(MapsActivity.this, "Something went wrong. Couldn't retrieve pins from server. Try again later.", Toast.LENGTH_LONG).show();

            }
        }

    }
}