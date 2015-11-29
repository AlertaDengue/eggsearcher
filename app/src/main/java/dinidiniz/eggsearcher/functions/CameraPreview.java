package dinidiniz.eggsearcher.functions;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    String TAG = "Camera";

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCamera.setDisplayOrientation(90);


        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);




    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public static List<String> getCameraResolutionList(){
        Camera c = Camera.open(); // attempt to get a Camera instance
        Camera.Parameters params = c.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        List<Integer> resolutionSpinnerListInteger = new ArrayList<>();
        for (Camera.Size singleSize:sizes){
            resolutionSpinnerListInteger.add(singleSize.height * singleSize.width / 1024000);
        }

        Collections.sort(resolutionSpinnerListInteger, Collections.reverseOrder());

        List<String> resolutionSpinnerList = new ArrayList<String>(resolutionSpinnerListInteger.size());
        for (Integer myInt : resolutionSpinnerListInteger) {
            resolutionSpinnerList.add(String.valueOf(myInt));
        }

        c.release();

        return resolutionSpinnerList;
    }

    public static List<String> getCameraResolutionListWithSizes(List<Camera.Size> sizes){

        List<Integer> resolutionSpinnerListInteger = new ArrayList<>();
        for (Camera.Size singleSize:sizes){
            resolutionSpinnerListInteger.add(singleSize.height * singleSize.width / 1024000);
        }

        Collections.sort(resolutionSpinnerListInteger, Collections.reverseOrder());

        List<String> resolutionSpinnerList = new ArrayList<String>(resolutionSpinnerListInteger.size());
        for (Integer myInt : resolutionSpinnerListInteger) {
            resolutionSpinnerList.add(String.valueOf(myInt));
        }

        return resolutionSpinnerList;
    }
}