package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MCSoftware on 2016-01-16.
 */
public class UpTimer {
    private Timer timer, _t;
    private double _count=0;
    public UpTimer() {

    }
    public void startTime(final int cancelAfter,int loopTime, final Activity activity) {
        _t = new Timer();
        _t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.e("here", "" + Math.ceil(_count));
                _count += 0.1;

                activity.runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run() {

                        if (_count > cancelAfter) {
                            _t.cancel();
                        }

                    }
                });
            }
        }, loopTime,loopTime);
    }
    public double currentTime() {
        return _count;
    }
    public void cancel() {
        _t.cancel();
        _count = -1;
    }
}
