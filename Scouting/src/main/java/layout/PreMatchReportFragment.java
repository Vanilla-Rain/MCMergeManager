package layout;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.team2706.scouting.mcmergemanager.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.R;

public class PreMatchReportFragment extends Fragment {
    // the fragment initialization parameters
    public static final String ARG_MATCH_SCHEDULE = "MatchSchedule";
    public static final String ARG_MATCHNO = "MatchNo";

    // TODO: Rename and change types of parameters
    private MatchSchedule matchSchedule;
    private int matchNo;

    private OnFragmentInteractionListener mListener;

    public static PreMatchReportFragment newInstance(MatchSchedule matchSchedule, int matchNo) {
        PreMatchReportFragment fragment = new PreMatchReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MATCH_SCHEDULE, matchSchedule.toString());
        args.putInt(ARG_MATCHNO, matchNo);
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
            matchSchedule = new MatchSchedule( getArguments().getString(ARG_MATCH_SCHEDULE) );
            matchNo = getArguments().getInt(ARG_MATCHNO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pre_match_report, container, false);
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
