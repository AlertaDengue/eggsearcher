package dinidiniz.eggsearcher.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.camera2.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dinidiniz.eggsearcher.functions.CameraPreview;
import dinidiniz.eggsearcher.R;


public class TelaFotografia extends AppCompatActivity {

    private int stateCaptureButton = 0;
    public static final String GO_TO_INTENT = "goto";
    public static final Integer GO_TO_COUNT = 0;
    public static final Integer GO_TO_CALIBRATE = 1;

    private Integer GO_TO;
    private Camera mCamera;
    private CameraPreview mPreview;
    String TAG = "TelaFotografia";
    Intent intent;
    FrameLayout preview;
    String filename;
    private int processSpinnerSelected;
    private int resolutionSpinnerSelected;
    private int namePhotoSpinnerSelected;
    private Spinner namePhotoSpinner;
    private String[] namePhotoSpinnerList;
    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_fotografia);

        res = getResources();
        //Get Intent sent
        GO_TO = getIntent().getExtras().getInt(GO_TO_INTENT);

        //Load Screen
        loadScreen();

        //Spinner Photo
        namePhotoSpinner = (Spinner) findViewById(R.id.namePhotoSpinner);
        namePhotoSpinnerList = res.getStringArray(R.array.namePhotoSpinnerList);

        ArrayAdapter<String> thresholdDataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, namePhotoSpinnerList);

        thresholdDataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        namePhotoSpinner.setAdapter(thresholdDataAdapter);

        namePhotoSpinner.setSelection(namePhotoSpinnerSelected);
        namePhotoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                releaseCamera();
                preview.removeView(mPreview);
                startCamera();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Start Camera
        startCamera();
    }

    public void getResultParameters(){
        try {
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/eggSearcher/");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            if (namePhotoSpinner.getSelectedItemPosition() == 2) {
                filename = getFileStreamPath("tempIMG.png").getAbsolutePath();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/eggSearcher/" + namePhotoSpinner.getSelectedItem() + "_" + currentDateandTime + ".png";
            }
        } catch (Exception e) {
            Log.i(TAG, "cant get address");
            Toast.makeText(this,"Capture your first image! :)", Toast.LENGTH_SHORT).show();
            filename = this.getFilesDir() + File.separator + "tempIMG.png";
        }

        Log.i(TAG, "address of file:" + filename);
    }

    public void startCamera(){
        getResultParameters();
        getCameraInstance();
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
    public void getCameraInstance(){
        mCamera = null;
        try {

            mCamera = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            params.setExposureCompensation(0);
            params.set("metering", "matrix");
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
            //params.setSceneMode(Camera.Parameters.SCENE_MODE_SNOW);
            params.setJpegQuality(100);
            if(namePhotoSpinner.getSelectedItemPosition() == 1) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }

            // Check what resolutions are supported by your camera
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            List<String> resolutionSpinnerList = CameraPreview.getCameraResolutionListWithSizes(sizes);

            Camera.Size mSize = sizes.get(0);

            for (Camera.Size size : sizes) {
                if (resolutionSpinnerList.get(resolutionSpinnerSelected).equals("" + size.height * size.width / 1024000)) {
                    Log.i("GetCameraInstance", "Resolution: " + size.width + " " + size.height);
                    mSize = size;
                }
            }

            params.setPictureSize(mSize.width, mSize.height);

            mCamera.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.i("GetCameraInstance", "Not Availible");
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Entra aqui se não há problemas em tirar a foto;

            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
                out.write(data);
                out.close();
                galleryAddPic(filename);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            camera.startPreview();
        }
    };


    public void CaptureImage(View view) {
        Button captureImage = (Button) findViewById(R.id.button_capture);
        if (stateCaptureButton == 0) {
            mCamera.takePicture(null, null, mPicture);
            captureImage.setText("Next");
            stateCaptureButton = 1;
        } else{
            saveScreen();
            releaseCamera();
            if (GO_TO.equals(GO_TO_CALIBRATE)){
                intent = new Intent(this, CalibrateActivity.class);
                startActivity(intent);
                this.finish();
            } else {
                if (processSpinnerSelected == 0) {
                    intent = new Intent(this, TelaContagem.class);
                    startActivity(intent);
                    this.finish();
                } else {
                    intent = new Intent(this, TelaFullAutomatic.class);
                    startActivity(intent);
                    this.finish();
                }
            }
        }

    }

    private void galleryAddPic(String filepath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filepath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
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
        editor.putInt("namePhotoSpinnerSelected", namePhotoSpinner.getSelectedItemPosition());
        editor.putString("imagepath", filename);
        editor.commit();
    }

    public void loadScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        namePhotoSpinnerSelected = sharedPref.getInt("namePhotoSpinnerSelected", 0);
        processSpinnerSelected = sharedPref.getInt("processSpinnerSelected", 0);
        resolutionSpinnerSelected = sharedPref.getInt("resolutionSpinnerSelected", 0);
    }


}


