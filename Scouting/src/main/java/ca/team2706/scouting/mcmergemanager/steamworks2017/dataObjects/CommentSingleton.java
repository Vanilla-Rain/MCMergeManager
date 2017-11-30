package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

import java.util.Set;

/**
 * Created by Merge on 2017-11-15.
 */

public class CommentSingleton {
    private static final CommentSingleton commentInstance = new CommentSingleton();
    private int teamNumber;
    private Set<TeamNumber> teamNumbers;

    public static CommentSingleton getInstance() {
        return commentInstance;
    }


    public void addTeamNumber (int i, Set<TeamNumber> set){
        this.teamNumber = i;

    }

    public Set getTeamNumbers(){
        return teamNumbers;
    }

    private CommentSingleton() {

    }
}

