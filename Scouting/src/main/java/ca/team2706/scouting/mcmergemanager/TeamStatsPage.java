package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.Vector;

public class TeamStatsPage extends ActionBarActivity {

    Intent srcIntent;

    int teamNo;

    int[] counts;
    double[] ptsPerS;
    double[] times;
    int wins, losses, ties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_team_stats_page);

        Intent intent = getIntent();
        srcIntent = intent;
        teamNo = intent.getIntExtra( getResources().getString(R.string.EXTRA_TEAM_NO), 0 );
        setTitle( "Stats for Team " + teamNo );

        counts = (int[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_TEAM_CYCLE_COUNTS));
        ptsPerS = (double[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_PTS_PER_S));
        times = (double[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_TIMES));
        wins = intent.getIntExtra(getResources().getString(R.string.EXTRA_WINS), 0);
        losses = intent.getIntExtra(getResources().getString(R.string.EXTRA_LOSSES), 0);
        ties = intent.getIntExtra(getResources().getString(R.string.EXTRA_TIES), 0);

        // average cycle time
        double avgCycleTime = 0;
        double avgPtsPerS = 0;
        int count = 0;

        // favourite cycle
        int max = -1;
        int maxIdx = -1;
        for(int i=0; i<18; i++) {
            if( times[i] != -1) {
                avgCycleTime += times[i];
                avgPtsPerS += ptsPerS[i];
                count++;
            }

            if(counts[i] > max) {
                max = counts[i];
                maxIdx = i;
            }
        }

        ((TextView) findViewById(R.id.teamRecordTV)).setText("Record: "+wins+"-"+losses+"-"+ties);

        double roundedTime = Math.round(avgCycleTime * 100) / 100.0 / count;
        ((TextView) findViewById(R.id.teamAvgCycleTimeTV)).setText( roundedTime+" s");

        double roundedPtsPerS = Math.round( avgPtsPerS*1000) / 1000.0;
        ((TextView) findViewById(R.id.teamAvgPtsPerSTV)).setText(roundedPtsPerS+" pts/s");
        
        ((TextView) findViewById(R.id.favCycleTypeTV)).setText(CycleAnalyzer.lookupName(maxIdx));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.team_stats_page, menu);
        return true;
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

    public void showCyclesBtn(View view) {
        Intent intent = new Intent(this, TeamCyclesReport.class);
        intent.putExtras(srcIntent);

        startActivity(intent);
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
            View rootView = inflater.inflate(R.layout.fragment_team_stats_page, container, false);
            return rootView;
        }
    }

}
