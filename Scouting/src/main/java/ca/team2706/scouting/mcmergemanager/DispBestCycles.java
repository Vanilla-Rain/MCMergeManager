package ca.team2706.scouting.mcmergemanager;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mike on 04/03/14.
 */
public class DispBestCycles extends ListActivity {

        private CycleArrayAdapter adapter;

        boolean choose_files; // vs folders
        String extraToReturn;

        Intent globalIntent;

        public double[] times;
        public MainActivity parent;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Intent intent = getIntent();
            times = (double[]) intent.getSerializableExtra(MainActivity.EXTRA_AVE_CYCLE_TIMES);

            fill();
        }

        private void fill()  {
            this.setTitle("Avg Points per second by cycle type");
            List<Option> cyclesViews = new ArrayList<Option>();

            for(int i=0; i<times.length; i++) {
                cyclesViews.add(new Option( CycleAnalyzer.lookupName(i), times[i]));
            }

            Collections.sort(cyclesViews);

            adapter = new CycleArrayAdapter( this, R.layout.best_cycle_view, cyclesViews);
            this.setListAdapter(adapter);

        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // do nothing -- not clickable
        }

        private class Option implements Comparable<Option>{
            private String cycleType;
            private double cycleTime;

            public Option(String cycleType, double cycleTime) {
                this.cycleType = cycleType;
                this.cycleTime = cycleTime;
            }
            public String getType() {
                return cycleType;
            }
            public double getTime(){
                return cycleTime;
            }
            @Override
            public int compareTo(Option o) {
                double cmp = o.cycleTime - this.cycleTime;

                if(cmp < 0) return -1;
                if(cmp > 0) return 1;
                return 0;
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
                    TextView t2 = (TextView) v.findViewById(R.id.ptsPerTV);

                    if(t1!=null)
                        t1.setText(o.getType());
                    if(t2!=null) {
                        double rounded = Math.round( o.getTime()*10000) / 10000.0;

                        t2.setText( rounded+" pts/s" );
                    }

                }
                return v;
            }


        }

    }