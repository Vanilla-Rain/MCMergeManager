package ca.team2706.scouting.mcmergemanager.backend.dataObjects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 05/02/17.
 */

public class NoteObject extends TeamDataObject {

    public NoteObject(int teamNo, String note) {
        super(teamNo, TeamDataType.NOTE);
        setData(note);
    }

    public NoteObject(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public String getNote() {
        return getData();
    }
}
