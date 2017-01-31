package ca.team2706.scouting.mcmergemanager.steamworks2017.gui;

// TODO: Complete all the TODOs before pushing

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.StatsEngine;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.TeamStatsReport;

public class TeamStatsActivity extends AppCompatActivity {

    TeamStatsReport m_teamStastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.steamworks2017_activity_team_stats);

        // unbundle the stats data from the intent
        Intent intent = getIntent();

        try {
            m_teamStastReport = (TeamStatsReport) intent.getSerializableExtra(getString(R.string.EXTRA_TEAM_STATS_REPORT));
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

        CheckBox badManners = (CheckBox) findViewById(R.id.badManners);
        badManners.setChecked(m_teamStastReport.badManners);




        /** Auto Mode **/


        if (m_teamStastReport.numMatchesPlayed != 0) {  // protects against divide-by-zero

            // TODO: Need to find out how many successful high/low goal cycles they made in auto -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.autoHighGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.auto_avgNumFuelScoredHigh,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.auto_avgNumFuelScoredHigh) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.autoLowGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.auto_avgNumFuelScoredLow,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.auto_avgNumFuelScoredLow) / m_teamStastReport.numMatchesPlayed));*/

            // TODO: Needs to find missed shots data for auto -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.autoMissedShotsTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.numMissedShotsInAuto,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.numMissedShotsInAuto) / m_teamStastReport.numMatchesPlayed));*/

            ((TextView) findViewById(R.id.crossedBaseLineTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.auto_numTimesCrossedBaseline,
                                                                                m_teamStastReport.numMatchesPlayed,
                                                                                ((double) m_teamStastReport.auto_numTimesCrossedBaseline) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.installedGearTV)).setText(String.format("%d / %d (%.2f)",
                                                                                m_teamStastReport.auto_numGearsDelivered,
                                                                                m_teamStastReport.auto_numGearsFailed,
                                                                                ((double) m_teamStastReport.auto_numGearsDelivered) / m_teamStastReport.auto_numGearsFailed));

        }


        /** Teleop Mode **/


        if (m_teamStastReport.numMatchesPlayed != 0) {  // protects against divide-by-zero

            // TODO: Needs number of high goal attempts -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.teleopHighGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuelScoredHigh_avgPerCycle,
                                                                            120 / m_teamStastReport.numMatchesPlayed,
                                                                            m_teamStastReport.teleop_fuelScoredHigh_avgPerMatch));*/


            ((TextView) findViewById(R.id.teleopHighSetupTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.teleop_fuelHigh_aveCycleTime) );

            // TODO: Needs number of low goal attempts -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.teleopLowGoalsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuelScoredLow_avgPerCycle,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            m_teamStastReport.teleop_fuelScoredLow_avgPerMatch));*/


            ((TextView) findViewById(R.id.teleopLowSetupTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.teleop_fuelLow_aveCycleTime) );


            // TODO: Needs missed shots per cycle for low & high -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.highMissedShots)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuelMissedHigh_avgPerMatch,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.teleop_fuelMissedHigh_avgPerMatch) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.lowMissedShots)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuelMissedLow_avgPerMatch,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.teleop_fuelMissedLow_avgPerMatch) / m_teamStastReport.numMatchesPlayed));*/

            // TODO: Needs to find average pickups per cycle -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.hopperPickupsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuelHopperPickups_avgPerMatch,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.teleop_fuelHopperPickups_avgPerMatch) / m_teamStastReport.numMatchesPlayed));
            ((TextView) findViewById(R.id.chutePickupsTV)).setText(String.format("%d / %d (%.2f)",
                    m_teamStastReport.teleop_fuelWallPickups_avgPerMatch,
                    m_teamStastReport.numMatchesPlayed,
                    ((double) m_teamStastReport.teleop_fuelWallPickups_avgPerMatch) / m_teamStastReport.numMatchesPlayed));

            ((TextView) findViewById(R.id.teleopGroundPickupsTV)).setText(String.format("%d / %d (%.2f)",
                    m_teamStastReport.teleop_fuelGroundPickups_avgPerMatch,
                    m_teamStastReport.numMatchesPlayed,
                    ((double) m_teamStastReport.teleop_fuelGroundPickups_avgPerMatch) / m_teamStastReport.numMatchesPlayed));*/


            // TODO: Needs missed pickups for hoppers, chute & ground -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.teleopFailedPickupsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_fuel,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            ((double) m_teamStastReport.numFailedPickups) / m_teamStastReport.numMatchesPlayed));*/

            ((TextView) findViewById(R.id.teleopTimePlayingDTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.avgTimeSpentPlayingDef) );

            ((TextView) findViewById(R.id.avgDeadnessTV)).setText(String.format("%d%%",
                                                                            m_teamStastReport.avgDeadness) );

            ((TextView) findViewById(R.id.highestDeadnessTV)).setText(String.format("\t%d%%",
                                                                            m_teamStastReport.highestDeadness) );

            ((TextView) findViewById(R.id.climbsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.climbSuccesses,
                                                                            m_teamStastReport.climbAttepmts,
                                                                            ((double) m_teamStastReport.climbSuccesses) / m_teamStastReport.climbAttepmts));

            ((TextView) findViewById(R.id.climbTimeTV)).setText(String.format("\t%.1f s",
                                                                            m_teamStastReport.climb_avgTime) );

            ((TextView) findViewById(R.id.failedClimbsTV)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.climbFailures,
                                                                            m_teamStastReport.climbAttepmts,
                                                                            ((double) m_teamStastReport.climbFailures) / m_teamStastReport.climbAttepmts));


        }

        /** Gears Stuff **/
        if (m_teamStastReport.numMatchesPlayed != 0) { // protects againts divide by zero exception

            // TODO: Needs gears dropped per cycle -- NEEDS MORE DATA
            /*((TextView) findViewById(R.id.teleopMissedGearsTV)).setText(String.format("%d / %d (%.2f)",
                    m_teamStastReport.teleop_gearsDropped_avgPerMatch,
                    m_teamStastReport.numMatchesPlayed,
                    ((double) m_teamStastReport.teleop_gearsDropped_avgPerMatch) / m_teamStastReport.numMatchesPlayed));*/

            // TODO: Needs number of gear installation attempts -- NEEDS MORE DATA
            ((TextView) findViewById(R.id.gearsInstalled)).setText(String.format("%d / %d (%.2f)",
                                                                            m_teamStastReport.teleop_gearsDelivered_avgPerMatch,
                                                                            m_teamStastReport.numMatchesPlayed,
                                                                            m_teamStastReport.teleop_gearsDelivered_avgPerMatch / m_teamStastReport.numMatchesPlayed));

            // TODO: More gears stuff needed

        }


        /** High Shots Map **/
        // TODO: This needs some serious renovation. I might do this later.
        /*final ImageView highGoalMap = (ImageView) findViewById(R.id.mapHigh);
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
        }*/


        /** Low Shots Map **/

        /*final ImageView lowGoalMap = (ImageView) findViewById(R.id.mapLow);
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
        }*/

    }
}
