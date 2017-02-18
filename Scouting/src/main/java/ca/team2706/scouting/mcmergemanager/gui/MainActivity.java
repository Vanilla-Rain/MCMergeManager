package ca.team2706.scouting.mcmergemanager.gui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

import java.util.Timer;
import java.util.TimerTask;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.App;
import ca.team2706.scouting.mcmergemanager.backend.BlueAllianceUtils;
import ca.team2706.scouting.mcmergemanager.backend.FTPClient;
import ca.team2706.scouting.mcmergemanager.backend.FileUtils;
import ca.team2706.scouting.mcmergemanager.backend.TakePicture;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.DataRequester;
import ca.team2706.scouting.mcmergemanager.backend.interfaces.FTPRequester;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchData;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchSchedule;

@TargetApi(21)
public class MainActivity extends AppCompatActivity
                implements DataRequester, PreMatchReportFragment.OnFragmentInteractionListener, FTPRequester{

    public int teamColour = Color.rgb(102, 51, 153);

    Intent globalIntent;
    MainActivity me;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    public static MatchData m_matchData;
    public static MatchSchedule m_matchSchedule;

    FileUtils mFileUtils;
    static FTPClient ftpClient;

    /** static initializer **/
    static {

    }

    /** A flag so that onResume() knows to sync photos for a particular team when we're returning from the camera app */
    boolean lauchedPhotoApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        setNavDrawer();

        globalIntent = new Intent();

        me = this;

        mFileUtils = new FileUtils(this);
        FileUtils.canWriteToStorage();


        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String ftpHostname = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_FTPHostname),"");
        String ftpUsername = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_FTPUsername),"");
        String ftpPassword = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_FTPPassword),"");

        if (ftpHostname != "" && ftpUsername != "" && ftpPassword != "") {
            ftpClient = new FTPClient(ftpHostname, ftpUsername, ftpPassword, FileUtils.sLocalTeamPhotosFilePath, FileUtils.sRemoteTeamPhotosFilePath);
            try {
                ftpClient.connect();
            } catch (Exception e) {
                Log.d("FTP|Connect", "Error while connecting");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //  This used to be needed for Google Drive, might not serve any purpose now...
        if (mFileUtils == null) {
            mFileUtils = new FileUtils(this);
        }

        if (lauchedPhotoApp) {
            // TODO
            // This used to call Google Drive. This need to be replaced with something else
            //mGoogleDriveUtils.syncOneTeamsPhotos(enterATeamNumberPopup.getTeamNumber());

            lauchedPhotoApp = false;
        }

        // tell the user where they are syncing their dada to
        updateDataSyncLabel();

        // fetch the match data from TheBlueAlliance to update the scores.
        BlueAllianceUtils.fetchMatchScheduleAndResults(this);
        m_matchData = mFileUtils.loadMatchDataFile();

        FileUtils.updateFilePathStrings();
    }

    /**
     * Called when activity gets invisible.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }


    /** Called when the user clicks the Scout Match button */
    public void scout(View view) {

        // if they've entered a Match Number in the box, pass that along.
        EditText matchNoET = (EditText) findViewById(R.id.matchNoET);
        int matchNo;
        try {
            matchNo = Integer.parseInt(matchNoET.getText().toString());
        } catch (NumberFormatException e) {
            matchNo = -1;
        }

        Intent intent = new Intent(this, PreGameActivity.class);
        intent.putExtra( getString(R.string.EXTRA_MATCH_NO), matchNo);
        intent.putExtra( getString(R.string.EXTRA_MATCH_SCHEDULE), m_matchSchedule);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.F
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Button handler for the Show [Match] Schedule button.
     */
    public void onShowScheduleClicked(View view) {

        if (m_matchSchedule != null) {
            // bundle the match data into an intent
            Intent intent = new Intent(this, MatchScheduleActivity.class);
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_SCHEDULE), m_matchSchedule.toString());
            startActivity(intent);
        } else {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null) { // not connected to the internet
                Toast.makeText(this, "No Schedule Data to show. No Internet?", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    public void onShowTeamScheduleClicked(View view) {
        if (m_matchSchedule == null) {
            Toast.makeText(this, "No Schedule Data to show. No Internet?", Toast.LENGTH_LONG).show();
            return;
        }

        enterATeamNumberPopup = new GetTeamNumberDialog("View the Schedule for one Team","Team Number", 1, this);
        enterATeamNumberPopup.displayAlertDialog();

        (new Timer()).schedule(new CheckSchedulePopupHasExited(), 250);
    }

    private void setNavDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(teamColour);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        //Set up drawer layout and navigation view
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navLayout);
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

        /**
         * Setup click events on the Navigation View Items.
         */
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                return false;
            }

        });
        android.support.v7.widget.Toolbar toolbar2 = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar2, R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    public GetTeamNumberDialog enterATeamNumberPopup;

    public void takePicture(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 123);
            return; // the popup is non-blocking, so they'll have to click "Take Picture" again.
        }

        enterATeamNumberPopup = new GetTeamNumberDialog("Team Number","Team Number", 1, this);
        enterATeamNumberPopup.displayAlertDialog();

        (new Timer()).schedule(new CheckPicturePopupHasExited(), 250);
    }


    @Override
    public void updateData(String[] matchResultsDataCSV, String[] matchScoutingDataCSV) {

    }

    @Override
    public void updateMatchSchedule(MatchSchedule matchSchedule) {
        m_matchSchedule = matchSchedule;
    }

    /**
     * Set the text for the label "Syncing Data To:" according to the saved preferences.
     */
    public void updateDataSyncLabel() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String event = SP.getString(getResources().getString(R.string.PROPERTY_event), "<Not Set>");

        String label = "Event:"+event;

        TextView tv = (TextView) findViewById(R.id.sync_settings_tv);
        tv.setText(label);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // empty?
    }

    class CheckPicturePopupHasExited extends TimerTask {
        public void run() {
            if (enterATeamNumberPopup.accepted) {
                if(enterATeamNumberPopup.accepted) {
                    int teamNumber;
                    try {
                        teamNumber = Integer.parseInt(enterATeamNumberPopup.inputResult);
                    } catch (NumberFormatException e) {
                        // TODO: should probably pop up a toast or something. There seems to be some threading issue with
                        // doing that from here.

                        Log.e(me.getResources().getString(R.string.app_name), e.toString());
                        return;
                    }

                    FileUtils fileUtils = new FileUtils(me);

                    Uri teamPhotoUri = fileUtils.getNameForNewPhoto(teamNumber);
                    String teamPhotoPath = teamPhotoUri.getPath();
                    Log.e(me.getResources().getString(R.string.app_name), "Saving to \""+teamPhotoPath+"\"");

                    TakePicture pic = new TakePicture(teamPhotoPath, me);
                    pic.capturePicture();
                    DisplayAlertDialog.accepted = false;
                }

            }
            else if (!enterATeamNumberPopup.canceled) {
                // schedule me to run again
                (new Timer()).schedule(new CheckPicturePopupHasExited(), 250);
            }
        }
    }

    class CheckSchedulePopupHasExited extends TimerTask {
        public void run() {
            if (enterATeamNumberPopup.accepted) {
                if(enterATeamNumberPopup.accepted) {
                    int teamNumber;
                    try {
                        teamNumber = Integer.parseInt(enterATeamNumberPopup.inputResult);
                    } catch (NumberFormatException e) {
                        Log.d(me.getResources().getString(R.string.app_name),
                                e.toString());
                        return;
                    }

                    // bundle the match data into an intent and launch the schedule activity
                    Intent intent = new Intent(me, MatchScheduleActivity.class);
                    intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_SCHEDULE), m_matchSchedule.toString());
                    intent.putExtra(getResources().getString(R.string.EXTRA_TEAM_NO), teamNumber);
                    startActivity(intent);

                    DisplayAlertDialog.accepted = false;
                }

            }
            else if (!enterATeamNumberPopup.canceled) {
                // schedule me to run again
                (new Timer()).schedule(new CheckSchedulePopupHasExited(), 250);
            }
        }
    }
    public void downloadFileCallback(String localFilename, String remoteFilename){
        //Here in case we need it later
    }
    public void uploadFileCallback(String localFilename, String remoteFilename){
        //Here in case we need it later
    }
    public void syncCallback(int changedFiles){
        //Here in case we need it later
    }
    public void dirCallback(FTPFile[] listing){
        //Here in case we need it later
    }
    public void updateSyncBar(String Caption, int Progress, Activity activity, boolean isRunning){
        final String caption = Caption;
        final int progress = Progress;
        final Activity activ = activity;
        final boolean isRunningf = isRunning;
        activity.runOnUiThread(new Runnable(){
            public void run(){
                TextView tv =(TextView)activ.findViewById(R.id.syncCaption);
                ProgressBar pb = (ProgressBar)activ.findViewById(R.id.syncBar);
                Button bu = (Button)activ.findViewById(R.id.syncButon);
                if(isRunningf){
                    bu.setText("syncing...");
                }else{
                    bu.setText("Sync Photos");
                }
                if(caption.startsWith("^")){
                    pb.setIndeterminate(true);
                    tv.setText("Getting File Listing...");
                }else{
                    pb.setIndeterminate(false);
                    tv.setText(caption);
                }
                pb.setProgress(progress);
            }
        });
    }
    public void syncPhotos(View v){
        try{
            ftpClient.syncAllFiles(this, this);
        }catch(Exception e){

        }

    }
}