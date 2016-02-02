package ca.team2706.scouting.mcmergemanager;

import android.util.Log;

import org.json.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 31/01/16.
 */
public class MatchSchedule {

    public class Match {
        private int matchNo;

        private int red1;
        private int red2;
        private int red3;
        private int blue1;
        private int blue2;
        private int blue3;

        // = -1 if the match has not been played yet.
        private int redScore;
        private int blueScore;


        // getters - this is how other classes will access the data (they can read it, but not change it)
        public int getMatchNo() { return matchNo; }
        public int getRed1() { return red1; }
        public int getRed2() { return red2; }
        public int getRed3() { return red3; }
        public int getBlue1() { return blue1; }
        public int getBlue2() { return blue2; }
        public int getBlue3() { return blue3; }
        public int getRedScore() { return redScore; }
        public int getBlueScore() { return blueScore; }

        // setters (they are private so that other classes can read the data, but not change it)
        private void setMatchNo(int matchNo) { this.matchNo = matchNo; }
        private void setRed1(int red1) { this.red1 = red1; }
        private void setRed2(int red2) { this.red2 = red2; }
        private void setRed3(int red3) { this.red3 = red3; }
        private void setBlue1(int blue1) { this.blue1 = blue1; }
        private void setBlue2(int blue2) { this.blue2 = blue2; }
        private void setBlue3(int blue3) { this.blue3 = blue3; }
        private void setRedScore(int redScore) { this.redScore = redScore; }
        private void setBlueScore(int blueScore) { this.blueScore = blueScore; }
    }

    private String matchScheduleJSONstr;

    private List<Match> matches = new ArrayList<Match>();

    public List<Match> getMatches(){ return matches; }

    public Match getMatchNo(int i){ return matches.get(i); }


    /** Constructors **/
    public MatchSchedule() {
        super();
    }

    /**
     * A costructor that parses the schedule as delivered by TheBlueAlliance.
     * @param jsonSchedule the match schedule data as returned by thebluealliance.com
     */
    public MatchSchedule(String jsonSchedule) {
        super();
        parseTBAData(jsonSchedule);
    }

    public String toString() { return matchScheduleJSONstr; }

    public void parseTBAData(String jsonSchedule) {

        matchScheduleJSONstr = jsonSchedule;

        JSONArray jsonArr;
        try {
            jsonArr = new JSONArray(jsonSchedule);

            // loop over individual matches
            for(int i=0;i<(jsonArr.length());i++)
            {
                JSONObject jsonMatch=jsonArr.getJSONObject(i);

                // I'm only interested in qualification matches
                String comp_level = jsonMatch.getString("comp_level");
                if (!comp_level.equals("qm")) {
                    continue;
                }

                Match match = new Match();

                match.setMatchNo(jsonMatch.getInt("match_number"));

                JSONObject jsonBlueAlliance = jsonMatch.getJSONObject("alliances").getJSONObject("blue");
                JSONObject jsonRedAlliance = jsonMatch.getJSONObject("alliances").getJSONObject("red");

                match.setBlueScore(jsonBlueAlliance.getInt("score"));
                match.setRedScore(jsonRedAlliance.getInt("score"));

                JSONArray blueTeams = jsonBlueAlliance.getJSONArray("teams");
                match.setBlue1(Integer.parseInt(blueTeams.getString(0).substring(3))); // TBA gives it to us as "frc2706", so skip the first 3 characters
                match.setBlue2(Integer.parseInt(blueTeams.getString(1).substring(3)));
                match.setBlue3(Integer.parseInt(blueTeams.getString(2).substring(3)));

                JSONArray redTeams = jsonRedAlliance.getJSONArray("teams");
                match.setRed1(Integer.parseInt(redTeams.getString(0).substring(3)));
                match.setRed2(Integer.parseInt(redTeams.getString(1).substring(3)));
                match.setRed3(Integer.parseInt(redTeams.getString(2).substring(3)));

                matches.add(match);
            }

        } catch(JSONException e) {
            // something went wrong
            Log.e("MCMergeManager", "Failed to parse the match schedule from thebluealliance. Maybe the data is not valid json?");
            return;
        }
    }

}
