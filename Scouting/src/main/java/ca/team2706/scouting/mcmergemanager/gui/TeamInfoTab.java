package ca.team2706.scouting.mcmergemanager.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.StatsEngine;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.TeamStatsReport;

/**
 * Created by cnnr2 on 2015-10-31.
 */
public class TeamInfoTab extends Fragment {

    private Bundle m_savedInstanceState = null;
    public View view;
    public TeamInfoFragment m_teamInfoFragment;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_savedInstanceState = savedInstanceState;

        view = inflater.inflate(R.layout.team_info_tab, container, false);

        launchImageButton();
        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // load the team stats fragment

                        // If we're being restored from a previous state,
                        // then we don't need to do anything and should return or else
                        // we could end up with overlapping fragments.
                        if (m_savedInstanceState != null) {
                            return false;
                        }

                        int teamNumber;
                        try {
                            teamNumber = Integer.parseInt(query);
                        } catch (NumberFormatException e) {
                            // they didn't type in a valid number. Too bad for them, we're not going any further!
                            return false;
                        }

                        // Create a new Fragment to be placed in the activity layout
                        m_teamInfoFragment = new TeamInfoFragment();
                        Bundle args = new Bundle();
                        args.putInt("teamNumber", teamNumber);
                        StatsEngine statsEngine = new StatsEngine(MainActivity.mMatchData, MainActivity.mMatchSchedule);

                        TeamStatsReport teamStatsReport = statsEngine.getTeamStatsReport(teamNumber);  // just so I can look at it in bebug
                        args.putSerializable(getString(R.string.EXTRA_TEAM_STATS_REPORT), teamStatsReport);
                        m_teamInfoFragment.setArguments(args);

                        // Add the fragment to the 'fragment_container' FrameLayout
                        getActivity().getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, m_teamInfoFragment).commit();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // TODO: show a filtered autocomplete list of teams at this event
                        return false;
                    }
                }
        );

        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public EditText editText;
    public  static boolean accepted = false;
    public  static boolean canceled = false;
    public  static String inputResult;
    public View edit;
    public AlertDialog.Builder alert;
    public boolean launchImageButton() {
        // Inflate the menu; this adds items to the action bar if it is present.
        ImageButton button = (ImageButton) view.findViewById(R.id.imageButton);

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
                //this stuff gets the edittext from the view and sets the hint and the inputtype
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
                                getActivity().getFragmentManager().beginTransaction()
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
