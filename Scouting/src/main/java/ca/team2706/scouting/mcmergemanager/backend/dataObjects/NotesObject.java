package ca.team2706.scouting.mcmergemanager.backend.dataObjects;

/**
 * Created by mike on 05/02/17.
 */

public class NotesObject extends TeamDataObject {

    public NotesObject(int teamNo, String note) {
        super(teamNo, TeamDataType.NOTE);
        setData(note);
    }

    public String getNote() {
        return getData();
    }
}
