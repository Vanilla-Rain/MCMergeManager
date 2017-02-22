package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by dwall on 23/01/17.
 */

public class GearDelivevryEvent extends Event {

    public static final int objectiveId = 22;

    public enum Lift {
        FEEDER_SIDE, CENTRE, BOILER_SIDE, NONE
    }

    public Lift lift;

    public GearDelivevryEvent(){

    }

    public GearDelivevryEvent(double timestamp, Lift lift) {
        super(timestamp);
        this.lift = lift;
    }
}
