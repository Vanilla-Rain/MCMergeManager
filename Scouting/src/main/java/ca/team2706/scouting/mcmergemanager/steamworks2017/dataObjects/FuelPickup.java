package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class FuelPickup {

    public int selection;
    public double time;

    public static final int HOPPER = 0;
    public static final int PLAYER_STATION = 1;
    public static final int GROUND = 2;

    public FuelPickup(double time, int selection) {
        this.selection = selection;
        this.time = time;
    }

}
