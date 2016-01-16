package ca.team2706.scouting.mcmergemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cnnr2 on 2015-10-31.
 */
public class TeamInfoTab extends Fragment {

    private Bundle m_savedInstanceState = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_savedInstanceState = savedInstanceState;

        View view = inflater.inflate(R.layout.team_info_tab, container, false);

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

                          int teamNumber = 0;
                          try {
                              teamNumber = Integer.parseInt(query);
                          } catch (NumberFormatException e) {
                              // they didn't type in a valid number. Too bad for them, we're not going any further!
                              return false;
                          }

                          // Create a new Fragment to be placed in the activity layout
                          TeamInfoFragment fragment = new TeamInfoFragment();
                          Bundle args = new Bundle();
                          args.putInt("teamNumber", teamNumber);
                          fragment.setArguments(args);

                          // Add the fragment to the 'fragment_container' FrameLayout
                          getActivity().getFragmentManager().beginTransaction()
                                  .replace(R.id.fragment_container, fragment).commit();
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
}
