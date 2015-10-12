package dinidiniz.eggsearcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AtividadePrincipal extends AppCompatActivity {

    public final static String EXTRA_FILE_PATH = "file_path";
    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    String TAG = "AtividadePrincipal";
    Intent intent;
    Bundle bundle;
    FrameLayout preview;
    String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atividade_principal);

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
            params.set("iso", "100");
            params.set("metering", "matrix");
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            c.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
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
            releaseCamera();
            saveScreen();
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


