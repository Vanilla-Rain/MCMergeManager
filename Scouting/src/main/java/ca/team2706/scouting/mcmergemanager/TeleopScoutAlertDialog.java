package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


/**
 * Created by MCSoftware on 2016-01-16.
 */
public class TeleopScoutAlertDialog {
    Handler m_handler;
    Runnable m_handlerTask ;
    public final UpTimer upTimer = new UpTimer();
    public int canceled = -1;

    public TeleopScoutAlertDialog(String title, Activity activity, String button1, int b1Code, String button2, int b2Code, String button3, int b3Code) {
        createAlert(title,activity, button3, b3Code, button2, b2Code, button1, b1Code);
    }

    private void createAlert(String title,final Activity activity, String button1, final int b1Code, String button2, final int b2Code, String button3, final int b3Code) {

        LayoutInflater myinflater = activity.getLayoutInflater();
        View alertLayout = myinflater.inflate(R.layout.alert_teleop_timer, null);

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(activity);

        final TextView timer = (TextView)alertLayout.findViewById(R.id.timerTextView);
        final long start = System.currentTimeMillis();
        alert.setView(alertLayout);
        alert.setCancelable(false);

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
                canceled = b2Code;
                upTimer.cancel();

            }
        });

        alert.setNeutralButton(button3, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        canceled = b3Code;
                        upTimer.cancel();
                    }
                }
        );

        alert.setPositiveButton(button1, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        canceled = b1Code;
                        upTimer.cancel();
                    }
                }
        );
        final AlertDialog dialog = alert.create();

        dialog.show();
    }
}