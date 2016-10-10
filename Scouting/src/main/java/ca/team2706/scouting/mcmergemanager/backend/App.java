package ca.team2706.scouting.mcmergemanager.backend;

import android.app.Application;
import android.content.Context;

/**
 * Created by mike on 09/10/16.
 * This is a helper class to be able to get the App's Context without needing to pass around pointers to MainActivity.
 */

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}