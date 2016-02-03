package ca.team2706.scouting.mcmergemanager;

import java.util.ArrayList;

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







    /** An inner-class encapsulating the data from a single robot from a single match. **/
    public class Match {

        // Member Variables
        public int matchNo;
        public int teamScouted;
        // blue & red scores?

        public AutoMode autoMode;
        public TeleopMode teleopMode;

        /** Empty constructor **/
        public Match() {

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



    /** An inner-class encapsulating the data from a single robot from a single autonomous mode. **/
    public class AutoMode {

        // Member Variables
        // TODO: What data's being collected here?

        /** Empty constructor **/
        public AutoMode() {

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
        public AutoMode(String serializedMatchData) {
            // TODO
        }
    }


    /** An inner-class encapsulating the data from a single robot from a single teleop mode. **/
    public class TeleopMode {

        // Member Variables
        // TODO: What data's being collected here?

        /** Empty constructor **/
        public TeleopMode() {

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
        public TeleopMode(String serializedMatchData) {
            // TODO
        }
    }
}
