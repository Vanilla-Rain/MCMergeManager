package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class GearPickup {

    public double time;
    public boolean success;
    public boolean wall; // true is from wall, false is from ground

    public static final boolean WALL = true;
    public static final boolean GROUND = false;

    public GearPickup(double time, boolean success, boolean wall) {
        this.time = time;
        this.success = success;
        this.wall = wall;
    }
}
