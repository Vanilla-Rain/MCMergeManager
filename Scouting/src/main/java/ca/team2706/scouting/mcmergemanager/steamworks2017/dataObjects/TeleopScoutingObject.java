package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeleopScoutingObject {

    public static double MATCH_TIME = 0; // TODO: get match time in milliseconds?

    public List<FuelPickupEvent> fuelPickups;
    public List<FuelShotEvent> fuelShots;
    public List<GearPickupEvent> gearPickups;
    public List<GearDelivevryEvent> gearDelivevries;

    public double fuelCycleTime; // TODO: [MikeO] ??
    public double gearCycleTime; // TODO: [MikeO] ??


    public TeleopScoutingObject() {
        fuelPickups = new ArrayList<FuelPickupEvent>();
        fuelShots = new ArrayList<FuelShotEvent>();
        gearPickups = new ArrayList<GearPickupEvent>();
        gearDelivevries = new ArrayList<GearDelivevryEvent>();
    }

    public TeleopScoutingObject(List<FuelPickupEvent> fuelPickups, List<FuelShotEvent> fuelShots, List<GearPickupEvent> gearPickups,
                                List<GearDelivevryEvent> gearDelivevries) {
        this.fuelPickups = fuelPickups;
        this.fuelShots = fuelShots;
        this.gearPickups = gearPickups;
        this.gearDelivevries = gearDelivevries;

//        cycleTime();
    }

    // TODO probably not going to be moved to stat engine
    // [MikeO] Yeah, I ended up doing something complicated in StatsEngine that includes this :'(
//    public void cycleTime() {
//
//        double fuelPickupCycleTime = 0;
//        for(FuelPickupEvent f : fuelPickups) {
//            fuelPickupCycleTime += f.endTime - f.timestamp;
//        }
//        // TODO: [MikeO] What if fuelPickups.size() == 0? divByZeroException?
//        fuelPickupCycleTime /= fuelPickups.size();
//
//        double fuelShotCycleTime = 0;
//        for(FuelShotEvent f : fuelShots) {
//            fuelShotCycleTime += f.endTime - f.timestamp;
//        }
//        fuelShotCycleTime /= fuelShots.size();
//
//        double gearPickupCycleTime = 0;
//        for(GearPickupEvent g : gearPickups) {
//            gearPickupCycleTime += g.endTime - g.endTime;
//        }
//        gearPickupCycleTime /= gearPickups.size();
//
//        double gearDeliveryCycleTime = 0;
//        for(GearDelivevryEvent g : gearDelivevries) {
//            gearDeliveryCycleTime += g.endTime - g.timestamp;
//        }
//        gearDeliveryCycleTime /= gearDelivevries.size();
//
//        // assuming that each cycle contains one pickup and one shot/delivery
//        gearCycleTime = gearPickupCycleTime + gearDeliveryCycleTime;
//        fuelCycleTime = fuelPickupCycleTime + fuelShotCycleTime;
//    }


    /**
     * Puts all events during teleop mode into a list, sorted by timestamp.
     * Useful for doing cycle analysis in StatsEngine.
     */
    public ArrayList<Event> getEvents() {
        ArrayList<Event> events = new ArrayList<>();

        // throw all the events in, then sort.

        for (Event e : fuelPickups)
            events.add(e);

        for (Event e : fuelShots)
            events.add(e);

        for (Event e : gearPickups)
            events.add(e);

        for (Event e : gearDelivevries)
            events.add(e);

        Collections.sort(events);

        return events;
    }
}
