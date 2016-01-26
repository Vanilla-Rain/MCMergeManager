package ca.team2706.scouting.mcmergemanager;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-22.
 */
//The Pre Game Object
public class PreGameObject implements Serializable {
    public int teamNumber;
    public int gameNumber;
    public PreGameObject(int teamNumber,int gameNumber) {
        this.teamNumber = teamNumber;
        this.gameNumber = gameNumber;
    }
}
