package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class GearDropoff {

    public boolean success;
    public double time;

    public GearDropoff(double time, boolean success) {
        this.time = time;
        this.success = success;
    }
}
