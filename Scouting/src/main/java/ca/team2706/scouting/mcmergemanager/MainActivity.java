package ca.team2706.scouting.mcmergemanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

@TargetApi(21)
public class
        MainActivity extends AppCompatActivity {

    // TODO: all these EXTRA names should go in the strings.xml file
    public final static String EXTRA_MATCH_NUM = "ca.team2706.scouting.mcmergemanager.MATCH_NUM_MSG";
    public final static String EXTRA_ALLIANCE_COLOUR = "ca.team2706.scouting.mcmergemanager.EXTRA_ALLIANCE_COLOUR";
    public final static String EXTRA_CHOOSE_FILES = "ca.team2706.scouting.mcmergemanager.EXTRA_CHOOSE_FILES";
    public final static String EXTRA_AVE_CYCLE_TIMES = "ca.team2706.scouting.mcmergemanager.EXTRA_AVE_CYCLE_TIMES";
    public int teamColour = Color.rgb(102, 51, 153);

//    public static final int MEDIA_TYPE_IMAGE = 1;
//    public static final int MEDIA_TYPE_VIDEO = 2;

    Intent globalIntent;
    MainActivity me;

    boolean matchResultsLoaded = false;
    boolean knightWatchLoaded = false;

    /** <matchAllianceKey, {RobotEvent} **/
    Hashtable<String, Vector<RobotEvent>> matches;

    /** < str(teamNum), {matchAllianceKey}> **/
    Hashtable<String, Vector<String>> matchesPlayedByTeam;

    /** <matchAllianceKey, str(score)> **/
    Hashtable<String, String> matchScores;


    Button loadMatchButton;
    TextView loadMatchResultsTV;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    private Uri fileUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    FileUtils mFileUtils;
    LayoutInflater inflater;

    /** A flag so that onResume() knows to sync photos for a particular team when we're returning from the camera app */
    boolean lauchedPhotoApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        setNavDrawer();

        this.loadMatchButton = (Button) findViewById(R.id.loadMatchResultsBtn);
        this.loadMatchResultsTV = (TextView) findViewById(R.id.loadMatchResultsTV);

        if (loadMatchButton == null)
          /*  Toast.makeText(this, "WTF!?!?!?!?!", Toast.LENGTH_SHORT).show();*/
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View vi = inflater.inflate(R.layout.team_info_tab, null); //log.xml is your file.







        globalIntent = new Intent();

        Intent sender = getIntent();
        if( sender.hasExtra( getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE) ) ) {
            globalIntent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE), sender.getStringExtra( getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE ) ) );
            loadMatchResultsFile();
        }
        if ( sender.hasExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) ) {
            globalIntent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE), sender.getStringExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) );
            loadKnightWatchDataFile();
        }

        me = this;

        // tell the user where they are syncing their dada to
        updateDataSyncLabel();

        // try logging into the Google Drive and make sure the correct files are there.
        mFileUtils = new FileUtils(this);
        mFileUtils.canWriteToStorage();
        mFileUtils.checkDriveConnectionAndFiles();
launchButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFiles();

        // try logging into the Google Drive and make sure the correct files are there.
        mFileUtils = new FileUtils(this);
        mFileUtils.checkDriveConnectionAndFiles();

        if (lauchedPhotoApp) {
            FileUtils fileUtils = new FileUtils(this);
            fileUtils.syncOneTeamsPhotos(enterATeamNumberPopup.getTeamNumber());

            lauchedPhotoApp = false;
        }
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

    /**
     * Set the text for the label "Syncing Data To:" according to the saved preferences.
     *
     * Note: This is called by FileUtils.checkDriveConnectionAndFiles() after it determines if it can connect to Drive
     */
    void updateDataSyncLabel() {
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

    public void loadFiles() {

        // match results file
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE) ) ) {
            // force a re-load of the files always
//            if( !matchResultsLoaded ) {
            loadMatchResultsFile();
//            }
        }

        // KnightWatch file
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) ) {
//            if( !knightWatchLoaded ) {
            loadKnightWatchDataFile();
//            }
        }

        // buttons
        if (globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)) && matchResultsLoaded ) {
            loadMatchButton.setText("Reload");
            File file = new File(globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));
            loadMatchResultsTV.setText("Loaded '"+ file.getName() + "'");
        }
        if (globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) && knightWatchLoaded ) {
            ((Button) findViewById(R.id.loadKnightWatchDataBtn)).setText("Reload");
            File file = new File(globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));
            ((TextView) findViewById(R.id.loadKnightWatchDataTV)).setText("Loaded '" +
                    file.getName() + "'");

            findViewById(R.id.bestCyclesBtn).setEnabled(true);
        }

        // if both files are loaded, activate the Team Stats button
        if(matchResultsLoaded && knightWatchLoaded) {
            findViewById(R.id.lookupTeamBtn).setEnabled(true);
        }
    }

    public void lookupTeamBtn(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Which team do you want stats for?");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int teamNo = Integer.parseInt(input.getText().toString());

                    // TODO: check that this is a team we have data for
                    if( matchesPlayedByTeam.get(""+teamNo) == null) {
                        Toast.makeText(me, "No data for team "+teamNo, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // These are things that could be needed further on
                    String strTeam = ""+teamNo;
                    Vector<String> matchesPlayed = matchesPlayedByTeam.get(strTeam);
                    Vector<Vector<RobotEvent>> allRobotEvents = new Vector<Vector<RobotEvent>>();
                    int wins=0, losses=0, ties = 0;
                    for(String matchKey : matchesPlayed) {
                        Vector<RobotEvent> match = matches.get(matchKey);
                        if(match != null)
                            allRobotEvents.add( match );

                        int myScore = Integer.parseInt(matchScores.get(matchKey));
                        int theirScore = Integer.parseInt(matchScores.get(getOtherAllianceKey(matchKey)));

                        if(myScore > theirScore)
                            wins++;
                        else if (myScore < theirScore)
                            losses++;
                        else
                            ties++;

                    }

                    Vector<Vector<CycleAnalyzer.Cycle>> allCycles = CycleAnalyzer.extractAllCycles(allRobotEvents);

                    double[] ptsPerS = CycleAnalyzer.computePtsPerSecond(allCycles);
                    int[] counts = CycleAnalyzer.computeCounts(allCycles);
                    double[] times = CycleAnalyzer.computeAverageTimes(allCycles);

                    Intent intent = new Intent(me, TeamStatsPage.class);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TEAM_CYCLE_COUNTS), counts);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TEAM_NO), teamNo);
                    intent.putExtra(getResources().getString(R.string.EXTRA_PTS_PER_S), ptsPerS);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TIMES), times);
                    intent.putExtra(getResources().getString(R.string.EXTRA_WINS), wins);
                    intent.putExtra(getResources().getString(R.string.EXTRA_LOSSES), losses);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TIES), ties);

                    startActivity(intent);

                } catch (Exception e) {
                    return;
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    // TODO: this is from the old KnightWatch and needs to be taken out.
    /** Called when the user clicks the ScoutRed button */
    public void scout(View view) {

   /*     EditText matchNumField = (EditText) findViewById(R.id.match_num_field);
        String matchNum = matchNumField.getText().toString();
        if(matchNum.equals("")) return;*/

        Intent intent = new Intent(this, PreGameActivity.class);
       /* intent.putExtra(EXTRA_MATCH_NUM, matchNum);
        intent.putExtra(EXTRA_ALLIANCE_COLOUR, getString(R.string.red_alliance));

        // copy the file names, if they are there
        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));

        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));
*/
        startActivity(intent);
    }
    public void sendMessage(View view) {
/*        Intent intent = new Intent(this, CameraActivity.class);


        startActivity(intent);*/

    }
    /** Called when the user clicks the ScoutBlue button */
    public void scoutBlue(View view) {

        EditText matchNumField = (EditText) findViewById(R.id.match_num_field);
        String matchNum = matchNumField.getText().toString();
        if(matchNum.equals("")) return;

        Intent intent = new Intent(this, AutoScouting.class);
        intent.putExtra(EXTRA_MATCH_NUM, matchNum);
        intent.putExtra(EXTRA_ALLIANCE_COLOUR, getString(R.string.blue_alliance) );

        // copy the file names, if they are there
        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));

        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));

        startActivity(intent);
    }


    public void loadMatchFileBtn(View view) {

        if( matchResultsLoaded ) {
            loadMatchResultsFile();
        } else {
            Intent intent = new Intent(this, FileChooser.class);
            intent.putExtras(globalIntent);
            intent.putExtra(FileChooser.EXTRA_TO_RETURN, getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE));
            intent.putExtra(EXTRA_CHOOSE_FILES, true);
            startActivity(intent);
        }
    }

    public void loadKnightWatchFileBtn(View view) {

        if ( globalIntent.hasExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) ) {
            loadKnightWatchDataFile();
        } else {
            Intent intent = new Intent(this, FileChooser.class);
            intent.putExtras(globalIntent);
            intent.putExtra(FileChooser.EXTRA_TO_RETURN, getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE));
            intent.putExtra(EXTRA_CHOOSE_FILES, true);
            startActivity(intent);
        }
    }

    public void bestCyclesBtn(View view) {

        Vector<Vector<CycleAnalyzer.Cycle>> allCycles = new Vector<Vector<CycleAnalyzer.Cycle>>();
        for( String matchName : matches.keySet()) {
            allCycles.add(CycleAnalyzer.extractCycles(matches.get(matchName)));
        }

        double[] ptsPerS = CycleAnalyzer.computePtsPerSecond(allCycles);
        double[] times = CycleAnalyzer.computeAverageTimes(allCycles);

        Intent intent = new Intent(this, DispBestCycles.class);
        intent.putExtra(EXTRA_AVE_CYCLE_TIMES, ptsPerS);
        startActivity(intent);
    }

    private String getMatchAllianceKey(int matchNum, String alliance) {
        return matchNum + alliance;
    }

    private String getOtherAllianceKey( String matchKey ) {
        if( matchKey.contains("red") )
            return matchKey.replace("red", "blue");

        if( matchKey.contains("blue") )
            return matchKey.replace("blue", "red");

        return null;
    }


    private void loadMatchResultsFile() {
        String fileName;
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)) ) {
            fileName = globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE));
        } else {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matchesPlayedByTeam = new Hashtable<String, Vector<String>>();
        matchScores = new Hashtable<String, String>();
        try {
            String line;
            line = br.readLine();

            while (line != null) {

                String[] cells = line.split(",");

                try {
                    // ignore the first column cause it's the time of day of the match
                    int matchNum = Integer.parseInt( cells[1] );
                    int red1 = Integer.parseInt( cells[2] );
                    int red2 = Integer.parseInt( cells[3] );
                    int red3 = Integer.parseInt( cells[4] );
                    int blue1 = Integer.parseInt( cells[5] );
                    int blue2 = Integer.parseInt( cells[6] );
                    int blue3= Integer.parseInt( cells[7] );
                    int redScore = Integer.parseInt( cells[8] );
                    int blueScore = Integer.parseInt( cells[9] );

                    if (matchesPlayedByTeam.containsKey(red1+"")) {
                        matchesPlayedByTeam.get( red1+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red1, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(red2+"")) {
                        matchesPlayedByTeam.get( red2+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red2, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(red3+"")) {
                        matchesPlayedByTeam.get( red3+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red3, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue1+"")) {
                        matchesPlayedByTeam.get( blue1+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue1, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue2+"")) {
                        matchesPlayedByTeam.get( blue2+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue2, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue3+"")) {
                        matchesPlayedByTeam.get( blue3+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue3, vec);
                    }

                    matchScores.put(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE), ""+redScore);
                    matchScores.put(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE), ""+blueScore);

                } catch( Exception e) {
                    Toast.makeText(this, "Error: Match Results file may be corrupted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                line = br.readLine();
            }

            br.close();
        } catch(IOException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matchResultsLoaded = true;
    }

    private void loadKnightWatchDataFile() {

        String fileName;
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) ) {
            fileName = globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE));
        } else {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matches = new Hashtable<String, Vector<RobotEvent>>();
        try {
            String line = null;
            line = br.readLine();

            while (line != null) {

                String[] cells = line.split(",");
                int matchNum = Integer.parseInt(cells[0].replace(RobotEvent.STR_MATCHNUM, ""));
                String alliance = cells[1].replace(RobotEvent.STR_ALLIANCE, "");

                Vector<RobotEvent> events = new Vector<RobotEvent>();

                // -1 cause the last one will be a comma with nothing after it
                for(int i=2; i<cells.length-1; i++) {
                    String eventType = cells[i].substring(cells[i].indexOf('{') + 1, cells[i].indexOf(';'));

                    if( eventType.equals( RobotEvent.STR_LG_SCORE) || eventType.equals( RobotEvent.STR_HG_SCORE) ) {
                        int minutes = Integer.parseInt(cells[i].substring(cells[i].indexOf(";") + 1, cells[i].indexOf(":")));
                        int seconds = Integer.parseInt(cells[i].substring(cells[i].indexOf(":") + 1, cells[i].indexOf("|") ));
                        String strAssis = cells[i].substring(cells[i].indexOf("|") + 1, cells[i].indexOf("}"));
                        int assists = Integer.parseInt(strAssis.replace(RobotEvent.STR_NUM_ASSIS, ""));
                        events.add( new RobotEvent(eventType, minutes, seconds, assists));

                    } else {
                        int minutes = Integer.parseInt(cells[i].substring(cells[i].indexOf(";") + 1, cells[i].indexOf(":")));
                        int seconds = Integer.parseInt(cells[i].substring(cells[i].indexOf(":") + 1, cells[i].indexOf("}")));
                        events.add( new RobotEvent(eventType, minutes, seconds));
                    }

                }

                matches.put(getMatchAllianceKey(matchNum, alliance), events);

                line = br.readLine();
            }

            br.close();
        } catch(Exception e) {
            Toast.makeText(this, "Error loading KnightWatch file.", Toast.LENGTH_SHORT).show();
            return;
        }



        knightWatchLoaded = true;
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
            case R.id.betting:
                intent = new Intent(this,Betting.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

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


public  boolean accepted;
    public GetTeamNumberDialog enterATeamNumberPopup;

    public void takePicture(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    123);
            return; // the popup is non-blocking, so they'll have to click "Take Picture" again.
        }

        GetTeamNumberDialog alert = new GetTeamNumberDialog("Team Number","Team Number", 1, this);
        alert.displayAlertDialog();

        Log.d("heya", Boolean.toString(accepted));
        enterATeamNumberPopup = new GetTeamNumberDialog("Team Number","Team Number", 1, this);
        enterATeamNumberPopup.displayAlertDialog();

        Timer timer = new Timer();
        timer.schedule(new CheckPopupHasExited(), 0, 500);
    }

    public static void startFragmentDialog() {

    }


    public String inputResult = "empty";

    public static String getInputResult() {
        return DisplayAlertDialog.inputResult;
    }

    public void setInputResult(String inputResult) {
        this.inputResult = inputResult;
    }

    public static boolean getAccepted() {
        return DisplayAlertDialog.accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    class CheckPopupHasExited extends TimerTask {
        public void run() {
            setAccepted(getAccepted());
            setInputResult(getInputResult());
            if (accepted) {
                if(enterATeamNumberPopup.accepted) {
                    int teamNumber;
                    try {
                        teamNumber = Integer.parseInt(enterATeamNumberPopup.inputResult);
                    } catch (NumberFormatException e) {
                        // TODO: should probably pop up a toast or something. There seems to be some threading issue with
                        // doing that from here.

                        Log.d(me.getResources().getString(R.string.app_name),
                                e.toString());
                        return;
                    }

                    FileUtils fileUtils = new FileUtils(me);
                    TakePicture pic = new TakePicture(fileUtils.getTeamPhotoPath(teamNumber), me);
                    pic.capturePicture();
                    DisplayAlertDialog.accepted = false;

                    // otherwise this thread will keep running in the background forever, even if you close the app.
                    // Every time you take a pic your phone will have more and more processes running making it slower and slower.
                    this.cancel();
                }

            }
            else if (enterATeamNumberPopup.canceled) {
                this.cancel();
            }
        }
    }

    public void launchButtons() {

    }

}