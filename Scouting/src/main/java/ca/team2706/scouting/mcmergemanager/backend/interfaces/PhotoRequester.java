package ca.team2706.scouting.mcmergemanager.backend.interfaces;

import android.graphics.Bitmap;

/**
 * This interface is meant to be implemented by an activity that requests team photos from the FileUtils.
 * Since syncing with Drive can take a few seconds, FileUtils will immediately call
 * the activity's updatePhotos(photos) with whatever photos are locally cached for that team.
 * If FileUtils is able to connect to Drive then it will call it again after performing the sync.
 *
 * Created by Mike Ounsworth
 */
public interface PhotoRequester {

    /**
     * Allows the FileUtils class to 'load' images into an activity or fragment.
     *
     * @param photos "Bitmap" is the generic image class in Android, it can hold any type of image.
     *               Use `imageView.setImageBitmap(photos[i]);` to display an image in an ImageView.
     */
    public abstract void updatePhotos(Bitmap[] photos);
}
