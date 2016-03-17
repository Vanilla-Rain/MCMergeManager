package ca.team2706.scouting.mcmergemanager.datamodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.StatsEngine;

/**
 * Created by MCSoftware on 2016-01-22.
 */
public class TeleopScoutingObject implements Serializable {

    public static final int DEFENSE_PORTCULLIS      = 1;
    public static final int DEFENSE_CHEVAL          = 2;
    public static final int DEFENSE_MOAT            = 3;
    public static final int DEFENSE_RAMPART         = 4;
    public static final int DEFENSE_DRAWBRIDGE      = 5;
    public static final int DEFENSE_SALLYPORT       = 6;
    public static final int DEFENSE_ROCKWALL        = 7;
    public static final int DEFENSE_ROUGH_TERRAIN   = 8;
    public static final int DEFENSE_LOW_BAR         = 9;

    public static final int NUM_DEFENSES = 10;


    public List<BallShot> ballsShot;
    public List<Integer> defensesBreached;
    public double timeDefending;
    public List<BallPickup> ballsPickedUp;

    public TeleopScoutingObject() {
        ballsShot = new ArrayList<>();
        defensesBreached = new ArrayList<>();
        ballsPickedUp = new ArrayList<>();
    }

    public TeleopScoutingObject(List<BallShot> ballsShot,List<Integer> defensesBreached, double timeDefending,List<BallPickup> ballsPickedUp) {
        this.ballsShot = ballsShot;
        this.defensesBreached = defensesBreached;
        this.timeDefending = timeDefending;
        this.ballsPickedUp = ballsPickedUp;
    }

    public static String getDefenseName(int i) {
        switch (i) {
            case DEFENSE_PORTCULLIS:
                return "Portcullis";
            case DEFENSE_CHEVAL:
                return "Chavale de Frise";
            case DEFENSE_MOAT:
                return "Moat";
            case DEFENSE_RAMPART:
                return "Rampart";
            case DEFENSE_DRAWBRIDGE:
                return "Drawbridge";
            case DEFENSE_SALLYPORT:
                return  "Sallyport";
            case DEFENSE_ROCKWALL:
                return "Rock Wall";
            case DEFENSE_ROUGH_TERRAIN:
                return "Rough Terrain";
            case DEFENSE_LOW_BAR:
                return "Low Bar";
        }
        return "";
    }
}
