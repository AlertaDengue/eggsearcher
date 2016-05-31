package dinidiniz.eggsearcher.activity;

import android.app.Activity;
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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;

/**
 * Created by leon on 09/05/16.
 */
public class CalibrateActivity extends Activity {


    DBHelper dbHelper;


    //Global variables to draw
    String filePath;
    boolean isDrawingRec = false;
    DrawView drawView;
    int canvasHeight;
    int canvasWidth;
    long startTime;
    long curTime;
    String TAG = "TelaContagem";
    Intent intent;
    FileOutputStream fileOutStream;
    Bitmap bitmapOriginal;
    Bitmap bitmap;
    private int photoHeight;
    private int photoWidth;
    private Mat imgMat;
    private Mat imgMatOriginal;
    private Mat countour;
    private Scalar contourScalar = new Scalar(255, 255, 255, 255);
    private int startColum;
    private int startRow;
    private int endColum;
    private int endRow;
    private MatOfPoint matContour;

    //Layout Variables
    private LinearLayout questionTabLayout;
    private LinearLayout linearLayout;
    private Button buttonYes;
    private Button buttonNo;


    //Studied variables
    int thresholdOn;
    double resolutionOn;
    int heightOn;
    double meanU;
    double standartDeviationU;
    int numberOfEggs = 0;


    //Global variables of Thresholding
    private List<MatOfPoint> contours;
    private int pointContour = 0;


    //Start OpenCV and download if necessary
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    startDrawView();

                    new thresholdingThread(CalibrateActivity.this).execute();

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
        setContentView(R.layout.activity_calibrate);

        dbHelper = new DBHelper(this);

        //Load to get canvaswidth and canvasheight
        loadScreen();


        //Control Scroll Views
        buttonNo = (Button) findViewById(R.id.buttonNo);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        linearLayout = (LinearLayout) findViewById(R.id.captured_image);
        questionTabLayout = (LinearLayout) findViewById(R.id.questionTab);


        //LETS GET HEIGHT OF THE WINDOWS
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int windowsHeight = displaymetrics.heightPixels;
        photoWidth = displaymetrics.widthPixels;

        if (getActionBar() != null) {
            photoHeight = windowsHeight - (questionTabLayout.getHeight() + getActionBar().getHeight());
        } else {
            photoHeight = windowsHeight - questionTabLayout.getHeight();
        }

        Log.i(TAG, "width: " + photoWidth + " ;height: " + photoHeight);

        //Make it sync OpenCV
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback)) {
            Log.e("TEST", "Cannot connect to OpenCV Manager");
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numberOfEggs", numberOfEggs);
        editor.commit();
    }

    public void startDrawView() {
        drawView = new DrawView(this);

        drawView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(drawView, canvasWidth, canvasHeight);
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

    public Bitmap getBitmapFromFilePath(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        int widthResolution = bitmap.getWidth();
        int heightResolution = bitmap.getHeight();

        int maxTextureSize = getMaxTextureSize();

        double maxResolution = (double) Math.max(widthResolution, heightResolution);

        if (maxResolution > maxTextureSize) {
            double factor = maxResolution / maxTextureSize;
            canvasWidth = (int) Math.floor(widthResolution / factor);
            canvasHeight = (int) Math.floor(heightResolution / factor);
        } else {
            canvasWidth = widthResolution;
            canvasHeight = heightResolution;
        }


        bitmap = getResizedBitmap(bitmap, canvasHeight, canvasWidth);
        bitmap = RotateBitmap(bitmap, getRightAngleImage(filePath));

        return bitmap;
    }

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


    public int goToNext() {

        Log.i(TAG, "Size: " + contours.size() + " ;pointContour: " + pointContour);
        int top = 0;
        int left = 0;
        int right = 0;
        int bottom = 0;
        if (pointContour < contours.size()) {
            while (Imgproc.contourArea(contours.get(pointContour)) < 100) {
                pointContour += 1;
                if (pointContour >= contours.size()) {
                    return 1;
                }
            }

            matContour = contours.get(pointContour);

            org.opencv.core.Rect rect = Imgproc.boundingRect(matContour);
            left = rect.x;
            right = rect.width + left;
            top = rect.y;
            bottom = rect.height + top;

            Log.i(TAG, "left: " + left + " ;right: " + right + " ;top: " + top + " ;bottom: " + bottom);

            int sumLeft = 2;
            int sumRight = 5;
            int sumTop = 2;
            int sumBottom = 4*sumTop;

            startColum = left - sumLeft;
            startRow = top - sumTop;
            endColum =  left - sumLeft + rect.width + 2*sumLeft;
            endRow = top - sumTop + rect.height + sumBottom;

            if (left < sumLeft) {
                sumLeft = left;
            }
            if (top < sumTop){
                sumTop = top;
            }

            if (canvasWidth - right < sumRight) {
                sumRight = canvasWidth - right;
            }

            if (canvasHeight - bottom < sumBottom){
                sumBottom = canvasHeight - bottom;
            }

            if (startColum < 0){
                startColum = 0;
            }

            if (startRow < 0){
                startRow = 0;
            }

            //DRAW JUST THE PERIMETER
            Mat imgMatPerimeter = imgMatOriginal.clone();

            Imgproc.drawContours(imgMatPerimeter, contours, pointContour, contourScalar, 1, 4, countour, 0, new org.opencv.core.Point());


            Utils.matToBitmap(imgMatPerimeter, bitmapOriginal);

            Log.i(TAG,"startColum: " + startColum + " ;startRow: " + " ;endColum: " + (endColum - startColum) + " ;endRow: " +  (endRow - startRow));

            bitmap = null;
            bitmap = Bitmap.createBitmap(bitmapOriginal, startColum, startRow, endColum - startColum, endRow - startRow);
            bitmap = getResizedBitmap(bitmap, photoHeight, photoWidth);

            //remove old view
            linearLayout.removeView(drawView);

            //Add new view
            startDrawView();

        }
        return 1;
    }

    public void addToDataIsEgg(boolean answer){
        //DRAW ALL POLYGON
        ArrayList<List<Integer>> pixels = new ArrayList<List<Integer>>();
        imgMat = new Mat(imgMatOriginal.rows(), imgMatOriginal.cols(), imgMatOriginal.type());
        List<Mat> mRgb = new ArrayList<Mat>(3);

        Imgproc.drawContours(imgMat, contours, pointContour, contourScalar, -1);

        int area = 0;

        Log.i(TAG, "startColum: " + startColum + " ;startRow: " + startRow + " ;endColum: " + endColum);
        Log.i(TAG, "endRow - startRow: " + (endRow - startRow) + " ;endColum - startColum: " + (endColum - startColum));
        Log.i(TAG, "Area total: " + ((endRow - startRow) * (endColum - startColum)));
        Log.i(TAG, "Stimated area: " + Imgproc.contourArea(matContour));

        for (int i= startRow; i< endRow;i++){
            for (int j= startColum; j< endColum;j++){
                //Log.i(TAG, "entrou");
                if (imgMat.get(i, j)[0] == 255) {
                    List<Integer> pixel = new ArrayList<Integer>();
                    double[] pixelOriginal = imgMatOriginal.get(i, j);
                    pixel.add((int) pixelOriginal[0]);
                    pixel.add((int) pixelOriginal[1]);
                    pixel.add((int) pixelOriginal[2]);
                    if (answer){
                        pixel.add(1);
                    } else {
                        pixel.add(0);
                    }
                    pixels.add(pixel);
                    area += 1;
                }
            }
        }

        Log.i(TAG, "total area pixeis ovos: " + area);

        MatOfPoint2f new_mat = new MatOfPoint2f(matContour.toArray());
        MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
        Imgproc.approxPolyDP(new_mat, approxCurve_temp, matContour.total() * 0.05, true);

        int isConvex = 0;
        if (Imgproc.isContourConvex(matContour)){
            isConvex = 1;
        }

        if (answer){
            dbHelper.insertAllPixels(pixels);
            dbHelper.insertContour(isConvex, (int) matContour.total(), area,
                    (int) approxCurve_temp.total(), 1
            );
        } else {
            dbHelper.insertAllPixels(pixels);
            dbHelper.insertContour(isConvex, (int) matContour.total(), area,
                    (int) approxCurve_temp.total(), 0
            );
        }

        pointContour += 1;
    }

    //Load SCREEN using this function
    public void loadScreen() {
        Resources res = getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //GET PICTURE WITH FILEPATH AND PUT IT IN BITMAP AND GET THE RIGHT SIZES OF THE CANVAS
        filePath = sharedPref.getString("imagepath", "/");
        bitmapOriginal = getBitmapFromFilePath(filePath);
        bitmap = bitmapOriginal;

        //GET HEIGHT OF THE PICTURE THAT WAS TAKEN
        heightOn = sharedPref.getInt("heightFromLentsNumberPickerSelected", 12);

        //GET THRESHOLD LEVEL SELECTED
        int thresholdSpinnerSelected = sharedPref.getInt("thresholdSpinnerSelected", 0);
        if (thresholdSpinnerSelected == 0) {
            thresholdOn = 92;
        } else {
            String valueString = res.getStringArray(R.array.thresholdSpinnerList)[thresholdSpinnerSelected];
            thresholdOn = Integer.parseInt(valueString);
        }

        //GET RESOLUTION OF THE PICTURE
        resolutionOn = (double) canvasHeight * canvasWidth / 1024000;
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
            bitmapOriginal = loadBitmapFromView(drawView);
            bitmap = bitmapOriginal;
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            publishProgress("Thresholding");


            countour = new Mat();
            contours = new ArrayList<MatOfPoint>();
            imgMat = new Mat();
            imgMatOriginal = new Mat();
            Mat threshold = new Mat();
            List<Mat> mRgb = new ArrayList<Mat>(3);
            int pointA = thresholdOn;
            int pointB = 10;


            //Get Blue channel
            Utils.bitmapToMat(bitmapOriginal, imgMatOriginal);
            imgMat = imgMatOriginal;
            Core.split(imgMat, mRgb);
            Mat mB = mRgb.get(0);

            //SET TRESHOLD VALUE

            MatOfDouble mu = new MatOfDouble();
            MatOfDouble sigma = new MatOfDouble();

            Core.meanStdDev(mB, mu, sigma);

            Log.i(TAG, "Mean of Red Channel: " + mu.get(0, 0)[0]);


            //Thresholding from pointA to pointB
            Imgproc.threshold(mB, mB, pointA, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mB, threshold, pointB, 255, Imgproc.THRESH_BINARY);


            publishProgress("Labelling connected components");

            Imgproc.findContours(threshold, contours, countour, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            Utils.matToBitmap(imgMat, bitmapOriginal);

            publishProgress("Counting areas");

            return 1;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer params) {

            //GO TO FIRST
            goToNext();

            buttonNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToDataIsEgg(false);
                    goToNext();
                }
            });

            buttonYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToDataIsEgg(true);
                    goToNext();
                }
            });
            //Change button
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
        private Rect rectangle = new Rect(0, 0, photoWidth, photoHeight);

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
            rPaint.setColor(Color.WHITE);
            rPaint.setStrokeWidth(3);
            rPaint.setStyle(Paint.Style.STROKE);


        }
        

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            canvas.drawBitmap(bitmap, null, rectangle, null);
            canvas.translate(0, 0);

            canvas.drawPath(mPath, paint);

            canvas.drawPath(circlePath, paint);

            canvas.save();

        }

        //FIRST WINDOW TO BE OPEN
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            canvas = new Canvas();

            canvas.setBitmap(bitmap.copy(Bitmap.Config.ALPHA_8, true));
        }

    }
}