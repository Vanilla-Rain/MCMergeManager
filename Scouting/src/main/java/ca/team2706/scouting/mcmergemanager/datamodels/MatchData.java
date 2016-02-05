package ca.team2706.scouting.mcmergemanager.datamodels;

import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.datamodels.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.datamodels.PostGameObject;
import ca.team2706.scouting.mcmergemanager.datamodels.PreGameObject;
import ca.team2706.scouting.mcmergemanager.datamodels.TeleopScoutingObject;

/**
 * A class to encapsulate all the data collected during a match so that it can be easily
 * passed around, saved to file, loaded from file, etc.
 *
 * Created by mike on 03/02/16.
 */
public class MatchData {
    
    // Member Variables
    public ArrayList<Match> matches;

    /** Empty constructor **/
    public MatchData() {

    }
    
    public void addMatch(Match match) {
        matches.add(match);
    }


    // String serializers so that it can be easily passed through intents

    /**
     * toString() turns it into a string
     */
    @Override
    public String toString() {
        // TODO
        return "";
    }

    /** De-serializing constructor **/
    public MatchData(String serializedMatchData) {
        // TODO
    }



    public MatchData filterByTeam(int teamNo) {

        MatchData matchData = new MatchData();

        for(Match match : matches) {
            if (match.preGame.teamNumber == teamNo)
                matchData.addMatch(match);
        }
        return new MatchData();
    }




    /** An inner-class encapsulating the data from a single robot from a single match. **/
    public static class Match {

        public PreGameObject preGame;
        public AutoScoutingObject autoMode;
        public TeleopScoutingObject teleopMode;
        public PostGameObject postGame;


        /** Empty constructor **/
        public Match() {

        }


        /** All the things costructor **/
        public Match(PreGameObject preGame, AutoScoutingObject autoMode, TeleopScoutingObject teleopMode, PostGameObject postGame) {
            this.preGame = preGame;
            this.autoMode = autoMode;
            this.teleopMode = teleopMode;
            this.postGame = postGame;
        }


        // String serializers so that it can be easily passed through intents

        /**
         * toString() turns it into a string
         */
        @Override
        public String toString() {
            // TODO
            return "";
        }

        /** De-serializing constructor **/
        public Match(String serializedMatchData) {
            // TODO
        }

    }

}
