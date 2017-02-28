package ca.team2706.scouting.mcmergemanager.steamworks2017.gui;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.Event;
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

public class TeleopScouting extends AppCompatActivity implements FragmentListener {

    // Data strings

    public static final String FUEL_PICKUP_EVENT_STRING = "FuelPickupEvent";
    public static final String GEAR_PICKUP_EVENT_STRING = "GearPickupEvent";
    public static final String GEAR_DELIVERY_EVENT_STRING = "FuelPickupEvent";
    public static final String FUEL_SHOT_EVENT_STRING = "FuelShotEvent";

    public void editNameDialogComplete(DialogFragment dialogFragment, Bundle data) {
        // Empty field is here because of interface.


        if (dialogFragment instanceof BallPickupFragment) {
            FuelPickupEvent fuelPickupEvent = (FuelPickupEvent) data.getSerializable(FUEL_PICKUP_EVENT_STRING);

            ballsHeld += fuelPickupEvent.amount;
            TextView numberBallsHolding = (TextView) findViewById(R.id.numberBallsHolding);
            numberBallsHolding.setText(String.valueOf(ballsHeld));

            teleopScoutingObject.add(fuelPickupEvent);
        }
        else if (dialogFragment instanceof GearPickupFragment) {
            GearPickupEvent gearPickupEvent = (GearPickupEvent) data.getSerializable(GEAR_PICKUP_EVENT_STRING);

            // TODO

            teleopScoutingObject.add(gearPickupEvent);
        }
        else if (dialogFragment instanceof GearDeliveryFragment) {
            GearDelivevryEvent gearDelivevryEvent = (GearDelivevryEvent) data.getSerializable(GEAR_DELIVERY_EVENT_STRING);

            // TODO

            teleopScoutingObject.add(gearDelivevryEvent);
        }
        else if (dialogFragment instanceof BallShootingFragment) {
            FuelShotEvent fuelShotEvent = (FuelShotEvent) data.getSerializable(FUEL_SHOT_EVENT_STRING);

            ballsHeld -= fuelShotEvent.numScored;
            TextView numberBallsHolding = (TextView) findViewById(R.id.numberBallsHolding);
            numberBallsHolding.setText(String.valueOf(ballsHeld));

            teleopScoutingObject.add(fuelShotEvent);
        }
        else if (dialogFragment instanceof ClimbingFragment) {
            this.postGameObject = (PostGameObject) data.getSerializable(ClimbingFragment.CLIMB_POST_GAME_OBJECT_STRING);
            toPostGame();
        }

    }



    private Handler m_handler;
    private Runnable m_handlerTask;
    private volatile boolean stopTimer;
    private int remainTime = 135;
    public int ballsHeld;
    public int gearHeld;
    public String ballsHeldString;
    public Event event = new Event();

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

        numberBallsHolding.setText(ballsHeldString);

        Button fab = (Button) findViewById(R.id.ballPickupButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event.timestamp = 135 - remainTime;
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
                event.timestamp = 135 - remainTime;
                showGearDelivery();
            }
        });

        Button openClimbingFrag = (Button) findViewById(R.id.startedClimbingButton);
        openClimbingFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event.timestamp = 135 - remainTime;
                showClimbing();
            }
        });
        Button openGearPickupFrag = (Button) findViewById(R.id.gearPickupButton);
        openGearPickupFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event.timestamp = 135 - remainTime;
                showGearPickup();}
        });

        m_handler = new Handler();

        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                Log.d("MCMergeManager","timer: "+remainTime);

                if (remainTime == 0) {
                    tvGameTime.setText("Game Over! Please Save and Return");
                    postGameObject.climbType = postGameObject.climbType.NO_CLIMB;

                    if(!stopTimer)
                        toPostGame();
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

                    // set an alarm to run this again in 1 second
                    if(!stopTimer)
                        m_handler.postDelayed(m_handlerTask, 1000);  // 1 second delay
                }
            }
        };
        m_handlerTask.run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer = true;
        m_handler.removeCallbacks(m_handlerTask);
    }

    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();

        BallPickupFragment ballPickupFragment = BallPickupFragment.newInstance("Subscribe", this);
        ballPickupFragment.show(fm, "fragment_edit_name");
    }

    private void showBallScoring() {
        FragmentManager fm = getFragmentManager();

        BallShootingFragment ballShootingFragment = BallShootingFragment.newInstance("Subscribe", this, ballsHeld);
        ballShootingFragment.show(fm, "fragment_edit_name");
    }

    private void showGearDelivery() {
        FragmentManager fm = getFragmentManager();
        GearDeliveryFragment gearDeliveryFragment = GearDeliveryFragment.newInstance("Subscribe", this);
        gearDeliveryFragment.show(fm, "fragment_edit_name");
    }

    private void showGearPickup() {
        FragmentManager fm = getFragmentManager();
        GearPickupFragment gearPickupFragment = GearPickupFragment.newInstance("Subscribe", this, gearHeld);
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

    public void toPostGame() {

        stopTimer = true;
        m_handler.removeCallbacks(m_handlerTask);

        Intent intent = new Intent(this,PostGameClass.class);
        // Pass gearDeliveryData to PostGameClass.class
        intent.putExtra("PreGameData", getIntent().getSerializableExtra("PreGameData"));
        intent.putExtra("AutoScoutingData", getIntent().getSerializableExtra("AutoScoutingData"));
        intent.putExtra("TeleopScoutingData", teleopScoutingObject);
        intent.putExtra("PostGameData", postGameObject);

        startActivity(intent);
    }

}
