package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class BallShot implements Serializable {
    public int x;
    public int y;
    public double shootTime;
    public int whichGoal; // 0 = failed, 1 = low goal, 2 = high goal
    public BallShot(int x, int y, double shootTime, int whichGoal) {
        this.x = x;
        this.y = y;
        this.shootTime = shootTime;
        this.whichGoal = whichGoal;
    }
}