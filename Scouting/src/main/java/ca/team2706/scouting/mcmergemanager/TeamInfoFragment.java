package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;


public class TeamInfoFragment extends Fragment
        implements PhotoRequester {

    private OnFragmentInteractionListener mListener;
    private int m_teamNumber;
    private View m_view;
    public  FileUtils fileUtils;
    private String textViewPerformanceString;
    private String nicknameString;
    public String name;
    public AlertDialog.Builder alert;

    public StatsEngine.TeamStatsReport m_teamStatsReport;

    public TeamInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        m_view = inflater.inflate(R.layout.fragment_team_info, null);


        fileUtils = new FileUtils(getActivity());
        final Bundle args = getArguments();

        if(args.get("teamNumber") !=null ) {
            m_teamNumber = (int) args.get("teamNumber");
            fileUtils.getTeamPhotos(m_teamNumber, this);

            Runnable getStuff = new Runnable() {
                public void run() {
                    textViewPerformanceString =  fileUtils.getBlueAllianceDataForTeam(m_teamNumber);
                    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null) { // not connected to the internet
                        nicknameString = fileUtils.getBlueAllianceData("nickname", "http://www.thebluealliance.com/api/v2/team/frc" + m_teamNumber + "?X-TBA-App-Id=frc2706:mergemanager:v01/");
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui
                            TextView textViewPerformance = (TextView) m_view.findViewById(R.id.textViewPerformance);
                            textViewPerformance.setText(textViewPerformanceString);

                            TextView nicknameTV = (TextView) m_view.findViewById(R.id.nicknameTV);
                            nicknameTV.setText(nicknameString);

                        }
                    });

                }
            };
            Thread getStuffThread = new Thread(getStuff);
            getStuffThread.start();

            m_teamStatsReport = (StatsEngine.TeamStatsReport) args.getSerializable(getString(R.string.EXTRA_TEAM_STATS_REPORT));
            if (m_teamStatsReport != null) {
                fillStatsData();
                m_view.findViewById(R.id.fullStatsBtn).setEnabled(true);
            }




        }
        Log.d("accepted", "" + args.getBoolean("accepted"));
        if(args.getBoolean("accepted")) {

            Toast.makeText(getActivity(),"Downloading... Please Wait",Toast.LENGTH_LONG).show();

            LayoutInflater myinflater = getActivity().getLayoutInflater();
            View alertLayout = myinflater.inflate(R.layout.progressbar, null);
            alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Downloading...");
            alert.setView(alertLayout);
            alert.setCancelable(false);
            final AlertDialog dialog = alert.create();
            dialog.show();
            final ProgressBar progress = (ProgressBar) alertLayout.findViewById(R.id.progressBar);
            final TextView team = (TextView) alertLayout.findViewById(R.id.textViewTeam);
            progress.setMax(100);
            progress.setProgress(0);

            Runnable downloadStuff = new Runnable() {
                public void run() {
                    for(int i = 0; i < 3; i++)
                        switch(i) {

                            case 0:
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        team.setText("2015");
                                    }
                                });

                                fileUtils.downloadBlueAllianceDataForEvent(args.getString("inputResult"), args.getString("selectedYear"), progress, dialog, 2015);

                                break;
                            case 1:
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        team.setText("2014");
                                        progress.setProgress(0);
                                    }
                                });
                                fileUtils.downloadBlueAllianceDataForEvent(args.getString("inputResult"), args.getString("selectedYear"), progress,dialog,2014);

                                break;
                            case 2:
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        team.setText("2013");
                                        progress.setProgress(0);
                                    }
                                });

                                fileUtils.downloadBlueAllianceDataForEvent(args.getString("inputResult"), args.getString("selectedYear"), progress, dialog,2013);


                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                });


                                break;
                        }

                }
            };
            Thread downloadStuffThread = new Thread(downloadStuff);
            downloadStuffThread.start();
            args.putBoolean("accepted", false);
        }

        Button fullStatsBtn = (Button) m_view.findViewById(R.id.fullStatsBtn);
        fullStatsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TeamStatsActivity.class);
                intent.putExtra(getString(R.string.EXTRA_TEAM_STATS_REPORT), m_teamStatsReport);
                startActivity(intent);
            }
        });




        return m_view;
    }

    private void fillStatsData() {

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


//    public void updateData(String[] matchResultsDataCSV, String[] matchScoutingDataCSV) {
//
//    }
//
//    public void updateMatchSchedule(MatchSchedule matchSchedule) {
//
//    }

}
