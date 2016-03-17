package ca.team2706.scouting.mcmergemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import ca.team2706.scouting.mcmergemanager.datamodels.AutoScoutingObject;
import ca.team2706.scouting.mcmergemanager.datamodels.MatchData;
import ca.team2706.scouting.mcmergemanager.datamodels.PostGameObject;
import ca.team2706.scouting.mcmergemanager.datamodels.PreGameObject;
import ca.team2706.scouting.mcmergemanager.datamodels.TeleopScoutingObject;

/**
 * Created by MCSoftware on 2016-01-18.
 */
public class PostGameActivity extends AppCompatActivity {
    public int progress = 0;
    public SeekBar seekBar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_post_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        final TextView  textView = (TextView) findViewById(R.id.textViewSeekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Percentage Deadness: " + progress + "/" + seekBar.getMax());

            }
        });
    }

    public void returnHome(View view) {
        Intent thisIntent = getIntent();
        PreGameObject p = (PreGameObject) thisIntent.getSerializableExtra("PreGameData");
        AutoScoutingObject a = (AutoScoutingObject) thisIntent.getSerializableExtra("AutoScoutingData");
        TeleopScoutingObject t  = (TeleopScoutingObject) thisIntent.getSerializableExtra("TeleopScoutingData");
        EditText e = (EditText)findViewById(R.id.editTextPost);
        CheckBox c = (CheckBox)findViewById(R.id.challengedCB);
        PostGameObject post = new PostGameObject(e.getText().toString(),c.isChecked(),progress);
        Intent intent = new Intent(this,MainActivity.class);

        // SAVING STUFF
        MatchData.Match match = new MatchData.Match(p, a, t, post);
        FileUtils fileUtils = new FileUtils(this);
        fileUtils.appendToMatchDataFile(match);

        startActivity(intent);
    }

}
