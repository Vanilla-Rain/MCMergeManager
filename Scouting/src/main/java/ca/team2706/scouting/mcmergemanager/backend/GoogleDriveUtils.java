package ca.team2706.scouting.mcmergemanager.backend;

import android.app.Activity;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.DataRequester;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.PhotoRequester;
import ca.team2706.scouting.mcmergemanager.gui.MainActivity;

/**
 * Created by mike on 09/10/16.
 */

public class GoogleDriveUtils implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Grabbed this constant from a tutorial ... seems kinda hacky to be hard-coding API codes, shouldn't there be a place I can reference this??
     */
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private static boolean mCanConnect = false;

    // This is not being used _yet_, but is here for future integration
    private static boolean mHasUnsyncedMatchScoutingData = false;


    private static DriveId mDriveIdTeamPhotosFolder;
    private static String  mRemoteToplevelFolderName;
    private static String  mRemoteTeamFolderName;
    private static String  mRemoteEventFolderName;
    private static String  mRemoteTeamPhotosFolderName;

    /**
     * If the user denys the app access to their drive, it asks over and over again and they don't have the chance to go to settings.
     */
    private boolean mRequestedAccessAlready = false;
    private boolean mCheckDriveFilesOnNextConnect = false;
    private GoogleApiClient mGoogleApiClient;


    /**
     * A pointer to myself so that the nested classes can use my ConnectionCallbacks
     **/
    private static GoogleDriveUtils m_me;

    private static Activity mActivity;


    /* ~~~ Contructor ~~~ */
    public GoogleDriveUtils(Activity activity) {
        mActivity = activity;
        m_me = this;

        // store string constants and preferences in member variables just for cleanliness
        // (since the strings are `static`, when any instances of FileUtils update these, all instances will get the updates)
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mRemoteToplevelFolderName = App.getContext().getString(R.string.FILE_TOPLEVEL_DIR);
        mRemoteTeamFolderName = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_googledrive_teamname), "<Not Set>");
        mRemoteEventFolderName = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_googledrive_event), "<Not Set>");
        mRemoteTeamPhotosFolderName = "Team Photos";
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
        } else {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String driveAccount = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_googledrive_account), "<Not Set>");

            if (driveAccount.equals("<Not Set>"))
                return;

            mGoogleApiClient = new GoogleApiClient.Builder(App.getContext())
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
    public void checkDriveConnectionAndFiles() {
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


    @Override
    public void onConnected(Bundle connectionHint) {
        Toast.makeText(App.getContext(), "Connected to Drive!", Toast.LENGTH_SHORT).show();

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
            Log.e(App.getContext().getResources().getString(R.string.app_name), "Exception while starting resolution activity", e);
        }

        mCanConnect = false;
        if (mActivity != null && mActivity instanceof MainActivity)
            ((MainActivity) mActivity).updateDataSyncLabel();
        Toast.makeText(App.getContext(), "Could not connect to Drive.", Toast.LENGTH_LONG).show();
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
        // TODO
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
     */
    public void syncAllTeamPhotos() {
        checkDriveConnectionAndFiles();
        if (!canConnectToDrive())
            return;

        // -1 tells TeamPhotoSyncerThread to sync all teams.
        (new TeamPhotoSyncerThread(-1, null)).start();
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
            Log.i(App.getContext().getResources().getString(R.string.app_name), "Starting the Drive folder sync");

            // Check if the file structure exists, and create it if it doesn't
            DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            DriveFolder topLevelfolder = checkOrCreateRemoteFolder(mGoogleApiClient, rootFolder, mRemoteToplevelFolderName);
            DriveFolder teamFolder = checkOrCreateRemoteFolder(mGoogleApiClient, topLevelfolder, mRemoteTeamFolderName);
            DriveFolder eventFolder = checkOrCreateRemoteFolder(mGoogleApiClient, teamFolder, mRemoteEventFolderName);
            DriveFolder teamPhotosFolder = checkOrCreateRemoteFolder(mGoogleApiClient, eventFolder, mRemoteTeamPhotosFolderName);
            if (teamPhotosFolder != null) {
                mDriveIdTeamPhotosFolder = teamPhotosFolder.getDriveId();
            }

            Log.i(App.getContext().getResources().getString(R.string.app_name), "Drive folder sync finished.");
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
            if (!FileUtils.canWriteToStorage() || googleApiClient == null || !googleApiClient.isConnected())
                return null;

            Query query = new Query.Builder().addFilter(Filters.and(
                    Filters.eq(SearchableField.TITLE, folderName),
                    Filters.contains(SearchableField.MIME_TYPE, "folder"))).build();

            DriveApi.MetadataBufferResult result = rootFolder.queryChildren(mGoogleApiClient, query).await();

            if (!result.getStatus().isSuccess()) {
                Log.e(App.getContext().getResources().getString(R.string.app_name), "Cannot query folders in the root of Google Drive.");
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
                Log.e(App.getContext().getResources().getString(R.string.app_name), "Error while trying to create the folder \"" + folderName + "\"");
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
            if (!FileUtils.canWriteToStorage())
                return;

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String driveAccount = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_googledrive_account), "<Not Set>");

            if (driveAccount.equals("<Not Set>"))
                return;
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(App.getContext())
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

                Log.i(App.getContext().getResources().getString(R.string.app_name),
                        "Beginning photo sync for team " + teamNumber);

                // get the list of local files

                File photosDir = new File(FileUtils.sLocalTeamPhotosFilePath + "/" + teamNumber);
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
                Log.i(App.getContext().getResources().getString(R.string.app_name), "Local Files: " + arrLocalFiles);

                Log.i(App.getContext().getResources().getString(R.string.app_name),
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
                Log.i(App.getContext().getResources().getString(R.string.app_name), "Remote Files: " + arrRemoteFiles);

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

                    String localFileToCreate = FileUtils.sLocalTeamPhotosFilePath + "/" + teamNumber + "/" + remoteFile.title;
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

                Log.i(App.getContext().getResources().getString(R.string.app_name),
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
