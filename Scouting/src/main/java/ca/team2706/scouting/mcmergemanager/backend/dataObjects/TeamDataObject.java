package ca.team2706.scouting.mcmergemanager.backend.dataObjects;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.App;

/**
 * Generic container object for data about a team that's not match data.
 *
 * Created by mike on 05/02/17.
 */
public abstract class TeamDataObject implements Serializable {

    public enum TeamDataType {
        UNKNOWN, PIT_SCOUTING_DATA, NOTE, REPAIR_TIME;
    }


    private int teamNo;
    private String eventCode;
    private TeamDataType type;
    private String data;

    public static String JSONKEY_TEAMNO    = "TeamNo";
    public static String JSONKEY_EVENTCODE = "EventCode";
    public static String JSONKEY_TYPE      = "Type";
    public static String JSONKEY_DATA      = "Data";

    /** CONSTRUCTOR **/
    protected TeamDataObject(int teamNo, String eventCode, TeamDataType type, String data) {
        this.teamNo = teamNo;
        this.eventCode = eventCode;
        this.type = type;
        this.data = data;
    }

    /** CONSTRUCTOR **/
    protected TeamDataObject(int teamNo, TeamDataType type) {
        this.teamNo = teamNo;
        this.type = type;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        this.eventCode = SP.getString(App.getContext().getResources().getString(R.string.PROPERTY_event), "<Not Set>");
    }



    /**
     * CONSTRUCTOR
     *
     * Populates a TeamDataObject from a JSONObject.
     *
     * @param jsonObj
     * @throws JSONException if the required fields are not present in the JSONObject
     */
    protected TeamDataObject(JSONObject jsonObj) throws JSONException {

        this.teamNo     = jsonObj.getInt(JSONKEY_TEAMNO);
        this.eventCode  = jsonObj.getString(JSONKEY_EVENTCODE);

        try {
            this.type = TeamDataType.valueOf(jsonObj.getString(JSONKEY_TYPE));
        } catch (IllegalArgumentException e) {
            // The string did not match any of the types
            this.type = TeamDataType.UNKNOWN;
        }

        this.data = jsonObj.getString(JSONKEY_DATA);
    }


    public int getTeamNo() {
        return teamNo;
    }

    public String getEventCode() {
        return eventCode;
    }

    protected String getData() {
        return data;
    }

    protected void setData(String data) {
        this.data = data;
    }

    protected abstract void initData() throws JSONException;


    /** Assumes data has alorady been filled with a string **/
    public JSONObject toJSON() throws JSONException {

        initData();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSONKEY_TEAMNO, teamNo);
        jsonObject.put(JSONKEY_EVENTCODE, eventCode);
        jsonObject.put(JSONKEY_TYPE, type.name());

        if (data != null)
            jsonObject.put(JSONKEY_DATA, data);
        else
            jsonObject.put(JSONKEY_DATA, "");

        return jsonObject;
    }


    @Override
    public String toString() {

        try {
            return toJSON().toString();
        } catch (JSONException e) {
            Log.e(App.getContext().getResources().getString(R.string.app_name), "Error converting TeamDataObject to JSON", e);

            return "";
        }
    }
}
