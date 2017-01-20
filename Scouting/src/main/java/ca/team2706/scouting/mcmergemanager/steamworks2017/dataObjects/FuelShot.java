package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class FuelShot {

    public double time;
    public int accuracy;
    public int amount;
    public int boiler;

    public static final int LOW = 0;
    public static final int HIGH = 1;

    public int x;
    public int y;

    public FuelShot(double time, int accuracy, int amount, int boiler, int x, int y){
        this.time = time;
        this.accuracy = accuracy;
        this.amount = amount;
        this.boiler = boiler;
        this.x = x;
        this.y = y;
    }
}
