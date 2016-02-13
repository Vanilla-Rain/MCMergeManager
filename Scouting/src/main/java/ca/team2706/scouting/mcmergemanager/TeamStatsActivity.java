package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class TeamStatsActivity extends AppCompatActivity {

    StatsEngine.TeamStatsReport m_teamStastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        // unbundle the stats data from the intent
        Intent intent = getIntent();

        try {
            m_teamStastReport = (StatsEngine.TeamStatsReport) intent.getSerializableExtra(getString(R.string.EXTRA_TEAM_STATS_REPORT));
        } catch (Exception e) {
            // maybe the extra wasn't there?
            // Nothing to display then
            m_teamStastReport = null;
        }

        displayStats();
    }


    private void displayStats() {

        /** Title **/
        ((TextView) findViewById(R.id.fullStatsReportTitle)).setText("Stats Report for Team "+m_teamStastReport.teamNo);


        /** General Info **/

        ((TextView) findViewById(R.id.fullStatsReportRecordTV)).setText( String.format("Record (W/L/T): %d/%d/%d",
                                                                                m_teamStastReport.wins,
                                                                                m_teamStastReport.losses,
                                                                                m_teamStastReport.ties) );

        ((TextView) findViewById(R.id.numMatchesPlayedTV)).setText( String.format("Matches Played: %d", m_teamStastReport.numMatchesPlayed));

        ((TextView) findViewById(R.id.OPRtv)).setText(String.format("OPR: %.2f", m_teamStastReport.OPR));
        ((TextView) findViewById(R.id.DPRtv)).setText( String.format("DPR: %.2f", m_teamStastReport.DPR) );

        if (m_teamStastReport.scheduleToughness < 1.0)
            ((TextView) findViewById(R.id.schedToughnessTV)).setText( String.format("Schedule Toughness: %.2f (easy)", m_teamStastReport.scheduleToughness) );
        else
            ((TextView) findViewById(R.id.schedToughnessTV)).setText( String.format("Schedule Toughness: %.2f (hard)", m_teamStastReport.scheduleToughness) );




        /** Auto Mode **/


        if (m_teamStastReport.numMatchesPlayed != 0) {
            ((TextView) findViewById(R.id.reachesTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numTimesReachedInAuto,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numTimesReachedInAuto) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.breachesTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.numTimesBreachedInAuto,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.numTimesBreachedInAuto) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.spyBotTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numTimesSpyBot,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numTimesSpyBot) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.autoHighGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.numSuccHighShotsInAuto,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.numSuccHighShotsInAuto) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.autoLowGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.numSuccLowShotsInAuto,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.numSuccLowShotsInAuto) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.autoMissedShotsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.numMissedShotsInAuto,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.numMissedShotsInAuto) / m_teamStastReport.numMatchesPlayed));

        }
    }
}
