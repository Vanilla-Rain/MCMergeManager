package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class AutoScoutingObject implements Serializable {
    public List<BallShot> ballsShot;
    public boolean isSpyBot;
    public List<Integer> defensesBreached;
    public boolean arrivedAtADefense;
    public AutoScoutingObject(List<BallShot> ballsShot, boolean isSpyBot, List<Integer> defensesBreached, boolean arrivedAtADefense) {
        this.ballsShot = ballsShot;
        this.isSpyBot = isSpyBot;
        this.defensesBreached = defensesBreached;
        this.arrivedAtADefense = arrivedAtADefense;
    }
}