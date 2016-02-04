package ca.team2706.scouting.mcmergemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.datamodels.BallShot;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchData;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;

/**
 * Created by mike on 04/02/16.
 */
public class StatsEngine {

    public class TeamStatsReport {
        public int teamNo;
        public int numMatchesPlayed;
        public int wins;
        public int losses;
        public int ties;
        public double ORP;
        public double DPR;
        public double scheduleToughness;

        public List<BallShot> missedTeleopShots;
        public List<BallShot> successfulTeleopShots;

        public int[] defensesBreached;

    }





    private MatchData matchData;
    private MatchSchedule matchSchedule;

    /** Contructor **/
    public StatsEngine(MatchData matchData, MatchSchedule matchSchedule) {
        this.matchData = matchData;
        this.matchSchedule = matchSchedule;
    }


    public TeamStatsReport getTeamStatsReport(int teamNo) {
        // TODO
    }

    public List<MatchData.Match> getMatchesForTeam(int teamNo) {
        List<MatchData.Match> matches = new ArrayList<>();

        for(MatchData.Match match : matchData.matches) {
            if (match.preGame.teamNumber == teamNo)
                matches.add(match);
        }
        return matches;
    }

    public List<BallShot> getMissedTeleopShotsByTeam(int teamNo) {

        List<BallShot> missedShots = new ArrayList<>();

        for(MatchData.Match match : getMatchesForTeam(teamNo)) {
            for(BallShot shot : match.teleopMode.ballsShot) {
                if (shot.whichGoal == shot.MISS)
                    missedShots.add(shot);
            }
        }

        return missedShots;
    }

    public List<BallShot> getSuccessfulTeleopShotsByTeam(int teamNo) {

        List<BallShot> succShots = new ArrayList<>();

        for(MatchData.Match match : getMatchesForTeam(teamNo) ) {
            for(BallShot shot : match.teleopMode.ballsShot) {
                if (shot.whichGoal == shot.HIGH_GOAL || shot.whichGoal == shot.LOW_GOAL)
                    succShots.add(shot);
            }
        }

        return succShots;
    }


    /**
     * This includes both auto and teleop breaches.
     *
     * @return an array of 8 ints holding the counts. The constants in TeleopScoutindObject can be used as indices to this array.
     */
    public int[] getNumberOfBreaches(int teamNo) {
        int[] breaches = new int[8];

        for(MatchData.Match match : getMatchesForTeam(teamNo) ) {
            // combine auto and teleop
            List<Integer> bs = match.autoMode.defensesBreached;
            bs.addAll(match.teleopMode.defensesBreached);

            for(Integer breach : bs) {
                breaches[breach]++;
            }
        }

        return breaches;
    }

}
