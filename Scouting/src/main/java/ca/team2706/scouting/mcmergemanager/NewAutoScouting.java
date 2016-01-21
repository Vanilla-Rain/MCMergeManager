package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.Serializable;
import java.util.ArrayList;

public class NewAutoScouting extends AppCompatActivity {
private PreGameActivity.PreGameObject preGameObject;
    public ArrayList<Integer> defensesBreached;
    public ArrayList<BallShot> ballsShot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_auto_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Spinner spinner = (Spinner) findViewById(R.id.defense_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.defense_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
defensesBreached = new ArrayList<Integer>();
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        spinner.setSelection(0);
                        break;
                    case 1:
                        defensesBreached.add(1);
                        spinner.setSelection(0);
                        break;
                    case 2:
                        defensesBreached.add(2);
                        spinner.setSelection(0);
                        break;
                    case 3:
                        defensesBreached.add(3);
                        spinner.setSelection(0);
                        break;
                    case 4:
                        defensesBreached.add(4);
                        spinner.setSelection(0);
                        break;
                    case 5:
                        defensesBreached.add(5);
                        spinner.setSelection(0);
                        break;
                    case 6:
                        defensesBreached.add(6);
                        spinner.setSelection(0);
                        break;
                    case 7:
                        defensesBreached.add(7);
                        spinner.setSelection(0);
                        break;
                    case 8:
                        defensesBreached.add(8);
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final ImageView imageViewMap = (ImageView) findViewById(R.id.map);
        imageViewMap.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    RelativeLayout imgHolder = (RelativeLayout) findViewById(R.id.relativeLayoutMap);

                    ImageView pointerImageView = new ImageView(NewAutoScouting.this);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    params.leftMargin = (int) event.getX();
                    params.topMargin = (int) event.getY() + (int) imageViewMap.getY() - 50;

                    pointerImageView.setImageResource(R.drawable.pinicon);
                    pointerImageView.setLayoutParams(params);
                /*    pointerImageView.setX(event.getX());
                    pointerImageView.setY(event.getY());*/
                    new TeleopScoutAlertDialog("Shooting...", NewAutoScouting.this, "High Goal", "Low Goal", "Missed");

                    imgHolder.addView(pointerImageView);

                }
                return true;
            }


        });
Intent thisIntent = getIntent();
       preGameObject  = (PreGameActivity.PreGameObject)thisIntent.getSerializableExtra("PreGameData");
    }
public void toTeleop(View view) {
    Intent intent = new Intent(this,NewTeleopScouting.class);
    intent.putExtra("PreGameData",(Serializable)preGameObject);
  //  intent.putExtra("AutoScoutingData",(Serializable)new AutoScoutingObject());
    startActivity(intent);
}
    public void ballPickup(View view) {

    }
    public class AutoScoutingObject {
        public ArrayList<BallShot> ballsShot;
        public boolean isSpyBot;
        public ArrayList<Integer> defensesBreached;
        public boolean arrivedAtADefense;
        public AutoScoutingObject(ArrayList<BallShot> ballsShot, boolean isSpyBot, ArrayList<Integer> defensesBreached, boolean arrivedAtADefense) {
            this.ballsShot = ballsShot;
            this.isSpyBot = isSpyBot;
            this.defensesBreached = defensesBreached;
            this.arrivedAtADefense = arrivedAtADefense;
        }
    }
    public class BallShot {
        public int x;
        public int y;
        public double shootTime;
        public int whichGoal; // 0 = failed, 1 = low goal, 2 = high goal
        public BallShot(int x, int y, double shootTime, int whichGoal) {
            this.x = x;
            this.y = y;
            this.shootTime = shootTime;
            this.whichGoal = whichGoal;
        }
    }
}
