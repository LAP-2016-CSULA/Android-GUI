package com.example.romsm.lap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;

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

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //Will contain user's tokens
    UserAccount user;
    String access_token;
    String refresh_token;

    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //Get saved tokens if any
        getSharedPref();
        //Start map activity if previous tokens found
        if(access_token !=null && refresh_token != null) {
            isLoggedIn = true;
            showProgress(true);
            user = new UserAccount(access_token, refresh_token);
            new GetUserInfoTask().execute();
            /*
            user = new UserAccount(access_token, refresh_token);
            Intent mapIntent = new Intent(LoginActivity.this, MapsActivity.class);
            mapIntent.putExtra("userTokens", user);
            startActivity(mapIntent);
            finish();
            */
        }
    }

    /**
     * Gets access token and refresh token
     */
    private void getSharedPref(){
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        access_token = settings.getString("accessToken", null);
        refresh_token= settings.getString("refreshToken", null);

        //debug
        Log.i(Constants.TAG, "Access: "+access_token);
        Log.i(Constants.TAG, "Refresh: "+refresh_token);
    }

    /**
     *  Saves the access tokens and refresh tokens as sharedpreferences
     */
    private void setTokensAsSharedPref(String accessToken, String refreshToken){
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("accessToken", accessToken);
        editor.putString("refreshToken", refreshToken);
        editor.commit();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        HttpURLConnection conn = null;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                URL url = new URL(Constants.ACCESS_TOKEN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("grant_type", "password")
                        .appendQueryParameter("username", mUsername)
                        .appendQueryParameter("password", mPassword)
                        .appendQueryParameter("scope", "read")
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
                Log.d(Constants.TAG, "login status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String tokenData = sb.toString();
                    user = new UserAccount(tokenData);
                    user.setUsername(mUsername);
                    Log.d(Constants.TAG, tokenData);
                    Log.d(Constants.TAG, "access:"+user.getAccessToken());
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
            }catch (JSONException e){
                Log.i(Constants.TAG, "JSON Exception");
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
            mAuthTask = null;
            //showProgress(false);

            if (success) {
                //setTokensAsSharedPref(user.getAccessToken(), user.getRefreshToken());
                new GetUserInfoTask().execute();
                //Intent mapIntent = new Intent(LoginActivity.this, MapsActivity.class);
                //mapIntent.putExtra("userTokens", user);
                //mapIntent.putExtra("username", mUsername);
                //startActivity(mapIntent);
                //finish();
            } else {
                showProgress(false);
                mUsernameView.setError(getString(R.string.error_incorrect_username));
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mUsernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * AsynTask used after getting tokens used to get users info such as username
     */
    public class GetUserInfoTask extends AsyncTask<Void, Void, Boolean> {
        HttpURLConnection conn = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            getNewAccessToken(user.getRefreshToken());
            try {
                URL url = new URL(Constants.GET_USER_INFO_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("client_id", Constants.CLIENT_ID);
                conn.setRequestProperty("client_secret", Constants.CLIENT_SECRET);
                conn.setRequestProperty("Authorization", "Bearer " + user.getAccessToken());
                conn.connect();

                int status = conn.getResponseCode();
                Log.d(Constants.TAG, "userinfo status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String userInfoData = sb.toString();

                    user.setUserInfo(userInfoData);
                    Log.d(Constants.TAG, userInfoData);
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
            }catch (JSONException e){
                Log.i(Constants.TAG, "JSON Exception");
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
            showProgress(false);
            if (success) {
                setTokensAsSharedPref(user.getAccessToken(), user.getRefreshToken());
                Intent mapIntent = new Intent(LoginActivity.this, MapsActivity.class);
                mapIntent.putExtra("userTokens", user);
                startActivity(mapIntent);
                finish();
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private void getNewAccessToken(String refreshToken){
            try {
                URL url = new URL(Constants.ACCESS_TOKEN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("grant_type", "refresh_token")
                        .appendQueryParameter("client_id", Constants.CLIENT_ID)
                        .appendQueryParameter("client_secret", Constants.CLIENT_SECRET)
                        .appendQueryParameter("refresh_token", refreshToken);
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
                Log.d(Constants.TAG, "refresh status " + status);

                if (status == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String responseString;
                    StringBuilder sb = new StringBuilder();

                    while ((responseString = reader.readLine()) != null) {
                        sb = sb.append(responseString);
                    }
                    String tokenData = sb.toString();
                    user.setTokens(tokenData);
                    Log.d(Constants.TAG, tokenData);
                }
            }catch (MalformedURLException e){
                Log.i(Constants.TAG, "Malformed Url");
                e.printStackTrace();
            }catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                e.printStackTrace();
            }catch (JSONException e){
                Log.i(Constants.TAG, "JSON Exception");
                e.printStackTrace();
            }finally {
                if (conn != null)
                    conn.disconnect();
            }
        }
    }
}

