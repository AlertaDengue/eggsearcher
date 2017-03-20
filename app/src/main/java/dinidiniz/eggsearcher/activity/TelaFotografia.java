package dinidiniz.eggsearcher.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dinidiniz.eggsearcher.Consts;
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

    private Camera.Size resolutionChoosen;

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

            //Focus everytime someone touch the screen
            preview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (mCamera != null) {
                        //Camera camera = Camera.getCamera();
                        mCamera.cancelAutoFocus();
                        Rect focusRect = new Rect(-500,-500,500,500);

                        Camera.Parameters parameters = mCamera.getParameters();
                        if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        if (parameters.getMaxNumFocusAreas() > 0) {
                            List<Camera.Area> mylist = new ArrayList<Camera.Area>();
                            mylist.add(new Camera.Area(focusRect, 1000));
                            parameters.setFocusAreas(mylist);
                        }

                        try {
                            mCamera.cancelAutoFocus();
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {
                                    if (camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                                        Camera.Parameters parameters = camera.getParameters();
                                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                        if (parameters.getMaxNumFocusAreas() > 0) {
                                            parameters.setFocusAreas(null);
                                        }

                                        // Check what resolutions are supported by your camera
                                        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                                        List<String> resolutionSpinnerList = CameraPreview.getCameraResolutionListWithSizes(sizes);

                                        resolutionChoosen = sizes.get(0);

                                        for (Camera.Size size : sizes) {
                                            if (resolutionSpinnerList.get(resolutionSpinnerSelected).equals("" + size.height * size.width / 1024000)) {
                                                Log.i("GetCameraInstance", "Resolution: " + size.width + " " + size.height);
                                                resolutionChoosen = size;
                                            }
                                        }

                                        parameters.setPictureSize(resolutionChoosen.width, resolutionChoosen.height);
                                        parameters.setExposureCompensation(0);
                                        parameters.set("metering", "matrix");
                                        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
                                        parameters.setJpegQuality(100);
                                        if(namePhotoSpinner.getSelectedItemPosition() == 1) {
                                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                        } else {
                                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                        }
                                        camera.setParameters(parameters);
                                        camera.startPreview();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
        }
    }



    /********************************************************/
    /** A safe way to get an instance of the Camera object. */
    public void getCameraInstance(){
        mCamera = null;
        try {
            //PUT ALL PARAMETERS HERE
            mCamera = Camera.open(); // attempt to get a Camera instance
            mCamera.cancelAutoFocus();
            Camera.Parameters params = mCamera.getParameters();

            // Check what resolutions are supported by your camera
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            List<String> resolutionSpinnerList = CameraPreview.getCameraResolutionListWithSizes(sizes);

            resolutionChoosen = sizes.get(0);

            for (Camera.Size size : sizes) {
                if (resolutionSpinnerList.get(resolutionSpinnerSelected).equals("" + size.height * size.width / 1024000)) {
                    Log.i("GetCameraInstance", "Resolution: " + size.width + " " + size.height);
                    resolutionChoosen = size;
                }
            }


            params.setFocusMode(params.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);


            params.setPictureSize(resolutionChoosen.width, resolutionChoosen.height);
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
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            Bitmap croppedBitmap = Bitmap.createBitmap(bmp, resolutionChoosen.width / 3, (int) Math.ceil(resolutionChoosen.height * 1.5 / 4), resolutionChoosen.width/3,
                    (int) Math.ceil(resolutionChoosen.height/ 4));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            data = stream.toByteArray();

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
                intent.putExtra(Consts.UPLOADED_PHOTO, false);
                startActivity(intent);
                this.finish();
            } else {
                if (processSpinnerSelected == 0) {
                    intent = new Intent(this, TelaContagem.class);
                    intent.putExtra(Consts.UPLOADED_PHOTO, false);
                    startActivity(intent);
                    this.finish();
                } else {
                    intent = new Intent(this, TelaFullAutomatic.class);
                    intent.putExtra(Consts.UPLOADED_PHOTO, false);
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

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", resolutionChoosen.width/3);
            cropIntent.putExtra("aspectY", resolutionChoosen.height*1.5/4);
            // indicate output X and Y
            cropIntent.putExtra("outputX", resolutionChoosen.width*2/3);
            cropIntent.putExtra("outputY", resolutionChoosen.height*2.5/4);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, 1);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
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
            setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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


