package ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MCSoftware on 2016-01-23.
 */
public class PostGameObject implements Serializable {
    public String notes;
    public boolean challenged;
    public int timeDead;
    public List<ScalingTime> scalingTower;

    public PostGameObject() {
        scalingTower = new ArrayList<>();
    }

    public PostGameObject(String notes, boolean challenged, int timeDead, List<ScalingTime> scalingTower) {
        this.notes = notes;
        this.challenged = challenged;
        this.timeDead = timeDead;
        this.scalingTower = scalingTower;
    }
}
