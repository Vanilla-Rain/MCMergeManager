package ca.team2706.scouting.mcmergemanager.gui;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import ca.team2706.scouting.mcmergemanager.stronghold2016.StatsEngine;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.stronghold2016.dataObjects.TeleopScoutingObject;

public class PreMatchReportFragment extends Fragment {
    // the fragment initialization parameters
    public static final String ARG_MATCH = "Match";
    public static final String ARG_STATS = "STATS_ENGINE";

    private MatchSchedule.Match m_match;
    private StatsEngine m_statsEngine;

    private View m_view;

    private OnFragmentInteractionListener mListener;

    public static PreMatchReportFragment newInstance(MatchSchedule.Match match, StatsEngine statsEngine) {
        PreMatchReportFragment fragment = new PreMatchReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MATCH, match.toString());
        args.putSerializable(ARG_STATS, statsEngine);
        fragment.setArguments(args);
        return fragment;
    }
    public PreMatchReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            m_match = new MatchSchedule.Match( getArguments().getString(ARG_MATCH) );
            m_statsEngine = (StatsEngine) getArguments().getSerializable(ARG_STATS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_view =  inflater.inflate(R.layout.fragment_pre_match_report, container, false);

        // stick all the data on the screen

        // by the way PrimaryTab is written, m_match.matchNo will always be populated
        ((TextView) m_view.findViewById(R.id.titleTV) ).setText("Pre-Match Report for Match #" + m_match.getMatchNo());

        if (m_match.getBlue1() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.blue1TV) ).setText(m_match.getBlue1()+"");

        if (m_match.getBlue2() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.blue2TV)).setText(m_match.getBlue2()+"");

        if (m_match.getBlue3() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.blue3TV)).setText(m_match.getBlue3()+"");

        if (m_match.getRed1() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.red1TV)).setText(m_match.getRed1()+"");

        if (m_match.getRed2() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.red2TV)).setText(m_match.getRed2()+"");

        if (m_match.getRed3() != 0) // else leave the default text
            ((TextView) m_view.findViewById(R.id.red3TV)).setText(m_match.getRed3()+"");


        // if the match has been played yet
        if (m_match.getBlueScore() >= 0 && m_match.getRedScore() >= 0) {
            ((TextView) m_view.findViewById(R.id.blueScoreTV)).setText(m_match.getBlueScore()+"");
            ((TextView) m_view.findViewById(R.id.redScoreTV)).setText(m_match.getRedScore()+"");
            ((TextView) m_view.findViewById(R.id.predicted_finalTV)).setText("(final)");
        } else {
            // display the OPR-predicted score instead
            displayOprPredection();
        }

        displayDefenseChoices();

        return m_view;
    }

    /**
     * If this match has not yet been played, display a prediction based on OPRs
     */
    private void displayOprPredection() {

        Map<Integer, Double> oprs = m_statsEngine.getOPRs();

        float bluePredScore=0, redPredScore=0;

        bluePredScore += oprs.get( m_match.getBlue1() );

        bluePredScore += oprs.get( m_match.getBlue2() );

        bluePredScore += oprs.get( m_match.getBlue3() );

        redPredScore += oprs.get( m_match.getRed1() );

        redPredScore += oprs.get( m_match.getRed2() );

        redPredScore += oprs.get(m_match.getRed3());


        ((TextView) m_view.findViewById(R.id.blueScoreTV)).setText( (int)bluePredScore +"");
        ((TextView) m_view.findViewById(R.id.redScoreTV)).setText((int)redPredScore +"");
        ((TextView) m_view.findViewById(R.id.predicted_finalTV)).setText("(predicted)");
    }

    private void displayDefenseChoices() {
        int[] blueDefensesBreached = new int[TeleopScoutingObject.NUM_DEFENSES];
        int[] redDefensesBreached = new int[TeleopScoutingObject.NUM_DEFENSES];

        for (int i=0; i<TeleopScoutingObject.NUM_DEFENSES; i++) {
            blueDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getBlue1() ).defensesBreached[i];
            blueDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getBlue2() ).defensesBreached[i];
            blueDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getBlue3() ).defensesBreached[i];
            redDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getRed1() ).defensesBreached[i];
            redDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getRed2() ).defensesBreached[i];
            redDefensesBreached[i] += m_statsEngine.getTeamStatsReport( m_match.getRed3() ).defensesBreached[i];
        }

        // now display them


        // Category A

        // blue
        ((TextView) m_view.findViewById(R.id.bluePortcullisTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] + ")");
        ((TextView) m_view.findViewById(R.id.blueChevaleTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL] + ")");

        if (redDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] < redDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL]) {
            // portcullis has fewer breaches
            ((TextView) m_view.findViewById(R.id.bluePortcullisTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.bluePortcullisTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        else if (redDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] > redDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL]) {
            // chevale de frise has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueChevaleTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.bluePortcullisTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        // else equal, don't suggest anything

        // red
        ((TextView) m_view.findViewById(R.id.redPortcullisTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] + ")");
        ((TextView) m_view.findViewById(R.id.redChevaleTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL] + ")");

        if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] < blueDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL]) {
            // portcullis has fewer breaches
            ((TextView) m_view.findViewById(R.id.redPortcullisTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redPortcullisTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        else if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_PORTCULLIS] > blueDefensesBreached[TeleopScoutingObject.DEFENSE_CHEVAL]) {
            // chevale de frise has fewer breaches
            ((TextView) m_view.findViewById(R.id.redChevaleTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redPortcullisTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        // else equal, don't suggest anything


        // Category B

        // blue
        ((TextView) m_view.findViewById(R.id.blueMoatTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] + ")");
        ((TextView) m_view.findViewById(R.id.blueRampartTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART] + ")");

        if (redDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] < redDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART]) {
            // moat has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueMoatTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueMoatTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        else if (redDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] > redDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART]) {
            // rampart has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueRampartTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueRampartTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        // else equal, don't suggest anything

        // red
        ((TextView) m_view.findViewById(R.id.redMoatTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] + ")");
        ((TextView) m_view.findViewById(R.id.redRampartTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART] + ")");

        if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] < blueDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART]) {
            // moat has fewer breaches
            ((TextView) m_view.findViewById(R.id.redMoatTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redMoatTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        else if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_MOAT] > blueDefensesBreached[TeleopScoutingObject.DEFENSE_RAMPART]) {
            // rampart has fewer breaches
            ((TextView) m_view.findViewById(R.id.redRampartTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redRampartTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        // else equal, don't suggest anything


        // Category C
        
        // blue
        ((TextView) m_view.findViewById(R.id.blueDrawbridgeTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] + ")");
        ((TextView) m_view.findViewById(R.id.blueSallyportTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT] + ")");

        if (redDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] < redDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT]) {
            // drawdridge has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueDrawbridgeTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueDrawbridgeTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        else if (redDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] > redDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT]) {
            // sallyport has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueSallyportTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueSallyportTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        // else equal, don't suggest anything

        // red
        ((TextView) m_view.findViewById(R.id.redDrawbridgeTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] + ")");
        ((TextView) m_view.findViewById(R.id.redSallyportTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT] + ")");

        if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] < blueDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT]) {
            // drawdridge has fewer breaches
            ((TextView) m_view.findViewById(R.id.redDrawbridgeTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redDrawbridgeTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        else if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_DRAWBRIDGE] > blueDefensesBreached[TeleopScoutingObject.DEFENSE_SALLYPORT]) {
            // sallyport has fewer breaches
            ((TextView) m_view.findViewById(R.id.redSallyportTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redSallyportTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        // else equal, don't suggest anything


        // Category D

        // blue
        ((TextView) m_view.findViewById(R.id.blueRockwallTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] + ")");
        ((TextView) m_view.findViewById(R.id.blueRoughTV)).append(" (" + redDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN] + ")");

        if (redDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] < redDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN]) {
            // drawdridge has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueRockwallTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueRockwallTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        else if (redDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] > redDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN]) {
            // sallyport has fewer breaches
            ((TextView) m_view.findViewById(R.id.blueRoughTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.blueRoughTV)).setTextColor(getResources().getColor(R.color.blue_alliance));
        }
        // else equal, don't suggest anything

        // red
        ((TextView) m_view.findViewById(R.id.redRockwallTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] + ")");
        ((TextView) m_view.findViewById(R.id.redRoughTV)).append(" (" + blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN] + ")");

        if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] < blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN]) {
            // drawdridge has fewer breaches
            ((TextView) m_view.findViewById(R.id.redRockwallTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redRockwallTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        else if (blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROCKWALL] > blueDefensesBreached[TeleopScoutingObject.DEFENSE_ROUGH_TERRAIN]) {
            // sallyport has fewer breaches
            ((TextView) m_view.findViewById(R.id.redRoughTV)).setTypeface(null, Typeface.BOLD);
            ((TextView) m_view.findViewById(R.id.redRoughTV)).setTextColor(getResources().getColor(R.color.red_alliance));
        }
        // else equal, don't suggest anything
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
