package ca.team2706.scouting.mcmergemanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;
import layout.PreMatchReportFragment;


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
                Bundle args = new Bundle();

                // bundle up the match
                MatchSchedule.Match match;
                if (MainActivity.m_matchSchedule != null) {
                    match = MainActivity.m_matchSchedule.getMatchNo(matchNo-1);
                } else {
                    match = new MatchSchedule.Match();
                    match.setMatchNo(matchNo-1);
                }
                args.putString(PreMatchReportFragment.ARG_MATCH, match.toString());


                fragment.setArguments(args);

                // Add the fragment to the 'fragment_container' FrameLayout
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container1, fragment).commit();
                return false;
            }
        });

        return v;
    }
}
