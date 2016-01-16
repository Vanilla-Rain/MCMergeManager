package ca.team2706.scouting.mcmergemanager;

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
import java.util.Collections;
import java.util.List;

public class TeamCyclesReport extends ListActivity {

    private CycleArrayAdapter adapter;

    Intent srcIntent;

    int teamNo;

    int[] counts;
    double[] ptsPerS;
    double[] times;
    int wins, losses, ties;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        teamNo = intent.getIntExtra( getResources().getString(R.string.EXTRA_TEAM_NO), 0 );
        setTitle( "Team " + teamNo+"'s cycles" );

        counts = (int[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_TEAM_CYCLE_COUNTS));
        ptsPerS = (double[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_PTS_PER_S));
        times = (double[]) intent.getSerializableExtra(getResources().getString(R.string.EXTRA_TIMES));

        fill();
    }

    private void fill()  {
        List<Option> cyclesViews = new ArrayList<Option>();

        for(int i=0; i<times.length; i++) {
            cyclesViews.add(new Option( CycleAnalyzer.lookupName(i), times[i], counts[i], ptsPerS[i]));
        }

        Collections.sort(cyclesViews);

        adapter = new CycleArrayAdapter( this, R.layout.team_cycle_view, cyclesViews);
        this.setListAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do nothing -- not clickable
    }

    private class Option implements Comparable<Option>{
        private String cycleType;
        private double time;
        private double ptsPerS;
        private int count;

        public Option(String cycleType, double time, int count, double ptsPerS) {
            this.cycleType = cycleType;
            this.time = time;
            this.ptsPerS = ptsPerS;
            this.count = count;
        }
        public String getType() {
            return cycleType;
        }
        public double getPtsPerS(){
            return ptsPerS;
        }
        public int getCount(){
            return count;
        }
        public double getTime() {
            return time;
        }
        @Override
        public int compareTo(Option o) {
            return o.count - this.count;
        }
    }

    private class CycleArrayAdapter extends ArrayAdapter<Option> {

        private Context c;
        private int id;
        private List<Option>items;

        public CycleArrayAdapter(Context context, int textViewResourceId,
                                 List<Option> objects) {
            super(context, textViewResourceId, objects);
            c = context;
            id = textViewResourceId;
            items = objects;
        }

        public Option getItem(int i)
        {
            return items.get(i);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(id, null);
            }
            final Option o = items.get(position);
            if (o != null) {
                TextView t1 = (TextView) v.findViewById(R.id.cycleTypeTV);
                TextView t2 = (TextView) v.findViewById(R.id.numberOfTimesTV);
                TextView t3 = (TextView) v.findViewById(R.id.avgTimeTV);
                TextView t4 = (TextView) v.findViewById(R.id.ptsPerTV);

                if(t1!=null)
                    t1.setText(o.getType());
                if(t2!=null)
                    t2.setText(o.getCount()+"x ");
                if (t3!=null) {
                    if(o.getTime() < 0) {
                        t3.setText("0.0 s");
                    } else {
                        double rounded = Math.round( o.getTime()*10) / 10.0;
                        t3.setText( rounded+" s");
                    }
                }
                if(t4!=null) {
                    double rounded = Math.round( o.getPtsPerS()*10000) / 10000.0;
                    t4.setText( rounded+" pts/s");
                }

            }
            return v;
        }
    }
}