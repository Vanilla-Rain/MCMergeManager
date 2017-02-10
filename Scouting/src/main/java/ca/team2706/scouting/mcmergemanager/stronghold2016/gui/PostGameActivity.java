package ca.team2706.scouting.mcmergemanager.stronghold2016.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.FileUtils;
import ca.team2706.scouting.mcmergemanager.gui.MainActivity;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchData;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.PostGameObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.PreGameObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.ScalingTime;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.TeleopScoutingObject;

/**
 * Created by MCSoftware on 2016-01-18.
 */
public class PostGameActivity extends AppCompatActivity {
    public int progress = 0;
    public SeekBar seekBar;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.stronghold2016_activity_post_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        final TextView  textView = (TextView) findViewById(R.id.textViewSeekBar);

        scalingTimes = new ArrayList<>();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Percentage Deadness: " + progress + "/" + seekBar.getMax());

            }
        });
    }

    public ArrayList<ScalingTime> scalingTimes;
    class CheckVarScale extends TimerTask {
        public TeleopTimerAlertDialog t;

        public void run() {

            if (t.canceled >= 0) {
                if (t.canceled == ScalingTime.FAILED) // I'm using this as a cancel button
                    this.cancel();

                scalingTimes.add(new ScalingTime(t.upTimer.currentTime(), t.canceled));
                this.cancel();
            }

        }
    }

    public void scalingTower(View view) {
        Timer timer = new Timer();
        CheckVarScale checkVar = new CheckVarScale();
        checkVar.t = new TeleopTimerAlertDialog("Scaling Tower...", this, "Scale Successful", ScalingTime.COMPLETED, "", -1, "Cancel", ScalingTime.FAILED);
        timer.schedule(checkVar, 0, 1000);
    }

    public void returnHome(View view) {
        Intent thisIntent = getIntent();
        PreGameObject p = (PreGameObject) thisIntent.getSerializableExtra("PreGameData");
        AutoScoutingObject a = (AutoScoutingObject) thisIntent.getSerializableExtra("AutoScoutingData");
        TeleopScoutingObject t  = (TeleopScoutingObject) thisIntent.getSerializableExtra("TeleopScoutingData");
        EditText e = (EditText)findViewById(R.id.editTextPost);
        CheckBox c = (CheckBox)findViewById(R.id.challengedCB);
        PostGameObject post = new PostGameObject(e.getText().toString(),c.isChecked(),progress, scalingTimes);
        Intent intent = new Intent(this,MainActivity.class);

        // SAVING STUFF
        MatchData.Match match = new MatchData.Match(p, a, t, post);
//        FileUtils.appendToMatchDataFile(match);

        startActivity(intent);
    }

}
