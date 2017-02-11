package ca.team2706.scouting.mcmergemanager.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ca.team2706.scouting.mcmergemanager.R;
import ca.team2706.scouting.mcmergemanager.backend.dataObjects.MatchSchedule;
import ca.team2706.scouting.mcmergemanager.backend.dataObjects.RepairTimeObject;

/**
 * Created by alden on 2017-02-04.
 */

public class RepairTimeCollection extends AppCompatActivity {

    private ArrayList<View> rows = new ArrayList<View>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.steamworks2017_repair_collection);

        // Unbundle the match schedule
        Intent intent = getIntent();
        String matchScheduleStr = intent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_SCHEDULE));
        MatchSchedule matchSchedule = new MatchSchedule(matchScheduleStr);

        // Get main layout
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.repair_collection_main_layout);

        //Get list of teams
        List<String> teams = matchSchedule.getTeamNumsAtEvent();

        // Create the fake list
        teams = new ArrayList<String>();
        teams.add("3242");
        teams.add("5467");

        for(String team: teams) {

            View row = generateRow(team);
            mainLayout.addView(row);

        }

    }

    public View generateRow(String teamNumber) {

        // Create a row
        LayoutInflater inflater = this.getLayoutInflater();
        View curRow = inflater.inflate(R.layout.steamworks2017_repair_collection_row, null);

        // Set the text thing
        TextView teamNumberTV = (TextView) curRow.findViewById(R.id.teamNumber);
        teamNumberTV.setText(teamNumber);

        rows.add(curRow);
        return curRow;

    }

    public void onSubmitClicked(View view) {

        RepairTimeObject[] repairObjects = generateRepairTimeObjects();

        // TODO: Send RepairTimeObjects to... somewhere?


    }

    public RepairTimeObject[] generateRepairTimeObjects() {

        ArrayList<RepairTimeObject> repairObjects = new ArrayList<RepairTimeObject>();

        // Loop through rows
        for(View curRow: rows) {

            RepairTimeObject repairObject = null;

            // Generate a repair object
            try {
                repairObject = generateRepairTimeObject(curRow);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Add repair object if it exists
            if(repairObject != null) {
                repairObjects.add(repairObject);
            }

        }

        RepairTimeObject[] out = new RepairTimeObject[repairObjects.size()];
        for(int i = 0; i < repairObjects.size(); i++) {
            out[i] = repairObjects.get(i);
        }

        return out;

    }

    public RepairTimeObject generateRepairTimeObject(View row) throws JSONException {

        // Get views & stuff
        TextView teamNumber = (TextView) row.findViewById(R.id.teamNumber);
        RadioButton notAvailable = (RadioButton) row.findViewById(R.id.notAvaiable);
        RadioButton yes = (RadioButton) row.findViewById(R.id.yes);
        RadioButton no = (RadioButton) row.findViewById(R.id.no);

        // Determine which radio button was selected
        if(notAvailable.isChecked()) {
            return new RepairTimeObject(Integer.parseInt("" + teamNumber.getText()), RepairTimeObject.RepairStatus.NOT_AVAILABLE);
        }
        else if(yes.isChecked()) {
            return new RepairTimeObject(Integer.parseInt("" + teamNumber.getText()), RepairTimeObject.RepairStatus.REPAIRING);
        }
        else if(no.isChecked()) {
            return new RepairTimeObject(Integer.parseInt("" + teamNumber.getText()), RepairTimeObject.RepairStatus.WORKING);
        }

        // If none were selected, return null
        return null;

    }

}
