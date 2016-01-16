package ca.team2706.scouting.mcmergemanager;

import java.util.Vector;

/**
 * Created by mike on 04/03/14.
 */
public class CycleAnalyzer {

    public final static int CYCLE_LG_0Pass = 0;
    public final static int CYCLE_LG_1Pass = 1;
    public final static int CYCLE_LG_2Pass = 2;
    public final static int CYCLE_LG_TRUSS_0Pass = 3;
    public final static int CYCLE_LG_TRUSS_1Pass = 4;
    public final static int CYCLE_LG_TRUSS_2Pass = 5;
    public final static int CYCLE_LG_TRUSS_CATCH_0Pass = 6;
    public final static int CYCLE_LG_TRUSS_CATCH_1Pass = 7;
    public final static int CYCLE_LG_TRUSS_CATCH_2Pass = 8;
    public final static int CYCLE_HG_0Pass = 9;
    public final static int CYCLE_HG_1Pass = 10;
    public final static int CYCLE_HG_2Pass = 11;
    public final static int CYCLE_HG_TRUSS_0Pass = 12;
    public final static int CYCLE_HG_TRUSS_1Pass = 13;
    public final static int CYCLE_HG_TRUSS_2Pass = 14;
    public final static int CYCLE_HG_TRUSS_CATCH_0Pass =15;
    public final static int CYCLE_HG_TRUSS_CATCH_1Pass = 16;
    public final static int CYCLE_HG_TRUSS_CATCH_2Pass = 17;

    public static String lookupName(int type) {
        switch(type) {
            case CYCLE_LG_0Pass: return "[1 Assis] L.G.";
            case CYCLE_LG_1Pass: return "[2 Assis] L.G.";
            case CYCLE_LG_2Pass: return "[3 Assis] L.G.";
            case CYCLE_LG_TRUSS_0Pass: return "[1 Assis] L.G. + Truss";
            case CYCLE_LG_TRUSS_1Pass: return "[2 Assis] L.G. + Truss";
            case CYCLE_LG_TRUSS_2Pass: return "[3 Assis] L.G. + Truss";
            case CYCLE_LG_TRUSS_CATCH_0Pass: return "[1 Assis] L.G. + Truss + Catch";
            case CYCLE_LG_TRUSS_CATCH_1Pass: return "[2 Assis] L.G. + Truss + Catch";
            case CYCLE_LG_TRUSS_CATCH_2Pass: return "[3 Assis] L.G. + Truss + Catch";
            case CYCLE_HG_0Pass: return "[1 Assis] H.G.";
            case CYCLE_HG_1Pass: return "[2 Assis] H.G.";
            case CYCLE_HG_2Pass: return "[3 Assis] H.G.";
            case CYCLE_HG_TRUSS_0Pass: return "[1 Assis] H.G. + Truss";
            case CYCLE_HG_TRUSS_1Pass: return "[2 Assis] H.G. + Truss";
            case CYCLE_HG_TRUSS_2Pass: return "[3 Assis] H.G. + Truss";
            case CYCLE_HG_TRUSS_CATCH_0Pass: return "[1 Assis] H.G. + Truss + Catch";
            case CYCLE_HG_TRUSS_CATCH_1Pass: return "[2 Assis] H.G. + Truss + Catch";
            case CYCLE_HG_TRUSS_CATCH_2Pass: return "[3 Assis] H.G. + Truss + Catch";
        }
        return "error";
    }


    private final static int ONE_ASSIS = 0;
    private final static int TWO_ASSIS = 1;
    private final static int THREE_ASSIS = 2;

    private final static int VOID_STATE = -1;
    private final static int CYCLE_STARTED = 0;
    private final static int LG = 1;
    private final static int HG = 2;
    private final static int TRUSS = 3;
    private final static int TRUSS_LG = 4;
    private final static int TRUSS_HG = 5;
    private final static int TRUSS_CATCH = 6;
    private final static int TRUSS_CATCH_LG = 7;
    private final static int TRUSS_CATCH_HG = 8;

    public static class Cycle {
        public int type;
        public long cycleTime;
        public int numTimes;

        public Cycle(int type, long cycleTime ) {
            this.type = type;
            this.cycleTime = cycleTime;
        }

        public Cycle(int type, long cycleTime, int numTimes ) {
            this.type = type;
            this.cycleTime = cycleTime;
            this.numTimes = numTimes;
        }
    }

    private static class State{
        public int passState;
        public int cycleTypeState;

        public State() {
            passState = ONE_ASSIS;
            cycleTypeState = VOID_STATE;
        }
    }

    public static Vector<Vector<Cycle>> extractAllCycles(Vector<Vector<RobotEvent>> allMatches) {
        if(allMatches.isEmpty()) {
            return new Vector<Vector<Cycle>>();
        }

        Vector<Vector<Cycle>> allCycles = new Vector<Vector<Cycle>>();
        for(Vector<RobotEvent> match : allMatches) {
            allCycles.add( extractCycles(match));
        }
        return allCycles;
    }

    public static Vector<Cycle> extractCycles(Vector<RobotEvent> match) {

        long cycleStartTime = 0;
        State state = new State();
        Vector<Cycle> cycles = new Vector<Cycle>();
        for(RobotEvent event : match) {
            // if new cycle, record the time
            if(event.eventType.equals(RobotEvent.STR_AUTO_BALLS_CLEARED)) {
                cycleStartTime = event.getTime();
                state = new State();
            }

            // pass it to the state function
            state = stateMachine(state, event);

            // deal with accepting states
            long cycleTime;
            switch(state.cycleTypeState){
                case LG:
                    cycleTime = event.getTime() - cycleStartTime;
                    cycleStartTime = event.getTime();
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_2Pass, cycleTime));
                            break;

                    }
                    break;
                case HG:
                    cycleTime = event.getTime() - cycleStartTime;
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_2Pass, cycleTime));
                            break;

                    }
                    break;
                case TRUSS_LG:
                    cycleTime = event.getTime() - cycleStartTime;
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_2Pass, cycleTime));
                            break;

                    }
                    break;
                case TRUSS_HG:
                    cycleTime = event.getTime() - cycleStartTime;
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_2Pass, cycleTime));
                            break;

                    }
                    break;
                case TRUSS_CATCH_LG:
                    cycleTime = event.getTime() - cycleStartTime;
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_CATCH_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_CATCH_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_LG_TRUSS_CATCH_2Pass, cycleTime));
                            break;

                    }
                    break;
                case TRUSS_CATCH_HG:
                    cycleTime = event.getTime() - cycleStartTime;
                    switch(state.passState) {
                        case ONE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_CATCH_0Pass, cycleTime));
                            break;
                        case TWO_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_CATCH_1Pass, cycleTime));
                            break;
                        case THREE_ASSIS:
                            cycles.add(new Cycle(CYCLE_HG_TRUSS_CATCH_2Pass, cycleTime));
                            break;

                    }
                    break;
            }
        }

        return cycles;
    }


    private static State stateMachine( State state, RobotEvent event ) {
        String symbol = event.eventType;

        // pass state
        if( symbol.equals(RobotEvent.STR_LG_SCORE) || symbol.equals(RobotEvent.STR_HG_SCORE) ) {
            if( event.numAssis == 1 )
                state.passState = ONE_ASSIS;
            if( event.numAssis == 2 )
                state.passState = TWO_ASSIS;
            if( event.numAssis == 3 )
                state.passState = THREE_ASSIS;
        }
//        switch(state.passState) {
//            case ONE_ASSIS:
//                if (symbol.equals(RobotEvent.STR_PASS))
//                    state.passState = TWO_ASSIS;
//                break;
//            case TWO_ASSIS:
//                if (symbol.equals(RobotEvent.STR_PASS))
//                    state.passState = THREE_ASSIS;
//                break;
//        }

        // cycle type state
        switch(state.cycleTypeState) {
            case VOID_STATE:

                if(symbol.equals(RobotEvent.STR_PICKUP))
                    state.cycleTypeState = CYCLE_STARTED;
                break;

            case CYCLE_STARTED:
                if(symbol.equals(RobotEvent.STR_LG_SCORE))
                    state.cycleTypeState = LG;
                else if(symbol.equals(RobotEvent.STR_HG_SCORE))
                    state.cycleTypeState = HG;
                else if(symbol.equals(RobotEvent.STR_TRUSS_THROW))
                    state.cycleTypeState = TRUSS;
                break;

            case TRUSS:
                if(symbol.equals(RobotEvent.STR_LG_SCORE))
                    state.cycleTypeState = TRUSS_LG;
                else if(symbol.equals(RobotEvent.STR_HG_SCORE))
                    state.cycleTypeState = TRUSS_HG;
                else if (symbol.equals(RobotEvent.STR_TRUSS_CATCH))
                    state.cycleTypeState = TRUSS_CATCH;
                break;

            case TRUSS_CATCH:
                if(symbol.equals(RobotEvent.STR_LG_SCORE))
                    state.cycleTypeState = TRUSS_CATCH_LG;
                else if(symbol.equals(RobotEvent.STR_HG_SCORE))
                    state.cycleTypeState = TRUSS_CATCH_HG;

                break;

        }
        return state;
    }

    public static double[] computePtsPerSecond(Vector<Vector<Cycle>> allCycles) {
        double[] times = computeAverageTimes( allCycles );
        double[] ptsPerS = new double[ times.length ];

        for(int i=0; i<18; i++) {
            if ( times[i] == -1 )
                ptsPerS[i] = 0;
            else
                ptsPerS[i] = pointValue(i) / times[i];
        }
        return ptsPerS;
    }

    public static double[] computeAverageTimes(Vector<Vector<Cycle>> allCycles) {

        int[] counts = new int[18];
        long[] times = new long[18];

        for(int i=0; i<18; i++) {
            counts[i] = 0;
            times[i] = 0;
        }
        for(Vector<Cycle> match : allCycles) {
            for(Cycle cycle : match) {
                counts[cycle.type]++;
                times[cycle.type] += cycle.cycleTime;
            }
        }

        double[] dtimes = new double[18];
        for(int i=0; i<18; i++) {
            if (counts[i] == 0)
                dtimes[i] = -1;
            else
                dtimes[i] = times[i] / (double) counts[i];
        }

        return dtimes;
    }

    public static int[] computeCounts(Vector<Vector<Cycle>> allCycles) {
        int[] counts = new int[18];
        for(int i=0; i<18; i++) {
            counts[i] = 0;
        }
        for(Vector<Cycle> match : allCycles) {
            for(Cycle cycle : match) {
                counts[cycle.type]++;
            }
        }

        return counts;
    }


    public static int pointValue( int cycleType ) {
        switch(cycleType) {
            case CYCLE_LG_0Pass: return 1;
            case CYCLE_LG_1Pass: return 11;
            case CYCLE_LG_2Pass: return 31;
            case CYCLE_LG_TRUSS_0Pass: return 11;
            case CYCLE_LG_TRUSS_1Pass: return 21;
            case CYCLE_LG_TRUSS_2Pass: return 41;
            case CYCLE_LG_TRUSS_CATCH_0Pass: return 21;
            case CYCLE_LG_TRUSS_CATCH_1Pass: return 31;
            case CYCLE_LG_TRUSS_CATCH_2Pass: return 51;
            case CYCLE_HG_0Pass: return 10;
            case CYCLE_HG_1Pass: return 20;
            case CYCLE_HG_2Pass: return 40;
            case CYCLE_HG_TRUSS_0Pass: return 20;
            case CYCLE_HG_TRUSS_1Pass: return 30;
            case CYCLE_HG_TRUSS_2Pass: return 50;
            case CYCLE_HG_TRUSS_CATCH_0Pass: return 30;
            case CYCLE_HG_TRUSS_CATCH_1Pass: return 40;
            case CYCLE_HG_TRUSS_CATCH_2Pass: return 60;
        }
        return -1;
    }
}