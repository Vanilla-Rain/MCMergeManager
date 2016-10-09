package ca.team2706.scouting.mcmergemanager.gui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ca.team2706.scouting.mcmergemanager.miscBackend.DataRequester;
import ca.team2706.scouting.mcmergemanager.miscBackend.FileUtils;
import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchData;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.miscBackend.TakePicture;

@TargetApi(21)
public class MainActivity extends AppCompatActivity
                implements DataRequester, PreMatchReportFragment.OnFragmentInteractionListener {

    public int teamColour = Color.rgb(102, 51, 153);

    Intent globalIntent;
    MainActivity me;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
//    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static MatchData m_matchData;
    public static MatchSchedule m_matchSchedule;

    FileUtils mFileUtils;


    /** A flag so that onResume() knows to sync photos for a particular team when we're returning from the camera app */
    boolean lauchedPhotoApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        setNavDrawer();

        globalIntent = new Intent();

        me = this;

        // try logging into the Google Drive and make sure the correct files are there.
        mFileUtils = new FileUtils(this);
        mFileUtils.canWriteToStorage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // try logging into the Google Drive and make sure the correct files are there.
        if (mFileUtils == null) {
            mFileUtils = new FileUtils(this);
        }
        mFileUtils.checkDriveConnectionAndFiles();

        if (lauchedPhotoApp) {
            mFileUtils.syncOneTeamsPhotos(enterATeamNumberPopup.getTeamNumber());
            lauchedPhotoApp = false;
        }

        // tell the user where they are syncing their dada to
        updateDataSyncLabel();

        // fetch the match data from TheBlueAlliance to update the scores.
        FileUtils.fetchMatchScheduleAndResults(this);
        m_matchData = mFileUtils.loadMatchDataFile();
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {
        if(mFileUtils != null)
            mFileUtils.disconnect();

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
        this.m_matchSchedule = matchSchedule;
    }

    /**
     * Set the text for the label "Syncing Data To:" according to the saved preferences.
     *
     * Note: This is called by FileUtils.checkDriveConnectionAndFiles() after it determines if it can connect to Drive
     */
    public void updateDataSyncLabel() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String driveTeam = SP.getString(getResources().getString(R.string.PROPERTY_googledrive_teamname), "<Not Set>");
        String driveEvent = SP.getString(getResources().getString(R.string.PROPERTY_googledrive_event), "<Not Set>");
        String driveAccount = SP.getString(getResources().getString(R.string.PROPERTY_googledrive_account), "<Not Set>");

        String label = "Syncing Data with: "+driveTeam+" / "+driveEvent+"\n\t\tusing: "+driveAccount;
        if (mFileUtils != null && !mFileUtils.canConnectToDrive())
            label = label + "\n! Cannot connect to Drive";

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
}