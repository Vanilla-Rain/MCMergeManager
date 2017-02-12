package ca.team2706.scouting.mcmergemanager.stronghold2016.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.PreGameObject;

public class AutoScouting extends AppCompatActivity {

    private AutoScoutingObject autoScoutingObject2017 = new AutoScoutingObject();
    private PreGameObject preGameObject;
    public int pointsScored;
    public ArrayList<BallShot> ballsShot;

    SeekBar simpleSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.steamworks2017_activity_auto_scouting);
        // initiate  views
        simpleSeekBar=(SeekBar)findViewById(R.id.autoBallSeekBar);
        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView tv = (TextView) findViewById(R.id.autoBallScoredTextView);
                tv.setText(progressChangedValue*5 + " points were scored");
                pointsScored = progressChangedValue;
            }
        });


        autoScoutingObject2017.numFuelScored = pointsScored;

        final CheckBox checkBox = (CheckBox) findViewById(R.id.crossedBaselineCheckBox);
        if (checkBox.isChecked()) {
            autoScoutingObject2017.crossedBaseline = true;
        }
        else {
            autoScoutingObject2017.crossedBaseline = false;
        }

        final CheckBox cb = (CheckBox) findViewById(R.id.startingGearCheckBox);
        if (cb.isChecked()) {

            autoScoutingObject2017.start_gear = true;
        }
        else {
            autoScoutingObject2017.start_gear = false;
        }

        final CheckBox checkiestOfBoxes = (CheckBox) findViewById(R.id.startingBallsCheckBox);
        if (checkiestOfBoxes.isChecked()) {
            autoScoutingObject2017.crossedBaseline = true;
        }
        else {
            autoScoutingObject2017.crossedBaseline = false;}
    }

    public void toTeleop(View view) {
        Intent intent = new Intent(this, TeleopScouting.class);

        intent.putExtra("PreGameData", preGameObject);

        startActivity(intent);
    }

    class CheckVar extends TimerTask {
        public int x;
        public int y;
        public TeleopTimerAlertDialog t;

        public void run() {
            if (t.canceled >= 0) {
                if (t.canceled == BallShot.MISS) // I'm using this as a cancel button
                    this.cancel();

                ballsShot.add(new BallShot(x, y, t.upTimer.currentTime(), t.canceled));
                this.cancel();
            }
        }
    }
}
