package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static com.google.android.gms.common.api.GoogleApiClient.*;

/**
 *
 * This is a helper class to hold common code for accessing shared scouting data files.
 * This class takes care of keeping a local cache, syncing to Google Drive, and (eventually) sharing with other bluetooth-connected devices also running the app.
 *
 * Created by Mike Ounsworth
 */
public class FileUtils
        implements ConnectionCallbacks, OnConnectionFailedListener {

    private static boolean mCanConnect = false;
    private static boolean mHasUnsyncedMatchScoutingData = false;

    /**
     * If the user denys the app access to their drive, it asks over and over again and they don't have the chance to go to settings.
     */
    private boolean mRequestedAccessAlready = false;

    private boolean mCheckDriveFilesOnNextConnect = false;

    /**
     *  Grabbed this constant from a tutorial ... seems kinda hacky to be hard-coding API codes, shouldn't there be a place I can reference this??
     */
    private static final int REQUEST_CODE_RESOLUTION = 3;

    /** A pointer to myself so that the nested classes can use my ConnectionCallbacks **/
    FileUtils m_me;

    private static Activity mActivity;

    private GoogleApiClient mGoogleApiClient;

    private static String mRemoteToplevelFolderName;
    private static String mRemoteTeamFolderName;
    private static String mRemoteEventFolderName;
    private static String mRemoteTeamPhotosFolderName;

    private static String mLocalToplevelFilePath;
    private static String mLocalTeamFilePath;
    private static String mLocalEventFilePath;
    private static String mLocalTeamPhotosFilePath;



    /**
     * Constructor
     *
     * @param activity This will be used to fetch string contants for file storage and displaying toasts.
     *                 Also, if this is a MainActivity, then this activity's .updateDataSyncLabel()
     *                 will be called when a Drive connection either suceeds or fails.
     */
    public FileUtils(Activity activity) {
        mActivity = activity;
        m_me = this;

        // store string constants and preferences in member variables just for cleanliness
        // (since the strings are `static`, when any instances of FileUtils update these, all instances will get the updates)

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
            mRemoteToplevelFolderName = activity.getString(R.string.FILE_TOPLEVEL_DIR);
            mRemoteTeamFolderName = SP.getString(mActivity.getResources().getString(R.string.PROPERTY_googledrive_teamname), "<Not Set>");
            mRemoteEventFolderName = SP.getString(mActivity.getResources().getString(R.string.PROPERTY_googledrive_event), "<Not Set>");
            mRemoteTeamPhotosFolderName = "Team Photos";



        mLocalToplevelFilePath   = "/sdcard/" + mRemoteToplevelFolderName;
        mLocalTeamFilePath       = mLocalToplevelFilePath + "/" + mRemoteTeamFolderName;
        mLocalEventFilePath      = mLocalTeamFilePath + "/" + mRemoteEventFolderName;
        mLocalTeamPhotosFilePath = mLocalEventFilePath + "/" + mRemoteTeamPhotosFolderName;

        checkLocalFileStructure();
        reset_mGoogleApiClient();
    }

    /**
     * Is there data in the local file `matchScoutingData_UNSYNCED.csv` that needs to be synced to Drive?
     */
    public boolean hasUnsyncedMatchScoutingData() {
        return mHasUnsyncedMatchScoutingData;
    }

    public boolean canConnectToDrive() {
        return mCanConnect;
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void reset_mGoogleApiClient() {
        // if it's already connected, disconnect it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String driveAccount = SP.getString(mActivity.getResources().getString(R.string.PROPERTY_googledrive_account), "<Not Set>");

        if (driveAccount.equals("<Not Set>"))
            return;

        mGoogleApiClient = new Builder(mActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(driveAccount)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    /**
     * Try logging in to the Google Drive useng the saved account.
     * If that works, make sure the file structure exists and create it if it does not.
     *
     * The file structure is:
     * MCMergeManager/
     *  - team_name/
     *      - event/
     *          - matchResults.csv
     *          - matchScoutingData.csv
     *          - Team Photos/
     *
     * Note that this is not instant, so it spawns a new thread to wait for Drive to respond, and to check the files.
     * It will set the appropriate members in MainActivity when it completes.
     */
    void checkDriveConnectionAndFiles() {
        // force a full check of the files at the next connect
        mCheckDriveFilesOnNextConnect = true;

        // First test to see if the credentials are even valid
        reset_mGoogleApiClient();

        if (mGoogleApiClient == null) {
            mCanConnect = false;
            return;
        } else {
            mGoogleApiClient.connect();
        }
    }

    /**
     * This class defines a background process for checking / setting the file structure on Google Drive.
     *
     * MCMergeManager/
     *  - team_name/
     *      - event/
     *          - Team Photos/
     *
     * Note: run() will close the GoogleApiClient when it exists.
     */
    private class CheckDriveFilesThread extends Thread {

        private GoogleApiClient mGooggleDRiveApiClient;

        public CheckDriveFilesThread(GoogleApiClient googleApiClient) {
            mGoogleApiClient = googleApiClient;
        }
        @Override
        public void run() {
            Log.i(mActivity.getResources().getString(R.string.app_name),
                    "Starting the Drive folder sync");

            // Check if the file structure exists, and create it if it doesn't

            // check for app-folder
            DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            DriveFolder topLevelfolder = checkOrCreateRemoteFolder(mGoogleApiClient, rootFolder, mRemoteToplevelFolderName);
            if (topLevelfolder == null) {
                // something went wrong, abort
                mGoogleApiClient.disconnect();
                return;
            }

            // check for teamName folder
            DriveFolder teamFolder = checkOrCreateRemoteFolder(mGoogleApiClient, topLevelfolder, mRemoteTeamFolderName);
            if (teamFolder == null) {
                // something went wrong, abort
                mGoogleApiClient.disconnect();
                return;
            }

            // check for event folder
            DriveFolder eventFolder = checkOrCreateRemoteFolder(mGoogleApiClient, teamFolder, mRemoteEventFolderName);
            if (teamFolder == null) {
                // something went wrong, abort
                mGoogleApiClient.disconnect();
                return;
            }

            // check for Team Photos folder
            DriveFolder teamPhotosFolder = checkOrCreateRemoteFolder(mGoogleApiClient, eventFolder, mRemoteTeamPhotosFolderName);
            if (teamFolder == null) {
                // something went wrong, abort
                mGoogleApiClient.disconnect();
                return;
            }

            Log.i(mActivity.getResources().getString(R.string.app_name),
                    "Drive folder sync finished.");

            mGoogleApiClient.disconnect();
        }
    }

    /** Helper just to avoid copy&paste'ing code
     * @return null if the folder can not be found.
     **/
    private DriveFolder checkOrCreateRemoteFolder(GoogleApiClient googleApiClient, DriveFolder rootFolder, String folderName) {
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TITLE, folderName),
                Filters.contains(SearchableField.MIME_TYPE, "folder"))).build();

        DriveApi.MetadataBufferResult result = rootFolder.queryChildren(mGoogleApiClient, query).await();

        if (!result.getStatus().isSuccess()) {
            Log.e(mActivity.getResources().getString(R.string.app_name),
                    "Cannot create folder in the root of Google Drive.");
            return null;
        } else {
            for (Metadata m : result.getMetadataBuffer()) {
                if (m.getTitle().equals(folderName)) {
                    // Folder exists - we found it!
                    DriveFolder folder = m.getDriveId().asDriveFolder();
                    result.getMetadataBuffer().release();
                    return folder;
                }
            }
        }

        result.getMetadataBuffer().release();

        // Folder not found; let's create it.
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName)
                .build();

        DriveFolder.DriveFolderResult result1 = rootFolder
                .createFolder(googleApiClient, changeSet).await();

        if (!result1.getStatus().isSuccess()) {
            Log.e(mActivity.getResources().getString(R.string.app_name),
                    "Error while trying to create the folder \"" + folderName + "\"");
            return null;
        }

        return result1.getDriveFolder();
    }


    /**
     * This checks the local file system for the appropriate files and folders, creating them if they
     * are missing.
     *
     *
     * The file structure is:
     * MCMergeManager/
     *  - team_name/
     *      - event/
     *          - matchResults.csv
     *          - matchScoutingData.csv
     *          - teamPhotos/
     *
     */
    public void checkLocalFileStructure() {
        File file = new File(mLocalToplevelFilePath);
        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();

            // create it
            file.mkdir();
        }

        file = new File(mLocalTeamFilePath);
        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();

            // create it
            file.mkdir();
        }

        file = new File(mLocalEventFilePath);
        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();

            // create it
            file.mkdir();
        }

        file = new File(mLocalTeamPhotosFilePath);
        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();

            // create it
            file.mkdir();
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Toast.makeText(mActivity, "Connected to Drive!", Toast.LENGTH_SHORT).show();

        mCanConnect = true;
        if (mActivity != null && mActivity instanceof MainActivity)
            ((MainActivity) mActivity).updateDataSyncLabel();

        if (mCheckDriveFilesOnNextConnect) {
            mCheckDriveFilesOnNextConnect = false;
            (new CheckDriveFilesThread(mGoogleApiClient)).start();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        if(mRequestedAccessAlready)
            return;

        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        try {
            mRequestedAccessAlready = true;
            connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e("MC Merge Manager", "Exception while starting resolution activity", e);
        }

        mCanConnect = false;
        if (mActivity != null && mActivity instanceof MainActivity)
            ((MainActivity) mActivity).updateDataSyncLabel();
        Toast.makeText(mActivity, "Could not connect to Drive.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // I'm not gonna do anything here.
    }


    /**
     * This will return both the matchResultsData, and the matchScoutingData to the DataRequester.
     *
     * Since syncing with Drive can take a few seconds, FileUtils will immediately call
     * the activity's updateData(matchResultsDataCSV, matchScoutingDataCSV) with whatever data is locally cached.
     * If FileUtils is able to connect to Drive then it will call it again after performing the sync.
     *
     */
    public void getMatchData(DataRequester requester) {
        // TODO
    }


    /**
     * Call this when you want to append new mactch scouting data to the shared file.
     *
     * The following is the proceedure for syncronizing `matchScoutingData.csv` with Drive:
     *
     * It will first append the new lines to a local file `matchScoutingData_UNSYNCED.csv`.
     * If drive in unavailable, that is all, if Drive is available then it will spawn a background thread to:
     *
     *  1. Pull the most recent version of `matchScoutingData.csv` from Drive.
     *
     *  2. Check if the file `matchScoutingData.LOCK` exists on Drive.
     *      2b. If Yes, it means another process is currently uploading data. Set mHasUnsyncedMatchScoutingData = true; and return.
     *      2b. If No, create a file on Drive named`matchScoutingData.LOCK` so that no other process tries to write to the file at the same time.
     *
     *  3. Append the lines in `matchScoutingData_UNSYNCED.csv` to the end of `matchScoutingData.csv`
     *     and leave `matchScoutingData_UNSYNCED.csv` as as empty file.
     *
     *       3b. We should scan for duplicate lines here, but I'm not 100% sure how to do that.
     *           What if two people scout the same team in the same match, is it right to just throw out one at random?
     *
     *  4. Upload `matchScoutingData.csv` to Drive.
     *
     *  5. Delete `matchScoutingData.LOCK` from drive.
     *
     *  6. Set mHasUnsyncedMatchScoutingData = false;
     *
     * @param csvLines
     */
    public void appendToMatchScoutingData(String[] csvLines) {
        // TODO
    }

    /**
     * Add a Note for that team.
     *
     * The intention of Notes is for the drive team to be able to read them quickly.
     * They should be short and fit on one line, so they will be truncated to 80 characters.
     */
    public void addNote(int teamNumber, String note) {
        // TODO
    }
    public void getNote(int teamNumber, String note) {
        // TODO
    }

    /**
     * If we can connect to Drive, fork a background thread te syncronize photos for the provided
     * team with Google Drive.
     *
     * Note: calling this in a loop is much less efficient that calling {@link #syncAllTeamPhotos()}
     *
     * @param teamNumber The team whos photos you want to sync with Drive
     */
    public void syncOneTeamsPhotos(int teamNumber) {
        syncOneTeamsPhotos(teamNumber, null);
    }

    /**
     * If we can connect to Drive, fork a background thread te syncronize photos for the provided
     * team with Google Drive, and call the requester's {@link PhotoRequester#updatePhotos(Bitmap[])} if it succeeds.
     *
     * Note: calling this in a loop is much less efficient that calling {@link #syncAllTeamPhotos()}
     *
     * @param teamNumber The team whos photos you want to sync with Drive
     */
    @Nullable
    public void syncOneTeamsPhotos(int teamNumber, PhotoRequester requester) {
        if (! canConnectToDrive() )
            return;


    }

    /**
     * If we can connect to Drive, fork a background thread te syncronize photos for all teams.
     *
     * Because of the 10 request / second limit to the free Google Drive API subscription,
     * syncing all the team photos can take a while.
     * syncTeamPhotos() will spawn a new background thread to synchronize the photos.
     * It will pop up a Toast when it's done syncronizing.
     */
    public void syncAllTeamPhotos() {
        checkDriveConnectionAndFiles();
        if (! canConnectToDrive() )
            return;

        Toast.makeText(mActivity, "Starting Team Photo Sync in background.", Toast.LENGTH_SHORT);

        // spawn a new thread to actually do the sync


        Toast.makeText(mActivity, "Done Syncing Team Photos!", Toast.LENGTH_SHORT);
    }


    /**
     * This method takes care of saving a team photo to the local cache and syncing it to Drive if Drive is available.
     *
     * @param teamNumber The team number, which will be used as the folder for the photo.
     *                   Does not have to be a team in the matchResults file,, the photo will get saved regardless.
     * @param photo The photo to get saved.
     */
    public void saveTeamPhoto(int teamNumber, Bitmap photo) {
        // TODO
    }


    /**
     * This method will return you all locally-cached photos for the requested team.
     * It will then spawn a new background thread, and if Drive is available, it will sync the photos
     * for the requested team only and then notify the requesting activity that it has new photos.
     *
     * Since syncing photos with Drive can take a few seconds, FileUtils.loadTeamPhotos() will immediately call
     * the PhotoRequester's updatePhotos(Bitmap[]) with whatever photos are locally cached for that team,
     * and if FileUtils is able to connect to Drive then it will call it again after performing the sync.
     *
     * @param teamNumber The team whos photos we want to load.
     * @param requester The activity that is requesting the photos. This activity's .updatePhotos(Bitmap[])
     *                  will be called with the loaded photos.
     * @return It will call requester.updatePhotos(Bitmap[]) with an array of Bitmaps containing all
     *          photos for that team, or a zero-length array if no photos were found for that team.
     */
    public void getTeamPhotos(int teamNumber, PhotoRequester requester) {

        /* First, return the requester any photos we have on the local drive */

        File photosDir = new File(mLocalTeamPhotosFilePath +"/"+teamNumber);

        // check if that folder exists
        if (!photosDir.isDirectory()) {
            // we have no photos for this team
            requester.updatePhotos(new Bitmap[0]);
            return;
        }


        File[] listOfFiles = photosDir.listFiles();
        ArrayList<Bitmap> arrBitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                // BitmapFactory will return `null` if the file cannot be parsed as an image, so no error-checking needed.
                Bitmap bitmap = BitmapFactory.decodeFile(listOfFiles[i].getPath());
                if (bitmap != null)
                    arrBitmaps.add(bitmap);
            }
            // else: how did that get there ???? .... skip I guess
        }
        requester.updatePhotos( arrBitmaps.toArray(new Bitmap[arrBitmaps.size()]) );


        /* Now, attempt to sync with Drive */
        if (canConnectToDrive()) {

        }

    }
    /**
     * Gets you the formatted string of Blue Alliance data for a particular team. If we already have data on this team in
     * /MCMergeManager/<TeamName>/<EventName>/thebluealliance.json then it will return that data, otherwise it will do an internet fetch and store the results in the file for future offline searches.
     *
     * This should trigger on searching for a team on the Team Info tab.
     */
    private ArrayList<String> downloadArray;
    private String joinedDownload;
    private String joined2015;
    private String joined2014;
    private String joined2013;
    private String joined;
    public String getBlueAllianceDataForTeam(int teamNumber) {
        store2015 = new ArrayList<>();
        store2014 = new ArrayList<>();
        store2013 = new ArrayList<>();
   /*     if (we already have data on them in the json file) {
            String data = extract the data from json format and build a pretty-print string
            return data; //TODO
        }*/

        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            try {
                downloadArray = new ArrayList<>();


                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
                    boolean done = false;

                    String[] parts = SP.getString("Download Data",null).split("\\.");

                    for (int g = 0; g < parts.length; g++) {



                        if (parts[g].contains(Integer.toString(teamNumber))) {
                            Log.d("Get Data", "Team Data Found");
                            downloadArray.add(parts[g]);

                            done = true;


                        }
                    }
                    if (!done) {

                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(mActivity, "No Internet and this team is not downloaded!", Toast.LENGTH_LONG).show();
                            }
                        });


                    } else {
                        String returning = TextUtils.join("\n", downloadArray);
                        String regex =  "\\b" + teamNumber;
                        returning = returning.replaceAll(regex, "");


                        return returning;
                    }


                }
             catch (java.lang.NullPointerException e) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mActivity, "No Internet and nothing has been downloaded!", Toast.LENGTH_LONG).show();
                    }
                });

            }

            // if (! file exitst(/MCMergeManager/<TeamName>/<EventName>/thebluealliance.json) )
        } else {
            ArrayList<String> comps2015 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2015/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2015 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2015/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2015.size(); i++) {
                String joe = comps2015.get(i);

                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2015" + comps2015.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {


                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        combine = "2015 " + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;

                        store2015.add(combine);
                        joined2015 = TextUtils.join("\n ", store2015);




                    }
                }


            }


            ArrayList<String> comps2014 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2014/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2014 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2014/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2014.size(); i++) {
                String joe = comps2014.get(i);

                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2014" + comps2014.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {
                    Log.d("p", test.get(p));

                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        combine = "2014 " + compsName2014.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;

                        store2014.add(combine);
                        joined2014 = TextUtils.join("\n ", store2014);




                    }
                }

            }


            ArrayList<String> comps2013 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2013/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            ArrayList<String> compsName2013 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + teamNumber + "/2013/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2013.size(); i++) {
                String joe = comps2013.get(i);

                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2013" + comps2013.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {


                    if (test.get(p).equals(Integer.toString(teamNumber))) { // Or use equals() if it actually returns an Object.
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        combine = "2013 " + compsName2013.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount;

                        store2013.add(combine);
                        joined2013 = TextUtils.join("\n ", store2013);





                    }

                }
            }
            joined = joined2015 + "\n" + joined2014 + "\n" + joined2013;
            //  }

        }
        return joined;
    }
    /**
     * This deletes a photo from the Drive.
     *
     * It loops over all photos for that team, camparing them against the supplied one with Bitmap.sameAs(Bitmap).
     */
    public void deletePhoto(int teamNumber, Uri photoURI) {
        // TODO
    }
/**
 * Call a BlueAlliance URL and a key and it will give you the parsed JSON back
 */
public String keyedJson;

    public String keyData;
    public String getBlueAllianceData( final String key, final String url) {



                BlueAllianceData blue = new BlueAllianceData();
                try {
                    JSONObject issueObj = new JSONObject( blue.readUrl(url) );

                    keyedJson =  issueObj.getString(key);


                } catch (Exception e) {
                    e.printStackTrace();
                }




        return keyedJson;

        }


    public String getBlueAllianceDataArrayAsString( final String key,final String url) {


            BlueAllianceData blue = new BlueAllianceData();
            try

            {
                ArrayList<String> array1 = new ArrayList<String>();
                JSONArray issueArray = new JSONArray(blue.readUrl(url));

                for (int i = 0; i < issueArray.length(); i++) {

                    JSONObject jsonobject = issueArray.getJSONObject(i);
                    keyData = jsonobject.getString(key);
                    array1.add(keyData);
                    String joined = TextUtils.join(", ", array1);

                    keyedJson = joined;
                }
            }

            catch(
            Exception e
            )

            {
                e.printStackTrace();
            }


        return keyedJson;
    }
    private  boolean threadFinish = false;
    public ArrayList<String> getBlueAllianceDataArrayAsArray( final String key, final String url) {
        ArrayList<String> array1 = new ArrayList<String>();


                BlueAllianceData blue = new BlueAllianceData();
                try {

                    JSONArray issueArray = new JSONArray(blue.readUrl(url));

                    for (int i = 0; i < issueArray.length(); i++) {

                        JSONObject jsonobject = issueArray.getJSONObject(i);
                        keyData = jsonobject.getString(key);
                        array1.add(keyData);
                        String joined = TextUtils.join(" ", array1);

                        keyedJson = joined;
                        threadFinish = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            return array1;

    }
    public ArrayList<String> getBlueAllianceDataDoubleArrayAsArray( final int key, final String url) {
        ArrayList<String> array1 = new ArrayList<String>();
                BlueAllianceData blue = new BlueAllianceData();
                try {

                    JSONArray issueArray = new JSONArray(blue.readUrl(url));

                    for (int i = 0; i < issueArray.length(); i++) {

                        JSONArray halfArray = issueArray.getJSONArray(i);
                        keyData = halfArray.getString(key);
                        array1.add(keyData);
                        String joined = TextUtils.join(", ", array1);

                        keyedJson = joined;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }



        return array1;
    }
    /**
     * Convenience method that calls deletePhoto(teamNumber, photo), and then calls requester's updatePhotos(Bitmap[])
     * with the new set of photos for that team.
     */
    public void deletePhate(int teamNumber, Bitmap photo, PhotoRequester requester) {
        // TODO
    }
    public void saveToTeamFolder(int teamNumber, String item) {
// TODO
    }
    /**
     * Fetches Blue Alliance data for all teams who are registered for a particular event and saves the data in
     * /MCMergeManager/<TeamName>/<EventName>/thebluealliance.json
     * so that the data is still accessible later, even if there's no internet connection later.
     *
     * This should trigger on a button, or maybe a settin+gs-menu item in Settings > Google Drive (we can rename that to something more appropriate).
     */
    public String eventName;
    public void downloadBlueAllianceDataForEvent(String eventName, String year,final ProgressBar progressBar, final AlertDialog dialog,final TextView team, int dataYear) {
        this.eventName = eventName;
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet

            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mActivity, "You expect to download stuff without internet...", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
            return;
        }
        store = new ArrayList<>();

        //Get all teams at event
        final ArrayList<String> downloadedCompArray = getBlueAllianceDataArrayAsArray("team_number", "http://www.thebluealliance.com/api/v2/event/"+ year + eventName + "/teams?X-TBA-App-Id=frc2706:mergemanager:v01/");
        String joined = TextUtils.join(",", downloadedCompArray);

        for (
                //For each team
                int o = 0;
                o < downloadedCompArray.size(); o++)

        {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Log.v("ProgressBar", "Current Size: " + progressBar.getProgress());
                    progressBar.setProgress(progressBar.getProgress() + (int)(100.0 / downloadedCompArray.size()));
                }
            });
            boolean looped = false;
            String joe = downloadedCompArray.get(o);
            //Get their nickname
            downloadedNickname = getBlueAllianceData("nickname", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "?X-TBA-App-Id=frc2706:mergemanager:v01/");
            //Get Competitions they went to
             ArrayList<String> comps2015 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
           //Get Names of Competitions they went to
            ArrayList<String> compsName2015 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");
            for (int i = 0; i < comps2015.size(); i++) {
                //For each competition


                String joey = comps2015.get(i);

                //TODO


                //Get their ranking
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/" + dataYear + comps2015.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {


                    if (test.get(p).equals(Integer.toString(Integer.parseInt(joe)))) {
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        combine = dataYear + " "/* //TODO year */ + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount + " " + joe;


                            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
                            SharedPreferences.Editor editor = SP.edit();
                            boolean done = false;

                            String[] parts = SP.getString("Download Data", "").split("\\.");

                            for (int g = 0; g < parts.length; g++) {
                                if (combine.equals(parts[g])) {
                                    Log.d("Download Data", "Found Duplicate");
                                    if(!looped)
                                    done = true;

                                    break;

                                }
                            }
                            if (!done) {

                                editor.putString("Download Data",SP.getString("Download Data","") + combine + ".").apply();
                                if (comps2015.size() > 1) {
                                    looped = true;
                                }
                            }
                        }
                    }
                }
            }

        Log.i("Download Data","Download Finished.");
        }




public String combine;
public ArrayList<String> store2015;
    public ArrayList<String> store2014;
    public ArrayList<String> store2013;
    public ArrayList<String> store;
public String downloadedNickname;
}
