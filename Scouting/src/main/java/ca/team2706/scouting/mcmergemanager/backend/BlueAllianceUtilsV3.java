package ca.team2706.scouting.mcmergemanager.backend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import ca.team2706.scouting.mcmergemanager.R;


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


    public static void test(Context context) throws IOException {
        RequestQueue queue = Volley.newRequestQueue(context);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        final String url = BASEURL + "/status";

        System.out.println(url);

        // prepare the Request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // display response
                System.out.println(response.toString() + "\nWriting should have gone well");
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        error.printStackTrace();
                    }
                });
        // add it to the RequestQueue
        queue.add(request);
    }
}
