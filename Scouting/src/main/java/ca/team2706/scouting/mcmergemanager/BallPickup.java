package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-23.
 */
public class BallPickup implements Serializable {
    public int selection;
    public double time;
    public BallPickup(int selection, double time) {
this.selection = selection;
        this.time = time;
    }
}
