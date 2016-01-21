package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

public class AutoScouting extends ActionBarActivity {
    private String m_matchNum;
    private String m_allianceColour;

    private Intent srcIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup layout = (ViewGroup) findViewById(R.layout.fragment_auto_scouting2);
        setContentView(R.layout.fragment_auto_scouting2);

        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);

        /** Alliance Colour Indicators **/
        Intent intent = getIntent();
        srcIntent = intent;
        m_allianceColour = intent.getStringExtra(MainActivity.EXTRA_ALLIANCE_COLOUR);
        m_matchNum = intent.getStringExtra(MainActivity.EXTRA_MATCH_NUM);

        // TextView allianceBanner = (TextView) findViewById(R.id.allianceColourTV);
        Button teleopBtn = (Button) findViewById(R.id.startTeleopBtn);

        if (m_allianceColour.equals(getString(R.string.red_alliance))) {
            if (toolbar != null) {
                toolbar.setTitle("Red Alliance");
                int redColor = Color.parseColor("#ffff595d");
                toolbar.setBackgroundColor(redColor);
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.red_alliance));
                    getWindow().setStatusBarColor(getResources().getColor(R.color.red_alliance));

                }
                setSupportActionBar(toolbar);

            }
         /*   allianceBanner.setText("Red Alliance");
            allianceBanner.setBackgroundResource(R.color.red_alliance);*/
            teleopBtn.setBackgroundResource(R.color.red_alliance);

        } else if (m_allianceColour.equals(getString(R.string.blue_alliance))) {
            if (toolbar != null) {
                toolbar.setTitle("Blue Alliance");
                int blueColor = Color.parseColor("#ff668bea");
                toolbar.setBackgroundColor(blueColor);
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.blue_alliance));
                    getWindow().setStatusBarColor(getResources().getColor(R.color.blue_alliance));

                }
                setSupportActionBar(toolbar);
          /*  allianceBanner.setText("Blue Alliance");
            allianceBanner.setBackgroundResource(R.color.blue_alliance);*/
                teleopBtn.setBackgroundResource(R.color.blue_alliance);
            } /*else {
                allianceBanner.setText("!Error!");
            }*/
        }

    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu){

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auto_scouting2, menu);
        return true;
    }

    public void startTeleop(View view) {
        Intent intent = new Intent(this, NewTeleopScouting.class);
        intent.putExtra(MainActivity.EXTRA_MATCH_NUM, m_matchNum);
        intent.putExtra(MainActivity.EXTRA_ALLIANCE_COLOUR, m_allianceColour);

        // copy the file names, if they are there
        if (srcIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    srcIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));

        if (srcIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    srcIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));

        // TODO: plus any auto-mode data

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_auto_scouting2, container, false);
            return rootView;
        }
    }

}
