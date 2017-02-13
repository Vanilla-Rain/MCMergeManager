package ca.team2706.scouting.mcmergemanager.steamworks2017.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;

/**
 * Created by Merge on 2017-02-12.
 */

public class PostGameClass extends AppCompatActivity {

    public int pointsScored;
    public ArrayList<BallShot> ballsShot;

    SeekBar deadTimeSeekBar;
    SeekBar defenseSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stronghold2016_activity_post_game);
        // initiate  views
        deadTimeSeekBar = (SeekBar) findViewById(R.id.timeDeadSeekBar);
        // perform seek bar change listener event used for getting the progress value
        deadTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView tv = (TextView) findViewById(R.id.time_dead_text_view);
                tv.setText(progressChangedValue * 5 + " points were scored");
                pointsScored = progressChangedValue;
            }
        });
    }
}
