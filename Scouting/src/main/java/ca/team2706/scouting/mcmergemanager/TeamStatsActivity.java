package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class TeamStatsActivity extends AppCompatActivity {

    StatsEngine.TeamStatsReport m_teamStastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // unbundle the stats data from the intent
        Intent intent = getIntent();

        try {
            m_teamStastReport = (StatsEngine.TeamStatsReport) intent.getSerializableExtra(getString(R.string.EXTRA_TEAM_STATS_REPORT));
        } catch (Exception e) {
            // maybe the extra wasn't there?
            // Nothing to display then
            m_teamStastReport = null;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void displayStats() {

    }
}
