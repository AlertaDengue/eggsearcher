package dinidiniz.eggsearcher.telas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
//import android.hardware.camera2.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dinidiniz.eggsearcher.functions.CameraPreview;
import dinidiniz.eggsearcher.R;


public class TelaFotografia extends AppCompatActivity {

    public final static String EXTRA_FILE_PATH = "file_path";
    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    String TAG = "TelaFotografia";
    Intent intent;
    Bundle bundle;
    FrameLayout preview;
    String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_fotografia);

        startCamera();
    }

    public void startCamera(){
        try {filename = getFileStreamPath("tempIMG.png").getAbsolutePath();
        } catch (Exception e) { Toast.makeText(this,"Capture your first image! :)", Toast.LENGTH_SHORT).show();}

        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(this,"Camera not available", Toast.LENGTH_SHORT).show();
            onBackPressed();
        } else {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }


    /********************************************************/
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            params.setExposureCompensation(0);
            params.set("metering", "matrix");
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            // Check what resolutions are supported by your camera
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size mSize = sizes.get(0);

            for (Camera.Size size : sizes) {
                if (size.width * size.height > mSize.width * mSize.height) {
                    Log.i("GetCameraInstance", "Available resolution: " + size.width + " " + size.height);
                    mSize = size;
                }
            }

            params.setPictureSize(mSize.width, mSize.height);

            c.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.i("GetCameraInstance", "Not Availible");
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Entra aqui se não há problemas em tirar a foto;

            try {
                FileOutputStream fileOutStream = openFileOutput("tempIMG.png", MODE_PRIVATE);
                fileOutStream.write(data);
                fileOutStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            camera.startPreview();
        }
    };


    public void CaptureImage(View view) {
        Button captureImage = (Button) findViewById(R.id.button_capture);
        if (captureImage.getText().toString().equals("Capture")) {
            mCamera.takePicture(null, null, mPicture);
            captureImage.setText("Next");
        } else{
            saveScreen();
            releaseCamera();
            captureImage.setText("Capture");
            intent = new Intent(this, TelaContagem.class);
            startActivity(intent);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            releaseCamera();              // release the camera immediately on pause event
        }
    }


    @Override
    protected void onStop(){
        super.onStop();
        if (mCamera != null) {
            releaseCamera();              // release the camera immediately on pause event
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            startCamera();
        }
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void onBackPressed() {
        Button captureImage = (Button) findViewById(R.id.button_capture);
        if (captureImage.getText().toString().equals("Next")) {
            captureImage.setText("Capture");
        } else {
            Log.d("CDA", "onBackPressed Called");
            Intent setIntent = new Intent(this, TelaInicial.class);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);}
    }

    public void saveScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("imagepath", filename);
        editor.commit();
    }

}


