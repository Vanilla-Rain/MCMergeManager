package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 16/01/17.
 */

public class AutoScoutingObject {

    public boolean start_gear;
    public boolean start_fuel;

    public int boiler;
    public int accuracy;
    public int open_hopper; // 0 if none, 1+ store how many opened
    public int gear_delivered;


    public static final int not_delivered = 0;
    public static final int fail_delivery = 1;
    public static final int success_delivered = 2;

    public AutoScoutingObject(boolean start_gear, boolean start_fuel, int open_hopper, int gear_delivered) {
        this.start_fuel = start_fuel;
        this.start_gear = start_gear;
        this.open_hopper = open_hopper;
        this.gear_delivered = gear_delivered;
    }
}
