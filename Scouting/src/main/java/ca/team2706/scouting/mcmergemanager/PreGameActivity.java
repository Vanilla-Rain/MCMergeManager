package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import ca.team2706.scouting.mcmergemanager.datamodels.PreGameObject;

public class PreGameActivity extends AppCompatActivity {
    private Intent srcIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_game);
        Intent intent = getIntent();
        srcIntent = intent;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Buttons
    public void startGame(View view) {
        EditText matchNumField = (EditText) findViewById(R.id.match_num_field);
        String matchNum = matchNumField.getText().toString();
        int matchNumInt;
        if(matchNum.equals("")) return;
        try {
            matchNumInt = Integer.parseInt(matchNum);
        } catch(NumberFormatException e) {
            return;
        }


        Intent intent = new Intent(this,NewAutoScouting.class);
        intent.putExtra("PreGameData",new PreGameObject(matchNumInt, -1)); //TODO TEAM NUMBERS
        startActivity(intent);
    }

}
