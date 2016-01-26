package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class TeleopScoutingObject implements Serializable {
    public List<BallShot> ballsShot;
    public List<Integer> defensesBreached;
    public double timeDefending;
    public List<BallPickup> ballsPickedUp;
    public List<ScalingTime> scalingTower;

    public TeleopScoutingObject(List<BallShot> ballsShot,List<Integer> defensesBreached, double timeDefending,List<BallPickup> ballsPickedUp,List<ScalingTime> scalingTower)
     {
this.ballsShot = ballsShot;
         this.defensesBreached = defensesBreached;
         this.timeDefending = timeDefending;
         this.ballsPickedUp = ballsPickedUp;
         this.scalingTower = scalingTower;
    }
}
