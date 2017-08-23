package dinidiniz.eggsearcher.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import dinidiniz.eggsearcher.Consts;
import dinidiniz.eggsearcher.R;

public class TelaContagem extends AppCompatActivity {

    static final float TOUCH_TOLERANCE = 15;
    static final private String TAG = TelaContagem.class.getName();
    float downX, downY;
    float curX, curY;
    boolean started = false;
    ToggleButton erasorCheckBox;
    ToggleButton cropToggleButton;
    Point size;
    //Global variables to draw
    String filePath;
    DrawView drawView;
    int canvasHeight;
    int canvasWidth;
    long startTime;
    long curTime;
    LinearLayout linearLayout;
    Button thresholdingImageButton;
    Intent intent;
    FileOutputStream fileOutStream;
    Bitmap bitmap;
    //Studied variables
    int thresholdOn;
    double resolutionOn;
    int heightStudied = 12;
    int resolutionStudied = 12;
    int meanUStudied = 200;
    int standardDeviationStudied = 20;
    int minimumThresholdArea = 25;
    int heightOn;
    double meanU;
    double standartDeviationU;


    private int numberOfEggs = 0;
    private int areaTotal = 0;
    private int[] areas;

    double[] weights = {1, 1, 1, 1};
    private Boolean seletecToUpload = true;
    //Thouch Events variables
    private float mX, mY;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    private int meanBChannel;
    //Start OpenCV and download if necessary
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //Load to get canvaswidth and canvasheight
                    loadScreen();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //GET TEXTURE SIZE
    public static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle getIntentExtras = getIntent().getExtras();
        seletecToUpload = getIntentExtras.getBoolean(Consts.UPLOADED_PHOTO);
        setContentView(R.layout.tela_contagem);

        Log.i(TAG, "Entrou na tela contagem");
        Log.i(TAG, "bitmap : " + (bitmap == null));

        //GET SCREEN SIZE
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        //Control Scroll Views
        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        erasorCheckBox = (ToggleButton) findViewById(R.id.eraserToggleButton);
        cropToggleButton = (ToggleButton) findViewById(R.id.cropToggleButton);


        //Make it sync OpenCV

        //if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback)) {
        //    Log.e("TEST", "Cannot connect to OpenCV Manager");
        //}
    }

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent setIntent = new Intent(this, TelaInicial.class);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(setIntent);
    }


    //Controls bar Moves and Eraser of Canvas
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean returnMoveEvent = true;

        if (erasorCheckBox.isChecked()) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: { // touch on the screen event;
                    mX = (int) (event.getX() + hScroll.getScrollX());
                    mY = (int) (event.getY() + vScroll.getScrollY());
                    drawView.touch_start(mX, mY);
                    drawView.toInvalidate();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: { // move event
                    mX = (int) (event.getX() + hScroll.getScrollX());
                    mY = (int) (event.getY() + vScroll.getScrollY());
                    drawView.touch_move(mX, mY);
                    drawView.toInvalidate();
                    return true;
                }
                case MotionEvent.ACTION_UP: {  // finger up event
                    drawView.touch_up();
                    drawView.toInvalidate();
                    return true;
                }
            }
        } else {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    mX = (int) (event.getX() + hScroll.getScrollX());
                    downX = (int) (event.getX() + hScroll.getScrollX());
                    mY = (int) (event.getY() + vScroll.getScrollY());
                    downY = (int) (event.getY() + vScroll.getScrollY());

                    return returnMoveEvent;

                case MotionEvent.ACTION_MOVE:
                    curX = (int) (event.getX() + hScroll.getScrollX());
                    curY = (int) (event.getY() + vScroll.getScrollY());

                    if (!cropToggleButton.isChecked()) {
                        vScroll.scrollBy(0, (int) (mY - curY));
                        hScroll.scrollBy((int) (mX - curX), 0);
                    } else {
                        drawView.toInvalidate();
                        if ((event.getX()) / size.x > 0.9 || (event.getY()) / size.y > 0.9 || (event.getX()) / size.x < 0.1 || (event.getY()) / size.y < 0.1) {
                            vScroll.scrollBy(0, (int) (curY - mY));
                            hScroll.scrollBy((int) (curX - mX), 0);
                        }
                        mX = curX;
                        mY = curY;
                    }

                    return returnMoveEvent;

                case MotionEvent.ACTION_UP:
                    if (cropToggleButton.isChecked()) {
                        curX = (int) (event.getX() + hScroll.getScrollX());
                        curY = (int) (event.getY() + vScroll.getScrollY());

                        Log.i(TAG, curX + " " + curY + " " + downX + " " + downY);

                        if (meetConditionsForCrop()) {
                            if (curX > canvasWidth) {
                                curX = canvasWidth;
                            }
                            if (curY > canvasHeight) {
                                curY = canvasHeight;
                            }
                            Log.i(TAG, curX + " " + curY + " " + downX + " " + downY);
                            bitmap = Bitmap.createBitmap(loadBitmapFromView(drawView), (int) downX, (int) downY, (int) (curX - downX), (int) (curY - downY));
                            canvasWidth = (int) Math.abs(downX - curX);
                            canvasHeight = (int) Math.abs(downY - curY);
                            linearLayout.removeView(drawView);
                            cropToggleButton.setChecked(false);
                            startDrawView();
                        }
                    }
                    return returnMoveEvent;
            }
        }
        return true;

    }

    public void thresholdImageButton(View view) {
        thresholdingImageButton = (Button) findViewById(R.id.button_Thresholding);
        if (thresholdingImageButton.getText().toString().equals("Thresholding")) {
            new thresholdingThread(this).execute();
        } else {
            new contagemThread(this).execute();
        }
    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Consts.numberOfEggs, numberOfEggs);
        editor.putInt(Consts.areaTotal, areaTotal);
        editor.putString(Consts.areas, Arrays.toString(areas).split("[\\[\\]]")[1]);
        editor.apply();
    }

    public void startDrawView() {
        drawView = new DrawView(this);

        linearLayout = (LinearLayout) findViewById(R.id.captured_image);
        drawView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(drawView, canvasWidth, canvasHeight);
    }

    private Boolean meetConditionsForCrop() {
        if (curX - downX > TOUCH_TOLERANCE && curY - downY > TOUCH_TOLERANCE && (curX - downX) > 50 && (curY - downY) > 50 && cropToggleButton.isChecked()) {
            return true;
        } else {
            return false;
        }
    }

    //Resize Bitmap
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int widthImage = bm.getWidth();
        int heightImage = bm.getHeight();


        float scaleWidth = ((float) newWidth) / widthImage;
        float scaleHeight = ((float) newHeight) / heightImage;


        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, widthImage, heightImage, matrix, false);
        return resizedBitmap;

    }

    //Load the bitmap from the canvas view
    public Bitmap loadBitmapFromView(View v) {

        Bitmap b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public double valueOfN(int m) {
        return Math.sqrt(m * (m + 1) / 2 * Math.pow(standartDeviationU, 2) * (Math.log(m + 1) - Math.log(m)) + m * (m + 1) * Math.pow(meanU, 2));
    }

    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "Activity paused. Bitmap : " + (bitmap == null));
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(TAG, "resultado do upload");
        switch (requestCode) {
            case Consts.SELECTED_PHOTO:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "resultado do upload");

                    InputStream is = null;
                    bitmap = null;
                    try {

                        is = getContentResolver().openInputStream(imageReturnedIntent.getData());

                        bitmap = BitmapFactory.decodeStream(is);
                        bitmap = adjustBitmap(bitmap);
                        if (is != null) {
                            is.close();
                        }

                        new getElementAtMiddleThread(TelaContagem.this).execute();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "cancelled");
                    Intent setIntent = new Intent(this, TelaInicial.class);
                    setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setIntent);
                    this.finish();
                }
                break;
        }
    }

    /***
     * Get Bitmap, if the user selected upload, will ask to fetch the picture, otherwise, will get a saved pic
     *
     * @param filePath
     * @return
     */
    public void getBitmap(String filePath) {

        Log.i(TAG, "lets get bitmap: " + seletecToUpload);
        if (bitmap == null) {
            if (seletecToUpload) {
                //Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //photoPickerIntent.setType("image/*");
                Intent photoPickerIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, Consts.SELECTED_PHOTO);
            } else {
                bitmap = BitmapFactory.decodeFile(filePath);
                bitmap = adjustBitmap(bitmap);

                new getElementAtMiddleThread(TelaContagem.this).execute();

            }
        }

    }

    /***
     * Adjust the bitmap to fit in the screen
     *
     * @param bitmap
     * @return adjusted bitmap
     */
    private Bitmap adjustBitmap(Bitmap bitmap) {
        int widthResolution = bitmap.getWidth();
        int heightResolution = bitmap.getHeight();

        int maxTextureSize = getMaxTextureSize();

        double maxResolution = (double) Math.max(widthResolution, heightResolution);

        if (maxResolution > maxTextureSize) {
            double factor = maxResolution / maxTextureSize;
            canvasWidth = (int) Math.floor(widthResolution / factor);
            canvasHeight = (int) Math.floor(heightResolution / factor);
        } else {
            //Its change because will rotate after here
            canvasWidth = heightResolution;
            canvasHeight = widthResolution;
        }

        Log.i(TAG, "CanvasHeight: " + canvasHeight + " ; canvasWidth: " + canvasWidth);


        bitmap = getResizedBitmap(bitmap, canvasHeight, canvasWidth);
        bitmap = RotateBitmap(bitmap, getRightAngleImage(filePath));

        return bitmap;
    }

    /***
     * Put the picture in the right angle
     *
     * @param photoPath
     * @return same picture but rotated
     */
    private int getRightAngleImage(String photoPath) {

        int degree = 0;
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 90;
            }

            Log.i(TAG, "degree to turn photo:" + (degree + 90));

            return degree + 90;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return degree;
    }

    public void loadScreen() {
        Resources res = getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //GET LOGISTIC WEIGHTS
        weights[0] = Double.longBitsToDouble(sharedPref.getLong(TelaConfiguracao.WEIGHT_LOGISTIC_R, 1));
        weights[1] = Double.longBitsToDouble(sharedPref.getLong(TelaConfiguracao.WEIGHT_LOGISTIC_G, 1));
        weights[2] = Double.longBitsToDouble(sharedPref.getLong(TelaConfiguracao.WEIGHT_LOGISTIC_B, 1));
        weights[3] = Double.longBitsToDouble(sharedPref.getLong(TelaConfiguracao.WEIGHT_LOGISTIC_GRAY, 1));

        //GET PICTURE WITH FILEPATH AND PUT IT IN BITMAP AND GET THE RIGHT SIZES OF THE CANVAS
        filePath = sharedPref.getString(Consts.imagepath, "/");

        //GET HEIGHT OF THE PICTURE THAT WAS TAKEN
        heightOn = sharedPref.getInt(Consts.heightFromLentsNumberPickerSelected, Consts.ORIGINAL_heightFromLentsNumberPickerSelected);

        //GET THRESHOLD LEVEL SELECTED
        int thresholdSpinnerSelected = sharedPref.getInt(Consts.thresholdSpinnerSelected, 0);
        String valueString = res.getStringArray(R.array.thresholdSpinnerList)[thresholdSpinnerSelected];
        thresholdOn = Integer.parseInt(valueString);


        //GET RESOLUTION OF THE PICTURE
        resolutionOn = (double) canvasHeight * canvasWidth / 1024000;

        double factorMultiplyer = (heightStudied / heightOn) * (resolutionOn / resolutionStudied);
        meanU = meanUStudied * factorMultiplyer;
        standartDeviationU = standardDeviationStudied * Math.sqrt(factorMultiplyer);
        Log.i(TAG, "Mean stimated: " + meanU);
        Log.i(TAG, "Standart deviation stimated: " + standartDeviationU);

        getBitmap(filePath);
    }

//OTHER CLASSES NOW

    /***
     * Class to identify and select the ovitrap in the middle of the picture
     */
    public class getElementAtMiddleThread extends AsyncTask<Void, String, Integer> {

        private ProgressDialog progress;
        private Context context;

        public getElementAtMiddleThread(Context context) {
            Log.i(TAG, "Get element in the middle");
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            publishProgress("Transforming Image");
            //Get mean of U channel of the bitmap
            Mat imgMat = new Mat();
            Utils.bitmapToMat(bitmap, imgMat);

            //List<Mat> mRgb = new ArrayList<Mat>(3);
            //Core.split(imgMat, mRgb);
            //Mat mB = mRgb.get(2);

            //MatOfDouble mu = new MatOfDouble();
            //MatOfDouble sigma = new MatOfDouble();
            //Imgproc.threshold(mB, mB, 130, 255, Imgproc.THRESH_BINARY_INV);
            //Core.meanStdDev(mB, mu, sigma, mB);

            /*
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2HLS_FULL);
            List<Mat> mHLS = new ArrayList<Mat>(3);
            Core.split(imgMat, mHLS);
            Mat mH = mHLS.get(0);

            Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mH);
            Log.i(TAG, "index: " + minMaxLocResult.minVal + "  " + minMaxLocResult.maxVal);

            Core.subtract(mH, new Scalar(minMaxLocResult.minVal), mH);
            Core.multiply(mH, new Scalar(((float) 255) / (minMaxLocResult.maxVal - minMaxLocResult.minVal)), mH);


            minMaxLocResult = Core.minMaxLoc(mH);
            Log.i(TAG, "index: " + minMaxLocResult.minVal + "  " + minMaxLocResult.maxVal);



            //Get mean of B channel for study porpose
            meanBChannel = ImageProcessing.getMeanOfBlueChannelInMat(imgMat);

            //Draw the rectangule to find the ovitrap in the middle
            //mH.convertTo(mH, CvType.CV_8UC1);
            //Mat matBlurCanny = mB.clone();

            Imgproc.Canny(mH, mH, 30, 200);

            Imgproc.dilate(mH, mH, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
            //Imgproc.erode(mH, mH, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1)));
            //Imgproc.rectangle(mH, new org.opencv.core.Point(0,0), new org.opencv.core.Point(canvasWidth,canvasHeight),new Scalar(255,255,255),20);
            //Invert the matrix to have points
            //Core.bitwise_not(mH, mH);

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

            publishProgress("Finding Contours");

            //Imgproc.findContours(mH, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            org.opencv.core.Point centroidOfImage = new org.opencv.core.Point(mH.cols() / 2, mH.rows() / 2);

            int indexOfMinDistance = 0;
            Double min = null;

            for (int idx = 0; idx < contours.size(); idx++) {
                org.opencv.core.Point centroid = ImageProcessing.getCentroidOfContour(contours.get(idx));
                double distance = Math.pow(centroid.x - centroidOfImage.x, 2) + Math.pow(centroid.y - centroidOfImage.y, 2);
                if ((Imgproc.contourArea(contours.get(idx)) > 150) && ((min == null) || (distance < min))) {
                    indexOfMinDistance = idx;
                    min = distance;
                }
            }

            Log.i(TAG, "index: " + indexOfMinDistance);

            publishProgress("Getting the middle one");

            Mat contour = org.opencv.core.Mat.zeros(imgMat.rows(), imgMat.cols(), CvType.CV_8UC1);
            Mat ones = org.opencv.core.Mat.ones(imgMat.rows(), imgMat.cols(), CvType.CV_8UC1);
            Imgproc.drawContours(contour, contours, indexOfMinDistance, new Scalar(255), -1);

            ones.setTo(new Scalar(0), contour);
            //imgMat.setTo(new Scalar(0), ones);


            Utils.matToBitmap(imgMat, bitmap);


            contour.release();
            */

            return 1;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer post) {
            Log.i(TAG, "Go to post execute");
            progress.dismiss();
            startDrawView();
        }
    }

    //PUBLIC CLASS TO COUNT THE EGGS IN A DIFFERENT THREAD
    public class contagemThread extends AsyncTask<Void, String, Integer> {

        private ProgressDialog progress;
        private Context context;

        public contagemThread(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            bitmap = loadBitmapFromView(drawView);
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat imgMat = new Mat();
            areaTotal = 0;
            areas = new int[10];

            publishProgress("Thresholding");
            Utils.bitmapToMat(bitmap, imgMat);
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(imgMat, imgMat, 250, 255, Imgproc.THRESH_BINARY);
            //thresholdingImage2(bitmap);

            System.out.println(Core.VERSION);

            publishProgress("Labelling connected components");

            Imgproc.findContours(imgMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            publishProgress("Counting areas");


            for (int idx = 0; idx < contours.size(); idx++) {
                //Mat contour = contours.get(idx);
                Mat countour = org.opencv.core.Mat.zeros(imgMat.rows(), imgMat.cols(), CvType.CV_8UC1);
                // CV_FILLED fills the connected components found
                Imgproc.drawContours(countour, contours, idx, new Scalar(1), -1);
                int countourArea = (int) Core.sumElems(countour).val[0];
                //org.opencv.core.Rect cRect = Imgproc.boundingRect(contours.get(idx));
                //Mat contour = Imgproc.dilate(cRect);
                //double contourarea = Imgproc.contourArea(contour);
                countour.release();
                for(int thresholdArea=3; thresholdArea <= areas.length*3; thresholdArea +=3){
                    if (countourArea > thresholdArea) {
                        areas[thresholdArea/3-1] =  areas[thresholdArea/3-1] + countourArea;
                    }
                }

            }

            areaTotal = areas[5];

            Log.i(TAG, "areas: " + Arrays.toString(areas));

            numberOfEggs = (int) Math.exp(0.5307 * Math.log(areaTotal));

            Log.i(TAG, "area total: " + areaTotal);


            publishProgress("Finishing");

            return 1;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer params) {
            progress.dismiss();
            saveScreen();
            intent = new Intent(context, TelaResultado.class);
            startActivity(intent);
        }
    }

    //PUBLIC CLASS TO THRESHOLD IN A DIFFERENT THREAD
    public class thresholdingThread extends AsyncTask<Void, String, Integer> {

        private ProgressDialog progress;
        private Context context;

        public thresholdingThread(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            bitmap = loadBitmapFromView(drawView);
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            publishProgress("Thresholding");

            Mat imgMat = new Mat();
            Mat threshold = new Mat();
            Mat mGrayscale = new Mat();
            List<Mat> mRgb = new ArrayList<Mat>(3);
            int pointA = thresholdOn;
            int pointB = 5;


            //Get each channel
            Utils.bitmapToMat(bitmap, imgMat);
            Core.split(imgMat, mRgb);
            Mat mR = mRgb.get(0);
            Mat mG = mRgb.get(1);
            Mat mB = mRgb.get(2);


            Imgproc.cvtColor(imgMat, mGrayscale, Imgproc.COLOR_RGB2GRAY);


            //Thresholding from pointA to pointB
            Imgproc.threshold(mR, mR, pointA, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mR, threshold, pointB, 255, Imgproc.THRESH_BINARY);


            //Start OVer everything
            mR = mRgb.get(0);
            mG = mRgb.get(1);
            mB = mRgb.get(2);


            publishProgress("Analysing each pixel");
            /*
            int rows = mR.rows();
            int cols = mR.cols();
            int type = imgMat.type();
            Mat newMat = new Mat(rows,cols, CvType.CV_8UC1);
            int n = 0;

            for(int r=0; r<rows; r++){
                for(int c=0; c<cols; c++){
                    double[] pixel = imgMat.get(r,c);
                    double redPixel = pixel[0];
                    double greenPixel = pixel[1];
                    double bluePixel = pixel[2];
                    double gray = redPixel * 0.299 + greenPixel * 0.587 + bluePixel * 0.114;
                    double logisticResult = weights[0] * redPixel + weights[1] * greenPixel + weights[2] * bluePixel
                            + weights[3] * gray + weights[4] * meanBChannel;

                    if (logisticResult > 0) {
                        logisticResult = 255;
                    } else {
                        logisticResult = 0;
                    }

                    if (n%100000 == 0) {
                        Log.i(TAG, "result = " + logisticResult);
                        Log.i(TAG, "" + weights[0] + " " +   " " + weights[1] + " " + weights[2] + " "
                                + weights[3] + " " + weights[4]);
                    }
                    n += 1;
                    newMat.put(r, c, logisticResult);
                }
            }


            */


            Mat[] matArrays = {mR, mG, mB, mGrayscale};
            Scalar alpha;

            for (int i = 0; i < matArrays.length; i++) {
                matArrays[i].convertTo(matArrays[i], CvType.CV_64FC1);
                alpha = new Scalar(weights[i], CvType.CV_64FC1);
                Core.multiply(matArrays[i], alpha, matArrays[i]);
            }

            Core.add(matArrays[0], matArrays[1], matArrays[1]);
            Core.add(matArrays[1], matArrays[2], matArrays[2]);
            Core.add(matArrays[2], matArrays[3], matArrays[3]);

            Mat zeros = org.opencv.core.Mat.zeros(mR.rows(), mR.cols(), CvType.CV_64FC1);

            Core.compare(matArrays[3], zeros, matArrays[3], Core.CMP_GT);


            Mat finalMat = new Mat();

            matArrays[3].copyTo(finalMat, threshold);

            /***
             * Dilate e erode process


            Imgproc.dilate(finalMat, finalMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
            Imgproc.erode(finalMat, finalMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
             */

            finalMat.convertTo(finalMat, imgMat.type());

            Utils.matToBitmap(finalMat, bitmap);

            return 1;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer params) {
            //remove old view
            linearLayout.removeView(drawView);

            //Add new view
            startDrawView();

            //Change button
            thresholdingImageButton.setText("Count");
            progress.dismiss();

        }
    }

    //PUBLIC CLASS OF CANVAS
    public class DrawView extends View {

        Paint rPaint;
        Canvas canvas = null;
        private Paint paint;
        private Context c;
        private Path mPath;
        private Path circlePath;
        private int sizeOfErasor = 50;
        private String TAG = "DrawView";
        private float mX, mY;
        private Rect rectangle = new Rect(0, 0, canvasWidth, canvasHeight);

        public DrawView(Context context) {
            super(context, null);
            init();
            c = context;

        }

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
            init();
            c = context;
        }

        public DrawView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
            c = context;
        }

        private void init() {
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(sizeOfErasor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

            circlePath = new Path();
            mPath = new Path();

            rPaint = new Paint();
            rPaint.setColor(Color.GREEN);
            rPaint.setStrokeWidth(3);
            rPaint.setStyle(Paint.Style.STROKE);


        }

        public void toInvalidate() {
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            canvas.drawBitmap(bitmap, null, rectangle, null);
            canvas.translate(0, 0);

            canvas.drawPath(mPath, paint);

            canvas.drawPath(circlePath, paint);

            if (meetConditionsForCrop()) {
                canvas.drawRect(downX, downY, curX, curY, rPaint);
            }

            canvas.save();

        }


        //TOUCHES EVENTS FUNCTIONS
        public void touch_start(float x, float y) {
            mPath.moveTo(x, y);
        }

        private void touch_move(float x, float y) {
            mX = x;
            mY = y;
            mPath.lineTo(x, y);

            circlePath.reset();
            circlePath.addCircle(x, y, sizeOfErasor, Path.Direction.CW);

        }

        private void touch_up() {
            //mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            //canvas.drawPath(mPath,  paint);
            // kill this so we don't double draw
        }


        //FIRST WINDOW TO BE OPEN
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            canvas = new Canvas();

            canvas.setBitmap(bitmap.copy(Bitmap.Config.ALPHA_8, true));
        }

    }
}