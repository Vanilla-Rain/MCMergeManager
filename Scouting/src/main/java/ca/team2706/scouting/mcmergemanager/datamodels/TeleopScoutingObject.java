package ca.team2706.scouting.mcmergemanager.datamodels;

import java.io.Serializable;
import java.util.List;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class TeleopScoutingObject implements Serializable {

    public static final int DEFENSE_PORTCULLIS  = 1;
    public static final int DEFENSE_CHEVAL      = 2;
    public static final int DEFENSE_MOAT        = 3;
    public static final int DEFENSE_RAMPART     = 4;
    public static final int DEFENSE_DRAWBRIDGE  = 5;
    public static final int DEFENSE_SALLYPORT   = 6;
    public static final int DEFENSE_ROCKWALL    = 7;
    public static final int DEFENSE_ROUGHT      = 8;
    public static final int DEFENSE_LOW_BAR     = 9;

    public static final int NUM_DEFENSES = 10;


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
