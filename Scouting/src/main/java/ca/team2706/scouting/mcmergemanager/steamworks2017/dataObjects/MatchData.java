package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class MatchData implements Serializable {

    // objective ids
    public static final int startFuelID = 25;
    public static final int startGearID = 26;
    public static final int crossedBaselineID = 27;
    public static final int gearDeliveredID = 28;
    public static final int boilerAttemptedID = 29;
    public static final int openHopperID = 31;
    public static final int climbID = 32;



    public static class Match implements Serializable {

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

        // TODO: this needs to be re-worked because MikeO changed around the data

        public Match(JSONObject jsonObject) {
            preGameObject = new PreGameObject();
            postGameObject = new PostGameObject();
            teleopScoutingObject = new TeleopScoutingObject();
            autoScoutingObject = new AutoScoutingObject();

            try {
                // pregame
                preGameObject.teamNumber = jsonObject.getInt("team_number");
                preGameObject.matchNumber = jsonObject.getInt("number");

                // autonomous

                autoScoutingObject.crossedBaseline = jsonObject.getBoolean("crossed_baseline");
                autoScoutingObject.start_fuel = jsonObject.getBoolean("start_fuel");
                autoScoutingObject.start_gear = jsonObject.getBoolean("start_gear");
                autoScoutingObject.boiler_attempted = jsonObject.getInt("boiler_attempted");
                autoScoutingObject.gear_delivered = jsonObject.getInt("gear_delivered");
                autoScoutingObject.open_hopper = jsonObject.getInt("open_hopper");

                // teleop
                JSONArray arrEve = (JSONArray) jsonObject.get("events");
                for(int i = 0; i < arrEve.length(); i++) {
                    JSONObject obj = new JSONObject(arrEve.get(i).toString());
                    Event event;
                    switch((int) obj.get("objective_id")) {
                        case FuelShotEvent.objectiveId:
                            event = new FuelShotEvent(obj.getDouble("start_time"), (boolean) obj.get("boiler"),
                                    (int) obj.get("fuel_scored"), (int )obj.get("fuel_missed"));
                            break;
                        case FuelPickupEvent.objectiveId:
                            try {
                                event = new FuelPickupEvent(obj.getDouble("start_time"),
                                        FuelPickupEvent.FuelPickupType.valueOf((String) obj.get("type")),
                                        (int) obj.get("fuel_amount"));
                            } catch(IllegalArgumentException e) {
                                Log.d("FuelPickupType error", e.toString());
                                event = new Event(FuelPickupEvent.objectiveId); // should I change this to something else, such as a default pickup type?
                            }
                            break;
                        case GearDelivevryEvent.objectiveId:
                            try {
                                event = new GearDelivevryEvent(obj.getDouble("start_time"),
                                        GearDelivevryEvent.Lift.valueOf((String) obj.get("lift")));
                            } catch(IllegalArgumentException e) {
                                Log.d("GearDeliveryEvent error", e.toString());
                                event = new Event(GearDelivevryEvent.objectiveId);
                            }
                            break;
                        case GearPickupEvent.objectiveId:
                            try {
                                event = new GearPickupEvent(obj.getDouble("start_time"),
                                        GearPickupEvent.GearPickupType.valueOf((String) obj.get("type")),
                                        (boolean) obj.get("success"));
                            } catch (IllegalArgumentException e) {
                                Log.d("GearPickupError", e.toString());
                                event = new Event(GearPickupEvent.objectiveId);
                            }
                            break;
                        case DefenseEvent.objectiveId:
                            event = new DefenseEvent(obj.getDouble("start_time"), (int) obj.get("defense_skill"));
                            break;
                        default:
                            event = new Event(DefenseEvent.objectiveId);
                            break;
                    }
                    teleopScoutingObject.add(event);
                }

                // postgame
                postGameObject.climb_time = jsonObject.getDouble("climb_time");
                postGameObject.climbType = PostGameObject.ClimbType.valueOf(jsonObject.getString("climb_type"));
                postGameObject.notes = jsonObject.getString("general_notes");
                postGameObject.time_dead = jsonObject.getDouble("time_dead");

            } catch(JSONException e) {
                Log.d("Error parsing json", e.toString());
            } catch(IllegalArgumentException e) {
                Log.d("enum failure", e.toString());
            }

        }


        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();

            try {
                // pregame
                jsonObject.put("team_number", preGameObject.teamNumber);
                jsonObject.put("number", preGameObject.matchNumber);

                // autonomous
                JSONArray arrAuto = new JSONArray();
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", startFuelID);
                    obj.put("success", autoScoutingObject.start_fuel);
                    arrAuto.put(obj);
                }
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", startGearID);
                    obj.put("success", autoScoutingObject.start_gear);
                    arrAuto.put(obj);
                }
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", boilerAttemptedID);
                    obj.put("success", autoScoutingObject.boiler_attempted);
                    arrAuto.put(obj);
                }
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", crossedBaselineID);
                    obj.put("success", autoScoutingObject.crossedBaseline);
                    arrAuto.put(obj);
                }
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", gearDeliveredID);
                    obj.put("success", autoScoutingObject.gear_delivered);
                    arrAuto.put(obj);
                }
                {
                    JSONObject obj = new JSONObject();
                    obj.put("objective_id", openHopperID);
                    obj.put("success", autoScoutingObject.open_hopper);
                    arrAuto.put(obj);
                }
                jsonObject.put("autonomies", arrAuto);

                // teleop
                JSONArray arr = new JSONArray();
                for(Event event : teleopScoutingObject.getEvents()) {
                    JSONObject obj = new JSONObject();
                    obj.put("start_time", event.timestamp);
                    if(event instanceof FuelPickupEvent) {
                        FuelPickupEvent e = (FuelPickupEvent) event;
                        obj.put("fuel_amount", e.amount);
                        obj.put("type", e.pickupType.toString());
                        obj.put("objective_id", FuelPickupEvent.objectiveId);
                    } else if(event instanceof FuelShotEvent) {
                        FuelShotEvent e = (FuelShotEvent) event;
                        obj.put("fuel_scored", e.numMissed);
                        obj.put("fuel_missed", e.numScored);
                        obj.put("boiler", e.boiler);
                        obj.put("objective_id", FuelShotEvent.objectiveId);
                    } else if(event instanceof GearPickupEvent) {
                        GearPickupEvent e = (GearPickupEvent) event;
                        obj.put("type", e.pickupType.toString());
                        obj.put("success", e.successful);
                        obj.put("objective_id", GearPickupEvent.objectiveId);
                    } else if(event instanceof GearDelivevryEvent) {
                        GearDelivevryEvent e = (GearDelivevryEvent) event;
                        obj.put("lift", e.lift.toString());
                        obj.put("objective_id", GearDelivevryEvent.objectiveId);
                    } else if(event instanceof DefenseEvent) {
                        DefenseEvent e = (DefenseEvent) event;
                        obj.put("defense_skill", e.skill);
                        obj.put("objective_id", DefenseEvent.objectiveId);
                    }
                    arr.put(obj);
                }

                // post game
                JSONObject obj = new JSONObject();
                obj.put("objective_id", climbID);
                obj.put("timestamp", postGameObject.climb_time);
                // TODO make it so there is an option for no climb
                if (postGameObject.climbType == PostGameObject.ClimbType.SUCCESS) {
                    obj.put("success", true);
                } else if (postGameObject.climbType == PostGameObject.ClimbType.FAIL){
                    obj.put("success", false);
                } else {
                    obj.put("success", null);
                }
                arr.put(obj);

                jsonObject.put("general_notes", postGameObject.notes);
                jsonObject.put("time_dead", postGameObject.time_dead);

                jsonObject.put("events", arr);

            } catch (JSONException e) {
                Log.d("JSON error :( - ", e.toString());
            }

            return jsonObject;
        }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//
//        // pre game
//        sb.append( String.format("%d,%d,", preGameObject.matchNumber, preGameObject.teamNumber));
//
//        // autonomous mode
//        sb.append( String.format("%b,%b,%d,%b,%d,%d,", autoScoutingObject.start_gear, autoScoutingObject.start_fuel,
//                autoScoutingObject.gear_delivered, autoScoutingObject.boiler, autoScoutingObject.numScored,
//                autoScoutingObject.open_hopper));
//
//        // teleop mode
//
//        // fuel
//        //cycle time
//        sb.append(teleopScoutingObject.fuelCycleTime + ",");
//
//        // fuel-pickup cycles
//        sb.append("{");
//        for(int i = 0; i < teleopScoutingObject.fuelPickups.size(); i++) {
//            FuelPickupEvent fuelPickup = teleopScoutingObject.fuelPickups.get(i);
//
//            sb.append(String.format("{%.2f;%.2f;%d;%d}", fuelPickup.timestamp, fuelPickup.endTime,
//                    fuelPickup.pickupType, fuelPickup.amount));
//
//            if(i < teleopScoutingObject.fuelPickups.size())
//                sb.append(":");
//        }
//        sb.append("},");
//
//        // fuel-shot cycles
//        sb.append("{");
//        for(int i = 0; i < teleopScoutingObject.fuelShots.size(); i++) {
//            FuelShotEvent fuelShot = teleopScoutingObject.fuelShots.get(i);
//
//            sb.append(String.format("{%.2f;%.2f;%b;%d;%d;%d}", fuelShot.timestamp, fuelShot.endTime,
//                    fuelShot.boiler, fuelShot.accuracy, fuelShot.x, fuelShot.y));
//
//            if(i < teleopScoutingObject.fuelShots.size())
//                sb.append(":");
//        }
//        sb.append("},");
//
//
//
//        // gears
//        // cycle time
//        sb.append(teleopScoutingObject.gearCycleTime + ",");
//
//        // gear-pickup cycles
//        sb.append("{");
//        for(int i = 0; i < teleopScoutingObject.gearPickups.size(); i++) {
//            GearPickupEvent gearPickup = teleopScoutingObject.gearPickups.get(i);
//
//            sb.append(String.format("{%.2f;%.2f;%b;%b}", gearPickup.timestamp, gearPickup.endTime, gearPickup.pickupLocation, gearPickup.successful));
//
//            if(i < teleopScoutingObject.gearPickups.size())
//                sb.append(":");
//        }
//        sb.append("},");
//
//        // gear-delivery cycles
//        sb.append("{");
//        for(int i = 0; i < teleopScoutingObject.gearDelivevries.size(); i++) {
//            GearDelivevryEvent gearDelivevry = teleopScoutingObject.gearDelivevries.get(i);
//
//            sb.append(String.format("{%.2f;%.2f;%d;%d}", gearDelivevry.timestamp, gearDelivevry.endTime, gearDelivevry.deliveryStatus, gearDelivevry.lift));
//
//            if(i < teleopScoutingObject.gearDelivevries.size())
//                sb.append(":");
//        }
//        sb.append("},");
//
//
//        // post game stuff
//        sb.append(String.format("%b,%d,%d,", postGameObject.climb, postGameObject.climb_time, postGameObject.time_dead));
//
//        // since commas, semi-colons, braces, and <enter> are all special characters for the text file, let's rip those out just to be safe.
//        String cleanedNotes = postGameObject.notes.replaceAll(",","").replaceAll(";","").replaceAll("\\{","")
//                .replaceAll("\\}","").replaceAll("\n","");
//        sb.append(cleanedNotes+",");
//
//        // new line for next match
//        sb.append("\n");
//
//        return sb.toString();
//    }
//
//        public Match(String str) {
//        preGameObject = new PreGameObject();
//        autoScoutingObject = new AutoScoutingObject();
//        teleopScoutingObject = new TeleopScoutingObject();
//        postGameObject = new PostGameObject();
//
//        String[] tokens = str.split(",");
//
//        // pre-game
//        preGameObject.matchNumber = Integer.valueOf(tokens[0]);
//        preGameObject.teamNumber = Integer.valueOf(tokens[1]);
//
//        // automode
//        autoScoutingObject.start_gear = Boolean.valueOf(tokens[2]);
//        autoScoutingObject.start_fuel = Boolean.valueOf(tokens[3]);
//        autoScoutingObject.gear_delivered = Integer.valueOf(tokens[4]);
//        autoScoutingObject.boiler = Boolean.valueOf(tokens[5]);
//        autoScoutingObject.numFuelScored = Integer.valueOf(tokens[6]);
//        autoScoutingObject.open_hopper = Integer.valueOf(tokens[7]);
//
//        // teleopmode
//
//        // fuelpickup
//        teleopScoutingObject.fuelCycleTime = Double.valueOf(tokens[8]);
//        if(!tokens[9].equals("")) {
//            String[] fuelPickups = tokens[9].split(":");
//            for(String s : fuelPickups) {
//                String[] fuelPickupTokens = s.split(";");
//                teleopScoutingObject.fuelPickups.add(new FuelPickupEvent(Double.valueOf(fuelPickupTokens[0]),
//                        Double.valueOf(fuelPickupTokens[1]), Integer.valueOf(fuelPickupTokens[2]),
//                        Integer.valueOf(fuelPickupTokens[3])));
//            }
//        }
//        //fuelshot
//        if(!tokens[10].equals("")) {
//            String[] fuelShots = tokens[10].split(":");
//            for(String s : fuelShots) {
//                String[] fuelPickupTokens = s.split(";");
//                teleopScoutingObject.fuelShots.add(new FuelShotEvent(Double.valueOf(fuelPickupTokens[0]),
//                        Double.valueOf(fuelPickupTokens[1]), Boolean.valueOf(fuelPickupTokens[2]),
//                        Integer.valueOf(fuelPickupTokens[3]), Integer.valueOf(fuelPickupTokens[4]),
//                        Integer.valueOf(fuelPickupTokens[5])));
//            }
//        }
//
//        // gears-pickups
//        teleopScoutingObject.gearCycleTime = Double.valueOf(tokens[11]);
//        if(!tokens[12].equals("")) {
//            String[] gearPickups = tokens[12].split(":");
//            for (String s : gearPickups) {
//                String[] gearPickupTokens = s.split(";");
//                teleopScoutingObject.gearPickups.add(new GearPickupEvent(Double.valueOf(gearPickupTokens[0]),
//                        Double.valueOf(gearPickupTokens[1]), Boolean.valueOf(gearPickupTokens[2]),
//                        Boolean.valueOf(gearPickupTokens[3])));
//            }
//        }
//        // gear delivery
//        if(!tokens[13].equals("")) {
//            String[] gearDelivery = tokens[13].split(":");
//            for (String s : gearDelivery) {
//                String[] gearDeliveryTokens = s.split(";");
//                teleopScoutingObject.gearDelivevries.add(new GearDelivevryEvent(Double.valueOf(gearDeliveryTokens[0]),
//                        Double.valueOf(gearDeliveryTokens[1]), Integer.valueOf(gearDeliveryTokens[2]),
//                        Integer.valueOf(gearDeliveryTokens[3])));
//            }
//        }
//
//        // post game
//        postGameObject.climb = Integer.valueOf(tokens[14]);
//        postGameObject.climb_time = Double.valueOf(tokens[15]);
//        postGameObject.time_dead = Integer.valueOf(tokens[16]);
//
//        postGameObject.notes = tokens[17];
//        }

    }


    // Member Variables
    public ArrayList<Match> matches;

    /** Empty constructor **/
    public MatchData() {
        matches = new ArrayList<>();
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public MatchData filterByTeam(int teamNo) {

        MatchData matchData = new MatchData();

        for(MatchData.Match match : matches) {
            if (match.preGameObject.teamNumber == teamNo)
                matchData.addMatch(match);
        }
        return matchData;
    }
}