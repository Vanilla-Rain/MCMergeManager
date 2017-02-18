package ca.team2706.scouting.mcmergemanager.steamworks2017.gui.Popups;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.FuelShotEvent;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.PostGameObject;
import ca.team2706.scouting.mcmergemanager.steamworks2017.gui.PostGameClass;

import static ca.team2706.scouting.mcmergemanager.R.*;

/**
 * Created by Merge on 2017-02-12.
 */

public class ClimbingFragment extends DialogFragment {

    SeekBar climbTimeSeekBar;
    public int pointsScored;

    public String test = " seconds to climb";
    public String pointsScoredString;
    public String textViewDisplayString;


    private PostGameObject postGameObject = new PostGameObject();
    private FragmentListener listener;


    public ClimbingFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ClimbingFragment newInstance(String title, FragmentListener listener) {
        ClimbingFragment frag = new ClimbingFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.listener = listener;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_climbing, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final DialogFragment me = this;

        view.findViewById(id.climbCancelButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);
                    }
                }
        );

// ballsScored.numScored
        // initiate  views
        climbTimeSeekBar=(SeekBar)view.findViewById(id.climbingTimeSeekBar);
        // perform seek bar change listener event used for getting the progress value
        climbTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView tvd = (TextView) getView().findViewById(id.climbTimeTextView);
                pointsScored = progressChangedValue*5;
                pointsScoredString = String.valueOf(pointsScored);
                textViewDisplayString = pointsScoredString + test;
                tvd.setText(textViewDisplayString);
//              Toast.makeText(getActivity(), "Data saved!", Toast.LENGTH_LONG).show();
            }

        });
        view.findViewById(id.climbFailedButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postGameObject.climbType = PostGameObject.ClimbType.FAIL;
                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                        Intent intent = new Intent(v.getContext(),PostGameClass.class);
                        startActivity(intent);

                    }
                }
        );

        view.findViewById(id.climbingSuccessButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postGameObject.climbType = PostGameObject.ClimbType.SUCCESS;
                        postGameObject.climb_time = pointsScored;
                        Log.i(getClass().getName(), "quit");
                        listener.editNameDialogCancel(me);

                        Intent intent = new Intent(v.getContext(),PostGameClass.class);
                        startActivity(intent);
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