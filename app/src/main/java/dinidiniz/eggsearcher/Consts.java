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
    public final static int ORIGINAL_heightFromLentsNumberPickerSelected = 12;
    public final static String numberOfEggs = "numberOfEggs";
    public final static String areaTotal = "areaTotal";
    public final static String namePhotoSpinnerSelected = "namePhotoSpinnerSelected";
    public final static String sampleNumber = "sampleNumber";
    public final static String areas = "areas";
    public final static String result_date = "resultDate";

    //User Data
    public final static String user_email = "userEmail";
    public final static String user_name = "userName";
    public final static String user_id = "userId";
    public final static String user_photourl = "userPhotoUrl";
    public final static String user_familyname = "userFamilyName";

    public static String getImagePath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/eggSearcher/";
    }
}
