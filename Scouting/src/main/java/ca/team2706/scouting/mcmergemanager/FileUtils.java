package ca.team2706.scouting.mcmergemanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import ca.team2706.scouting.mcmergemanager.datamodels.BallPickup;
import ca.team2706.scouting.mcmergemanager.datamodels.BallShot;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchData;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.datamodels.ScalingTime;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

/**
 * This is a helper class to hold common code for accessing shared scouting data files.
 * This class takes care of keeping a local cache, syncing to Google Drive, and (eventually) sharing with other bluetooth-connected devices also running the app.
 * <p/>
 * Created by Mike Ounsworth
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtils implements ConnectionCallbacks, OnConnectionFailedListener {

    /**
     * Grabbed this constant from a tutorial ... seems kinda hacky to be hard-coding API codes, shouldn't there be a place I can reference this??
     */
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static boolean mCanConnect = false;
    // This is not being used _yet_, but is here for future integration
    private static boolean mHasUnsyncedMatchScoutingData = false;
    private static Activity mActivity;
    private static String mRemoteToplevelFolderName;
    private static String mRemoteTeamFolderName;
    private static String mRemoteEventFolderName;
    private static String mRemoteTeamPhotosFolderName;
    private static DriveId mDriveIdTeamPhotosFolder;
    private static String mLocalToplevelFilePath;
    private static String mLocalTeamFilePath;
    private static String mLocalEventFilePath;
    private static String mLocalTeamPhotosFilePath;
    /**
     * A pointer to myself so that the nested classes can use my ConnectionCallbacks
     **/
    FileUtils m_me;
    /**
     * If the user denys the app access to their drive, it asks over and over again and they don't have the chance to go to settings.
     */
    private boolean mRequestedAccessAlready = false;
    private boolean mCheckDriveFilesOnNextConnect = false;
    private GoogleApiClient mGoogleApiClient;
    // TODO: this variable needs a better name.
    // Also, it seems that you're setting it, but never reading it, is it even doing anything??
    private boolean threadFinish = false;

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

        mLocalToplevelFilePath   = Environment.getExternalStorageDirectory().getPath()
                                    +"/"+ mRemoteToplevelFolderName;
        mLocalTeamFilePath       = mLocalToplevelFilePath + "/" + mRemoteTeamFolderName;
        mLocalEventFilePath      = mLocalTeamFilePath + "/" + mRemoteEventFolderName;
        mLocalTeamPhotosFilePath = mLocalEventFilePath + "/" + mRemoteTeamPhotosFolderName;

        checkLocalFileStructure();
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

    /**
     * Checks if we have the permission read / write to the internal USB STORAGE,
     * requesting that permission if we do not have it.
     *
     * @return whether or not we have the STORAGE permission.
     */
    public boolean canWriteToStorage() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);

            // check if they clicked Deny
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }


        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);

            // check if they clicked Deny
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
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
        } else {
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
    }

    /**
     * Try logging in to the Google Drive useng the saved account.
     * If that works, make sure the file structure exists and create it if it does not.
     */
    void checkDriveConnectionAndFiles() {
        // First, test to see if the credentials are even valid
        reset_mGoogleApiClient();

        if (mGoogleApiClient == null) {
            mCanConnect = false;
            return;
        } else {
            // force a full check of the files at the next connect
            mCheckDriveFilesOnNextConnect = true;
            mGoogleApiClient.connect();
        }
    }

    /**
     * This checks the local file system for the appropriate files and folders, creating them if they
     * are missing.
     * <p/>
     * The file structure is:
     * MCMergeManager/
     *  - team_name/
     *      - Team Photos/
     *      - event/
     *          - matchScoutingData.csv
     */
    public void checkLocalFileStructure() {
        // check for STORAGE permission
        if (!canWriteToStorage())
            return;

        makeDirectory(mLocalToplevelFilePath);
        makeDirectory(mLocalTeamFilePath);
        makeDirectory(mLocalEventFilePath);
        makeDirectory(mLocalTeamPhotosFilePath);
    }

    private void makeDirectory(String directoryName) {

        Log.d(mActivity.getResources().getString(R.string.app_name), "Making directory: "+directoryName);


        File file = new File(directoryName);
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
            (new CheckDriveFilesThread()).start();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        if (mRequestedAccessAlready)
            return;

        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        try {
            mRequestedAccessAlready = true;
            connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(mActivity.getResources().getString(R.string.app_name), "Exception while starting resolution activity", e);
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
     * Call this when you want to append new mactch scouting data to the shared file.
     *
     * The following is the proceedure for syncronizing `matchScoutingData.csv` with Drive:
     *
     * It will first append the new lines to a local file `matchScoutingData_UNSYNCED.csv`.
     * If drive in unavailable, that is all, if Drive is available then it will spawn a background thread to:
     *
     * 1. Pull the most recent version of `matchScoutingData.csv` from Drive.
     *
     * 2. Check if the file `matchScoutingData.LOCK` exists on Drive.
     * 2b. If Yes, it means another process is currently uploading data. Set mHasUnsyncedMatchScoutingData = true; and return.
     * 2b. If No, create a file on Drive named`matchScoutingData.LOCK` so that no other process tries to write to the file at the same time.
     *
     * 3. Append the lines in `matchScoutingData_UNSYNCED.csv` to the end of `matchScoutingData.csv`
     * and leave `matchScoutingData_UNSYNCED.csv` as as empty file.
     *
     * 3b. We should scan for duplicate lines here, but I'm not 100% sure how to do that.
     * What if two people scout the same team in the same match, is it right to just throw out one at random?
     *
     * 4. Upload `matchScoutingData.csv` to Drive.
     *
     * 5. Delete `matchScoutingData.LOCK` from drive.
     *
     * 6. Set mHasUnsyncedMatchScoutingData = false;
     */
    public void syncMatchData() {

    }


    /**
     * Data format:
     * "matchNo<int>,teamNo<int>,isSpyBot<boolean>,reached<boolean>,{autoDefenseBreached<int>;...},{{autoBallShot_X<int>,autoBallShot_Y<int>,autoBallShot_time<.2double>,autoBallshot_which<int>};...},{teleopDefenseBreached<int>;...},{{teleopBallShot_X<int>,teleopBallShot_Y<int>,teleopBallShot_time<.2double>,teleopBallshot_which<int>};...},timeDefending<,2double>,{{ballPickup_selection<int>;ballPickup_time<,2double>};...},{{scaling_time<.2double>;scaling_comelpted<int>};...},notes<String>,challenged<boolean>,timeDead<int>"
     *
     * Or, in printf / format strings:
     * "%d,%d,%b,%b,{%d;...},{{%d;%d;%.2f;%d};...},{%d;...},{{%d;%d;%.2f;%d};...},%,2f,{{%d,%,2f};...},{{%.2f;%d};...},%s,%b,%d"
     */
    public void appendToMatchDataFile(MatchData.Match match) {

        /** build the string **/
        StringBuilder sb = new StringBuilder();

        /** Pre-game **/
        sb.append( String.format("%d,%d,", match.preGame.matchNumber, match.preGame.teamNumber) );



        /** Auto Mode **/

        sb.append( String.format("%b,%b,", match.autoMode.isSpyBot, match.autoMode.reachedDefense) );

        // a list of defensesBreached
        // {defenseBreached<int>;defenseBreached<int>;...}
        sb.append("{");
        for(int i=0; i<match.autoMode.defensesBreached.size(); i++) {
            sb.append(match.autoMode.defensesBreached.get(i)+";");

            if (i < match.autoMode.defensesBreached.size() - 1 )
                sb.append(";");
            else
                sb.append("},");
        }

        // a list of BallShots
        // {{ballShot_X<int>,ballShot_Y<int>,ballShot_time<.3double>,ballshot_which<int>};...}
        sb.append("{");
        for(int i=0; i<match.autoMode.ballsShot.size(); i++) {
            BallShot ballShot = match.autoMode.ballsShot.get(i);

            sb.append(String.format("{%d;%d;%.2f;%d}",ballShot.x,ballShot.y,ballShot.shootTime,ballShot.whichGoal));

            if (i < match.autoMode.ballsShot.size() - 1)
                sb.append(";");
            else
                sb.append("},");
        }



        /** Teleop Mode **/

        // a list of defensesBreached
        // {defenseBreached<int>;defenseBreached<int>;...}
        sb.append("{");
        for(int i=0; i<match.teleopMode.defensesBreached.size(); i++) {
            sb.append(match.teleopMode.defensesBreached.get(i)+";");

            if (i < match.teleopMode.defensesBreached.size() - 1 )
                sb.append(";");
        }
        sb.append("},");

        // a list of BallShots
        // {{ballShot_X<int>,ballShot_Y<int>,ballShot_time<.3double>,ballshot_which<int>};...}
        sb.append("{");
        for(int i=0; i<match.teleopMode.ballsShot.size(); i++) {
            BallShot ballShot = match.teleopMode.ballsShot.get(i);

            sb.append(String.format("{%d;%d;%.2f;%d}",ballShot.x,ballShot.y,ballShot.shootTime,ballShot.whichGoal));

            if (i < match.teleopMode.ballsShot.size() - 1)
                sb.append(";");
        }
        sb.append("},");

        sb.append( String.format("%.2f,",match.teleopMode.timeDefending));

        // Ball Pickup
        // {{%d,%,2f};...}
        sb.append("{");
        for(int i=0; i<match.teleopMode.ballsPickedUp.size(); i++) {
            BallPickup pickup = match.teleopMode.ballsPickedUp.get(i);

            sb.append( String.format("{%d;%.2f}", pickup.selection, pickup.time));

            if (i < match.teleopMode.ballsPickedUp.size() - 1)
                sb.append(";");
            else
                sb.append("},");
        }


        // Scaling Times
        // {{%.2f;%d};...}
        sb.append("{");
        for(int i=0; i<match.teleopMode.scalingTower.size(); i++) {
            ScalingTime scale = match.teleopMode.scalingTower.get(i);

            sb.append( String.format("{%.2f;%d}", scale.time, scale.completed));

            if (i < match.teleopMode.scalingTower.size() - 1)
                sb.append(";");
        }
        sb.append("},");


        /** Post-Game **/

        // since commas, semi-colons, braces, and <enter> are all special characters for the text file, let's rip those out just to be safe.
        String cleanedNotes = match.postGame.notes .replaceAll(",","")
                .replaceAll(";","")
                .replaceAll("\\{","")
                .replaceAll("\\}","")
                .replaceAll("\n","");
        sb.append(cleanedNotes+",");

        sb.append( String.format("%b,",match.postGame.challenged) );
        sb.append( String.format("%d",match.postGame.timeDead) );

        sb.append("\n");




        String outFileName = mLocalEventFilePath +"/"+ mActivity.getResources().getString(R.string.matchScoutingDataFileName);

        Log.d(mActivity.getResources().getString(R.string.app_name), "Saving data to file: "+outFileName);

        File outfile = new File(outFileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
            bw.append( sb.toString() );
            bw.flush();
            bw.close();
        } catch (IOException e) {

        }
    }

    /**
     * Add a Note for a particular team.
     * <p/>
     * The intention of Notes is for the drive team to be able to read them quickly.
     * They should be short and fit on one line, so they will be truncated to 80 characters.
     */
    public void addNote(int teamNumber, String note) {
        // TODO
    }

    /**
     * Retrieves all the notes for a particular team.
     *
     * @param teamNumber the team number you want notes for.
     * @return All the notes for this team concatenated into a single string, with each note beginning with a bullet "-",
     * and ending with a newline (except for the last one).
     */
    public String getNotesForTeam(int teamNumber) {
        // TODO

        return "";
    }

    /**
     * If we can connect to Drive, fork a background thread te syncronize photos for the provided
     * team with Google Drive.
     * <p/>
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
     * <p/>
     * Note: calling this in a loop is much less efficient that calling {@link #syncAllTeamPhotos()}
     *
     * @param teamNumber The team whos photos you want to sync with Drive
     */
    public void syncOneTeamsPhotos(int teamNumber, PhotoRequester requester) {
        if (!canConnectToDrive())
            return;

        (new TeamPhotoSyncerThread(teamNumber, requester)).start();
    }


    /**
     * If we can connect to Drive, fork a background thread te syncronize photos for all teams.
     * <p/>
     * Because of the 10 request / second limit to the free Google Drive API subscription,
     * syncing all the team photos can take a while.
     * syncTeamPhotos() will spawn a new background thread to synchronize the photos.
     * It will pop up a Toast when it's done syncronizing.
     */
    public void syncAllTeamPhotos() {
        checkDriveConnectionAndFiles();
        if (!canConnectToDrive())
            return;

        // -1 tells TeamPhotoSyncerThread to sync all teams.
        (new TeamPhotoSyncerThread(-1, null)).start();
    }

    /**
     * This method takes care of saving a team photo to the local cache and syncing it to Drive if Drive is available.
     *
     * @param teamNumber The team number, which will be used as the folder for the photo.
     *                   Does not have to be a team in the matchResults file,, the photo will get saved regardless.
     * @param photo      The photo to get saved.
     */
    public void saveTeamPhoto(int teamNumber, Bitmap photo) {
        // check for STORAGE permission
        if (!canWriteToStorage())
            return;


        // TODO

    }

    public String getTeamPhotoPath(int teamNumber) {

        String photoName = teamNumber + "_" + new Date().getTime() + ".jpg";
        return mLocalTeamPhotosFilePath + "/" + photoName;
    }

    /**
     * This deletes a photo from the Drive.
     * <p/>
     * It loops over all photos for that team, camparing them against the supplied one with Bitmap.sameAs(Bitmap).
     */
    public void deletePhoto(int teamNumber, Uri photoURI) {
        // TODO
    }

    /**
     * We take photos by calling the system camera app and telling it where to save the photo.
     * This function will provide a file name in the correct location in the Team Photos/teamNumber directory.
     * <p/>
     * If a photos directory does not already exist for this team, this function will create one.
     *
     * @param teamNumber
     * @return Can return NULL if we do not have permission to write to STORAGE.
     */
    public Uri getNameForNewPhoto(int teamNumber) {
        // check if a photo folder exists for this team, and create it if it does not.
        String dir = mLocalTeamPhotosFilePath + "/" + teamNumber + "/";
        File file = new File(dir);

        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();

            // create it
            file.mkdir();
        }

        String fileName = dir + teamNumber + "_" + (new Date().getTime()) + ".jpg";
        return Uri.fromFile(new File(fileName));
    }

    /**
     * This method will return you all locally-cached photos for the requested team.
     * It will then spawn a new background thread, and if Drive is available, it will sync the photos
     * for the requested team only and then notify the requesting activity that it has new photos.
     * <p/>
     * Since syncing photos with Drive can take a few seconds, FileUtils.loadTeamPhotos() will immediately call
     * the PhotoRequester's updatePhotos(Bitmap[]) with whatever photos are locally cached for that team,
     * and if FileUtils is able to connect to Drive then it will call it again after performing the sync.
     *
     * @param teamNumber The team whos photos we want to load.
     * @param requester  The activity that is requesting the photos. This activity's .updatePhotos(Bitmap[])
     *                   will be called with the loaded photos.
     * @return It will call requester.updatePhotos(Bitmap[]) with an array of Bitmaps containing all
     * photos for that team, or a zero-length array if no photos were found for that team.
     */
    public void getTeamPhotos(int teamNumber, PhotoRequester requester) {
        // check for STORAGE permission
        if (!canWriteToStorage())
            return;

        /* First, return the requester any photos we have on the local drive */

        File photosDir = new File(mLocalTeamPhotosFilePath + "/" + teamNumber);

        // check if that folder exists
        if (!photosDir.isDirectory()) {
            // we have no photos for this team
            requester.updatePhotos(new Bitmap[0]);
            return;
        }


        File[] listOfFiles = photosDir.listFiles();
        ArrayList<Bitmap> arrBitmaps = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                // BitmapFactory will return `null` if the file cannot be parsed as an image, so no error-checking needed.
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (bitmap != null)
                    arrBitmaps.add(bitmap);
            }
            // else: if it's not a file, then what is it???? .... skip I guess
        }
        requester.updatePhotos(arrBitmaps.toArray(new Bitmap[arrBitmaps.size()]));


        /* Now, attempt to sync with Drive */
        if (canConnectToDrive()) {
            (new TeamPhotoSyncerThread(teamNumber, requester)).start();
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
        String joined2015 = "";
        String joined2014 = "";
        String joined2013 = "";
        String joined = "";

        String combine;
        ArrayList<String> store2015 = new ArrayList<>();
        ArrayList<String> store2014 = new ArrayList<>();
        ArrayList<String> store2013 = new ArrayList<>();

        /* if (we already have data on them in the json file) {
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

                String[] parts = SP.getString("Download Data", null).split("\\.");
                for (int g = 0; g < parts.length; g++) {
                    if (parts[g].contains(Integer.toString(teamNumber))) {
                        Log.d(mActivity.getResources().getString(R.string.app_name), "Team Data Found");
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
                    String regex = "\\b" + teamNumber;
                    returning = returning.replaceAll(regex, "");

                    return returning;
                }
            } catch (java.lang.NullPointerException e) {
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
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/2014" + comps2014.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {

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
     * Call a BlueAlliance URL and a key and it will give you the parsed JSON back
     */
    public String getBlueAllianceData(final String key, final String url) {
        String keyedJson = "";
        BlueAllianceData blue = new BlueAllianceData();
        try {
            JSONObject issueObj = new JSONObject(blue.readUrl(url));
            keyedJson = issueObj.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyedJson;
    }

    /**
     * <Some Description>
     */
    public String getBlueAllianceDataArrayAsString(final String key, final String url) {
        String keyedJson = "";

        BlueAllianceData blue = new BlueAllianceData();
        try {
            ArrayList<String> array1 = new ArrayList<>();
            JSONArray issueArray = new JSONArray(blue.readUrl(url));

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
    public ArrayList<String> getBlueAllianceDataDoubleArrayAsArray(final int key, final String url) {
        ArrayList<String> array1 = new ArrayList<>();
        BlueAllianceData blue = new BlueAllianceData();
        try {
            JSONArray issueArray = new JSONArray(blue.readUrl(url));
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

    /**
     * This will return both the matchResultsData, and the matchScoutingData to the DataRequester.
     * <p/>
     * Since syncing with Drive can take a few seconds, FileUtils will immediately call
     * the activity's updateData(matchResultsDataCSV, matchScoutingDataCSV) with whatever data is locally cached.
     * If FileUtils is able to connect to Drive then it will call it again after performing the sync.
     */
    public void getMatchData(DataRequester requester) {
        // TODO
    }

    public static void fetchMatchScheduleAndResults(final DataRequester requester) {

        // check if we have internet connectivity
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            return;
        }

        new Thread()
        {
            public void run() {
                // Connor's BlueAllianceData class seems to be generic enough that I can reuse it here

                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String TBA_event = SP.getString(mActivity.getResources().getString(R.string.PROPERTY_googledrive_event), "<Not Set>");
                BlueAllianceData bad = new BlueAllianceData();
                String scheduleStr;
                try {
                    scheduleStr = bad.readUrl("http://www.thebluealliance.com/api/v2/event/"+TBA_event+"/matches?X-TBA-App-Id=frc2706:mergemanager:v01/");
                } catch (Exception e) {
                    Log.e(mActivity.getResources().getString(R.string.app_name), "Error fetching schedule data from thebluealliance. "+e.getStackTrace());
                    return;
                }

                MatchSchedule schedule = new MatchSchedule(scheduleStr);

                // return data to the requester
                requester.updateMatchSchedule(schedule);
            }
        }.start();


        // TODO should maybe do this is a background thread.


    }

    /**
     * Fetches Blue Alliance data for all teams who are registered for a particular event and saves the data in
     * /MCMergeManager/<TeamName>/<EventName>/thebluealliance.json
     * so that the data is still accessible later, even if there's no internet connection later.
     * <p/>
     * This should trigger on a button, or maybe a settin+gs-menu item in Settings > Google Drive (we can rename that to something more appropriate).
     */
    public void downloadBlueAllianceDataForEvent(String eventName, String year, final ProgressBar progressBar, final AlertDialog dialog, int dataYear) {
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    Log.v(mActivity.getResources().getString(R.string.app_name), "ProgressBar Current Size: " + progressBar.getProgress());
                    progressBar.setProgress(progressBar.getProgress() + (int) (100.0 / downloadedCompArray.size()));
                }
            });
            boolean looped = false;
            String joe = downloadedCompArray.get(o);

            //Get their nickname
            String downloadedNickname = getBlueAllianceData("nickname", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "?X-TBA-App-Id=frc2706:mergemanager:v01/");
            // [Mike] ^^^ This variable is never used, that means it's doing a slow internet call that we never use...??
            // We should either save and display that, or get rid of it altogether.

            //Get Competitions they went to
            ArrayList<String> comps2015 = getBlueAllianceDataArrayAsArray("event_code", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");

            //Get Names of Competitions they went to
            ArrayList<String> compsName2015 = getBlueAllianceDataArrayAsArray("name", "http://www.thebluealliance.com/api/v2/team/frc" + joe + "/" + dataYear + "/events?X-TBA-App-Id=frc2706:mergemanager:v01/");

            for (int i = 0; i < comps2015.size(); i++) {
                //For each competition

                //TODO
                // [Mike] ^^^ ??? What's that empty TODO about??

                //Get their ranking
                ArrayList<String> test = getBlueAllianceDataDoubleArrayAsArray(1, "http://www.thebluealliance.com/api/v2/event/" + dataYear + comps2015.get(i) + "/rankings?X-TBA-App-Id=frc2706:mergemanager:v01/");
                for (int p = 0; p < test.size(); p++) {
                    if (test.get(p).equals(Integer.toString(Integer.parseInt(joe)))) {
                        // Found at index i. Break or return if necessary.

                        int compAmount = test.size();
                        compAmount -= 1;
                        String combine = dataYear + " "/* //TODO year */ + compsName2015.get(i) + " seeded " + Integer.toString(p) + "/" + compAmount + " " + joe;

                        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        SharedPreferences.Editor editor = SP.edit();
                        boolean done = false;

                        String[] parts = SP.getString("Download Data", "").split("\\.");

                        for (int g = 0; g < parts.length; g++) {
                            if (combine.equals(parts[g])) {
                                Log.d(mActivity.getResources().getString(R.string.app_name), "Download Data: Found Duplicate");
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

        Log.i(mActivity.getResources().getString(R.string.app_name), "Download Data: Download Finished.");
    }

    /**
     * <Some Description>
     */
    public ArrayList<String> getBlueAllianceDataArrayAsArray(final String key, final String url) {
        ArrayList<String> array1 = new ArrayList<>();
        BlueAllianceData blue = new BlueAllianceData();

        try {
            JSONArray issueArray = new JSONArray(blue.readUrl(url));
            for (int i = 0; i < issueArray.length(); i++) {
                JSONObject jsonobject = issueArray.getJSONObject(i);
                String keyData = jsonobject.getString(key);
                array1.add(keyData);

                threadFinish = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array1;
    }

    /**
     * This class defines a background process for checking / setting the file structure on Google Drive.
     * It will set the appropriate members in MainActivity when it completes.
     * <p/>
     * Directory structure:
     * <p/>
     * MCMergeManager/
     * - team_name/
     * - Team Photos/
     * - event/
     * <p/>
     * Note: run() will disconnect the GoogleApiClient when it exists.
     */
    private class CheckDriveFilesThread extends Thread {

        public CheckDriveFilesThread() {
        }

        @Override
        public void run() {
            Log.i(mActivity.getResources().getString(R.string.app_name), "Starting the Drive folder sync");

            // Check if the file structure exists, and create it if it doesn't
            DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            DriveFolder topLevelfolder = checkOrCreateRemoteFolder(mGoogleApiClient, rootFolder, mRemoteToplevelFolderName);
            DriveFolder teamFolder = checkOrCreateRemoteFolder(mGoogleApiClient, topLevelfolder, mRemoteTeamFolderName);
            DriveFolder eventFolder = checkOrCreateRemoteFolder(mGoogleApiClient, teamFolder, mRemoteEventFolderName);
            DriveFolder teamPhotosFolder = checkOrCreateRemoteFolder(mGoogleApiClient, eventFolder, mRemoteTeamPhotosFolderName);
            if (teamPhotosFolder != null) {
                mDriveIdTeamPhotosFolder = teamPhotosFolder.getDriveId();
            }

            Log.i(mActivity.getResources().getString(R.string.app_name), "Drive folder sync finished.");
            mGoogleApiClient.disconnect();
        }

        /**
         * Helper just to avoid copy&paste'ing code.
         * <p/>
         * Note: since this contains blocking Drive API calls, this will crash if you try to call it from the main thread,
         * it can only be called from other threads.
         *
         * @return null if the folder can not be found.
         **/
        private DriveFolder checkOrCreateRemoteFolder(GoogleApiClient googleApiClient, DriveFolder rootFolder, String folderName) {
            // check for STORAGE permission and client connected
            if (!canWriteToStorage() || googleApiClient == null || !googleApiClient.isConnected())
                return null;

            Query query = new Query.Builder().addFilter(Filters.and(
                    Filters.eq(SearchableField.TITLE, folderName),
                    Filters.contains(SearchableField.MIME_TYPE, "folder"))).build();

            DriveApi.MetadataBufferResult result = rootFolder.queryChildren(mGoogleApiClient, query).await();

            if (!result.getStatus().isSuccess()) {
                Log.e(mActivity.getResources().getString(R.string.app_name), "Cannot query folders in the root of Google Drive.");
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
                Log.e(mActivity.getResources().getString(R.string.app_name), "Error while trying to create the folder \"" + folderName + "\"");
                return null;
            }

            return result1.getDriveFolder();
        }
    }

    private class TeamPhotoSyncerThread extends Thread {
        private int mTeamNumber;
        private PhotoRequester mRequester;

        /**
         * teamNumber can = -1, and requester can be null. This is the behaviour of run() depending on whether the parameters are null:
         * <p/>
         * teamNumber is set, requester is set :
         * The photos for that team will be synced and returned to requester.
         * <p/>
         * teamNumber == -1, requester is set :
         * All team photos will be synced, nothing will be returned to the requester
         * (because it it meant to enly retun photos for a simgle team).
         * <p/>
         * teamNumber is set, requester == null :
         * The photos for that team will be synced, nothing is returned.
         * <p/>
         * teamNumber == -1, requester == null :
         * All team photos are synced, nothing is returned.
         * <p/>
         * Note: Since this could be a very long-running thread (several minutes) it created its own GoogleApiClient
         * so as not to interfere with other threads.
         */
        public TeamPhotoSyncerThread(int teamNumber, PhotoRequester requester) {
            mTeamNumber = teamNumber;
            mRequester = requester;
        }

        @Override
        public void run() {
            // check for STORAGE permission
            if (!canWriteToStorage())
                return;

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String driveAccount = SP.getString(mActivity.getResources().getString(R.string.PROPERTY_googledrive_account), "<Not Set>");

            if (driveAccount.equals("<Not Set>"))
                return;
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .setAccountName(driveAccount)
                    .addConnectionCallbacks(m_me)
                    .addOnConnectionFailedListener(m_me)
                    .build();

            googleApiClient.blockingConnect();
            if (!googleApiClient.isConnected())
                return;


            if (mTeamNumber <= 0) {
                syncPhotosForTeam(googleApiClient, mTeamNumber);
            } else {
                mRequester = null;

                // TODO: loop over all folders in the photos dir
            }
        }

        private void syncPhotosForTeam(GoogleApiClient googleApiClient, int teamNumber) {
            if (mTeamNumber >= 0) {

                Log.i(mActivity.getResources().getString(R.string.app_name),
                        "Beginning photo sync for team " + teamNumber);

                // get the list of local files

                File photosDir = new File(mLocalTeamPhotosFilePath + "/" + teamNumber);
                // TODO: generalize this to also handle syncAllPhotos

                /****** get the list of local files ******/

                // check if that folder exists
                if (!photosDir.isDirectory()) {
                    // we have no photos for this team
                    if (mRequester != null)
                        mRequester.updatePhotos(new Bitmap[0]);
                    googleApiClient.disconnect();
                    return;
                }

                File[] listOfFiles = photosDir.listFiles();
                ArrayList<String> arrLocalFiles = new ArrayList<>();
                for (File file : listOfFiles) {
                    // make sure it's an image file
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    if (bitmap != null) {
                        arrLocalFiles.add(file.getPath());
                    }
                    // else: if it's not a file, then what is it???? .... skip I guess
                }


                // Navigate to the correct folder - there has to be a more efficient way to do this

                DriveFolder rootFolder = Drive.DriveApi.getFolder(googleApiClient, mDriveIdTeamPhotosFolder);
                Log.i(mActivity.getResources().getString(R.string.app_name), "Local Files: " + arrLocalFiles);

                Log.i(mActivity.getResources().getString(R.string.app_name),
                        "Drive rootFolder: " + rootFolder.getMetadata(googleApiClient).await().getMetadata().getTitle());

                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, "" + teamNumber))
                        .build();

                DriveApi.MetadataBufferResult result = rootFolder.queryChildren(googleApiClient, query).await();

                DriveFolder teamPhotosFolder = null;
                for (Metadata m : result.getMetadataBuffer()) {
                    teamPhotosFolder = m.getDriveId().asDriveFolder();
                }
                result.getMetadataBuffer().close();


                ArrayList<PathAndDriveId> arrRemoteFiles = new ArrayList<>();
                if (teamPhotosFolder != null) {
                    query = new Query.Builder()
                            .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.photo"))
                            .build();
                    result = teamPhotosFolder.queryChildren(googleApiClient, query).await();

                    for (Metadata m : result.getMetadataBuffer()) {
                        arrRemoteFiles.add(new PathAndDriveId(
                                mRemoteToplevelFolderName + "/" + mRemoteTeamFolderName + "/" + mRemoteTeamPhotosFolderName + "/" + mTeamNumber + "/" + m.getTitle(),
                                m.getDriveId(),
                                m.getTitle()
                        ));
                    }
                    result.getMetadataBuffer().close();
                }
                Log.i(mActivity.getResources().getString(R.string.app_name), "Remote Files: " + arrRemoteFiles);

                // Remove files that are in both lists - these don't need to be synced.
                for (PathAndDriveId remoteFile : arrRemoteFiles) {
                    String remotePath = remoteFile.path;
                    if (arrLocalFiles.contains(remotePath)) {
                        arrRemoteFiles.remove(remoteFile);
                        arrLocalFiles.remove(remotePath);
                    }
                }


                /****** Download any Files we're missing locally ******/

                for (PathAndDriveId remoteFile : arrRemoteFiles) {
                    // the Drive API actually makes a local cache of the file, so let's copy the contents into our app's file structure
                    DriveApi.DriveContentsResult fileResult = remoteFile.driveId.asDriveFile().open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();

                    if (!fileResult.getStatus().isSuccess()) {
                        // file can't be opened
                        continue;
                    }

                    String localFileToCreate = mLocalTeamPhotosFilePath + "/" + teamNumber + "/" + remoteFile.title;
                    DriveContents contents;
                    InputStream in;
                    FileOutputStream fout;
                    try {
                        // DriveContents object contains pointers to the actual byte stream, which we will manually copy
                        contents = fileResult.getDriveContents();
                        in = contents.getInputStream();
                        fout = new FileOutputStream(localFileToCreate);

                        //read bytes from source file and write to destination file
                        byte[] b = new byte[1024];
                        int noOfBytes = 0;
                        while ((noOfBytes = in.read(b)) != -1)
                            fout.write(b, 0, noOfBytes);
                        in.close();
                        fout.close();
                        contents.discard(googleApiClient);
                    } catch (IOException e) {
                        // something went wrong, delete the file we were trying to create
                        (new File(localFileToCreate)).delete();
                    }
                }


                /****** Upload any files that are missing remotely ******/

                for (String localFile : arrLocalFiles) {

                    // if the remote folder does not exist, create it. This should only need to be called once.
                    if (teamPhotosFolder == null) {
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("" + teamNumber).build();
                        DriveFolder.DriveFolderResult folderResult = rootFolder.createFolder(googleApiClient, changeSet).await();
                        teamPhotosFolder = folderResult.getDriveFolder();
                    }

                    DriveContents contents = null;
                    try {
                        FileInputStream in = new FileInputStream(localFile);

                        // create a new file in Drive. This works a little different than normal file IO it that you create the file first,
                        // then tell it at the end which folder it's part of. Think of "folders" in drive more like "tags" or "labels".
                        DriveApi.DriveContentsResult contentResult = Drive.DriveApi.newDriveContents(googleApiClient).await();
                        contents = contentResult.getDriveContents();
                        OutputStream out = contents.getOutputStream();
                        //read bytes from source file and write to destination file
                        byte[] b = new byte[1024];
                        int noOfBytes;
                        while ((noOfBytes = in.read(b)) != -1)
                            out.write(b, 0, noOfBytes);
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        // something went wrong, discard the changes to the Drive file
                        if (contents != null)
                            contents.discard(googleApiClient);
                    }

                    if (contents != null) {
                        String[] splitFilename = localFile.split("/");
                        String filename = splitFilename[splitFilename.length - 1];
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(filename)
                                .setMimeType(URLConnection.guessContentTypeFromName(filename))
                                .build();

                        teamPhotosFolder.createFile(googleApiClient, changeSet, contents).await();
                    }
                }

                Log.i(mActivity.getResources().getString(R.string.app_name),
                        "Finished photo sync for team " + teamNumber);
                googleApiClient.disconnect();


                // hand the photos back to the PhotoRequester
                if (mRequester != null) {
                    // first, get the list of image files
                    listOfFiles = photosDir.listFiles();
                    ArrayList<Bitmap> arrLocalBitmaps = new ArrayList<>();
                    for (File file : listOfFiles) {
                        // make sure it's an image file
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        if (bitmap != null) {
                            arrLocalBitmaps.add(bitmap);
                        }
                        // else: if it's not a file, then what is it???? .... skip I guess
                    }
                    mRequester.updatePhotos((Bitmap[]) arrLocalBitmaps.toArray());

                }
            }
        }

        private class PathAndDriveId {
            public String path;
            public DriveId driveId;
            public String title;

            public PathAndDriveId(String path, DriveId driveId, String title) {
                this.path = path;
                this.driveId = driveId;
                this.title = title;
            }
        }
    }
}
