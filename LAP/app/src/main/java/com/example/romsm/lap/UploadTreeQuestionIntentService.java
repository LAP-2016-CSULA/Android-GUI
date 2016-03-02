package com.example.romsm.lap;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.List;

public class UploadTreeQuestionIntentService extends IntentService {
    public static final String PARAM_IN_UPDATE = "iupd"; //update

    public static final String PARAM_IN_LAT = "ilat"; //latitude
    public static final String PARAM_IN_LONG = "ilong"; //longitude
    public static final String PARAM_IN_SPECIES = "ispc"; //species id
    public static final String PARAM_IN_CHNG = "ichng"; //changed_by

    public static final String PARAM_IN_IMG = "iimg"; //image path
    public static final String PARAM_IN_TREE = "itree"; //tree id
    public static final String PARAM_IN_CHOICES = "ichoices"; //choices

    public static final String PARAM_OUT_MSG = "omsg"; //outgoing message
    public static final String PARAM_OUT_BOOL = "obool"; //outgoing error?

    public UploadTreeQuestionIntentService(){
        super("UploadTreeQuestionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isUpdate = intent.getBooleanExtra(PARAM_IN_UPDATE, false);

        double lat = intent.getDoubleExtra(PARAM_IN_LAT, 0);
        double lng = intent.getDoubleExtra(PARAM_IN_LONG, 0);
        int species = intent.getIntExtra(PARAM_IN_SPECIES, 1);
        int changed_by = intent.getIntExtra(PARAM_IN_CHNG, 1);

        String imagePath = intent.getStringExtra(PARAM_IN_IMG);
        int treeID = intent.getIntExtra(PARAM_IN_TREE, 0);
        int[] choices = intent.getIntArrayExtra(PARAM_IN_CHOICES);

        boolean isComplete = false;

        if(!isUpdate){
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Constants.POST_TREE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("long", String.valueOf(lng))
                        .appendQueryParameter("lat", String.valueOf(lat))
                        .appendQueryParameter("landmark", null)
                        .appendQueryParameter("species", String.valueOf(species))
                        .appendQueryParameter("changed_by", String.valueOf(changed_by));
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
                    isComplete = true;

                }
            } catch (MalformedURLException e) {
                Log.i(Constants.TAG, "Malformed Url");
                isComplete = false;
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(Constants.TAG, "IO Exception");
                isComplete = false;
                e.printStackTrace();
            } catch (JSONException e) {
                Log.i(Constants.TAG, "JSON Exception");
                isComplete = false;
                e.printStackTrace();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        String charset = "UTF-8";
        try {
            MultipartUtility multipart = new MultipartUtility(Constants.POST_DAILY_UPDATE_URL, charset);

            reduceImageSize(imagePath);
            File treeFile = new File(imagePath);
            multipart.addFilePart("image", treeFile);

            multipart.addFormField("tree", String.valueOf(treeID));
            multipart.addFormField("changed_by", String.valueOf(changed_by));
            for(int id : choices){
                multipart.addFormField("choices", String.valueOf(id));
                Log.d(Constants.TAG, "choice[" + id+"]");
            }
            List<String> response = multipart.finish();

            Log.d(Constants.TAG, "add daily updates response: ");
            for (String line : response) {
                Log.d(Constants.TAG, line);
            }

            if(!response.isEmpty()){
                isComplete = true;
            }
        } catch (IOException e) {
            Log.i(Constants.TAG, "IO Exception");
            isComplete = false;
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(TreeQuestionsActivity.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if(isUpdate){
            broadcastIntent.putExtra(PARAM_OUT_MSG, "Updated Tree");
        }
        else{
            broadcastIntent.putExtra(PARAM_OUT_MSG, "Added Tree");
        }
        broadcastIntent.putExtra(PARAM_OUT_BOOL, isComplete);
        sendBroadcast(broadcastIntent);

    }

    private void reduceImageSize(String mCurrentPhotoPath){
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
        File dir = new File(mCurrentPhotoPath);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
        try{
            FileOutputStream fOut = new FileOutputStream(dir);
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        }catch (Exception e) {
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
}
