package ca.team2706.scouting.mcmergemanager.datamodels;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class AutoScoutingObject implements Serializable {

    public boolean isSpyBot;
    public boolean reachedDefense;
    public List<Integer> defensesBreached;
    public List<BallShot> ballsShot;

    public AutoScoutingObject(List<BallShot> ballsShot, boolean isSpyBot, List<Integer> defensesBreached, boolean reachedDefense) {
        this.ballsShot = ballsShot;
        this.isSpyBot = isSpyBot;
        this.defensesBreached = defensesBreached;
        this.reachedDefense = reachedDefense;
    }
}