package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Jama.*;

import ca.team2706.scouting.mcmergemanager.datamodels.BallPickup;
import ca.team2706.scouting.mcmergemanager.datamodels.BallShot;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchData;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.datamodels.ScalingTime;
import ca.team2706.scouting.mcmergemanager.datamodels.TeleopScoutingObject;

/**
 * Created by mike on 04/02/16.
 */
public class StatsEngine implements Serializable{

    public class TeamStatsReport implements Serializable {

        // Overall Stats
        public int    teamNo;
        public int    numMatchesPlayed;
        public int    wins;
        public int    losses;
        public int    ties;
        public double OPR;  // Offensive Power Rating, a standard stat in FRC
        public double DPR;  // Defensive Power Rating, a standard stat in FRC
        // could also include CCWM, and PMR, see http://www.chiefdelphi.com/media/papers/2174
        public double scheduleToughness;    // Whether, on average, their opponents were stronger than their allies, or the other way around.
                                            // ie.: Are they seeded artificially high or artificially low by schedule luck?

        // Auto Stats
        public int numTimesReachedInAuto;
        public int numTimesBreachedInAuto;
        public int numTimesSpyBot;
        public int numSuccHighShotsInAuto;
        public int numSuccLowShotsInAuto;
        public int numMissedShotsInAuto;

        // Teleop Stats
        public List<BallShot> missedTeleopShots;         // to draw pins on map
        public List<BallShot> successfulTeleopHighShots; // to draw pins on map
        public List<BallShot> successfulTeleopLowShots;  // to draw pins on map
        public int    numSuccHighShotsInTeleop;
        public int    numSuccLowShotsInTeleop;
        public int    numMissedShotsInTeleop;
        public double avgHighShotTime;
        public double avgLowShotTime;
        public double avgTimeSpentPlayingDef;
        public int    numSuccPickupsFromGround;
        public int    numSuccPickupsFromWall;
        public int    numFailedPickups;
        public int    numTimesChallenged;
        public int    avgDeadness;  // an int representing % of match spent wich mech problems
        public int    highestDeadness;

        /** Counts of how many times they've successfully breached each defense, including both auto and teleop.
         * Note that this is an array of 9 items where the 0th item is not used. **/
        public int[] defensesBreached;

        // Scaling
        public int    numSuccessfulScales;
        public int    numFailedScales;
        public double avgScaleTime;


        public MatchSchedule teamMatcheSchedule;
        public MatchData teamMatchData;

        /** Private constructor so that it can only be generated by a StatsEngine instance **/
        private TeamStatsReport() {
            defensesBreached = new int[TeleopScoutingObject.NUM_DEFENSES];
            missedTeleopShots = new ArrayList<>();
            successfulTeleopHighShots = new ArrayList<>();
            successfulTeleopLowShots = new ArrayList<>();
        }

    }






    /** Class Stats Engine **/

    private MatchData matchData;
    private MatchSchedule matchSchedule;

    /** Contructor **/
    public StatsEngine(MatchData matchData, MatchSchedule matchSchedule) {
        this.matchData = matchData;
        this.matchSchedule = matchSchedule;
    }

    /** This constructor meant more for testing than for actual use **/
    public StatsEngine(MatchSchedule matchSchedule) {
        this.matchSchedule = matchSchedule;

        computeOPRs();
    }


    /**
     * Fill in a TeamStatsReport about the given team.
     */
    public TeamStatsReport getTeamStatsReport(int teamNo) {
        TeamStatsReport teamStatsReport = new TeamStatsReport();

        fillInOverallStats(teamStatsReport, teamNo);
        fillInAutoStats(teamStatsReport, teamNo);
        fillInTeleopStats(teamStatsReport, teamNo);

        return teamStatsReport;
    }





    // mapping team numbers to various stats
    private Map<Integer, Double> OPRs;
    private Map<Integer, Double> DPRs;
    private Map<Integer, WLT> records;  // need for computation of schedule toughness


    private class WLT implements Serializable {
        int wins;
        int losses;
        int ties;

        public void addWin()  { wins++; }
        public void addLoss() { losses++; }
        public void addTie()  { ties++; }
    }

    public Map<Integer, Double> getOPRs() {
        return OPRs;
    }


    private void computeDPRs() {
        // TODO
        DPRs = new HashMap<>();
    }

    /** Do some linear algebra to compute Offensive Power Rating for each team. **/
    private void computeOPRs() {
        if ( matchSchedule == null)
            return;


        // First, build the participation matrix
        // ie which matches did which team participate in.
        // Only include matches that we have a score for.

        // Figure out what the axis are for our matrix
        ArrayList<Integer> teams = new ArrayList<>();
        HashMap<Integer, Integer> blueScores = new HashMap<>();  // map of match #'s to scores
        HashMap<Integer, Integer> redScores = new HashMap<>();  // map of match #'s to scores

        for(MatchSchedule.Match match : matchSchedule.getMatches()) {
            if (match.getBlueScore() != -1) {
                blueScores.put(match.getMatchNo(), match.getBlueScore());

                if (!teams.contains(match.getBlue1()))
                    teams.add(match.getBlue1());

                if (!teams.contains(match.getBlue2()))
                    teams.add(match.getBlue2());

                if (!teams.contains(match.getBlue3()))
                    teams.add(match.getBlue3());
            }

            if (match.getRedScore() != -1) {
                redScores.put(match.getMatchNo(), match.getRedScore());

                if (! teams.contains(match.getRed1()) )
                    teams.add(match.getRed1());

                if (! teams.contains(match.getRed2()) )
                    teams.add(match.getRed2());

                if (! teams.contains(match.getRed3()) )
                    teams.add(match.getRed3());
            }
        }

        // sort all arrays and use position in array as indices into the matrix
        ArrayList<Integer> blueMatcheNos = new ArrayList<>(blueScores.keySet());
        ArrayList<Integer> redMatcheNos = new ArrayList<>(redScores.keySet());

        Collections.sort(teams);
        Collections.sort(blueMatcheNos);
        Collections.sort(redMatcheNos);

        int offset = blueScores.size();

        // build the matrix
        // This is a participation matrix with the matches in the first axis, and teams in the second.
        // If a team has participated in a match, that cell gets a 1, otherwise it gets a 0.
        // Note that we treat the blue and red alliances as if they were playing completely seperate matches for the sake of OPR.
        double[][] Mdbl = new double[blueScores.size()+redScores.size()][teams.size()];
        double[] allScores = new double[blueScores.size()+redScores.size()];

        for(MatchSchedule.Match match : matchSchedule.getMatches()) {
            if (match.getBlueScore() != -1) {
                int matchNoIdx = blueMatcheNos.indexOf(match.getMatchNo());
                Mdbl[matchNoIdx][ teams.indexOf(match.getBlue1()) ] = 1;
                Mdbl[matchNoIdx][ teams.indexOf(match.getBlue2()) ] = 1;
                Mdbl[matchNoIdx][ teams.indexOf(match.getBlue3()) ] = 1;

                allScores[matchNoIdx] = match.getBlueScore();
            }

            if (match.getRedScore() != -1) {
                int matchNoIdx = blueMatcheNos.indexOf(match.getMatchNo()) + offset;
                Mdbl[matchNoIdx][ teams.indexOf(match.getRed1()) ] = 1;
                Mdbl[matchNoIdx][ teams.indexOf(match.getRed2()) ] = 1;
                Mdbl[matchNoIdx][ teams.indexOf(match.getRed3()) ] = 1;

                allScores[matchNoIdx] = match.getRedScore();
            }
        }

        Matrix M = new Matrix(Mdbl);
        Matrix Y = new Matrix(allScores, allScores.length);

        Matrix MatOPRs = M.transpose().times(M).inverse().times(M.transpose()).times(Y);


        // now that we have the data, fill in the hashmap
        OPRs = new HashMap<>();
        for(int i=0; i<teams.size(); i++) {
            OPRs.put(teams.get(i), MatOPRs.get(i, 0));
        }

    }

    private void computeRecords() {
        records = new HashMap<>();

        for(MatchSchedule.Match match : matchSchedule.getMatches()) {
            if (match.getBlueScore() == -1 || match.getRedScore() == -1)
                continue;   // this match has not been played yet

            if (match.getBlueScore() > match.getRedScore()) {
                createAndGetTeam(match.getBlue1()).addWin();
                createAndGetTeam(match.getBlue2()).addWin();
                createAndGetTeam(match.getBlue3()).addWin();
                createAndGetTeam(match.getRed1()).addLoss();
                createAndGetTeam(match.getRed2()).addLoss();
                createAndGetTeam(match.getRed3()).addLoss();
            } else if (match.getBlueScore() < match.getRedScore()) {
                createAndGetTeam(match.getBlue1()).addLoss();
                createAndGetTeam(match.getBlue2()).addLoss();
                createAndGetTeam(match.getBlue3()).addLoss();
                createAndGetTeam(match.getRed1()).addWin();
                createAndGetTeam(match.getRed2()).addWin();
                createAndGetTeam(match.getRed3()).addWin();
            } else {
                createAndGetTeam(match.getBlue1()).addTie();
                createAndGetTeam(match.getBlue2()).addTie();
                createAndGetTeam(match.getBlue3()).addTie();
                createAndGetTeam(match.getRed1()).addTie();
                createAndGetTeam(match.getRed2()).addTie();
                createAndGetTeam(match.getRed3()).addTie();
            }
        }
    }

    /** I wish this was included in the default Map interface **/
    private WLT createAndGetTeam(int teamNo) {
        if (!records.containsKey(teamNo))
            records.put(teamNo, new WLT());

        return records.get(teamNo);
    }

    /**
     * Schedule toughness is a measure of your opponents' wins over your allies' wins.
     * A toughness of 1.0 is a neutral schedule.
     */
    private double computeScheduleToughness(int teamNo) {
        if (records == null)
            computeRecords();

        int alliesWins = 0;
        int opponentsWins = 0;

        for(MatchSchedule.Match match : matchSchedule.filterByTeam(teamNo).getMatches()) {
            // am I blue or red?
            if (match.getBlue1() == teamNo || match.getBlue2() == teamNo || match.getBlue3() == teamNo) {
                if (match.getBlue1() != teamNo)
                    alliesWins += records.get(match.getBlue1()).wins;

                if (match.getBlue2() != teamNo)
                    alliesWins += records.get(match.getBlue2()).wins;

                if (match.getBlue3() != teamNo)
                    alliesWins += records.get(match.getBlue3()).wins;

                opponentsWins += records.get(match.getRed1()).wins;
                opponentsWins += records.get(match.getRed2()).wins;
                opponentsWins += records.get(match.getRed3()).wins;
            }
            else {
                if (match.getRed1() != teamNo)
                    alliesWins += records.get(match.getRed1()).wins;

                if (match.getRed2() != teamNo)
                    alliesWins += records.get(match.getRed2()).wins;

                if (match.getRed3() != teamNo)
                    alliesWins += records.get(match.getRed3()).wins;

                opponentsWins += records.get(match.getBlue1()).wins;
                opponentsWins += records.get(match.getBlue2()).wins;
                opponentsWins += records.get(match.getBlue3()).wins;
            }
        }

        if (alliesWins == 0) {
            if (opponentsWins == 0)
                return 1.0;
            else
                return Double.POSITIVE_INFINITY;
        }

        return (((double) opponentsWins * 2) / (alliesWins * 3));
    }


    /**
     * // Auto Stats
     * public int numTimesReachedInAuto;
     * public int numTimesBreachedInAuto;
     * public int numTimesSpyBot;
     * public int numSuccHighShotsInAuto;
     * public int numSuccLowShotsInAuto;
     * public int numMissedShotsInAuto;
     */
    private void fillInAutoStats(TeamStatsReport teamStatsReport, int teamNo) {

        if (teamStatsReport.teamMatchData == null) {
            if (matchData == null)
                return;

            teamStatsReport.teamMatchData = matchData.filterByTeam(teamNo);
        }

        for(MatchData.Match match : teamStatsReport.teamMatchData.matches) {
            teamStatsReport.numTimesReachedInAuto += match.autoMode.reachedDefense ? 1 : 0;

            // breaches
            for(Integer breach : match.autoMode.defensesBreached) {
                teamStatsReport.defensesBreached[breach]++;
            }
            teamStatsReport.numTimesBreachedInAuto += match.autoMode.defensesBreached.size() > 0 ? 1 : 0;

            teamStatsReport.numTimesSpyBot += match.autoMode.isSpyBot ? 1 : 0;

            for(BallShot shot : match.autoMode.ballsShot) {
                switch (shot.whichGoal) {

                    case BallShot.HIGH_GOAL:
                        teamStatsReport.numSuccHighShotsInAuto++;
                        break;

                    case BallShot.LOW_GOAL:
                        teamStatsReport.numSuccLowShotsInAuto++;
                        break;

                    case BallShot.MISS:
                        teamStatsReport.numMissedShotsInAuto++;
                        break;
                }
            }
        }
    }

    /**
     *
     * // Overall Stats
     * public int    teamNo;
     * public int    numMatchesPlayed;
     * public int    wins;
     * public int    losses;
     * public int    ties;
     * public double OPR;
     * public double DPR;
     * public double scheduleToughness;
     */
    private void fillInOverallStats(TeamStatsReport teamStatsReport, int teamNo) {
        teamStatsReport.teamNo = teamNo;

        if (teamStatsReport.teamMatcheSchedule == null) {
            if (matchSchedule == null)
                return;

            teamStatsReport.teamMatcheSchedule = matchSchedule.filterByTeam(teamNo);
        }

        teamStatsReport.numMatchesPlayed = teamStatsReport.teamMatcheSchedule.getMatches().size();

        if (records == null)
            computeRecords();

        if (records.get(teamNo) != null) {
            teamStatsReport.wins = records.get(teamNo).wins;
            teamStatsReport.losses = records.get(teamNo).losses;
            teamStatsReport.ties = records.get(teamNo).ties;
        }

        if (OPRs == null)
            computeOPRs();

        if (OPRs.get(teamNo) != null)
            teamStatsReport.OPR = OPRs.get(teamNo);

        if (DPRs == null)
            computeDPRs();

        if (DPRs.get(teamNo) != null)
            teamStatsReport.DPR = DPRs.get(teamNo);

        teamStatsReport.scheduleToughness = computeScheduleToughness(teamNo);

    }

    /**
     * public List<BallShot> missedTeleopShots;         // to draw pins on map
     * public List<BallShot> successfulTeleopHighShots; // to draw pins on map
     * public List<BallShot> successfulTeleopLowShots;  // to draw pins on map
     * public int    numSuccHighShotsInTeleop;
     * public int    numSuccLowShotsInTeleop;
     * public int    numMissedShotsInTeleop;
     * public double avgHighShotTime;
     * public double avgLowShotTime;
     * public double avgTimeSpentPlayingDef;
     * public int    numSuccPickupsFromGround;
     * public int    numSuccPickupsFromWall;
     * public int    numFailedPickups;
     * public int    numTimesChallenged;
     * public double avgDeadness;  // an int representing % of match spent wich mech problems
     * public double highestDeadness;
     *
     * public int[] defensesBreached;
     *
     * public int    numSuccessfulScales;
     * public int    numFailedScales;
     * public double avgScaleTime;
     */
    private void fillInTeleopStats(TeamStatsReport teamStatsReport, int teamNo) {
        // assumption: the teamStatsReport starts zeroed out.

        if (teamStatsReport.teamMatchData == null) {
            if (matchData == null)
                return;
            teamStatsReport.teamMatchData = matchData.filterByTeam(teamNo);
        }

        for(MatchData.Match match : teamStatsReport.teamMatchData.matches) {
            for (BallShot shot : match.teleopMode.ballsShot) {
                switch (shot.whichGoal) {

                    case BallShot.HIGH_GOAL:
                        teamStatsReport.numSuccHighShotsInTeleop++;
                        teamStatsReport.avgHighShotTime += shot.shootTime;
                        teamStatsReport.successfulTeleopHighShots.add(shot);
                        break;

                    case BallShot.LOW_GOAL:
                        teamStatsReport.numSuccLowShotsInTeleop++;
                        teamStatsReport.avgLowShotTime += shot.shootTime;
                        teamStatsReport.successfulTeleopLowShots.add(shot);
                        break;

                    case BallShot.MISS:
                        teamStatsReport.numMissedShotsInTeleop++;
                        teamStatsReport.missedTeleopShots.add(shot);
                }
            }
            teamStatsReport.avgTimeSpentPlayingDef += match.teleopMode.timeDefending;


            for(BallPickup pickup : match.teleopMode.ballsPickedUp) {
                switch (pickup.selection) {
                    case BallPickup.GROUND:
                        teamStatsReport.numSuccPickupsFromGround++;
                        break;

                    case BallPickup.WALL:
                        teamStatsReport.numSuccPickupsFromWall++;
                        break;

                    case BallPickup.FAIL:
                        teamStatsReport.numFailedPickups++;
                        break;
                }
            }

            teamStatsReport.numTimesChallenged += match.postGame.challenged ? 1 : 0;
            teamStatsReport.avgDeadness += match.postGame.timeDead;

            if(teamStatsReport.highestDeadness < match.postGame.timeDead)
                teamStatsReport.highestDeadness = match.postGame.timeDead;

            for(Integer breach : match.teleopMode.defensesBreached)
                teamStatsReport.defensesBreached[breach]++;

            // scales
            for(ScalingTime scale : match.teleopMode.scalingTower) {
                if (scale.completed != 0)
                    teamStatsReport.numSuccessfulScales++;
                else
                    teamStatsReport.numFailedScales++;

                teamStatsReport.avgScaleTime += scale.time;
            }

        } // end loop over matches

        if (teamStatsReport.teamMatchData.matches.size() != 0) {
            teamStatsReport.avgHighShotTime /= teamStatsReport.numSuccHighShotsInTeleop;
            teamStatsReport.avgLowShotTime /= teamStatsReport.numSuccLowShotsInTeleop;
            teamStatsReport.avgTimeSpentPlayingDef /= teamStatsReport.teamMatchData.matches.size();
            teamStatsReport.avgDeadness /= teamStatsReport.teamMatchData.matches.size();
            teamStatsReport.avgScaleTime /= teamStatsReport.numSuccessfulScales + teamStatsReport.numFailedScales;
        }
    }

}
