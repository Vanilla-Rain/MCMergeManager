package ca.team2706.scouting.mcmergemanager.stronghold2016.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.StatsEngine;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;

import static ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.TeleopScoutingObject.*;

public class TeamStatsActivity extends AppCompatActivity {

    StatsEngine.TeamStatsReport m_teamStastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.steamworks2017_activity_team_stats);

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

        if (m_teamStastReport == null)
            return;

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


        if (m_teamStastReport.numMatchesPlayed != 0) {  // protects against divide-by-zero

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


        /** Teleop Mode **/


        if (m_teamStastReport.numMatchesPlayed != 0) {  // protects against divide-by-zero

            ((TextView) findViewById(R.id.teleopHighGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numSuccHighShotsInTeleop,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numSuccHighShotsInTeleop) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.teleopHighSetupTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.avgHighShotTime) );


            ((TextView) findViewById(R.id.teleopLowGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numSuccLowShotsInTeleop,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numSuccLowShotsInTeleop) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.teleopLowSetupTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.avgLowShotTime) );


            ((TextView) findViewById(R.id.teleopMissedShotsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numMissedShotsInTeleop,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numMissedShotsInTeleop) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.teleopGroundPickupsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numSuccPickupsFromGround,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numSuccPickupsFromGround) / m_teamStastReport.numMatchesPlayed));


            ((TextView) findViewById(R.id.teleopWallPickupsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numSuccPickupsFromWall,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numSuccPickupsFromWall) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.teleopFailedPickupsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numFailedPickups,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numFailedPickups) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.teleopTimePlayingDTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.avgTimeSpentPlayingDef) );

            ((TextView) findViewById(R.id.avgDeadnessTV)).setText(String.format("%d%%",
                                                                            m_teamStastReport.avgDeadness) );

            ((TextView) findViewById(R.id.highestDeadnessTV)).setText(String.format("\t%d%%",
                                                                            m_teamStastReport.highestDeadness) );

            ((TextView) findViewById(R.id.challengesTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numTimesChallenged,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numTimesChallenged) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.scalesTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numSuccessfulScales,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numSuccessfulScales) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.scaleTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.avgScaleTime) );

            ((TextView) findViewById(R.id.failedScalesTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.numFailedScales,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numFailedScales) / m_teamStastReport.numMatchesPlayed));
        }



        /** Defenses Breached **/

        if (m_teamStastReport.numMatchesPlayed != 0) {  // protects against divide-by-zero

            // I'm aware I could do this with a for-loop, but I think this makes the code easier to read

            ((TextView) findViewById(R.id.lowBarTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_LOW_BAR],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_LOW_BAR]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.portcullisTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_PORTCULLIS],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_PORTCULLIS]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.chevaleTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_CHEVAL],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_CHEVAL]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.moatTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_MOAT],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_MOAT]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.rampartTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_RAMPART],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_RAMPART]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.drawbridgeTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_DRAWBRIDGE],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_DRAWBRIDGE]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.sallyportTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_SALLYPORT],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_SALLYPORT]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.rockWallTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_ROCKWALL],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_ROCKWALL]) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.roughTerrainTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.defensesBreached[DEFENSE_ROUGH_TERRAIN],
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.defensesBreached[DEFENSE_ROUGH_TERRAIN]) / m_teamStastReport.numMatchesPlayed));

        }



        /** High Shots Map **/

        final ImageView highGoalMap = (ImageView) findViewById(R.id.mapHigh);
        for (BallShot shot : m_teamStastReport.successfulTeleopHighShots) {

            // draw a new pin on the map
            RelativeLayout imgHolder = (RelativeLayout) findViewById(R.id.mapHighLayout);

            ImageView pointerImageView = new ImageView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            params.leftMargin = (int) highGoalMap.getX() + shot.x - 25;
            params.topMargin =  (int) highGoalMap.getY() + shot.y - 25;

            pointerImageView.setImageResource(R.drawable.pinicon);
            pointerImageView.setLayoutParams(params);
            imgHolder.addView(pointerImageView);
        }


        /** Low Shots Map **/

        final ImageView lowGoalMap = (ImageView) findViewById(R.id.mapLow);
        for (BallShot shot : m_teamStastReport.successfulTeleopLowShots) {

            // draw a new pin on the map
            RelativeLayout imgHolder = (RelativeLayout) findViewById(R.id.mapLowLayout);

            ImageView pointerImageView = new ImageView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            params.leftMargin = (int) lowGoalMap.getX() + shot.x - 25;
            params.topMargin = (int) lowGoalMap.getY() + shot.y - 25;

            pointerImageView.setImageResource(R.drawable.pinicon);
            pointerImageView.setLayoutParams(params);
            imgHolder.addView(pointerImageView);
        }

    }
}
