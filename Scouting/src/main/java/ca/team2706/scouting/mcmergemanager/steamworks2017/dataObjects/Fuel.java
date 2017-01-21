package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class Fuel {

    public double time;
    public int accuracy;
    public int amount;
    public boolean boiler; // true is high, false is low

    public int x;
    public int y;

    public int pickupLocation;
    public static final int HOPPER = 0;
    public static final int PLAYER_STATION = 1;
    public static final int GROUND = 2;

    public Fuel(double time, int pickupLocation, int amount, boolean boiler, int accuracy, int x, int y){
        this.time = time;
        this.accuracy = accuracy;
        this.amount = amount;
        this.boiler = boiler;
        this.x = x;
        this.y = y;
        this.pickupLocation = pickupLocation;
    }
}
