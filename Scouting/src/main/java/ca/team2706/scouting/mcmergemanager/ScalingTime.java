package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-23.
 */
public class ScalingTime implements Serializable {
    public double time;
    public int completed;

    //1 = failed, 0 = completed
    public static final int COMPLETED   = 0;
    public static final int FAILED      = 1;

    public ScalingTime(double time, int completed) {
        this.time = time;
        this.completed = completed;
    }
}
 