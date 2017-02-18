package ca.team2706.scouting.mcmergemanager.backend;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import ca.team2706.scouting.mcmergemanager.R;

/**
 * Created by dwall on 06/02/17.
 */

public class JsonUtils {

    private static JSONArray jsonArray;
    private static JSONObject jsonObject; // TODO: figure out why I can't just create this variable in the method
    private static String fileName = "/sdcard/"+ App.getContext().getString(R.string.FILE_TOPLEVEL_DIR) + "/Team2706/matchData.txt";

    private static JSONArray[][] objects;
    private static JSONObject obj;

    public static void getCompetition(final Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        final String url = "http://ftp.team2706.ca:3000/competitions/153/matches.json";
        jsonArray = new JSONArray();

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        jsonArray = response;

                        // create file for json
                        saveJsonFile(context);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public static void getMatch(final Context context, int matchID) {
        RequestQueue queue = Volley.newRequestQueue(context);
        final String url = "http://ftp.team2706.ca:3000/matches/" + matchID + ".json";

        jsonObject = new JSONObject();

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        jsonObject = response;


                        try {
                            System.out.println(jsonObject.get("team_id"));
                        } catch(JSONException e) {
                            Log.d("JSON error: ", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        queue.add(getRequest);
    }



    public static synchronized void saveJsonFile(Context context) {

        System.out.println("writing to file");
        // write json file to stuff
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(jsonArray.toString());
            System.out.println("Successfully Copied JSON Object to File...");
            file.close();
        } catch(IOException e) {
            Log.d("Error writing file: ", e.toString());
        }
    }

    public static synchronized void readJsonFile(Context context) {
        String str = "";

        try {
            FileInputStream fis = new FileInputStream (new File(fileName));

            if ( fis != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                fis.close();
                str = stringBuilder.toString();

                jsonArray = new JSONArray(str);
            }
        } catch (FileNotFoundException e) {
            Log.d("Can't find file: ", e.toString());
        } catch(IOException e) {
            Log.d("Error reading file", e.toString());
        } catch(JSONException e) {
            Log.d("Error parsing json", e.toString());
        }

        System.out.println(jsonArray.toString());

        parseOneMatch(12);
    }

    public static void parseOneMatch(int matchId) {
        obj = new JSONObject();


    }

    public static void postMatch(final Context context) {
        final String url = "http://ftp.team2706.ca:3000/competitions/1/matches.json";
        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String URL = "http://...";
            // Prepares POST gearDeliveryData...
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("TakeoffID", "2");
            jsonBody.put("ViewPhoto1", "image base64 content");
            jsonBody.put("ViewPhoto2", "image base64 content");
            // "OrderLineid": "964"...
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("OrderLineid","964");
            jsonObject1.put("OrderLinePhoto1","image base64 content");
            jsonObject1.put("OrderLinePhoto2","image base64 content");
            // "OrderLineid": "963"...
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("OrderLineid","963");
            jsonObject2.put("OrderLinePhoto1","image base64 content");
            jsonObject2.put("OrderLinePhoto2","image base64 content");
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject1);
            jsonArray.put(jsonObject2);
            // "LineItems"...
            jsonBody.put("LineItems", jsonArray);
            final String mRequestBody = jsonBody.toString();
            // Volley request...
            StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                                mRequestBody, "utf-8");
                        return null;
                    }
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
