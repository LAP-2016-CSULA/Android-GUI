package com.example.romsm.lap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

    private String modeSelected = "tree";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        user = (UserAccount) intent.getSerializableExtra("userTokens");
        Log.i(Constants.TAG, user.getAccessToken()+"");

        Toast.makeText(this, "Hello, " + user.getUsername(), Toast.LENGTH_LONG).show();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                if (modeSelected.equals("tree")){
                    final LatLng location = latLng;
                    //Show dialog asking user if they want to add a tree
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button

                            //Uncomment below to switch to Tree List Activity
                            /*Intent listIntent = new Intent(MapsActivity.this, TreeSpeciesListActivity.class);
                            startActivity(listIntent);*/

                            //adds tree marker
                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title("Tree")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tree_48dp)));
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
            }
        });


        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
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
          //  mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You Are Here"));

        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
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
        usernameItem.setTitle(user.getUsername()+" : " + ((user.getIsStaff() == true) ? "Admin" : "User"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new UserLogoutTask().execute();
                return true;

            case R.id.action_bird:
                modeSelected = "bird";
                changeItemsIcon(item);
                return true;

            case R.id.action_tree:
                modeSelected = "tree";
                changeItemsIcon(item);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //changes icon of selected option item
    public void changeItemsIcon(MenuItem item){
        if(modeSelected.equals("tree")){
            item.setIcon(R.drawable.ic_tree_48dp);
            MenuItem birdItem = menu.findItem(R.id.action_bird);
            birdItem.setIcon(R.drawable.ic_bird_gray_48dp);
        }
        else if(modeSelected.equals("bird")){
            item.setIcon(R.drawable.ic_bird_48dp);
            MenuItem treeItem = menu.findItem(R.id.action_tree);
            treeItem.setIcon(R.drawable.ic_tree_gray_48dp);
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
}