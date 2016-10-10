package ca.team2706.scouting.mcmergemanager.gui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.StatsEngine;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchSchedule;


public class PrimaryTab extends Fragment {

    private View v;
    private Bundle m_savedInstanceState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.primary_fragment_tab, null);
        m_savedInstanceState = savedInstanceState;

        TextView matchNoTV = (TextView) v.findViewById(R.id.matchNoET);

        matchNoTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // load the Pre-match recport fragment

                // If we're being restored from a previous state,
                // then we don't need to do anything and should return or else
                // we could end up with overlapping fragments.
                if (m_savedInstanceState != null) {
                    return false;
                }

                int matchNo = 0;
                try {
                    matchNo = Integer.parseInt( v.getText().toString() );
                } catch (NumberFormatException e) {
                    // they didn't type in a valid number. Too bad for them, we're not going any further!
                    return false;
                }


                // Create a new Fragment to be placed in the activity layout
                PreMatchReportFragment fragment = new PreMatchReportFragment();

                try {
                    // this is a little convoluted, since we're sending the whole schedule, we could just send the match number rather than a copy of that match

                    // bundle up the data it needs
                    Bundle args = new Bundle();
                    MatchSchedule.Match match;
                    match = MainActivity.m_matchSchedule.getMatchNo(matchNo-1);
                    args.putString(PreMatchReportFragment.ARG_MATCH, match.toString());  // if match == null, this will throw an exception and be caught
                    if (MainActivity.m_matchSchedule == null) return false;

                    StatsEngine statsEngine = new StatsEngine(MainActivity.m_matchData, MainActivity.m_matchSchedule);
                    args.putSerializable(PreMatchReportFragment.ARG_STATS, statsEngine);

                    fragment.setArguments(args);

                    // Add the fragment to the 'fragment_container' FrameLayout
                    getActivity().getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container1, fragment).commit();
                } catch (Exception e) {
                    // if we don't have the data, don't display it
                }

                return false;
            }
        });

        return v;
    }
}
