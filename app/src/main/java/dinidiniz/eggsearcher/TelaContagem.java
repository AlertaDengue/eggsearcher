package dinidiniz.eggsearcher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.sqrt;

public class TelaContagem extends Activity {

    //Thouch Events variables
    private float mX, mY;
    private float curX, curY;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    boolean started = false;
    CheckBox erasorCheckBox;

    //Global variables to draw
    String filePath;
    DrawView drawView;
    int canvasHeight;
    int canvasWidth;


    String TAG = "TelaContagem";
    LinearLayout linearLayout;
    Button thresholdingImageButton;
    Intent intent;
    FileOutputStream fileOutStream;
    Bitmap bitmap;

    //Studied variables
    int heightStudied = 8;
    int heightOn = 16;
    double meanU = 67.15 * (heightStudied / heightOn);
    double standartDeviationU = 13.8862341907 * sqrt(heightStudied / heightOn);
    int numberOfEggs = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_contagem);

        //Load to get canvaswidth and canvasheight
        loadScreen();


        //Control Scroll Views
        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        erasorCheckBox = (CheckBox) findViewById(R.id.erasorCheckBox);

        //Make it install OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);

        drawView = new DrawView(this);
        //drawView.setDrawingCacheEnabled(true);

        linearLayout = (LinearLayout) findViewById(R.id.captured_image);
        drawView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(drawView, canvasWidth, canvasHeight);


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        linearLayout.removeView(drawView);
    }


    //Controls bar Moves and Eraser of Canvas
    @Override
    public boolean onTouchEvent(MotionEvent event) {

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
                    mX = event.getX();
                    mY = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    curX = event.getX();
                    curY = event.getY();
                    if (started) {
                        vScroll.scrollBy(0, (int) (mY - curY));
                        hScroll.scrollBy((int) (mX - curX), 0);
                    } else {
                        started = true;
                    }
                    mX = curX;
                    mY = curY;
                    return true;
                case MotionEvent.ACTION_UP:
                    started = false;
                    return true;
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
        String filename = getFileStreamPath("tempIMG2.png").getAbsolutePath();
        editor.putString("imagepath", filename);
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
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //Load the bitmap from the canvas view
    public Bitmap loadBitmapFromView(View v) {

        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public double valueOfN(int m) {
        return meanU / (standartDeviationU * (sqrt(m) + sqrt(m + 1)));
    }

    public void loadScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        filePath = sharedPref.getString("imagepath", "/");
        int widthResolution = sharedPref.getInt("widthResolution", 0);
        int heightResolution = sharedPref.getInt("heightResolution", 0);

        int maxTextureSize = getMaxTextureSize();

        double maxResolution = (double) Math.max(widthResolution,heightResolution);

        if (maxResolution > maxTextureSize) {
            double factor = maxResolution/maxTextureSize;
            canvasWidth = (int) Math.floor(widthResolution/factor);
            canvasHeight = (int) Math.floor(heightResolution/factor);
        } else {
            canvasWidth = widthResolution;
            canvasHeight = heightResolution;
        }
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
            List<Double> areasList = new ArrayList<Double>();

            publishProgress("Thresholding");
            Utils.bitmapToMat(bitmap, imgMat);
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(imgMat, imgMat, 127, 255, Imgproc.THRESH_BINARY);
            //thresholdingImage2(bitmap);

            publishProgress("Labelling connected components");
            Imgproc.findContours(imgMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

            publishProgress("Counting areas");
            for (int idx = 0; idx < contours.size(); idx++) {
                Mat contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(contour);
                areasList.add(contourarea);
            }


            publishProgress("Finishing");
            Collections.sort(areasList);
            int cutNote;
            for (cutNote = 0; cutNote < areasList.size(); cutNote++) {
                if (areasList.get(cutNote) < 15) {
                } else {
                    break;
                }
            }

            int m = 1;
            double soma = 0;
            int numeroCaso = 0;

            for (int idy = cutNote; idy < areasList.size(); idy++) {
                while (areasList.get(idy) > meanU * (m) + standartDeviationU * valueOfN(m) * sqrt(m)) {
                    m += 1;
                }
                numberOfEggs += m;
                soma += areasList.get(idy);
                numeroCaso += 1;
            }

            Log.i(TAG, soma / numeroCaso + "");
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
            int pointA = 30;
            int pointB = 10;

            try {
                fileOutStream = openFileOutput("tempIMG2.png", MODE_PRIVATE);

                //Get Blue channel
                Utils.bitmapToMat(bitmap, imgMat);
                Core.split(imgMat, mRgb);
                Mat mB = mRgb.get(0);

                //Thresholding from pointA to pointB
                Imgproc.threshold(mB, mB, pointA, 255, Imgproc.THRESH_TOZERO_INV);
                Imgproc.threshold(mB, threshold, pointB, 255, Imgproc.THRESH_BINARY);

                Utils.matToBitmap(threshold, bitmap);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream);
                fileOutStream.flush();
                fileOutStream.close();
                saveScreen();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

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

            //load again to gain new image
            loadScreen();

            //Add new view
            drawView = new DrawView(context);
            drawView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            linearLayout.addView(drawView, canvasWidth, canvasHeight);


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
        private Path mPath;
        private Path circlePath;
        Canvas canvas = null;
        private int sizeOfErasor = 50;
        private String TAG = "DrawView";
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        public DrawView(Context context) {
            super(context, null);
            init();
            c = context;
            circlePath = new Path();
            mPath = new Path();

        }

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
            init();
            c = context;
            circlePath = new Path();
            mPath = new Path();
        }

        public DrawView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
            c = context;
            circlePath = new Path();
            mPath = new Path();
        }

        private void init() {
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(sizeOfErasor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

        }

        public void toInvalidate() {
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            Rect rectangle = new Rect(0, 0, canvasWidth, canvasHeight);
            canvas.drawBitmap(bitmap, null, rectangle, null);
            canvas.translate(0, 0);

            canvas.drawPath(mPath, paint);

            canvas.drawPath(circlePath, paint);


            canvas.save();

        }

        public Bitmap getResizedBitmap(String filePath, int newHeight, int newWidth) {

            Bitmap bm = BitmapFactory.decodeFile(filePath);

            int widthImage = bm.getWidth();
            int heightImage = bm.getHeight();


            float scaleWidth = ((float) newWidth) / widthImage;
            float scaleHeight = ((float) newHeight) / heightImage;


            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, widthImage, heightImage, matrix, false);
            return resizedBitmap;

        }

        public Bitmap RotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);

            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }


        //TOUCHES EVENTS FUNCTIONS
        public void touch_start(float x, float y) {
            mPath.moveTo(x, y);
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mX = x;
                mY = y;
                mPath.lineTo(x, y);

                circlePath.reset();
                circlePath.addCircle(mX, mY, sizeOfErasor, Path.Direction.CW);
            }
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


            if (bitmap != null) {
                bitmap.recycle();
            }

            canvas = new Canvas();

            if (filePath.equals(c.getFileStreamPath("tempIMG.png").getAbsolutePath())) {
                bitmap = getResizedBitmap(filePath, canvasHeight, canvasWidth);
                bitmap = RotateBitmap(bitmap, 90);
            } else if (filePath.equals(c.getFileStreamPath("tempIMG2.png").getAbsolutePath())) {
                bitmap = BitmapFactory.decodeFile(filePath);
            } else {
                bitmap = getResizedBitmap(filePath, canvasHeight, canvasWidth);
            }

            canvas.setBitmap(bitmap.copy(Bitmap.Config.ALPHA_8, true));
        }

    }
}