package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by Merge on 2017-11-15.
 */

public class CommentSingleton {
    private static final CommentSingleton ourInstance = new CommentSingleton();

    public static CommentSingleton getInstance() {
        return ourInstance;
    }

    int potato;
    public void addTeamNumber (int i){
        potato += 1;

    }

    private CommentSingleton() {
    }
}
