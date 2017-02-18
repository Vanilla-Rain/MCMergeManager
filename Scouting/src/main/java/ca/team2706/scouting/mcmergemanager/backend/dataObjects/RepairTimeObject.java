package ca.team2706.scouting.mcmergemanager.backend.dataObjects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.App;

/**
 * Created by alden on 2017-02-04.
 */

public class RepairTimeObject extends TeamDataObject {

    public enum RepairStatus {
        NOT_AVAILABLE, REPAIRING, WORKING;
    }

    private RepairStatus repairStatus;
    private long timestamp = new Date().getTime();

    private static String JSONKEY_REPAIR_STATUS = "RepairStatus";
    private static String JSONKEY_TIMESTAMP = "Timestamp";

    /** Standard Constructor
     * Note that this gearDeliveryData will be saved with the timestamp of when this object was created.
     **/
    public RepairTimeObject(int teamNo, RepairStatus repairStatus) throws JSONException {
        super(teamNo, TeamDataType.REPAIR_TIME);

        this.repairStatus = repairStatus;


        // This will be nested inside a TeamDataObject,
        // so let's set the payload
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSONKEY_REPAIR_STATUS, this.repairStatus.name());
        jsonObject.put(JSONKEY_TIMESTAMP, timestamp);
        setData(jsonObject.toString());
    }

    /**
     * CONSTRUCTOR
     *
     * Populates a RepairTimeObject from a JSONObject.
     *
     * @param jsonObj
     * @throws JSONException if the required fields are not present in the JSONObject
     */
    public RepairTimeObject(JSONObject jsonObj) throws JSONException {
        // Pull out all the generic gearDeliveryData for a TeamDataObject.
        super(jsonObj);

        // Pull out data specific to a RepairTimeObject.
        JSONObject datajson = new JSONObject(this.getData());
        this.repairStatus = RepairStatus.valueOf(datajson.getString(JSONKEY_REPAIR_STATUS));
        this.timestamp = datajson.getLong(JSONKEY_TIMESTAMP);

    }

    public RepairStatus getRepairStatus() {
        return repairStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void initData() throws JSONException {

        // set this.data

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSONKEY_REPAIR_STATUS, repairStatus.name());
        jsonObject.put(JSONKEY_TIMESTAMP, timestamp);

        setData(jsonObject.toString());
    }
}
