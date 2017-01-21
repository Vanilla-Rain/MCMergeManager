package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class Gear {

    public double time;
    public int dropped;
    public boolean wall; // true is from wall, false is from ground
    public int lift;

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    public static final int DROP_NOT = 0;
    public static final int DROP_PICKUP = 1;
    public static final int DROP_DELIVERY = 2;
    public static final int DROP_MOVING = 3;

    public Gear(double time, boolean wall, int lift, int dropped) {
        this.time = time;
        this.dropped = dropped;
        this.wall = wall;
        this.lift = lift;
    }
}
