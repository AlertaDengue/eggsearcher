package dinidiniz.eggsearcher.telas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dinidiniz.eggsearcher.R;

/**
 * Created by leon on 16/11/15.
 */
public class TelaFullAutomatic extends Activity {

    String filePath;
    Bitmap bitmap;
    int canvasWidth;
    int canvasHeight;

    int heightOn;
    int thresholdOn;
    double resolutionOn;
    double meanU;
    double standartDeviationU;
    int heightStudied = 12;
    int resolutionStudied = 12;
    int numberOfEggs;

    Intent intent;

    String TAG = "Tela Full Automatic";
    //Start OpenCV and download if necessary
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    loadNewThread();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void loadNewThread(){
        new contagemThread(this).execute();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.tela_full_automatic);

        loadScreen();

        //Make it sync OpenCV
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback)) {
            Log.e("TEST", "Cannot connect to OpenCV Manager");
        }
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    public double valueOfN(int m) {
        return Math.sqrt(m * (m + 1) / 2 * Math.pow(standartDeviationU, 2) * (Math.log(m + 1) - Math.log(m)) + m * (m + 1) * Math.pow(meanU, 2));
    }

    public void loadScreen() {
        Resources res = getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //GET PICTURE WITH FILEPATH AND PUT IT IN BITMAP AND GET THE RIGHT SIZES OF THE CANVAS
        filePath = sharedPref.getString("imagepath", "/");
        bitmap = BitmapFactory.decodeFile(filePath);

        //GET HEIGHT OF THE PICTURE THAT WAS TAKEN
        heightOn = sharedPref.getInt("heightFromLentsNumberPickerSelected", 12);

        //GET THRESHOLD LEVEL SELECTED
        int thresholdSpinnerSelected = sharedPref.getInt("thresholdSpinnerSelected", 0);
        if (thresholdSpinnerSelected == 0) {
            thresholdOn = 80;
        } else {
            thresholdOn = Integer.getInteger(res.getStringArray(R.array.thresholdSpinnerList)[thresholdSpinnerSelected]);
        }

        //GET RESOLUTION OF THE PICTURE
        resolutionOn = (double) canvasHeight * canvasWidth / 1024000;

        double factorMultiplyer = (heightStudied / heightOn) * (resolutionOn / resolutionStudied);
        meanU = 35 * factorMultiplyer;
        standartDeviationU = 10 * Math.sqrt(factorMultiplyer);

    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String filename = getFileStreamPath("tempIMG2.png").getAbsolutePath();
        editor.putString("imagepath", filename);
        editor.putInt("numberOfEggs", numberOfEggs);
        editor.commit();
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
            progress.setMessage("Starting...");
            progress.show();
        }

        protected Integer doInBackground(Void... params) {
            List<Mat> mRgb = new ArrayList<Mat>(3);
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat imgMat = new Mat();
            List<Double> areasList = new ArrayList<Double>();


            int pointA = thresholdOn;
            int pointB = 10;

            publishProgress("Thresholding");
            //Get Blue channel
            Utils.bitmapToMat(bitmap, imgMat);
            Core.split(imgMat, mRgb);
            Mat mB = mRgb.get(0);

            //Thresholding from pointA to pointB
            Imgproc.threshold(mB, mB, pointA, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mB, imgMat, pointB, 255, Imgproc.THRESH_BINARY);
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

            Log.i(TAG, "[1]");
            int m = 1;
            double soma = 0;
            int numeroCaso = 0;

            for (int idy = cutNote; idy < areasList.size(); idy++) {
                while (areasList.get(idy) > valueOfN(m)) {
                    m += 1;
                }
                numberOfEggs += m;
                soma += areasList.get(idy);
                numeroCaso += 1;
            }

            Log.i(TAG, "[2]");

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

}
