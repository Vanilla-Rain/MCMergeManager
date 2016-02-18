package ca.team2706.scouting.mcmergemanager.datamodels;

import java.io.Serializable;

/**
 * Created by MCSoftware on 2016-01-23.
 */
public class PostGameObject implements Serializable {
    public String notes;
    public boolean challenged;
    public int timeDead;

    public PostGameObject() {

    }

    public PostGameObject(String notes, boolean challenged, int timeDead) {
        this.notes = notes;
        this.challenged = challenged;
        this.timeDead = timeDead;
    }
}
