package dinidiniz.eggsearcher.telas;

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
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.functions.CameraPreview;
import dinidiniz.eggsearcher.functions.ConnectedComponentsLabelling;

import static java.lang.Math.sqrt;

public class TelaContagem extends Activity {

    //Thouch Events variables
    private float mX, mY;
    float downX, downY;
    float curX, curY;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    boolean started = false;
    CheckBox erasorCheckBox;
    Point size;

    //Global variables to draw
    String filePath;
    boolean isDrawingRec = false;
    DrawView drawView;
    int canvasHeight;
    int canvasWidth;
    static final float TOUCH_TOLERANCE = 15;
    long startTime;
    long curTime;


    String TAG = "TelaContagem";
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
    int minimumThresholdArea = 100;
    int heightOn;
    double meanU;
    double standartDeviationU;
    int numberOfEggs = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_contagem);

        //Load to get canvaswidth and canvasheight
        loadScreen();


        //GET SCREEN SIZE
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        //Control Scroll Views
        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        erasorCheckBox = (CheckBox) findViewById(R.id.erasorCheckBox);

        //Make it sync OpenCV
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback))
        {
            Log.e("TEST", "Cannot connect to OpenCV Manager");
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
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
                    startTime = event.getEventTime();

                    return returnMoveEvent;

                case MotionEvent.ACTION_MOVE:
                    curX = (int) (event.getX() + hScroll.getScrollX());
                    curY = (int) (event.getY() + vScroll.getScrollY());

                    if(!isDrawingRec) {
                        if (started) {
                            curTime = event.getEventTime();
                            float dx = Math.abs(curX - downX);
                            float dy = Math.abs(curY - downY);

                            if (dx < TOUCH_TOLERANCE || dy < TOUCH_TOLERANCE || !isDrawingRec) {
                                if (curTime - startTime > 1350) {
                                    isDrawingRec = true;
                                }
                            }
                            started = false;
                        } else {
                            vScroll.scrollBy(0, (int) (mY - curY));
                            hScroll.scrollBy((int) (mX - curX), 0);
                        }
                        mX = curX;
                        mY = curY;
                    } else {
                        drawView.toInvalidate();
                        if((event.getX())/size.x > 0.9 || (event.getY())/size.y > 0.9 || (event.getX())/size.x < 0.1 ||(event.getY())/size.y < 0.1){
                            vScroll.scrollBy(0, (int) (curY - mY));
                            hScroll.scrollBy((int) (curX - mX), 0);
                        }
                        mX = curX;
                        mY = curY;
                    }

                    return returnMoveEvent;

                case MotionEvent.ACTION_UP:
                    if(isDrawingRec){
                        float dxNew = curX - downX;
                        float dyNew = curY - downY;

                        if (dxNew > TOUCH_TOLERANCE && dyNew > TOUCH_TOLERANCE) {
                            if (curX > bitmap.getWidth()) {
                                curX = bitmap.getWidth();
                            }
                            if (curY > bitmap.getHeight()) {
                                curY = bitmap.getHeight();
                            }
                            bitmap = Bitmap.createBitmap(loadBitmapFromView(drawView), (int) downX, (int) downY, (int) (curX - downX), (int) (curY - downY));
                            canvasWidth = (int) Math.abs(downX - curX);
                            canvasHeight = (int) Math.abs(downY - curY);
                            linearLayout.removeView(drawView);
                            startDrawView();
                        }
                    }
                    isDrawingRec = false;
                    started = true;
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
        editor.putInt("numberOfEggs", numberOfEggs);
        editor.commit();
    }

    //Start OpenCV and download if necessary
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    startDrawView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void startDrawView(){
        drawView = new DrawView(this);

        linearLayout = (LinearLayout) findViewById(R.id.captured_image);
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

        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public double valueOfN(int m) {
        return Math.sqrt( m * (m + 1) /2 * Math.pow(standartDeviationU, 2) * (Math.log(m+1) - Math.log(m)) + m * (m + 1) * Math.pow(meanU, 2));
    }

    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap getBitmapFromFilePath(String filePath){
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        int widthResolution = bitmap.getWidth();
        int heightResolution =  bitmap.getHeight();

        int maxTextureSize = getMaxTextureSize();

        double maxResolution = (double) Math.max(widthResolution,heightResolution);

        if (maxResolution > maxTextureSize) {
            double factor = maxResolution/maxTextureSize;
            canvasWidth = (int) Math.floor(widthResolution / factor);
            canvasHeight = (int) Math.floor(heightResolution/factor);
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

    public void loadScreen() {
        Resources res = getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //GET PICTURE WITH FILEPATH AND PUT IT IN BITMAP AND GET THE RIGHT SIZES OF THE CANVAS
        filePath = sharedPref.getString("imagepath", "/");
        bitmap = getBitmapFromFilePath(filePath);

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

        double factorMultiplyer = (heightStudied / heightOn) * (resolutionOn/resolutionStudied);
        meanU = meanUStudied * factorMultiplyer;
        standartDeviationU = standardDeviationStudied * Math.sqrt(factorMultiplyer);
        Log.i(TAG, "Mean stimated: " + meanU);
        Log.i(TAG, "Standart deviation stimated: " + standartDeviationU);

    }

    //OTHER CLASSES NOW

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
            Mat labelImage;
            Mat stats = new Mat();
            Mat centroids = new Mat();
            List<Double> areasList = new ArrayList<Double>();

            publishProgress("Thresholding");
            Utils.bitmapToMat(bitmap, imgMat);
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(imgMat, imgMat, 127, 255, Imgproc.THRESH_BINARY);
            //thresholdingImage2(bitmap);

            System.out.println(Core.VERSION);

            publishProgress("Labelling connected components");

            Imgproc.findContours(imgMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            publishProgress("Counting areas");


            for (int idx = 0; idx < contours.size(); idx++) {
                Mat contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(contour);
                areasList.add(contourarea*3 + 10);
            }




            publishProgress("Finishing");
            Collections.sort(areasList);
            int cutNote;
            for (cutNote = 0; cutNote < areasList.size(); cutNote++) {
                if (areasList.get(cutNote) < minimumThresholdArea) {
                } else {
                    break;
                }
            }

            int m = 1;
            double soma = 0;
            double deviation = 0;
            int numeroCaso = 0;
            String areas = "";

            for (int idy = cutNote; idy < areasList.size(); idy++) {
                while (areasList.get(idy) > valueOfN(m)) {
                    Log.i(TAG, "Value of N: " + valueOfN(m));
                    m += 1;
                }
                numberOfEggs += m;
                soma += areasList.get(idy);
                areas += ", " + areasList.get(idy);
                deviation = Math.pow(areasList.get(idy), 2);
                numeroCaso += 1;
            }

            Log.i(TAG, "List: " + areas);

            Log.i(TAG, "Mean: " + soma / numeroCaso);
            Log.i(TAG, "Deviation: " + Math.pow(deviation/numeroCaso, ((float)1)/2));
            Log.i(TAG, numberOfEggs + "");
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
            List<Mat> mRgb = new ArrayList<Mat>(3);
            int pointA = thresholdOn;
            int pointB = 10;


            //Get Blue channel
            Utils.bitmapToMat(bitmap, imgMat);
            Core.split(imgMat, mRgb);
            Mat mB = mRgb.get(0);

            //SET TRESHOLD VALUE

            MatOfDouble mu = new MatOfDouble();
            MatOfDouble sigma = new MatOfDouble();

            Core.meanStdDev(mB, mu, sigma);

            Log.i(TAG, "Mean of Red Channel: " + mu.get(0,0)[0]);


            //Thresholding from pointA to pointB
            Imgproc.threshold(mB, mB, pointA, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mB, threshold, pointB, 255, Imgproc.THRESH_BINARY);
            Imgproc.dilate(threshold, threshold, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
            Imgproc.erode(threshold, threshold, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));


            Utils.matToBitmap(threshold, bitmap);

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

    //PUBLIC CLASS OF CANVAS
    public class DrawView extends View {

        private Paint paint;
        private Context c;
        Paint rPaint;
        private Path mPath;
        Canvas canvas = null;
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
            rPaint.setColor(Color.WHITE);
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

            if(isDrawingRec){
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