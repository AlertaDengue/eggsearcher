package dinidiniz.eggsearcher;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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

    private static final String TAG = App.class.getSimpleName();

    /***
     * On create of the application lets install the examples pictures
     */
    @Override
    public void onCreate() {
        super.onCreate();

        File storageDir = new File(Consts.getImagePath());
        File file = new File(storageDir, "example.jpg");
        if (!file.exists()) {
            storageDir.mkdirs();
            BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.example);
            Bitmap exampleBitmap = drawable.getBitmap();

            try {

                OutputStream output = new FileOutputStream(file);

                // Compress into png format image from 0% - 100%
                exampleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();

                Gallery.galleryAddPic(getApplicationContext(), file.getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
