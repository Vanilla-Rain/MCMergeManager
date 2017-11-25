package ca.team2706.scouting.mcmergemanager.backend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by daniel on 15/11/17.
 */

public class BlueAllianceUtilsV3 {

    public static final String BASEURL = "http://www.thebluealliance.com/api/v3";
    public static final String AUTHKEY = "8GLetjJXz2pNCZuY0NnwejAw0ULn9TzbsYeLkYyzeKwDeRsK9MiDnxEGgy6UksW1";

    private Activity mActivity;

    private static boolean sPermissionsChecked = false;
//    public final OkHttpClient client;

    /* ~~~ Constructor ~~~ */
    public BlueAllianceUtilsV3(Activity activity) {
        mActivity = activity;
//        client = new OkHttpClient();

        checkInternetPermissions(activity);
    }

    public static boolean checkInternetPermissions(Activity activity) {
        if (activity == null)
            return false;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.INTERNET}, 123);

            // check if they clicked Deny
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED)
                sPermissionsChecked = false;
        }

        sPermissionsChecked = true;
        return sPermissionsChecked;
    }


    public static void test() {
        new Thread() {
            public void run() {

                OkHttpClient client = new OkHttpClient();

                // Build the request for the file
                Request request = new Request.Builder()
                        .url(BASEURL + "/status")
                        .header("X-TBA-Auth-Key", AUTHKEY)
                        .build();

                try {
                    // Send request
                    Response response = client.newCall(request).execute();

                    System.out.println(response.body().string());

                    response.close();
                } catch(IOException e) {
                    Log.d("Error getting tbaV3", e.toString());
                }
            }
        }.start();
    }
}
