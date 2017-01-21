package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by dwall on 20/01/17.
 */

public class MatchData {

    // Member Variables
    public ArrayList<Match> matches;

    /** Empty constructor **/
    public MatchData() {
        matches = new ArrayList<>();
    }

    public void addMatch(Match match) {
        matches.add(match);
    }


    // String serializers to read / write it to file

    /**
     * toString() turns it into a string
     */
    public static class Match implements Serializable{

        public PreGameObject preGameObject;
        public PostGameObject postGameObject;
        public TeleopScoutingObject teleopScoutingObject;
        public AutoModeObject autoModeObject;

        public Match(PreGameObject preGameObject, PostGameObject postGameObject, TeleopScoutingObject teleopScoutingObject,
                     AutoModeObject autoModeObject) {
            this.preGameObject = preGameObject;
            this.autoModeObject = autoModeObject;
            this.postGameObject = postGameObject;
            this.teleopScoutingObject = teleopScoutingObject;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            // pre game
            sb.append( String.format("%d,%d", preGameObject.matchNumber, preGameObject.teamNumber));

            // autonomous mode
            sb.append( String.format("%b,%b,%d,%b,%d,%d", autoModeObject.start_gear, autoModeObject.start_fuel,
                    autoModeObject.gear_delivered, autoModeObject.boiler, autoModeObject.accuracy,
                    autoModeObject.open_hopper));
//
//            // teleop mode
//            sb.append("{");
//            for(int i = 0; i < teleopScoutingObject.)
            return "";
        }

    }
}
