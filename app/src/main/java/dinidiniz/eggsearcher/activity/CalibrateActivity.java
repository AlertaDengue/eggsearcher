package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import dinidiniz.eggsearcher.Consts;
import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.helper.ImageProcessing;

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
    //Variables to zoom in and out
    PointF last = new PointF();
    PointF start = new PointF();
    //Studied variables
    int thresholdOn;
    double resolutionOn;
    int heightOn;
    double meanU;
    double standartDeviationU;
    int numberOfEggs = 0;
    private Bitmap bitmapModificate;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Bitmap bitmapOriginal;
    private TextView percentagePixelsTextView;
    private int photoHeight;
    private int photoWidth;
    private Mat imgMat;
    private Mat imgMatOriginal;
    private Mat countour;
    private Scalar contourScalar = new Scalar(0, 255, 0, 20);
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
    private Button buttonPass;
    private int startColumZoom;
    private int startRowZoom;
    private int endColumZoom;
    private int endRowZoom;
    //Global variables of Thresholding
    private List<MatOfPoint> contours;
    private int pointContour = 0;
    private int meanBChannel;

    private Boolean selectedToUpload = true;


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
        setContentView(R.layout.activity_calibrate);

        //Check if is to upload a picture or not
        Bundle getIntentExtras = getIntent().getExtras();
        selectedToUpload = getIntentExtras.getBoolean(Consts.UPLOADED_PHOTO);

        dbHelper = new DBHelper(this);
        percentagePixelsTextView = (TextView) findViewById(R.id.percentagePixelsTextView);


        //Control Scroll Views
        buttonNo = (Button) findViewById(R.id.buttonNo);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        buttonPass = (Button) findViewById(R.id.buttonPass);
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
    }

    /***
     * Function to control the zoom in or zoom out
     *
     * @param event
     * @return true to continue event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "down");
                start.set(event.getX(), event.getY());
                last.set(start);
                break;
            case MotionEvent.ACTION_MOVE:

                int delta = (int) (last.y - event.getY());
                //If the courser moved down direction
                //If starts after will start at the the start
                if (startColumZoom - delta < startColum) {
                    if (startColumZoom - delta < 0) {
                        startColumZoom = 0;
                    } else {
                        startColumZoom = startColumZoom - delta;
                    }
                } else {
                    startColumZoom = startColum;
                }

                //If ends early will end at it was ending in the first place
                if (endColumZoom + 2 * delta > endColum - startColum) {
                    if (startColumZoom + endColumZoom + 2 * delta > canvasWidth) {
                        endColumZoom = canvasWidth - startColumZoom;
                    } else {
                        endColumZoom = endColumZoom + 2 * delta;
                    }
                } else {
                    endColumZoom = endColum - startColum;
                }

                //If starts after will start at the the start
                if (startRowZoom - delta < startRow) {
                    if (startRowZoom - delta < 0) {
                        startRowZoom = 0;
                    } else {
                        startRowZoom = startRowZoom - delta;
                    }
                } else {
                    startRowZoom = startRow;
                }

                //If ends early will end at it was ending in the first place
                if (endRowZoom + 2 * delta > endRow - startRow) {
                    if (startRowZoom + endRowZoom + 2 * delta > canvasHeight) {
                        endRowZoom = canvasHeight - startRowZoom;
                    } else {
                        endRowZoom = endRowZoom + 2 * delta;
                    }
                } else {
                    endRowZoom = endRow - startRow;
                }


                //get bitmap
                bitmap = null;
                bitmap = Bitmap.createBitmap(bitmapModificate, startColumZoom, startRowZoom, endColumZoom, endRowZoom);
                bitmap = getResizedBitmap(bitmap, photoHeight, photoWidth);
                bitmap2 = null;
                bitmap2 = Bitmap.createBitmap(bitmapOriginal, startColumZoom, startRowZoom, endColumZoom, endRowZoom);
                bitmap2 = getResizedBitmap(bitmap2, photoHeight, photoWidth);

                //Restart drawView
                drawView.invalidate();

                last.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "Pointer down");
                break;
        }
        return true;
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
                        bitmap2 = bitmap;
                        bitmapModificate = bitmap;
                        bitmapOriginal = bitmap;
                        if (is != null) {
                            is.close();
                        }

                        startDrawView();

                        new thresholdingThread(this).execute();
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

    public void getBitmap(String filePath) {

        Log.i(TAG, "lets get bitmap: " + selectedToUpload);
        if (bitmap == null) {
            if (selectedToUpload) {
                //Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //photoPickerIntent.setType("image/*");
                Intent photoPickerIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                this.startActivityForResult(photoPickerIntent, Consts.SELECTED_PHOTO);
            } else {
                bitmap = BitmapFactory.decodeFile(filePath);
                bitmap = adjustBitmap(bitmap);
                bitmap2 = bitmap;
                bitmapModificate = bitmap;
                bitmapOriginal = bitmap;

                startDrawView();
                new thresholdingThread(this).execute();

            }
        }

    }

    public Bitmap adjustBitmap(Bitmap bitmap) {
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

        bitmap = RotateBitmap(bitmap, getRightAngleImage(filePath));
        bitmap = getResizedBitmap(bitmap, canvasHeight, canvasWidth);

        Log.i(TAG, "bitmap width: " + bitmap.getWidth() + " ;bitmap height: " + bitmap.getHeight());

        return bitmap;
    }

    /***
     * Get the right angle of the figure
     *
     * @param photoPath
     * @return
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

    /***
     * Function to go back to initial screen after seeing the last egg
     */
    private void goBacktoInitialScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("That was the last egg in the picture that our system detected")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        intent = new Intent(CalibrateActivity.this, TelaInicial.class);
                        startActivity(intent);
                        CalibrateActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public int goToNext() {

        Log.i(TAG, "Size: " + contours.size() + " ;pointContour: " + pointContour);
        int top = 0;
        int left = 0;
        int right = 0;
        int bottom = 0;
        if (contours.size() == 0) {
            goBacktoInitialScreen();
        }

        if (pointContour < contours.size()) {
            while (Imgproc.contourArea(contours.get(pointContour)) < 5) {
                pointContour += 1;
                if (pointContour >= contours.size()) {
                    goBacktoInitialScreen();
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

            int sumLeft = 50;
            int sumTop = 50;

            startColum = left - sumLeft;
            startRow = top - sumTop;
            endColum = left + rect.width + sumLeft;
            endRow = top + rect.height + sumTop;

            Log.i(TAG, "startColum: " + startColum + " ;startRow: " + startRow + " ;endColum: " + (endColum - startColum) + " ;endRow: " + (endRow - startRow));
            Log.i(TAG, "canvasWidth: " + canvasWidth + "; canvasHeight: " + canvasHeight);


            if (endColum > canvasWidth) {
                endColum = canvasWidth;
            }

            if (endRow > canvasHeight) {
                endRow = canvasHeight;
            }

            if (startColum < 0) {
                startColum = 0;
            }

            if (startRow < 0) {
                startRow = 0;
            }

            //DRAW JUST THE PERIMETER
            Mat imgMatPerimeter = imgMatOriginal.clone();

            Imgproc.drawContours(imgMatPerimeter, contours, pointContour, contourScalar, 1, 8, countour, 0, new org.opencv.core.Point());


            Utils.matToBitmap(imgMatPerimeter, bitmapModificate);

            Log.i(TAG, "startColum: " + startColum + " ;startRow: " + startRow + " ;endColum: " + (endColum - startColum) + " ;endRow: " + (endRow - startRow));


            //Put start to the zoom as well
            startColumZoom = startColum;
            startRowZoom = startRow;
            endColumZoom = endColum - startColum;
            endRowZoom = endRow - startRow;

            //Create bitmap of the part of the original bitmap interested and than zoom in
            bitmap = null;
            bitmap = Bitmap.createBitmap(bitmapModificate, startColum, startRow, endColum - startColum, endRow - startRow);
            bitmap = getResizedBitmap(bitmap, photoHeight, photoWidth);

            //O SEGUNDO bitmap é para ser usado embaixo do primeiro e permitir o canal alpha existir.
            bitmap2 = null;
            bitmap2 = Bitmap.createBitmap(bitmapOriginal, startColum, startRow, endColum - startColum, endRow - startRow);
            bitmap2 = getResizedBitmap(bitmap2, photoHeight, photoWidth);

            //remove old view
            linearLayout.removeView(drawView);

            //Add new view
            startDrawView();


        }
        return 1;
    }

    public void addToDataIsEgg(boolean answer) {
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

        Outerloop:
        for (int i = startRow; i < endRow; i++) {
            for (int j = startColum; j < endColum; j++) {
                //Log.i(TAG, "entrou");
                if (imgMat.get(i, j)[1] == 255) {
                    List<Integer> pixel = new ArrayList<Integer>();
                    double[] pixelOriginal = imgMatOriginal.get(i, j);
                    pixel.add((int) pixelOriginal[0]);
                    pixel.add((int) pixelOriginal[1]);
                    pixel.add((int) pixelOriginal[2]);
                    pixel.add((int) (pixelOriginal[0] * 0.299 + pixelOriginal[1] * 0.587 + pixelOriginal[2] * 0.114));
                    pixel.add(meanBChannel);
                    if (answer) {
                        pixel.add(1);
                    } else {
                        pixel.add(0);
                    }
                    pixels.add(pixel);
                    area += 1;

                    if (area > 500) {
                        break Outerloop;
                    }
                }
            }
        }

        Log.i(TAG, "total area pixeis ovos: " + area);

        MatOfPoint2f new_mat = new MatOfPoint2f(matContour.toArray());
        MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
        Imgproc.approxPolyDP(new_mat, approxCurve_temp, matContour.total() * 0.05, true);

        int isConvex = 0;
        if (Imgproc.isContourConvex(matContour)) {
            isConvex = 1;
        }

        if (answer) {
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

    /***
     * Function to load with screen; get shared preferences to see default values
     */
    public void loadScreen() {
        Resources res = getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //GET PICTURE WITH FILEPATH AND PUT IT IN BITMAP AND GET THE RIGHT SIZES OF THE CANVAS
        filePath = sharedPref.getString(Consts.imagepath, "/");

        getBitmap(filePath);
        imgMatOriginal = new Mat();

        //GET HEIGHT OF THE PICTURE THAT WAS TAKEN
        heightOn = sharedPref.getInt(Consts.heightFromLentsNumberPickerSelected, 12);

        //GET THRESHOLD LEVEL SELECTED
        int thresholdSpinnerSelected = sharedPref.getInt(Consts.thresholdSpinnerSelected, 0);
        String valueString = res.getStringArray(R.array.thresholdSpinnerList)[thresholdSpinnerSelected];
        thresholdOn = Integer.parseInt(valueString);


        Log.i(TAG, "threshold point: " + thresholdOn);

        //GET RESOLUTION OF THE PICTURE
        resolutionOn = (double) canvasHeight * canvasWidth / 1024000;
    }

    /***
     * Thread to work each pixel and put them in dataset.
     */
    public class isEggThread extends AsyncTask<Void, String, Integer> {

        private ProgressDialog progress;
        private Context context;
        private Boolean isEgg;

        public isEggThread(Context context, Boolean isEgg) {
            this.context = context;
            this.isEgg = isEgg;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            publishProgress("Adding pixels to Data");
            addToDataIsEgg(isEgg);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Starting...");
            progress.show();
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progress.setMessage("Going to next fragment");
            goToNext();
            progress.dismiss();


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
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            publishProgress("Thresholding");

            countour = new Mat();
            contours = new ArrayList<MatOfPoint>();
            imgMat = new Mat();

            Mat threshold = new Mat();
            List<Mat> mRgb = new ArrayList<Mat>(3);
            int pointA = thresholdOn;
            int pointB = 5;
            Utils.bitmapToMat(bitmapModificate, imgMatOriginal);

            //Get Blue channel
            imgMat = imgMatOriginal;
            //Get Blue channel
            Utils.bitmapToMat(bitmap, imgMat);
            Core.split(imgMat, mRgb);
            Mat mR = mRgb.get(0);

            //Get mean of the blue channel after getting only pixels bellow 180
            meanBChannel = ImageProcessing.getMeanOfBlueChannelInMat(imgMat);
            Log.i(TAG, "Mean of Blue Channel: " + meanBChannel);


            //Thresholding from pointA to pointB
            Imgproc.threshold(mR, mR, pointA, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mR, threshold, pointB, 255, Imgproc.THRESH_BINARY);


            publishProgress("Labelling connected components");

            Imgproc.findContours(threshold, contours, countour, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            Utils.matToBitmap(imgMat, bitmapModificate);

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
                    new isEggThread(context, false).execute();
                    percentagePixelsTextView.setText(String.format("%.2f", (((double) pointContour) / contours.size()) * 100) + "% already counted");
                }
            });


            buttonYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new isEggThread(context, true).execute();
                    goToNext();
                    percentagePixelsTextView.setText(String.format("%.2f", (((double) pointContour) / contours.size()) * 100) + "% already counted");
                }
            });

            buttonPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pointContour += 1;
                    goToNext();
                    percentagePixelsTextView.setText(String.format("%.2f", (((double) pointContour) / contours.size()) * 100) + "% already counted");
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

            /*
            Mat bitmat2 = new Mat();
            Utils.bitmapToMat(bitmap2, bitmat2);

            List<Mat> mRgb = new ArrayList<Mat>(3);
            Bitmap redBitmap = Bitmap.createBitmap(bitmat2.cols(), bitmat2.rows(), Bitmap.Config.ARGB_8888);
            Bitmap greenBitmap = Bitmap.createBitmap(bitmat2.cols(), bitmat2.rows(), Bitmap.Config.ARGB_8888);
            Bitmap blueBitmap = Bitmap.createBitmap(bitmat2.cols(), bitmat2.rows(), Bitmap.Config.ARGB_8888);
            Core.split(bitmat2, mRgb);

            Utils.matToBitmap(mRgb.get(0), redBitmap);
            Utils.matToBitmap(mRgb.get(1), greenBitmap);
            Utils.matToBitmap(mRgb.get(2), blueBitmap);

            Paint redPaint = new Paint();
            redPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
            redPaint.setShader(new BitmapShader(redBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            redPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DARKEN));

            Paint greenPaint = new Paint();
            greenPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
            greenPaint.setShader(new BitmapShader(greenBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            greenPaint.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.DARKEN));

            Paint bluePaint = new Paint();
            bluePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
            bluePaint.setShader(new BitmapShader(blueBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            bluePaint.setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN));

            Paint alphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            alphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));


            canvas.drawRect(rectangle, redPaint);
            canvas.drawRect(rectangle, greenPaint);
            canvas.drawRect(rectangle, bluePaint);
            canvas.drawBitmap(bitmap, 0, 0, alphaPaint);
            */


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