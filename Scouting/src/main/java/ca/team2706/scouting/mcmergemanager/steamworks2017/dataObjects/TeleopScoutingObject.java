package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import java.util.ArrayList;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.stronghold2016.gui.TeleopScouting;

/**
 * Created by dwall on 16/01/17.
 */

public class TeleopScoutingObject {

    public List<FuelPickup> fuelPickups;
    public List<FuelShot> fuelShots;
    public List<GearPickup> gearPickups;
    public List<GearDropoff> gearDropoffs;

    public double fuelCycleTime;
    public double gearCycleTime;

    public TeleopScoutingObject() {
        fuelPickups = new ArrayList<FuelPickup>();
        fuelShots = new ArrayList<FuelShot>();
        gearPickups = new ArrayList<GearPickup>();
        gearDropoffs = new ArrayList<GearDropoff>();
    }

    public TeleopScoutingObject(List<FuelPickup> fuelPickups, List<FuelShot> fuelShots, List<GearPickup> gearPickups, List<GearDropoff> gearDropoffs) {
        this.fuelShots = fuelShots;
        this.fuelPickups = fuelPickups;
        this.gearDropoffs = gearDropoffs;
        this.gearPickups = gearPickups;
    }

    public void cycleTime() {
        double fuelPickupCycleTime = 0;
        for(FuelPickup f : fuelPickups) {
            fuelPickupCycleTime += f.time;
        }
        fuelPickupCycleTime /= fuelPickups.size();

        double fuelShootCycleTime = 0;
        for(FuelShot f : fuelShots) {
            fuelShootCycleTime += f.time;
        }
        fuelShootCycleTime /= fuelShots.size();

        // TODO how to combine fuel cycle times?
        // A) keep them separate, B) add them together???

        double gearPickupCycleTime = 0;
        for(GearPickup g : gearPickups) {
            gearPickupCycleTime += g.time;
        }
        gearPickupCycleTime /= gearPickups.size();

        double gearDropoffCycleTime = 0;
        for(GearDropoff g : gearDropoffs) {
            gearDropoffCycleTime += g.time;
        }
        gearDropoffCycleTime /= gearDropoffs.size();

        // combine stuff together
        fuelCycleTime = fuelPickupCycleTime + fuelShootCycleTime;
        gearCycleTime = gearDropoffCycleTime + gearPickupCycleTime;
    }
}
