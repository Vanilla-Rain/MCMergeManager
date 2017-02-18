package ca.team2706.scouting.mcmergemanager.steamworks2017.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.gui.PreGameActivity;
import ca.team2706.scouting.mcmergemanager.gui.PrimaryTab;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.DefenseEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.PostGameObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;

/**
 * Created by Merge on 2017-02-12.
 */

public class PostGameClass extends AppCompatActivity {

    public int timeDead;
    public int timeDefending;

    private PostGameObject postGameObject = new PostGameObject();
    private DefenseEvent defenseEvent = new DefenseEvent();

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
                // Not used by anything, just need to override it in the thing
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView tv = (TextView) findViewById(R.id.time_dead_text_view);
                tv.setText(progressChangedValue * 5 + " seconds dead");
                timeDead = progressChangedValue*5;
            }
        });

        defenseSeekBar = (SeekBar) findViewById(R.id.time_defending_seekbar);

        defenseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used by anything, just need to override it in the thing
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView tv = (TextView) findViewById(R.id.time_defending_text_view);
                tv.setText(progressChangedValue * 5 + " seconds defending");
                timeDefending = progressChangedValue*5;

            }
        });

        Button fab = (Button) findViewById(R.id.post_game_submit_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defenseEvent.skill = timeDefending;
                postGameObject.time_dead = timeDead;

                Intent intent = new Intent(view.getContext(),PreGameActivity.class);
                startActivity(intent);
            }
        });
    }
}





