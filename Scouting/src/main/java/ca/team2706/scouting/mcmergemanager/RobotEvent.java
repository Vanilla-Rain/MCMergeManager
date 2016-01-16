package ca.team2706.scouting.mcmergemanager;

/**
 * Created by mike on 03/03/14.
 */
public class RobotEvent implements java.io.Serializable {

    public static final String STR_AUTO_BALLS_CLEARED = "AutoBallsCleared";
    public static final String STR_PICKUP = "Pickup";
    public static final String STR_PASS = "Pass";
    public static final String STR_DROPPED = "Dropped";
    public static final String STR_TRUSS_THROW = "TrussThrow";
    public static final String STR_TRUSS_CATCH = "TrussCatch";
    public static final String STR_TRUSS_CONTROLLED = "TrussControlled";
    public static final String STR_TRUSS_UNCONTROLLED = "TrussUncontrolled";
    public static final String STR_LG_SCORE = "LG_score";
    public static final String STR_LG_MISS = "LG_missed";
    public static final String STR_HG_SCORE = "HG_score";
    public static final String STR_HG_MISS = "HG_missed";
    public static final String STR_MATCHNUM = "matchNum: ";
    public static final String STR_ALLIANCE = "alliance: ";
    public static final String STR_RED_ALLIANCE = "red";
    public static final String STR_BLUE_ALLIANCE = "blue";
    public static final String STR_NUM_ASSIS = " assists: ";


    public String eventType;
    public int minutes;
    public int seconds;
    public int numAssis;

    public RobotEvent(String eventType, long time){
        this.eventType = eventType;
        long millis = time;
        seconds = (int) (millis / 1000);
        minutes = seconds / 60;
        seconds = seconds % 60;

    }

    public RobotEvent(String eventType, int minutes, int seconds ) {
        this.eventType = eventType;
        this.minutes = minutes;
        this.seconds = seconds;
        this.numAssis = 0;
    }

    public RobotEvent(String eventType, int minutes, int seconds, int numAssis ) {
        this.eventType = eventType;
        this.minutes = minutes;
        this.seconds = seconds;
        this.numAssis = numAssis;
    }

    public long getTime(){
        return minutes*60 + seconds;
    }



    public String toString() {
        if (eventType.equals( STR_HG_SCORE) || eventType.equals( STR_LG_SCORE) )
            return "{"+eventType+";"+minutes+":"+seconds+"|"+STR_NUM_ASSIS+numAssis+"}, ";
        return "{"+eventType+";"+minutes+":"+seconds+"}, ";
    }
}