package ca.team2706.scouting.mcmergemanager;

        import android.os.Bundle;
        import android.support.design.widget.FloatingActionButton;
        import android.support.design.widget.Snackbar;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.text.method.LinkMovementMethod;
        import android.view.View;
        import android.widget.TextView;

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
