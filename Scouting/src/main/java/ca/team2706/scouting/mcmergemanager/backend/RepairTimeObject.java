package ca.team2706.scouting.mcmergemanager.backend;

import java.util.Date;

/**
 * Created by alden on 2017-02-04.
 */

public class RepairTimeObject {

    public enum RepairStatus {
        NOT_AVAILABLE, REPAIRING, WORKING;
    }

    private int teamNumber;
    private RepairStatus repairStatus;
    private long recordTime = new Date().getTime();

    public RepairTimeObject(int teamNumber, RepairStatus repairStatus) {
        this.teamNumber = teamNumber;
        this.repairStatus = repairStatus;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public RepairStatus getRepairStatus() {
        return repairStatus;
    }

    public long getRecordTime() {
        return recordTime;
    }

}
