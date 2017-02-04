package ca.team2706.scouting.mcmergemanager.gui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.steamworks2017.dataObjects.MatchSchedule;

/**
 * Created by alden on 2017-02-04.
 */

public class RepairTimeCollection extends ListActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Unbundle the match schedule
        Intent intent = getIntent();
        String matchScheduleStr = intent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_SCHEDULE));
        MatchSchedule matchSchedule = new MatchSchedule(matchScheduleStr);

        

    }

    public View generateRow(String teamNumber) {

        LayoutInflater inflater = this.getLayoutInflater();
        View curRow = inflater.inflate(R.layout.schedule_row_layout, null);

        TextView teamNumberTV = (TextView) curRow.findViewById(R.id.teamNumber);
        teamNumberTV.setText(teamNumber);

        return curRow;

    }

}
