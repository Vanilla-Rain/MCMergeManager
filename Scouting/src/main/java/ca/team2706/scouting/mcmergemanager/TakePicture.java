package ca.team2706.scouting.mcmergemanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cnnr2 on 2015-12-05.
 */
public class TakePicture {
    public static int teamNumber;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    Activity launchActivity;

    public TakePicture(int teamNumber, Activity launchActivity) {
        this.teamNumber = teamNumber;
        this.launchActivity = launchActivity;
    }

    public void capturePicture() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        FileUtils utils = new FileUtils(launchActivity);
        Uri fileUri = utils.getNameForNewPhoto(teamNumber);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        launchActivity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        Log.d(launchActivity.getResources().getString(R.string.app_name),
                "Done taking photo");
    }
}


