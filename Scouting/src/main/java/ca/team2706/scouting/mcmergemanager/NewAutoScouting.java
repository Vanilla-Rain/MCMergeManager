package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
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

import ca.team2706.scouting.mcmergemanager.datamodels.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.datamodels.BallShot;
import ca.team2706.scouting.mcmergemanager.datamodels.PreGameObject;

public class NewAutoScouting extends AppCompatActivity {

private PreGameObject preGameObject;

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
        ballsShot = new ArrayList<>();
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
                    Timer timer = new Timer();
                    CheckVar checkVar = new CheckVar();
                    checkVar.x = (int)event.getX();
                    checkVar.y = (int)event.getY();
                    checkVar.t = new TeleopScoutAlertDialog("Shooting...", NewAutoScouting.this, "High Goal", "Low Goal", "Missed");
                    timer.schedule(checkVar, 0, 1000);



                    imgHolder.addView(pointerImageView);

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
    Intent intent = new Intent(this,NewTeleopScouting.class);

    intent.putExtra("PreGameData",preGameObject);
    intent.putExtra("AutoScoutingData",  new AutoScoutingObject(ballsShot, cb.isChecked(), defensesBreached, cb2.isChecked()));

    startActivity(intent);

}
    public void ballPickup(View view) {

    }


    class CheckVar extends TimerTask {
        public int x;
        public int y;
        public TeleopScoutAlertDialog t;
        public void run() {

            if (t.canceled > 0) {
                ballsShot.add(new BallShot(x,y,t.upTimer.currentTime(),t.canceled));
            }

        }
    }

}
