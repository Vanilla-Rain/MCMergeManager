package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dwall on 16/01/17.
 */

public class TeleopScoutingObject {

    public List<Fuel> fuels;
    public List<Gear> gears;

    public double fuelCycleTime;
    public double gearCycleTime;

    public TeleopScoutingObject() {
        fuels = new ArrayList<Fuel>();
        gears = new ArrayList<Gear>();
    }

    public TeleopScoutingObject(List<Fuel> fuels, List<Gear> gears) {
        this.fuels = fuels;
        this.gears = gears;
    }

    public void cycleTime() {

        double fuelCycleTime = 0;
        for(Fuel f : fuels) {
            fuelCycleTime += f.time;
        }
        fuelCycleTime /= fuels.size();

        double gearCycleTime = 0;
        for(Gear g : gears) {
            gearCycleTime += g.time;
        }
        gearCycleTime /= gears.size();
    }
}
