package ca.team2706.scouting.mcmergemanager.datamodels;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-23.
 */
public class BallPickup implements Serializable {
    public int selection;
    public double time;

    // 1 = wall, 2 = ground, 3 = fail
    
    public BallPickup(int selection, double time) {
        this.selection = selection;
        this.time = time;
    }
}
