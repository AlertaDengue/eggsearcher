package dinidiniz.eggsearcher;

import android.os.Environment;

/**
 * Created by leon on 31/10/16.
 */
public class Consts {

    public final static String UPLOADED_PHOTO = "uploaded";
    public final static int GO_TO_COUNT = 15;
    public final static int SELECTED_PHOTO = 100;
    public final static String imagepath = "imagepath";
    public final static String heightFromLentsNumberPickerSelected = "heightFromLentsNumberPickerSelected";
    public final static String thresholdSpinnerSelected = "thresholdSpinnerSelected";

    public static String getImagePath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/eggSearcher/";
    }
}
