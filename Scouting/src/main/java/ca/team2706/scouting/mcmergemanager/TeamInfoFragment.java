package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class TeamInfoFragment extends Fragment
                implements PhotoRequester, DataRequester {

    private OnFragmentInteractionListener mListener;
    private int m_teamNumber;
    private View m_view;

    public TeamInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_view = inflater.inflate(R.layout.fragment_team_info,null);

        Bundle args = getArguments();
        m_teamNumber = (int) args.get("teamNumber");

        FileUtils fileUtils = new FileUtils(getActivity());
        fileUtils.getTeamPhotos(m_teamNumber, this);

        return m_view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {

    }



    public void updatePhotos(Bitmap[] photos) {
        if (photos.length == 0) {
            // there are no photos to display
            Toast.makeText(getActivity(), "No Images to display", Toast.LENGTH_SHORT).show();

            // we should probably like hide the whole photos bar or something.
            return;
        }

        ImageView imageView = (ImageView) m_view.findViewById(R.id.imageView);
        imageView.setImageBitmap(photos[0]);
    }


    public void updateData(String[] matchResultsDataCSV, String[] matchScoutingDataCSV) {

    }

}
