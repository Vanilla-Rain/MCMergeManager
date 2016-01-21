package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by MCSoftware on 2016-01-16.
 */
public class TeleopScoutAlertDialog {
    Handler m_handler;
    Runnable m_handlerTask ;
    private boolean canceled;
    public TeleopScoutAlertDialog(String title, Activity activity, String button1,String button2,String button3) {
        createAlert(title,activity, button3,button2,button1);
    }
    private void createAlert(String title,final Activity activity, String button1, String button2, String button3) {
        LayoutInflater myinflater = activity.getLayoutInflater();
        View alertLayout = myinflater.inflate(R.layout.alert_teleop_timer, null);

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(activity);

        final TextView timer = (TextView)alertLayout.findViewById(R.id.timerTextView);
        final long start = System.currentTimeMillis();
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final UpTimer upTimer = new UpTimer();
        upTimer.startTime(60, 100, activity);
        m_handler = new Handler();
        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                timer.setText("" + (double) Math.round((upTimer.currentTime() * 100) * 10) / 1000.0);
                m_handler.postDelayed(m_handlerTask, 100);  // 1 second delay
            }
        };
        m_handlerTask.run();

        alert.setTitle(title);
        alert.setNegativeButton(button2, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                upTimer.cancel();

            }
        });

        alert.setNeutralButton(button3, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        canceled = true;
                        upTimer.cancel();
                    }
                }

        );
        alert.setPositiveButton(button1, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        canceled = true;
                        upTimer.cancel();
                    }
                }

        );
        final AlertDialog dialog = alert.create();

        dialog.show();
    }
public class DialogReturnData {
    public DialogReturnData() {

    }
}
}