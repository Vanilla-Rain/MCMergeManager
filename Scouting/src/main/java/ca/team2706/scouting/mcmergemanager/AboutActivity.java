package ca.team2706.scouting.mcmergemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        // I have spent way too much time trying to get this stupid back button to work... I'm out of ideas and patients.
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
