package dinidiniz.eggsearcher.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

import dinidiniz.eggsearcher.activity.TelaResultado;

/**
 * Created by leon on 20/10/16.
 */
public class ImageProcessing {

    private final static String TAG = ImageProcessing.class.getName();

    /***
     * Get mean of the Blue channel excluding the background
     * @param imgMat
     * @return mean of the blue channel
     */
    public static int getMeanOfBlueChannelInMat(Mat imgMat){

        List<Mat> mRgb = new ArrayList<Mat>(3);
        Core.split(imgMat, mRgb);
        Mat mB = mRgb.get(2);

        MatOfDouble mu = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();
        Imgproc.threshold(mB, mB, 180, 255, Imgproc.THRESH_TOZERO_INV);
        Core.meanStdDev(mB, mu, sigma, mB);
        return (int) mu.get(0, 0)[0];
    }

    /***
     * Get the centroid of a Contour given
     * @param contour
     * @return Point that represents where is the centroid
     */
    public static Point getCentroidOfContour(MatOfPoint contour){
        org.opencv.core.Rect p = Imgproc.boundingRect(contour);
        int x = (int) (p.x + 0.5*(p.width));
        int y = (int) (p.y + + 0.5*(p.height));
        return new Point(x, y);
    }


    public static int minIndex(List<Double> list) {
        Integer i=0;
        Integer minIndex=-1;
        Double min=null;
        for (Double x : list) {
            if ((x!=null) && ((min==null) || (x<min))) {
                min = x;
                minIndex = i;
            }
            i++;
        }
        return minIndex;
    }

    public static int maxIndex(List<Double> list) {
        Integer i=0;
        Integer maxIndex=-1;
        Double max=null;
        for (Double x : list) {
            if ((x!=null) && ((max==null) || (x>max))) {
                max = x;
                 maxIndex = i;
            }
            i++;
        }
        return  maxIndex;
    }

    //PUBLIC CLASS TO COUNT THE EGGS IN A DIFFERENT THREAD

}
