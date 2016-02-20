package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MCSoftware on 2016-01-16.
 */
public class UpTimer {
    private Timer timer, timer1;
    private double count = 0;
    public UpTimer() {

    }
    public void startTime(final int cancelAfter,int loopTime, final Activity activity) {
        timer1 = new Timer();
        timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                count += 0.1;

                activity.runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run() {

                        if (count > cancelAfter) {
                            timer1.cancel();
                        }

                    }
                });
            }
        }, loopTime, loopTime);
    }

    public double currentTime() {
        return count;
    }

    public void cancel() {
        timer1.cancel();
    }
}
