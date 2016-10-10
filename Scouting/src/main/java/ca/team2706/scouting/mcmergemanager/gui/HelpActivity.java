package ca.team2706.scouting.mcmergemanager.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvEventIds = (TextView)findViewById(R.id.eventIdsTextView);
        tvEventIds.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
