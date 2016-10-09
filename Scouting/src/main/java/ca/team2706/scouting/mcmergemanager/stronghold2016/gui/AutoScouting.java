package ca.team2706.scouting.mcmergemanager.stronghold2016.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.PreGameObject;

public class AutoScouting extends AppCompatActivity {

    private PreGameObject preGameObject;

    public ArrayList<Integer> defensesBreached;
    public ArrayList<BallShot> ballsShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stronghold2016_activity_auto_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Spinner spinner = (Spinner) findViewById(R.id.defense_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.defense_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        ballsShot = new ArrayList<>();
        defensesBreached = new ArrayList<Integer>();

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0)
                    defensesBreached.add(position);

                spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final ImageView imageViewMap = (ImageView) findViewById(R.id.map);
        imageViewMap.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    // draw a new pin on the map
                    RelativeLayout imgHolder = (RelativeLayout) findViewById(R.id.relativeLayoutMap);

                    ImageView pointerImageView = new ImageView(AutoScouting.this);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    params.leftMargin = (int) event.getX() + (int) imageViewMap.getX() - 25;
                    params.topMargin = (int) event.getY() + (int) imageViewMap.getY() - 25;

                    pointerImageView.setImageResource(R.drawable.pinicon);
                    pointerImageView.setLayoutParams(params);
                    imgHolder.addView(pointerImageView);

                    Timer timer = new Timer();
                    CheckVar checkVar = new CheckVar();
                    checkVar.x = (int)event.getX();
                    checkVar.y = (int)event.getY();
                    checkVar.t = new TeleopTimerAlertDialog("Shooting...", AutoScouting.this, "High Goal", BallShot.HIGH_GOAL, "Low Goal", BallShot.LOW_GOAL, "Cancel", BallShot.MISS);
                    timer.schedule(checkVar, 0, 1000);
                }
                return true;
            }


        });

        Intent thisIntent = getIntent();
        preGameObject  = (PreGameObject)thisIntent.getSerializableExtra("PreGameData");
    }
    public void toTeleop(View view) {
        CheckBox cb = (CheckBox)findViewById(R.id.buttoncheckBox);
        CheckBox cb2 = (CheckBox)findViewById(R.id.buttonArrivedAtDefense);
        Intent intent = new Intent(this,TeleopScouting.class);

        intent.putExtra("PreGameData",preGameObject);
        intent.putExtra("AutoScoutingData",  new AutoScoutingObject(ballsShot, cb.isChecked(), defensesBreached, cb2.isChecked()));

        startActivity(intent);
    }

    class CheckVar extends TimerTask {
        public int x;
        public int y;
        public TeleopTimerAlertDialog t;
        public void run() {
            if (t.canceled >= 0) {
                if(t.canceled == BallShot.MISS) // I'm using this as a cancel button
                    this.cancel();

                ballsShot.add(new BallShot(x, y, t.upTimer.currentTime(), t.canceled));
                this.cancel();
            }
        }
    }

}
