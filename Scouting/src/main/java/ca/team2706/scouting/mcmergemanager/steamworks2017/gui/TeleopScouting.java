package ca.team2706.scouting.mcmergemanager.steamworks2017.gui;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.gui.PreGameActivity;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.FuelPickupEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.FuelShotEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.GearDelivevryEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.GearPickupEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.PostGameObject;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.TeleopScoutingObject;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.BallPickupFragment;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.BallShootingFragment;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.ClimbingFragment;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.FragmentListener;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.GearDeliveryFragment;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups.GearPickupFragment;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.PostGameClass;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallPickup;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.BallShot;

public class TeleopScouting extends AppCompatActivity implements FragmentListener {


    public void editNameDialogComplete(DialogFragment dialogFragment, Bundle data) {
        // Empty field is here because of interface.
        GearDelivevryEvent gearDelivevryEvent = (GearDelivevryEvent) data.getSerializable("GearDeliveryEvent");
        FuelPickupEvent fuelPickupEvent = (FuelPickupEvent) data.getSerializable("FuelPickupEvent");
        GearPickupEvent gearPickupEvent = (GearPickupEvent) data.getSerializable("GearPickupEvent");
        FuelShotEvent fuelShotEvent = (FuelShotEvent) data.getSerializable("FuelShotEvent");

        teleopScoutingObject.add(fuelShotEvent);
        teleopScoutingObject.add(gearDelivevryEvent);
        teleopScoutingObject.add(fuelPickupEvent);
        teleopScoutingObject.add(gearPickupEvent);
    }

    Handler m_handler;
    Runnable m_handlerTask;
    private int remainTime = 135;
    public int ballsHeld;
    public String ballsHeldString;

    public static TeleopScoutingObject teleopScoutingObject;

    private PostGameObject postGameObject = new PostGameObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teleopScoutingObject = new TeleopScoutingObject();
        setContentView(R.layout.steamworks2017_activity_teleop_scouting);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        //final Spinner spinner = (Spinner) findViewById(R.id.defense_spinner);
        final TextView tvGameTime = (TextView) findViewById(R.id.textViewGameTime);

        TextView numberBallsHolding = (TextView) findViewById(R.id.numberBallsHolding);
        ballsHeldString = String.valueOf(ballsHeld);

        numberBallsHolding.setText(ballsHeldString);

        Button fab = (Button) findViewById(R.id.ballPickupButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }


        });

        Button openBallScoringFrag = (Button) findViewById(R.id.ballShootingButton);
        openBallScoringFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBallScoring();
            }
        });

        Button openGearDeliveryFrag = (Button) findViewById(R.id.gearDeliveryButton);
            openGearDeliveryFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGearDelivery();
            }
        });

        Button openClimbingFrag = (Button) findViewById(R.id.startedClimbingButton);
        openClimbingFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClimbing();
            }
        });
        Button openGearPickupFrag = (Button) findViewById(R.id.gearPickupButton);
        openGearPickupFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGearPickup();}
        });

        m_handler = new Handler();

        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                if (remainTime == 0) {
                    tvGameTime.setText("Game Over! Please Save and Return");
                    postGameObject.climbType = postGameObject.climbType.NO_CLIMB;

                    Intent i=new Intent(getApplicationContext(), PostGameClass.class);
                    i.putExtra("PreGameData", getIntent().getSerializableExtra("PreGameData"));
                    i.putExtra("AutoScoutingData", getIntent().getSerializableExtra("AutoScoutingData"));
                    i.putExtra("TeleopScoutingData", getIntent().getSerializableExtra("TeleopScoutingObject"));
                    startActivity(i);
                } else {
                    remainTime--;
                    int minuets = remainTime / 60;
                    int remainSec = remainTime - minuets * 60;
                    String remainSecString;
                    if (remainSec < 10)
                        remainSecString = "0" + remainSec;
                    else
                        remainSecString = remainSec + "";

                    tvGameTime.setText(minuets + ":" + remainSecString);
                    m_handler.postDelayed(m_handlerTask, 1000);  // 1 second delay
                }
            }
        };
        m_handlerTask.run();
    }

    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();

        BallPickupFragment ballPickupFragment = BallPickupFragment.newInstance("Subscribe", this, ballsHeld);
        ballPickupFragment.show(fm, "fragment_edit_name");
    }

    private void showBallScoring() {
        FragmentManager fm = getFragmentManager();
        BallShootingFragment ballShootingFragment = BallShootingFragment.newInstance("Subscribe", this);
        ballShootingFragment.show(fm, "fragment_edit_name");
    }

    private void showGearDelivery() {
        FragmentManager fm = getFragmentManager();
        GearDeliveryFragment gearDeliveryFragment = GearDeliveryFragment.newInstance("Subscribe", this);
        gearDeliveryFragment.show(fm, "fragment_edit_name");
    }

    private void showGearPickup() {
        FragmentManager fm = getFragmentManager();
        GearPickupFragment gearPickupFragment = GearPickupFragment.newInstance("Subscribe", this);
        gearPickupFragment.show(fm, "fragment_edit_name");
    }

    private void showClimbing() {
        FragmentManager fm = getFragmentManager();
        ClimbingFragment climbingFragment = ClimbingFragment.newInstance("Subscribe", this);
        climbingFragment.show(fm, "fragment_edit_name");
    }

    @Override
    public void editNameDialogCancel(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    public void toPostGame (View view) {
        Intent intent = new Intent(this, PreGameActivity.class);
        startActivity(intent);
    }

}
