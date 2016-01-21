package ca.team2706.scouting.mcmergemanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NewTeleopScouting extends AppCompatActivity {
    Handler m_handler;
    Runnable m_handlerTask;
    Handler m_handlerDefending;
    Runnable m_handlerTaskDefending;
    private int remainTime = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_teleop_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Spinner spinner = (Spinner) findViewById(R.id.defense_spinner);
        final TextView tvGameTime = (TextView) findViewById(R.id.textViewGameTime);

        m_handler = new Handler();
        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                Log.e("loop", remainTime + "");
                if (remainTime == 0) {
                    tvGameTime.setText("Game Over! Please Save and Return");
                } else {
                    remainTime--;
                    int minuets = remainTime / 60;
                    int remainSec = remainTime - minuets * 60;
                    String remainSecString;
                    if(remainSec < 10)
                         remainSecString = "0" + remainSec;
                    else
                    remainSecString = remainSec + "";

                    tvGameTime.setText(minuets + ":" + remainSecString);
                    m_handler.postDelayed(m_handlerTask, 1000);  // 1 second delay
                }
            }
        };
        m_handlerTask.run();


        int selectedCurrent = spinner.getSelectedItemPosition();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.defense_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        spinner.setSelection(0);
                        break;
                    case 1:
                        spinner.setSelection(0);
                        break;
                    case 2:
                        spinner.setSelection(0);
                        break;
                    case 3:
                        spinner.setSelection(0);
                        break;
                    case 4:
                        spinner.setSelection(0);
                        break;
                    case 5:
                        spinner.setSelection(0);
                        break;
                    case 6:
                        spinner.setSelection(0);
                        break;
                    case 7:
                        spinner.setSelection(0);
                        break;
                    case 8:
                        spinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final ImageView imageViewMap = (ImageView) findViewById(R.id.map);
        imageViewMap.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    int width = metrics.widthPixels;
                    int height = metrics.heightPixels;
                    RelativeLayout imgHolder = (RelativeLayout) findViewById(R.id.relativeLayoutMap);

                    ImageView pointerImageView = new ImageView(NewTeleopScouting.this);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    params.leftMargin = (int) event.getX();
                    params.topMargin = (int) event.getY() + (int) imageViewMap.getY() - 50;

                    pointerImageView.setImageResource(R.drawable.pinicon);
                    pointerImageView.setLayoutParams(params);
                /*    pointerImageView.setX(event.getX());
                    pointerImageView.setY(event.getY());*/
                    new TeleopScoutAlertDialog("Shooting...", NewTeleopScouting.this, "High Goal", "Low Goal", "Missed");

                    imgHolder.addView(pointerImageView);

                }
                return true;
            }


        });
    }





    //Button - Called in XML's onClick
    public void postGame(View view) {
        Intent intent = new Intent(this,PostGameActivity.class);
        startActivity(intent);
    }
    public void scalingTower(View view) {
        new TeleopScoutAlertDialog("Scaling Tower...",NewTeleopScouting.this,"Scale Successful","","Scale Failed");
    }
public void ballPickup(View view) {
    new TeleopScoutAlertDialog("Picking up ball",NewTeleopScouting.this,"Ground","Wall","Failed");
}
    public boolean isRunning = false;
    public void defendingStart(View view) {
        if(isRunning)
            isRunning = false;
        else
            isRunning = true;

        final TextView textViewDefending = (TextView)findViewById(R.id.textViewDefending);
        final UpTimer upTimer = new UpTimer();

        upTimer.startTime(150,100,NewTeleopScouting.this);
        m_handlerDefending= new Handler();

        m_handlerTaskDefending = new Runnable() {
            @Override
            public void run () {
                NumberFormat formatter = new DecimalFormat("#0.0");
                textViewDefending.setText("" + formatter.format(upTimer.currentTime()));

                if(upTimer.currentTime() >= 150 || !isRunning) {
upTimer.cancel();
                    textViewDefending.setText("Not Currently Defending");
                } else {
                    m_handlerDefending.postDelayed(m_handlerTaskDefending, 100);  // 1 second delay
                }

            }
        };
    m_handlerTaskDefending.run();
    }

        }
