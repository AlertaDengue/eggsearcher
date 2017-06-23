package dinidiniz.eggsearcher.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.functions.CameraPreview;
import dinidiniz.eggsearcher.helper.Gallery;


public class TelaFotografia extends AppCompatActivity {

    public static final String GO_TO_INTENT = "goto";
    public static final Integer GO_TO_COUNT = 0;
    public static final Integer GO_TO_CALIBRATE = 1;
    String TAG = TelaFotografia.class.getName();
    Intent intent;
    FrameLayout preview;
    String filename;
    private Integer GO_TO;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean alreadyAccessed = false;
    private int processSpinnerSelected;
    private int resolutionSpinnerSelected;
    private int namePhotoSpinnerSelected;
    private int photoAreaSpinnerSelected;
    private int heightFromLentsNumberPickerSelected;
    private float photoAreaWeight;
    private Spinner namePhotoSpinner;
    private String[] namePhotoSpinnerList;
    private Resources res;
    private ToggleButton captureToggleButton;

    private Camera.Size resolutionChoosen;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Entra aqui se não há problemas em tirar a foto;
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

            int photoAreaIndex = (int) Math.ceil(photoAreaWeight * 2 + 1);

            Log.i(TAG, "photoAreaIndex: " + photoAreaIndex + "; photoAreaWeight: " + photoAreaWeight);
            Bitmap croppedBitmap = Bitmap.createBitmap(bmp, (int) Math.ceil(resolutionChoosen.width * photoAreaWeight / photoAreaIndex),
                    (int) Math.ceil(resolutionChoosen.height * photoAreaWeight / photoAreaIndex), (int) Math.ceil(resolutionChoosen.width / photoAreaIndex),
                    (int) Math.ceil(resolutionChoosen.height / photoAreaIndex));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            data = stream.toByteArray();

            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
                out.write(data);
                out.close();
                Gallery.galleryAddPic(getApplicationContext(), filename);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            goToNextScreen();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_fotografia);

        captureToggleButton = (ToggleButton) findViewById(R.id.captureToggleButton);

        res = getResources();

        //Get Intent sent
        GO_TO = getIntent().getExtras().getInt(GO_TO_INTENT);

        //Load Screen
        loadScreen();

        //Change photo area conforms configuration
        LinearLayout photoArea1LinearLayout = (LinearLayout) findViewById(R.id.photoArea1LinearLayout);
        LinearLayout photoArea2LinearLayout = (LinearLayout) findViewById(R.id.photoArea2LinearLayout);
        LinearLayout photoArea3LinearLayout = (LinearLayout) findViewById(R.id.photoArea3LinearLayout);
        LinearLayout photoArea4LinearLayout = (LinearLayout) findViewById(R.id.photoArea4LinearLayout);

        LinearLayout[] photoAreaLinearLayoutList = {photoArea1LinearLayout, photoArea2LinearLayout, photoArea3LinearLayout, photoArea4LinearLayout};


        for (LinearLayout photoAreaLinearLayout : photoAreaLinearLayoutList) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) photoAreaLinearLayout.getLayoutParams();
            params.weight = photoAreaWeight;
            photoAreaLinearLayout.setLayoutParams(params);
        }

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
                if (mPreview != null) {
                    preview.removeView(mPreview);
                }
                startCamera();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Start Camera
        startCamera();


    }

    public void getResultParameters() {
        try {
            File storageDir = new File(Consts.getImagePath());
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            if (namePhotoSpinner.getSelectedItemPosition() == 2) {
                filename = getFileStreamPath("tempIMG.png").getAbsolutePath();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                filename = Consts.getImagePath() + namePhotoSpinner.getSelectedItem() + "_" +
                        resolutionChoosen.height + "_" +  resolutionChoosen.width  +
                        "_" + heightFromLentsNumberPickerSelected + "_" + currentDateandTime + ".png";
            }
        } catch (Exception e) {
            Log.i(TAG, "cant get address");
            Toast.makeText(this, "Capture your first image! :)", Toast.LENGTH_SHORT).show();
            filename = this.getFilesDir() + File.separator + "tempIMG.png";
        }

        Log.i(TAG, "address of file:" + filename);
    }

    public void startCamera() {
        getResultParameters();
        getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
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
                        Rect focusRect = new Rect(-500, -500, 500, 500);

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
                                        if (namePhotoSpinner.getSelectedItemPosition() == 1) {
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

    private int findBackFacingCamera() {

        // Search for the front facing camera

        int cameraId = 0;
        boolean cameraFront;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public void getCameraInstance() {
        mCamera = null;
        try {
            //PUT ALL PARAMETERS HERE
            mCamera = Camera.open(findBackFacingCamera()); // attempt to get a Camera instance
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
            if (namePhotoSpinner.getSelectedItemPosition() == 1) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }


            mCamera.setParameters(params);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.i("GetCameraInstance", "Not Availible");
        }
    }

    public void CaptureImage(View view) {

        if (!alreadyAccessed) {
            alreadyAccessed = true;
            if (captureToggleButton.isChecked()) {
                mCamera.takePicture(null, null, mPicture);
                captureToggleButton.setChecked(true);
            }
        }

    }

    public void goToNextScreen() {
        saveScreen();
        releaseCamera();
        if (GO_TO.equals(GO_TO_CALIBRATE)) {
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


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event

    }


    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            startCamera();
        }
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Consts.namePhotoSpinnerSelected, namePhotoSpinner.getSelectedItemPosition());
        editor.putString(Consts.imagepath, filename);
        editor.apply();
    }

    public void loadScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        namePhotoSpinnerSelected = sharedPref.getInt(Consts.namePhotoSpinnerSelected, 0);
        processSpinnerSelected = sharedPref.getInt("processSpinnerSelected", 0);
        resolutionSpinnerSelected = sharedPref.getInt("resolutionSpinnerSelected", 0);
        heightFromLentsNumberPickerSelected = sharedPref.getInt(Consts.heightFromLentsNumberPickerSelected, Consts.ORIGINAL_heightFromLentsNumberPickerSelected);

        photoAreaSpinnerSelected = sharedPref.getInt("photoAreaSpinnerSelected", 0);
        String[] photoAreaSpinnerList = res.getStringArray(R.array.photoAreaSpinnerList);
        String areaPhoto = photoAreaSpinnerList[photoAreaSpinnerSelected];
        photoAreaWeight = (float) (Math.sqrt(Integer.parseInt(areaPhoto.substring(areaPhoto.lastIndexOf(':') + 1))) - 1) / 2;
    }


}


