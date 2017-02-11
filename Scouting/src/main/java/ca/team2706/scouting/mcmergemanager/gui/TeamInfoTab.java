package ca.team2706.scouting.mcmergemanager.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.List;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.StatsEngine;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.TeamStatsReport;

/**
 * Created by cnnr2 on 2015-10-31.
 */
public class TeamInfoTab extends Fragment {
//                        implements View.OnKeyListener {

    private static boolean accepted = false;
    private static boolean canceled = false;

    private Bundle mSavedInstanceState = null;

    private View mView;
    private FragmentManager mFragmentManager;
    private TeamInfoFragment mTeamInfoFragment;
    private AutoCompleteTextView mAutoCompleteTextView;

    private EditText editText;
    private static String inputResult;
    private View edit;
    private AlertDialog.Builder alert;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;

        mView = inflater.inflate(R.layout.team_info_tab, container, false);

        bindDownloadImageButton();
        bindSearchImageButton();

        mAutoCompleteTextView = (AutoCompleteTextView) mView.findViewById(R.id.teamNumberAutoCompleteTV);

//        mAutoCompleteTextView.setOnKeyListener(this);

        return mView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mFragmentManager = getFragmentManager();

        // Build the autocomplete list
        List<String> teamsAtEventList = MainActivity.sMatchSchedule.getTeamNumsAtEvent();
        String[] autocompleteList = new String[teamsAtEventList.size()];
        for (int i = 0; i < teamsAtEventList.size(); i++)
            autocompleteList[i] = teamsAtEventList.get(i);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.me, android.R.layout.simple_dropdown_item_1line,
                autocompleteList);

        mAutoCompleteTextView.setAdapter(adapter);
        mAutoCompleteTextView.setThreshold(0);
    }


    public boolean bindSearchImageButton() {
        ImageButton button = (ImageButton) mView.findViewById(R.id.searchBtn);

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // load the team stats fragment

                // If we're being restored from a previous state,
                // then we don't need to do anything and should return or else
                // we could end up with overlapping fragments.
                if(mSavedInstanceState!=null) {
                    return;
                }

                int teamNumber;
                try {
                    teamNumber = Integer.valueOf(mAutoCompleteTextView.getText().toString());
                }

                catch(NumberFormatException e) {
                    // they didn't type in a valid number. Too bad for them, we're not going any further!
                    return;
                }

                // Create a new Fragment to be placed in the activity layout
                mTeamInfoFragment=new TeamInfoFragment();

                Bundle args = new Bundle();
                args.putInt("teamNumber",teamNumber);
                StatsEngine statsEngine = new StatsEngine(MainActivity.sMatchData, MainActivity.sMatchSchedule);

                TeamStatsReport teamStatsReport = statsEngine.getTeamStatsReport(teamNumber);  // just so I can look at it in bebug
                args.putSerializable( getString(R.string.EXTRA_TEAM_STATS_REPORT),teamStatsReport );
                mTeamInfoFragment.setArguments(args);

                // Add the fragment to the 'fragment_container' FrameLayout
//                getActivity()
//                .getFragmentManager()
                mFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, mTeamInfoFragment)
                .commit();
            }
        });

        return true;
    }

    public boolean bindDownloadImageButton() {
        // Inflate the menu; this adds items to the action bar if it is present.
        ImageButton button = (ImageButton) mView.findViewById(R.id.imageButton);

        button.setOnClickListener(new View.OnClickListener() {


            @Override

            public void onClick(View view) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_download_alert, null);
                alert = new AlertDialog.Builder(getActivity());
                Log.e("this far", "so close");
                alert.setTitle("Download Competition");
                alert.setView(alertLayout);
                alert.setCancelable(false);
                final Bundle args = new Bundle();
                Spinner spinner = (Spinner) alertLayout.findViewById(R.id.year_spinner);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.year_array, android.R.layout.simple_spinner_item);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                args.putString("selectedYear", "2016");
                                break;
                            case 1:
                                args.putString("selectedYear", "2015");
                                break;
                            case 2:
                                args.putString("selectedYear", "2014");
                                break;
                            case 3:
                                args.putString("selectedYear", "2013");
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);
                //this stuff gets the edittext from the mView and sets the hint and the inputtype
                edit = alertLayout.findViewById(R.id.inputHint);
                if (edit instanceof EditText) {
                    editText = (EditText) edit;
                    editText.setHint("Competition ID - See Help for more");

                }


                Log.d("editing hint", "you got that");


                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        canceled = true;
                    }
                });

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                accepted = true;
                                inputResult = editText.getText().toString();
                                TeamInfoFragment fragment1 = new TeamInfoFragment();

                                args.putString("inputResult", inputResult);
                                args.putBoolean("accepted", accepted);
                                fragment1.setArguments(args);

                                // Add the fragment to the 'fragment_container' FrameLayout
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, fragment1).commit();
                            }
                        }
                );
                AlertDialog dialog = alert.create();
                dialog.show();
            }


        });

        return true;

    }

}
