package dinidiniz.eggsearcher;

import android.app.Application;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dinidiniz.eggsearcher.helper.Gallery;

/**
 * Created by leon on 07/04/17.
 */
public class App extends Application {
    private Tracker mTracker;
    private static final String TAG = App.class.getSimpleName();

    /***
     * On create of the application lets install the examples pictures
     */
    @Override
    public void onCreate() {
        super.onCreate();

        File storageDir = new File(Consts.getImagePath());
        File file = new File(storageDir, "example.jpg");

        try {

            if (!file.exists() && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                storageDir.mkdirs();
                BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.example);
                Bitmap exampleBitmap = drawable.getBitmap();


                OutputStream output = new FileOutputStream(file);

                // Compress into png format image from 0% - 100%
                exampleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();

                Gallery.galleryAddPic(getApplicationContext(), file.getAbsolutePath());

            }

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
