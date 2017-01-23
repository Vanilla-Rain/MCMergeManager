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
        public AutoScoutingObject autoScoutingObject;

        public Match(PreGameObject preGameObject, PostGameObject postGameObject, TeleopScoutingObject teleopScoutingObject,
                     AutoScoutingObject autoScoutingObject) {
            this.preGameObject = preGameObject;
            this.autoScoutingObject = autoScoutingObject;
            this.postGameObject = postGameObject;
            this.teleopScoutingObject = teleopScoutingObject;
        }

        // TODO: test this code to make sure it works

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            // pre game
            sb.append( String.format("%d,%d,", preGameObject.matchNumber, preGameObject.teamNumber));

            // autonomous mode
            sb.append( String.format("%b,%b,%d,%b,%d,%d,", autoScoutingObject.start_gear, autoScoutingObject.start_fuel,
                    autoScoutingObject.gear_delivered, autoScoutingObject.boiler, autoScoutingObject.accuracy,
                    autoScoutingObject.open_hopper));

            // teleop mode

            // fuel
            //cycle time
            sb.append(teleopScoutingObject.fuelCycleTime + ",");

            // fuel cycles
            sb.append("{");
            for(int i = 0; i < teleopScoutingObject.fuelShots.size(); i++) {
                FuelShot fuelShot = teleopScoutingObject.fuelShots.get(i);

                sb.append(String.format("{%.2f;%d;%d;%b;%d;%d;%d}", fuelShot.time, fuelShot.pickupLocation, fuelShot.amount, fuelShot.boiler,
                        fuelShot.accuracy, fuelShot.x, fuelShot.y));

                if(i < teleopScoutingObject.fuelShots.size())
                    sb.append(":");
            }
            sb.append("},");

            // gears
            // cycle time
            sb.append(teleopScoutingObject.gearCycleTime + ",");

            // gear cycles
            sb.append("{");
            for(int i = 0; i < teleopScoutingObject.gears.size(); i++) {
                Gear gear = teleopScoutingObject.gears.get(i);

                sb.append(String.format("{%.2f;%b;%d;%d}", gear.time, gear.wall, gear.lift, gear.dropped));

                if(i < teleopScoutingObject.gears.size())
                    sb.append(":");
            }
            sb.append("},");

            // post game stuff
            sb.append(String.format("%b,%d,%d,", postGameObject.climb, postGameObject.climb_time, postGameObject.time_dead));

            // since commas, semi-colons, braces, and <enter> are all special characters for the text file, let's rip those out just to be safe.
            String cleanedNotes = postGameObject.notes.replaceAll(",","").replaceAll(";","").replaceAll("\\{","")
                    .replaceAll("\\}","").replaceAll("\n","");
            sb.append(cleanedNotes+",");

            // new line for next match
            sb.append("\n");

            return sb.toString();
        }

        public Match(String str) {
            preGameObject = new PreGameObject();
            autoScoutingObject = new AutoScoutingObject();
            teleopScoutingObject = new TeleopScoutingObject();
            postGameObject = new PostGameObject();

            String[] tokens = str.split(",");

            // pre-game
            preGameObject.matchNumber = Integer.valueOf(tokens[0]);
            preGameObject.teamNumber = Integer.valueOf(tokens[1]);

            // automode
            autoScoutingObject.start_gear = Boolean.valueOf(tokens[2]);
            autoScoutingObject.start_fuel = Boolean.valueOf(tokens[3]);
            autoScoutingObject.gear_delivered = Integer.valueOf(tokens[4]);
            autoScoutingObject.boiler = Boolean.valueOf(tokens[5]);
            autoScoutingObject.accuracy = Integer.valueOf(tokens[6]);
            autoScoutingObject.open_hopper = Integer.valueOf(tokens[7]);

            // teleopmode

            // fuel
            teleopScoutingObject.fuelCycleTime = Double.valueOf(tokens[8]);
            if(!tokens[9].equals("")) {
                String[] fuel = tokens[9].split(":");
                for(String s : fuel) {
                    String[] fuelTokens = s.split(";");
                    teleopScoutingObject.fuelShots.add(new FuelShot(Double.valueOf(fuelTokens[0]),
                            Integer.valueOf(fuelTokens[1]), Integer.valueOf(fuelTokens[2]),
                            Boolean.valueOf(fuelTokens[3]), Integer.valueOf(fuelTokens[4]),
                            Integer.valueOf(fuelTokens[5]), Integer.valueOf(fuelTokens[6])));
                }
            }

            // gears
            teleopScoutingObject.gearCycleTime = Double.valueOf(tokens[10]);
            if(!tokens[11].equals("")) {
                String[] gear = tokens[11].split(":");
                for (String s : gear) {
                    String[] gearTokens = s.split(";");
                    teleopScoutingObject.gears.add(new Gear(Double.valueOf(gearTokens[0]),
                            Boolean.valueOf(gearTokens[1]), Integer.valueOf(gearTokens[2]),
                            Integer.valueOf(gearTokens[3])));
                }
            }

            // post game
            postGameObject.climb = Integer.valueOf(tokens[12]);
            postGameObject.climb_time = Double.valueOf(tokens[13]);
            postGameObject.time_dead = Integer.valueOf(tokens[14]);

            postGameObject.notes = tokens[15];
        }

    }
}
