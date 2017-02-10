package ca.team2706.scouting.mcmergemanager.backend;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.DataRequester;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchSchedule;

public class BlueAllianceUtils {

    private Activity mActivity;

    private static boolean sPermissionsChecked = false;

    /* ~~~ Constructor ~~~ */
    public BlueAllianceUtils(Activity activity) {
        mActivity = activity;

        checkInternetPermissions(activity);
    }


    public static boolean checkInternetPermissions(Activity activity) {
        if (activity == null)
            return false;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.INTERNET},
                    123);

            // check if they clicked Deny
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED)
                sPermissionsChecked = false;
        }

        sPermissionsChecked = true;
        return sPermissionsChecked;
    }

    @NonNull
    private static String readUrl(String urlString) throws Exception {
        if(!sPermissionsChecked)
            throw new IllegalStateException("BlueAllianceUtils: make sure you call BlueAllianceUtils.checkInternetPermissions() and the user has clicked \"Yes\" before trying to contact thebluealliance.com!");

        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


    /**
     * Gets you the formatted string of Blue Alliance data for a particular team.
     * If we already have data on this team in
     * /MCMergeManager/<TeamName>/<EventName>/thebluealliance.json
     * then it will return that data, otherwise it will do an internet fetch and store the results
     * in the file for future offline searches.
     * <p/>
     * This should trigger on searching for a team on the Team Info tab.
     */
    public String getBlueAllianceDataForTeam(int teamNumber) {

        ArrayList<String> downloadArray;
        String joined2016 = "";
        String joined2015 = "";
        String joined2014 = "";
        String joined2013 = "";
        String joined = "";

        String combine;
        ArrayList<String> store2016 = new ArrayList<>();
        ArrayList<String> store2015 = new ArrayList<>();
        ArrayList<String> store2014 = new ArrayList<>();
        ArrayList<String> store2013 = new ArrayList<>();

        /* if (we already have data on them in the json file) {
            String data = extract the data from json format and build a pretty-print string
            return data; //TODO
        }*/

        ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            try {
                downloadArray = new ArrayList<>();

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
                boolean done = false;

                String[] parts = SP.getString("Download Data", null).split("\\.");
                for (int g = 0; g < parts.length; g++) {
                    if (parts[g].contains(Integer.toString(teamNumber))) {
                        Log.d(App.getContext().getResources().getString(R.string.app_name), "Team Data Found");
                        downloadArray.add(parts[g]);

                        done = true;
                    }
                }
                if (!done) {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(App.getContext(), "No Internet and this team is not downloaded!", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    String returning = TextUtils.join("\n", downloadArray);
                    String regex = "\\b" + teamNumber;
                    returning = returning.replaceAll(regex, "");

                    return returning;
                }
            } catch (java.lang.NullPointerException e) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(App.getContext(), "No Internet and nothing has been downloaded!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            // if (! file exitst(/MCMergeManager/<TeamName>/<EventName>/thebluealliance.json) )
        } else {
            ArrayList<String> comps2016 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2016/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2016 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2016/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2016.size(); i++) {
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2016" + comps2016.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {
                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
//                        combine = "2016 " + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;
                        combine = Integer.toString(p) + "/" + compAmount + " - 2016 " + compsName2016.get(i);

                        store2015.add(combine);
                        joined2015 = TextUtils.join("\n", store2015);
                    }
                }
            }

            ArrayList<String> comps2015 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2015/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2015 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2015/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2015.size(); i++) {
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2015" + comps2015.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {
                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
//                        combine = "2015 " + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;
                        combine = "\t\t" + Integer.toString(p) + "/" + compAmount + " - 2015 " + compsName2015.get(i);

                        store2015.add(combine);
                        joined2015 = TextUtils.join("\n", store2015);
                    }
                }
            }

            ArrayList<String> comps2014 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2014/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2014 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2014/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2014.size(); i++) {
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2014" + comps2014.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {

                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
//                        combine = "2014 " + compsName2014.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;
                        combine = Integer.toString(p) + "/" + compAmount + " - 2014 " + compsName2014.get(i);

                        store2014.add(combine);
                        joined2014 = TextUtils.join("\n", store2014);

                    }
                }

            }

            ArrayList<String> comps2013 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2013/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2013 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2013/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2013.size(); i++) {
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2013" + comps2013.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {


                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
//                        combine = "2013 " + compsName2013.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;
                        combine = "\t\t" + Integer.toString(p) + "/" + compAmount + " - 2013 " + compsName2013.get(i);

                        store2013.add(combine);
                        joined2013 = TextUtils.join("\n", store2013);
                    }

                }
            }
            joined = joined2015 + "\n" + joined2014 + "\n" + joined2013;
            //  }

        }
        return joined;
    }



    /**
     * Call a BlueAlliance URL and a key and it will give you the parsed JSON back
     */
    public static String getBlueAllianceData(final String key, final String url) {
        String keyedJson = "";
        try {
            JSONObject issueObj = new JSONObject(readUrl(url));
            keyedJson = issueObj.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyedJson;
    }

    /**
     * <Some Description>
     */
    public static String getBlueAllianceDataArrayAsString(final String key, final String url) {
        String keyedJson = "";

        try {
            ArrayList<String> array1 = new ArrayList<>();
            JSONArray issueArray = new JSONArray(readUrl(url));

            for (int i = 0; i < issueArray.length(); i++) {
                JSONObject jsonobject = issueArray.getJSONObject(i);
                String keyData = jsonobject.getString(key);
                array1.add(keyData);
                keyedJson = TextUtils.join(", ", array1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyedJson;
    }

    /**
     * <Some Description>
     */
    public static ArrayList<String> getBlueAllianceDataDoubleArrayAsArray(final int key, final String url) {
        ArrayList<String> array1 = new ArrayList<>();
        try {
            JSONArray issueArray = new JSONArray(readUrl(url));
            for (int i = 0; i < issueArray.length(); i++) {
                JSONArray halfArray = issueArray.getJSONArray(i);
                String keyData = halfArray.getString(key);
                array1.add(keyData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array1;
    }


    public static void fetchMatchScheduleAndResults(final DataRequester requester) {

        // check if we have internet connectivity
        ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            return;
        }

        new Thread()
        {
            public void run() {
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
                String TBA_event = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_event), "<Not Set>");
                String scheduleStr;
                try {
                    scheduleStr = readUrl("http://www.thebluealliance.com/api/v2/event/"+TBA_event+"/matches?X-TBA-App-Id=frc2706:mergemanager:v01/");
                } catch (Exception e) {
                    Log.e(App.getContext().getResources().getString(R.string.app_name), "Error fetching schedule data from thebluealliance. ",e);
                    return;
                }

                MatchSchedule schedule = new MatchSchedule(scheduleStr);

                // return data to the requester
                requester.updateMatchSchedule(schedule);
            }
        }.start();
    }


    /**
     * Fetches Blue Alliance data for all teams who are registered for a particular event and saves the data in
     * /MCMergeManager/<TeamName>/<EventName>/thebluealliance.json
     * so that the data is still accessible later, even if there's no internet connection later.
     * <p/>
     * This should trigger on a button, or maybe a settin+gs-menu item in Settings.
     */
    public void downloadBlueAllianceDataForEvent(String eventName, String year, final ProgressBar progressBar, final AlertDialog dialog, int dataYear) {
        ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            return;
        }

        //Get all teams at event
        final ArrayList<String> downloadedCompArray = getBlueAllianceDataArrayAsArray("team_number", "http://www.thebluealliance.com/api/v2/event/" + year + eventName + "/teams?X-TBA-App-Id=frc2706:mergemanager:v01/");

        for (
            //For each team
                int o = 0;
                o < downloadedCompArray.size(); o++)

        {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Log.v(App.getContext().getResources().getString(R.string.app_name), "ProgressBar Current Size: " + progressBar.getProgress());
                    progressBar.setProgress(progressBar.getProgress() + (int) (100.0 / downloadedCompArray.size()));
                }
            });
            boolean looped = false;
            String joe = downloadedCompArray.get(o);

            //Get Competitions they went to
            ArrayList<String> comps2015 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");

            //Get Names of Competitions they went to
            ArrayList<String> compsName2015 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");

            for (int i = 0; i < comps2015.size(); i++) {
                //For each competition

                //Get their ranking
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/" + dataYear + comps2015.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {
                    if (test.get(p).equals(Integer.toString(Integer.parseInt(joe)))) {
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        String combine = dataYear + " "/* //TODO year */ + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount + " " + joe;

                        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
                        SharedPreferences.Editor editor = SP.edit();
                        boolean done = false;

                        String[] parts = SP.getString("Download Data", "").split("\\.");

                        for (int g = 0; g < parts.length; g++) {
                            if (combine.equals(parts[g])) {
                                Log.d(App.getContext().getResources().getString(R.string.app_name), "Download Data: Found Duplicate");
                                if (!looped)
                                    done = true;

                                break;
                            }
                        }
                        if (!done) {
                            editor.putString("Download Data", SP.getString("Download Data", "") + combine + ".").apply();
                            if (comps2015.size() > 1) {
                                looped = true;
                            }
                        }
                    }
                }
            }
        }

        Log.i(App.getContext().getResources().getString(R.string.app_name), "Download Data: Download Finished.");
    }

    /**
     * <Some Description>
     */
    public static ArrayList<String> getBlueAllianceDataArrayAsArray(final String key, final String url) {
        ArrayList<String> array1 = new ArrayList<>();

        try {
            JSONArray issueArray = new JSONArray(readUrl(url));
            for (int i = 0; i < issueArray.length(); i++) {
                JSONObject jsonobject = issueArray.getJSONObject(i);
                String keyData = jsonobject.getString(key);
                array1.add(keyData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array1;
    }

}