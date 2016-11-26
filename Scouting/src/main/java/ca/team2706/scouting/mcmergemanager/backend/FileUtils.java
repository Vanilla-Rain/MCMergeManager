package ca.team2706.scouting.mcmergemanager.backend;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.PhotoRequester;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallPickup;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchData;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.ScalingTime;

/**
 * This is a helper class to hold common code for accessing shared scouting data files.
 * This class takes care of keeping a local cache, syncing to the server, and (eventually) sharing with other bluetooth-connected devices also running the app.
 * <p/>
 * Created by Mike Ounsworth
 */
public class FileUtils {

    private static Activity mActivity;

    /**
     * A pointer to myself so that the nested classes can use my ConnectionCallbacks
     **/
    FileUtils m_me;

    public static String sLocalToplevelFilePath;
    public static String sLocalTeamFilePath;
    public static String sLocalEventFilePath;
    public static String sLocalTeamPhotosFilePath;

    /* Static initializer */
    {
        // store string constants and preferences in member variables just for cleanliness
        // (since the strings are `static`, when any instances of FileUtils update these, all instances will get the updates)
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        sLocalToplevelFilePath = "/sdcard/"+ App.getContext().getString(R.string.FILE_TOPLEVEL_DIR);
        sLocalTeamFilePath = sLocalToplevelFilePath + "/" + SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_teamname), "<Not Set>");
        sLocalEventFilePath = sLocalTeamFilePath + "/" + SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_event), "<Not Set>");
        sLocalTeamPhotosFilePath = sLocalTeamFilePath + "/" + "Team Photos";
    }

    /**
     * Constructor
     *
     * @param activity This will be used to fetch string constants for file storage and displaying toasts.
     */
    public FileUtils(Activity activity) {
        mActivity = activity;
        m_me = this;

        checkLocalFileStructure();
    }


    /**
     * Checks if we have the permission read / write to the internal USB STORAGE,
     * requesting that permission if we do not have it.
     *
     * @return whether or not we have the STORAGE permission.
     */
    public static boolean canWriteToStorage() {
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

        makeDirectory(sLocalToplevelFilePath);
        makeDirectory(sLocalTeamFilePath);
        makeDirectory(sLocalEventFilePath);
        makeDirectory(sLocalTeamPhotosFilePath);
    }


    private void makeDirectory(String directoryName) {

        Log.d(App.getContext().getResources().getString(R.string.app_name), "Making directory: " + directoryName);


        File file = new File(directoryName);
        if (!file.isDirectory()) {
            // in case there's a regular file there with the same name
            file.delete();
            // create it
            file.mkdir();
        }
    }

    /**
     * Take one match of data and stick it at the end of the match data file.
     *
     * Data format:
     * "matchNo<int>,teamNo<int>,isSpyBot<boolean>,reached<boolean>,{autoDefenseBreached<int>;...},{{autoBallShot_X<int>;autoBallShot_Y<int>;autoBallShot_time<.2double>;autoBallshot_which<int>}:...},{teleopDefenseBreached<int>;...},{{teleopBallShot_X<int>;teleopBallShot_Y<int>;teleopBallShot_time<.2double>;teleopBallshot_which<int>}:...},timeDefending<,2double>,{{ballPickup_selection<int>;ballPickup_time<,2double>}:...},{{scaling_time<.2double>;scaling_comelpted<int>}:...},notes<String>,challenged<boolean>,timeDead<int>"
     *
     * Or, in printf / format strings:
     * "%d,%d,%b,%b,{%d;...},{{%d:%d:%.2f:%d};...},{%d;...},{{%d:%d:%.2f:%d};...},%,2f,{{%d;%,2f}:...},{{%.2f;%d}:...},%s,%b,%d"
     */
    public void appendToMatchDataFile(MatchData.Match match) {

        String outFileName = sLocalEventFilePath +"/"+ App.getContext().getResources().getString(R.string.matchScoutingDataFileName);

        Log.d(App.getContext().getResources().getString(R.string.app_name), "Saving data to file: "+outFileName);

        File outfile = new File(outFileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
            bw.append( match.toString() );
            bw.flush();
            bw.close();
        } catch (IOException e) {

        }


        outFileName = sLocalEventFilePath +"/"+ App.getContext().getResources().getString(R.string.matchScoutingDataFileNameUNSYNCHED);

        Log.d(App.getContext().getResources().getString(R.string.app_name), "Saving data to file: "+outFileName);

        outfile = new File(outFileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
            bw.append( match.toString() );
            bw.flush();
            bw.close();
        } catch (IOException e) {

        }
    }


    /**
     * Load the entire file of match data into Objects.
     *
     * Data format:
     * "matchNo<int>,teamNo<int>,isSpyBot<boolean>,reached<boolean>,{autoDefenseBreached<int>;...},{{autoBallShot_X<int>;autoBallShot_Y<int>;autoBallShot_time<.2double>;autoBallshot_which<int>}:...},{teleopDefenseBreached<int>;...},{{teleopBallShot_X<int>;teleopBallShot_Y<int>;teleopBallShot_time<.2double>;teleopBallshot_which<int>}:...},timeDefending<,2double>,{{ballPickup_selection<int>;ballPickup_time<,2double>}:...},{{scaling_time<.2double>;scaling_comelpted<int>}:...},notes<String>,challenged<boolean>,timeDead<int>"
     *
     * Or, in printf / format strings:
     * "%d,%d,%b,%b,{%d;...},{{%d:%d:%.2f:%d};...},{%d;...},{{%d:%d:%.2f:%d};...},%,2f,{{%d;%,2f}:...},{{%.2f;%d}:...},%s,%b,%d"
     */
    public MatchData loadMatchDataFile() {

        MatchData matchData = new MatchData();
        List<String> matchStrs = new ArrayList<>();

        // read the file
        String inFileName = sLocalEventFilePath +"/"+ App.getContext().getResources().getString(R.string.matchScoutingDataFileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(inFileName));
            String line = br.readLine();

            while (line != null) {
                // braces are for human readibility, but make parsing harder
                line = line.replace("{","").replace("}","");
                matchStrs.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            Log.e(App.getContext().getResources().getString(R.string.app_name), "loadMatchDataFile:: " + e.toString());
            return null;
        }

        // parse all the matches into the MatchData object
        boolean parseFailure = false;
        for (String matchStr : matchStrs) {

            try {
                MatchData.Match match = new MatchData.Match(matchStr);
                matchData.addMatch(match);
            } catch (Exception e) {
                Log.e(App.getContext().getResources().getString(R.string.app_name), "loadMatchDataFile:: "+e.toString());
                parseFailure = true;
                continue;
            }
        }
        if (parseFailure) {
            Toast.makeText(App.getContext(), "Warning: match data may be corrupted or malformed.", Toast.LENGTH_SHORT).show();
        }

        return matchData;
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
        String dir = sLocalTeamPhotosFilePath + "/" + teamNumber + "/";
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


    public String getTeamPhotoPath(int teamNumber) {
        return sLocalTeamPhotosFilePath + "/" + teamNumber;
    }


    /**
     * This method will return you all locally-cached photos for the requested team.
     * It will then spawn a new background thread, and if <photo server> is available, it will sync the photos
     * for the requested team only and then notify the requesting activity that it has new photos.
     * <p/>
     * Since syncing photos with the server can take a few seconds, FileUtils.loadTeamPhotos() will immediately call
     * the PhotoRequester's updatePhotos(Bitmap[]) with whatever photos are locally cached for that team,
     * and if FileUtils is able to connect to the server then it will call it again after performing the sync.
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

        File photosDir = new File(sLocalTeamPhotosFilePath + "/" + teamNumber);

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
                Bitmap bitmap = loadScaledDownImage(file.getPath());
                if (bitmap != null)
                    arrBitmaps.add(bitmap);
            }
            // else: if it's not a file, then what is it???? .... skip I guess
        }
        requester.updatePhotos(arrBitmaps.toArray(new Bitmap[arrBitmaps.size()]));


        /* This used to sync with Google Drive, now we need something different */
        // TODO

    }

    /**
     * Calculates how much to scale the image based on the size of the screen and then loads
     * a scaled down version into memory.
     */
    private static Bitmap loadScaledDownImage(String imagePath) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int reqSize = displayMetrics.widthPixels / 4;

        if (height > reqSize || width > reqSize) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps
            // height or width larger than the requested size.
            while ((halfHeight / inSampleSize) > reqSize
            || (halfWidth / inSampleSize) > reqSize) {
            inSampleSize *= 2;
            }
        }

        // Decode bitmap with inSampleSize set
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath);
    }
}
