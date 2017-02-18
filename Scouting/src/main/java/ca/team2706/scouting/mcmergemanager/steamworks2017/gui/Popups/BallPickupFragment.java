package ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups;

/**
 * Created by JustinT, mostly copied from John(From mikes work) on 2017-02-06.
 */

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.FuelPickupEvent;

import static ca.team2706.scouting.mcmergemanager.stronghold2016.gui.TeleopScouting.teleopScoutingObject;

public class BallPickupFragment extends DialogFragment {
    private FragmentListener listener;
    private FuelPickupEvent ballPickups = new FuelPickupEvent();
    public Bundle fuelPickupData = new Bundle();

    public BallPickupFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static BallPickupFragment newInstance(String title, FragmentListener listener) {
        BallPickupFragment frag = new BallPickupFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.listener = listener;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ball_pickup, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final DialogFragment me = this;

        view.findViewById(R.id.ball_pickup_cancel_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);
                    }
                }
        );

        view.findViewById(R.id.ground_ball_pickup_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ballPickups.pickupType = FuelPickupEvent.FuelPickupType.GROUND;

                        fuelPickupData.putSerializable("FuelPickupEvent", ballPickups);
                        listener.editNameDialogComplete(me, fuelPickupData);

                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                    }
                }
        );

        view.findViewById(R.id.other_ball_pickup_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ballPickups.pickupType = FuelPickupEvent.FuelPickupType.GROUND;
                        fuelPickupData.putSerializable("FuelPickupEvent", ballPickups);
                        listener.editNameDialogComplete(me, fuelPickupData);

                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                    }
                }
        );

        view.findViewById(R.id.hopper_ball_pickup_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ballPickups.pickupType = FuelPickupEvent.FuelPickupType.HOPPER;
                        fuelPickupData.putSerializable("FuelPickupEvent", ballPickups);
                        listener.editNameDialogComplete(me, fuelPickupData);

                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                    }
                }
        );

        view.findViewById(R.id.feeder_ball_pickup_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ballPickups.pickupType = FuelPickupEvent.FuelPickupType.WALL;
                        fuelPickupData.putSerializable("FuelPickupEvent", ballPickups);
                        listener.editNameDialogComplete(me, fuelPickupData);

                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                    }
                }
        );
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        listener.editNameDialogCancel(this);
    }

}