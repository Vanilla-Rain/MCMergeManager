package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TeamInfoFragment extends Fragment
                implements PhotoRequester, DataRequester {

    private OnFragmentInteractionListener mListener;
    private int m_teamNumber;
    private View m_view;
    public  FileUtils fileUtils;
    private String textViewPerformanceString;
    private String textViewScoreString;
public String name;
    public AlertDialog.Builder alert;
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
        textViewScore = (TextView) m_view.findViewById(R.id.textViewScore);
        textViewPerformance = (TextView) m_view.findViewById(R.id.textViewPerformance);



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
            textViewScoreString = fileUtils.getBlueAllianceData("nickname", "http://www.thebluealliance.com/api/v2/team/frc" + m_teamNumber + "?X-TBA-App-Id=frc2706:mergemanager:v01/");
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewPerformance.setText(textViewPerformanceString);
                textViewScore.setText(textViewScoreString);
//stuff that updates ui

            }
        });

    }
};
Thread getStuffThread = new Thread(getStuff);
            getStuffThread.start();

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


    public TextView textViewScore;
    public TextView textViewPerformance;

    public boolean accepted;

    private static String STORETEXT;

    public String inputResult;







}
