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
import android.graphics.Point;
import android.graphics.Rect;
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
import java.util.List;

public class TelaContagem extends Activity {

    //Thouch Events variables
    private float mx, my;
    private float curX, curY;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    boolean started = false;
    CheckBox erasorCheckBox;
    private int mScrollX = 0, mScrollY = 0;
    List<Point> points = new ArrayList<Point>();

    DrawView drawView;
    int canvasHeight = 4096;
    int canvasWidth = 3100;
    String TAG = "TelaContagem";
    LinearLayout linearLayout;
    Button thresholdingImageButton;
    int pixel;
    int A;
    int Red;
    int Green;
    int Blue;
    int width;
    int height;
    int[][] matrix;
    int[] areaMatrix;
    FileOutputStream fileOutStream;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_contagem);

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

    //Controls bar Moves and Eraser of Canvas
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (erasorCheckBox.isChecked()) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN: { // touch on the screen event;
                            Point point = new Point();
                            point.x = (int) (event.getX() + mScrollX);
                            point.y = (int) (event.getY() + mScrollY);
                            points.add(point);
                            drawView.toInvalidate();
                            return true;
                        }
                        case MotionEvent.ACTION_MOVE: { // move event
                            Point point = new Point();
                            point.x = (int) (event.getX() + mScrollX);
                            point.y = (int) (event.getY() + mScrollY);
                            points.add(point);
                            drawView.toInvalidate();
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {  // finger up event
                            return true;
                        }
                    }
                } else {
                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:
                                mx = event.getX();
                                my = event.getY();
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                curX = event.getX();
                                curY = event.getY();
                                if (started) {
                                    vScroll.scrollBy(0, (int) (my - curY));
                                    mScrollY = (int) (mScrollY + my - curY);
                                    hScroll.scrollBy((int) (mx - curX), 0);
                                    mScrollX = (int) (mScrollX + mx - curX);
                                } else {
                                    started = true;
                                }
                                mx = curX;
                                my = curY;
                                return true;
                            case MotionEvent.ACTION_UP:
                                started = false;
                                return true;
                        }
                }
                return true;

    }



    public void thresholdImageButton(View view){
        thresholdingImageButton = (Button) findViewById(R.id.button_Thresholding);
        if (thresholdingImageButton.getText().toString().equals("Thresholding")) {

            new thresholdingThread(this).execute();
        } else {
            new contagemThread(this).execute();
        }
    }

    public void saveScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String filename = getFileStreamPath("tempIMG2.png").getAbsolutePath();
        editor.putString("imagepath", filename);
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


    public Bitmap thresholdingImage(Bitmap bitmap){
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        matrix = new int[width][height];
        int gray;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = bitmap.getPixel(x, y);
                //A = Color.alpha(pixel);
                Red = Color.red(pixel);
                Green = Color.green(pixel);
                Blue = Color.blue(pixel);
                // use 20 to 60 as threshold, in -> white, out -> black
                if ((Blue < 40) && (Blue > 10) && (Green < 40) && (Green > 20) && (Red < 100)) {
                    gray = 255;
                } else {
                    gray = 0;
                }
                // set new pixel color to output bitmap
                bitmap.setPixel(x, y, Color.argb(255, gray, gray, gray));
                matrix[x][y] = gray*1000;
            }
        }
        return bitmap;
    }

    //Load the bitmap from the canvas view
    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    //OTHER CLASSES NOW

    //PUBLIC CLASS TO COUNT THE EGGS IN A DIFFERENT THREAD
    public class contagemThread extends AsyncTask<Void,String,Integer> {

        private ProgressDialog progress;
        private Context context;

        public contagemThread(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute(){
            progress = new ProgressDialog(context);
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void ... params) {
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat imgMat = new Mat();
            List<Double> areasList = new ArrayList<Double>();


            Bitmap bitmap = loadBitmapFromView(drawView);
            publishProgress("Thresholding");
            Utils.bitmapToMat(bitmap, imgMat);
            Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(imgMat, imgMat, 127, 255, Imgproc.THRESH_BINARY);
            //thresholdingImage2(bitmap);
            publishProgress("Labelling connected components");
            Imgproc.findContours(imgMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
            //matrix = ConnectedComponentsLabelling.twoPass(matrix);
            publishProgress("Counting areas");
            for (int idx = 0; idx < contours.size(); idx++) {
                Mat contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(contour);
                areasList.add(contourarea);
            }
            //areaMatrix = ConnectedComponentsLabelling.areaCount(matrix);
            Log.i(TAG, TextUtils.join(", ", areasList));
            return 1;
        }
        @Override
        protected void onProgressUpdate(String... params){
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer params) {
            progress.dismiss();
        }
    }

    //PUBLIC CLASS TO THRESHOLD IN A DIFFERENT THREAD
    public class thresholdingThread extends AsyncTask<Void,String,Integer> {

        private ProgressDialog progress;
        private Context context;
        private Bitmap bitmap;

        public thresholdingThread(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute(){
            progress = new ProgressDialog(context);
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void ... params) {
            publishProgress("Thresholding");
            bitmap = loadBitmapFromView(drawView);

            Mat imgMat = new Mat();
            Mat threshold = new Mat();
            List<Mat> mRgb = new ArrayList<Mat>(3);
            int pointA = 35;
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
        protected void onProgressUpdate(String... params){
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer params) {
            //remove old view
            linearLayout.removeView(drawView);

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

    //PUBLIC CLASS OF CANVAS
    public class DrawView extends View {

        private Bitmap bitmap;
        private Paint paint;
        private String filePath;
        private Context c;
        Canvas canvas = null;
        private String TAG = "DrawView";

        public DrawView(Context context) {
            super(context, null);
            init();
            c = context;
            loadScreen(c);

        }

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
            init();
            c = context;
            loadScreen(c);
        }

        public DrawView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
            c = context;
            loadScreen(c);
        }

        private void init(){
            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(10);

        }

        public void toInvalidate(){
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            Rect rectangle = new Rect(0, 0, canvasWidth, canvasHeight);
            canvas.drawBitmap(bitmap, null, rectangle, null);
            canvas.translate(0, 0);
            canvasDraw(canvas);
            canvas.save();

        }

        public void loadScreen(Context context){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            filePath = sharedPref.getString("imagepath", "/");
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

        public Bitmap RotateBitmap(Bitmap source, float angle)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);

            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }



        public void canvasDraw(Canvas canvas){
            if (points != null) {
                for (Point point : points) {
                    canvas.drawCircle(point.x, point.y, 40, paint);
                }
            }
        }

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);



            if (bitmap != null) {
                bitmap .recycle();
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