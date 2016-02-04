package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ca.team2706.scouting.mcmergemanager.datamodels.MatchSchedule;

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

        activity = this;

        // unbundle the match schedule from the intent
        Intent intent = getIntent();
        String matchScheduleStr = intent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_SCHEDULE));
        int teamNo = intent.getIntExtra(getResources().getString(R.string.EXTRA_TEAM_NO), -1) ;


        TextView titleView = new TextView(activity);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);

        matchSchedule = new MatchSchedule(matchScheduleStr);

        if (teamNo == -1) {
            titleView.setText("Full Schedule");
        } else {
            matchSchedule = matchSchedule.filterByTeam(teamNo);
            titleView.setText("Schedule for Team "+teamNo);
        }

        MatchesArrayAdapter adapter = new MatchesArrayAdapter(this, R.layout.schedule_row_layout, matchSchedule);
        this.getListView().addHeaderView(titleView);
        setListAdapter(adapter);

    }


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
            }

            final MatchSchedule.Match match = matchSchedule.getMatchNo(position);
            if (match != null) {
                TextView matchNoTV=(TextView) convertView.findViewById(R.id.schedule_matchNoTV);
                matchNoTV.setText( String.format("%3s  |", match.getMatchNo()) );

                TextView blueAllianceTV=(TextView) convertView.findViewById(R.id.schedule_blueAllianceTV);
                blueAllianceTV.setText( String.format("%4s, %4s, %4s", match.getBlue1(), match.getBlue2(),match.getBlue3()) );

                TextView redAllianceTV=(TextView) convertView.findViewById(R.id.schedule_redAllianceTV);
                redAllianceTV.setText( String.format("%4s, %4s, %4s",match.getRed1(), match.getRed2(), match.getRed3()) );

                TextView blueScoreTV=(TextView) convertView.findViewById(R.id.schedule_blueScoreTV);
                blueScoreTV.setText( String.format("%4d", match.getBlueScore()) );

                TextView redScoreTV=(TextView) convertView.findViewById(R.id.schedule_redScoreTV);
                redScoreTV.setText( String.format("%4d", match.getRedScore()) );
            }

            return convertView;
        }
    }

}


