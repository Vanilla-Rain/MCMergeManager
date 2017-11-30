package ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects;

/**
 * Created by Merge on 2017-11-22.
 */

public class TeamNumber {

    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {

        // this will need to be a list, probably
        this.comment = comment;
    }

    private int teamNumber;

    public int getTeamNumber() {
        return teamNumber;
    }

    public void addNewComment(String comment) {
        // add it to the list
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamNumber that = (TeamNumber) o;

        return teamNumber == that.teamNumber;
    }

}
