package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * Created by mike on 31/01/16.
 */
public class ScheduleActivity extends ListActivity {

    Activity activity;

    MatchSchedule matchSchedule;

    ArrayList<String> matchNos = new ArrayList<String>();
    ArrayList<String> blueAlliances = new ArrayList<String>();
    ArrayList<String> redAlliances = new ArrayList<String>();
    ArrayList<String> blueScores = new ArrayList<String>();
    ArrayList<String> redScores = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Match Schedule");

        activity = this;

        // unbundle the match schedule from the intent
        Intent intent = getIntent();
        String matchScheduleStr = intent.getStringExtra( getResources().getString(R.string.EXTRA_MATCH_SCHEDULE) );
        matchSchedule = new MatchSchedule(matchScheduleStr);

        MatchesArrayAdapter adapter = new MatchesArrayAdapter(this, R.layout.schedule_row_layout, matchSchedule);
        setListAdapter(adapter);

//        // show a default thing in the odd case that there's nothing to show.
//        matchNos.add("Nothing to display, maybe you don't have an internet connection?");
//
//
//        // initiate the listadapters
//        setListAdapter( new ArrayAdapter <String>(this,
//                R.layout.schedule_row_layout, R.id.schedule_matchNoTV, matchNos));
//
//        setListAdapter( new ArrayAdapter <String>(this,
//                R.layout.schedule_row_layout, R.id.schedule_blueAllianceTV, redAlliances));
//
//        setListAdapter( new ArrayAdapter <String>(this,
//                R.layout.schedule_row_layout, R.id.schedule_redAllianceTV, redAlliances));
//
//        setListAdapter( new ArrayAdapter <String>(this,
//                R.layout.schedule_row_layout, R.id.schedule_blueScoreTV, blueScores));
//
//        setListAdapter( new ArrayAdapter <String>(this,
//                R.layout.schedule_row_layout, R.id.schedule_redScoreTV, redScores));
    }


//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        LayoutInflater inflater = getLayoutInflater();
//
//        if(convertView == null){
//
//            convertView=inflater.inflate(R.layout.schedule_row_layout, null);
//        }
//
//        MatchSchedule.Match match = matchSchedule.getMatchNo(position);
//
//        TextView matchNoTV=(TextView) convertView.findViewById(R.id.schedule_matchNoTV);
//        matchNoTV.setText(match.getMatchNo() + " | ");
//
//        TextView blueAllianceTV=(TextView) convertView.findViewById(R.id.schedule_blueAllianceTV);
//        blueAllianceTV.setText(match.getBlue1()+"," +match.getBlue2()+","+match.getBlue3());
//
//        TextView redAllianceTV=(TextView) convertView.findViewById(R.id.schedule_redAllianceTV);
//        redAllianceTV.setText(match.getRed1()+","+match.getRed2()+","+match.getRed3());
//
//        TextView blueScoreTV=(TextView) convertView.findViewById(R.id.schedule_blueScoreTV);
//        blueScoreTV.setText(""+match.getBlueScore());
//
//        TextView redScoreTV=(TextView) convertView.findViewById(R.id.schedule_redScoreTV);
//
//
//        return convertView;
//    }

//    @Override
//    public void updateData(String[] matchResultsDataCSV, String[] matchScoutingDataCSV) {
//
//    }

//    @Override
//    public void updateMatchSchedule(MatchSchedule matchSchedule) {
//
//
//        List<MatchSchedule.Match> matches = matchSchedule.getMatches();
//        for(MatchSchedule.Match match : matches) {
//            matchNos.add(match.getMatchNo() + " | ");
//            blueAlliances.add(match.getBlue1()+","+match.getBlue2()+","+match.getBlue3());
//            redAlliances.add(match.getRed1()+","+match.getRed2()+","+match.getRed3());
//            blueScores.add(""+match.getBlueScore());
//            redScores.add(""+match.getRedScore());
//        }
//
//    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do nothing -- not clickable
    }


    private class MatchesArrayAdapter extends ArrayAdapter<MatchSchedule.Match> {

        private Context c;
        private int id;
        private MatchSchedule matchSchedule;

        public MatchesArrayAdapter(Context context, int textViewResourceId,
                                 MatchSchedule matchSchedule) {
            super(context, textViewResourceId, matchSchedule.getMatches());
            c = context;
            id = textViewResourceId;
            this.matchSchedule = matchSchedule;
        }

        public MatchSchedule.Match getItem(int i)
        {
            return matchSchedule.getMatchNo(i);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = activity.getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.schedule_row_layout, null);
//                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v = vi.inflate(id, null);
            }

            final MatchSchedule.Match match = matchSchedule.getMatchNo(position);
            if (match != null) {
                TextView matchNoTV=(TextView) convertView.findViewById(R.id.schedule_matchNoTV);
                matchNoTV.setText(match.getMatchNo() + " | ");

                TextView blueAllianceTV=(TextView) convertView.findViewById(R.id.schedule_blueAllianceTV);
                blueAllianceTV.setText(match.getBlue1()+"," +match.getBlue2()+","+match.getBlue3());

                TextView redAllianceTV=(TextView) convertView.findViewById(R.id.schedule_redAllianceTV);
                redAllianceTV.setText(match.getRed1()+","+match.getRed2()+","+match.getRed3());

                TextView blueScoreTV=(TextView) convertView.findViewById(R.id.schedule_blueScoreTV);
                blueScoreTV.setText(""+match.getBlueScore());

                TextView redScoreTV=(TextView) convertView.findViewById(R.id.schedule_redScoreTV);
                redScoreTV.setText(""+match.getRedScore());
            }

            return convertView;
        }
    }

}


