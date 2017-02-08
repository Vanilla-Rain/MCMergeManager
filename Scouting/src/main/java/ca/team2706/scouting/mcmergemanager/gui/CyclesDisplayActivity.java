package ca.team2706.scouting.mcmergemanager.gui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import ca.team2706.scouting.mcmergemanager.R;


public class CyclesDisplayActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cycles_display_activity);



        // TODO: instead of fake data, unpack the TeamStatsReport from the intent
        // and display real data.


        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.cycles_display_main_layout);

        // in the real app, get this from the team's data.
        int matchesPlayed = 20;

        // populate the things with fake simulated data, just so it looks like something
        int matchNoCounter = 1 + (int) (Math.random()*10);

        for(int i=0; i< matchesPlayed; i++) {
            // simulate the match number
            matchNoCounter += 1 + (int) (Math.random()*10);


            CycleDisplayLayout cycleDisplayLayout = (CycleDisplayLayout) getLayoutInflater()
                    .inflate(R.layout.cycles_display, null);

            simulateMatchData(cycleDisplayLayout, matchNoCounter);

            mainLayout.addView(cycleDisplayLayout);

        }
    }


    private void simulateMatchData(CycleDisplayLayout cycleDisplayLayout, int matchNo) {
        // set the match number
        cycleDisplayLayout.setMatchNo(matchNo);

        // simulate some gear cycles
        for(double timer=0; timer<135; ) {
            double startTime = (timer += Math.random()*20);
            double endTime   = (timer += 12.5 + Math.random()*30);
            boolean succeeded = Math.random() < 0.8;

            cycleDisplayLayout.addCycle(CycleDisplayLayout.GEAR_CYCLE, startTime, endTime, succeeded);

            timer += 5 + Math.random()*20;
        }


        // simulate some defense cycles
        int numDefenses = (int) (Math.random()*3);
        double timer = 0;
        for(int j=0; j<numDefenses && timer < 135; j++) {
            double startTime = (timer += 10 + Math.random()*20);
            double endTime   = (timer += 5 + Math.random()*15);
            boolean succeeded = Math.random() < 0.8;

            cycleDisplayLayout.addCycle(CycleDisplayLayout.DEFENSE_CYCLE, startTime, endTime, succeeded);

            timer += 5 + Math.random()*30;
        }


        // simulate a climb?
        if(Math.random() < 0.6) {
            double startTime = 115 - Math.random()*20;

            // succeeded?
            boolean succeeded = Math.random() < 0.7;

            cycleDisplayLayout.addCycle(CycleDisplayLayout.CLIMB_CYCLE, startTime, 135, succeeded);
        }
    }
}
