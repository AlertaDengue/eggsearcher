package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.functions.CameraPreview;
import dinidiniz.eggsearcher.helper.Logistic;

/**
 * Created by leon on 15/11/15.
 */
public class TelaConfiguracao extends Activity {
    final private static String TAG = "tela configuracao";
    final public  static String WEIGHT_LOGISTIC_R = "logisticr";
    final public  static String WEIGHT_LOGISTIC_G = "logisticg";
    final public  static String WEIGHT_LOGISTIC_B = "logisticb";
    final public  static String WEIGHT_LOGISTIC_GRAY = "logisticgray";
    final public  static String WEIGHT_LOGISTIC_MEANB = "logisticmeanb";


    private Spinner thresholdSpinner;
    private Spinner processSpinner;
    private Spinner resolutionSpinner;
    private Spinner photoAreaSpinner;
    private NumberPicker heightFromLentsNumberPicker;
    private int thresholdSpinnerSelected;
    private int processSpinnerSelected;
    private int heightFromLentsNumberPickerSelected;
    private int resolutionSpinnerSelected;
    private int photoAreaSpinnerSelected;
    private Resources res;
    private CheckBox flashCheckBox;
    private Boolean flashChackBoxSelected;
    private Button deleteTrainningButton;
    private DBHelper dbHelper;
    private double[] weights = {0, 0, 0, 0, 0};
    private TextView percentageErrorLogisticTextView;
    private long error;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_configuracao);
        dbHelper = new DBHelper(this);

        loadScreen();

        res = getResources();

        //THRESHOLD SPINNER
        thresholdSpinner = (Spinner) findViewById(R.id.thresholdSpinner);
        String[] thresholdSpinnerList = res.getStringArray(R.array.thresholdSpinnerList);

        ArrayAdapter<String> thresholdDataAdapter = new ArrayAdapter<String>
                (this, R.layout.simple_spinner, thresholdSpinnerList);

        thresholdDataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        thresholdSpinner.setAdapter(thresholdDataAdapter);

        thresholdSpinner.setSelection(thresholdSpinnerSelected);

        //PROCESS SPINNER
        processSpinner = (Spinner) findViewById(R.id.processSpinner);
        String[] processSpinnerList = res.getStringArray(R.array.processSpinnerList);

        ArrayAdapter<String> processDataAdapter = new ArrayAdapter<String>
                (this, R.layout.simple_spinner, processSpinnerList);

        processDataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        processSpinner.setAdapter(processDataAdapter);

        processSpinner.setSelection(processSpinnerSelected);

        //RESOLUTION SPINNER

        resolutionSpinner = (Spinner) findViewById(R.id.resolutionSpinner);
        List<String> resolutionSpinnerList = CameraPreview.getCameraResolutionList();

        ArrayAdapter<String> resolutionDataAdapter = new ArrayAdapter<String>
                (this, R.layout.simple_spinner, resolutionSpinnerList);

        resolutionDataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        resolutionSpinner.setAdapter(resolutionDataAdapter);

        resolutionSpinner.setSelection(resolutionSpinnerSelected);

        //PHOTO AREA SPINNER

        photoAreaSpinner = (Spinner) findViewById(R.id.photoAreaSpinner);
        String[] photoAreaSpinnerList = res.getStringArray(R.array.photoAreaSpinnerList);

        ArrayAdapter<String> photoAreaDataAdapter = new ArrayAdapter<String>
                (this, R.layout.simple_spinner, photoAreaSpinnerList);

        photoAreaDataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        photoAreaSpinner.setAdapter(photoAreaDataAdapter);

        photoAreaSpinner.setSelection(photoAreaSpinnerSelected);


        //HEIGHT NUMBER PICKER

        heightFromLentsNumberPicker = (NumberPicker) findViewById(R.id.heightFromLents);
        heightFromLentsNumberPicker.setMaxValue(15);
        heightFromLentsNumberPicker.setMinValue(8);
        heightFromLentsNumberPicker.setValue(heightFromLentsNumberPickerSelected);

        //Delete all Trainning button
        deleteTrainningButton = (Button) findViewById(R.id.deleteTrainnningButton);
        percentageErrorLogisticTextView = (TextView) findViewById(R.id.percentageErrorLogisticTextView);
        if (error == 0) {
            percentageErrorLogisticTextView.setText("NOT TRAINED");
        } else {
            percentageErrorLogisticTextView.setText("TRAINED");
        }


    }

    public void deleteTrainningButton(View view) {
        dbHelper.deleteAllPixelsFromTable(DBHelper.PIXEL_TABLE_NAME);
        dbHelper.deleteAllPixelsFromTable(DBHelper.CONTOURS_TABLE_NAME);
        percentageErrorLogisticTextView.setText("NOT TRAINED");
        error = 0;
    }

    public void trainningButton(View view) {
        new logisticPixels().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveScreen();
    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("thresholdSpinnerSelected", thresholdSpinner.getSelectedItemPosition());
        editor.putInt("processSpinnerSelected", processSpinner.getSelectedItemPosition());
        editor.putInt("heightFromLentsNumberPickerSelected", heightFromLentsNumberPicker.getValue());
        editor.putInt("resolutionSpinnerSelected", resolutionSpinner.getSelectedItemPosition());
        editor.putInt("photoAreaSpinnerSelected", photoAreaSpinner.getSelectedItemPosition());
        editor.putLong(WEIGHT_LOGISTIC_R, Double.doubleToRawLongBits(weights[0]));
        editor.putLong(WEIGHT_LOGISTIC_G, Double.doubleToRawLongBits(weights[1]));
        editor.putLong(WEIGHT_LOGISTIC_B, Double.doubleToRawLongBits(weights[2]));
        editor.putLong(WEIGHT_LOGISTIC_GRAY, Double.doubleToRawLongBits(weights[3]));
        editor.putLong(WEIGHT_LOGISTIC_MEANB, Double.doubleToRawLongBits(weights[4]));
        editor.putLong("error", error);
        editor.commit();
    }

    public void loadScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        thresholdSpinnerSelected = sharedPref.getInt("thresholdSpinnerSelected", 0);
        processSpinnerSelected = sharedPref.getInt("processSpinnerSelected", 0);
        resolutionSpinnerSelected = sharedPref.getInt("resolutionSpinnerSelected", 0);
        photoAreaSpinnerSelected = sharedPref.getInt("photoAreaSpinnerSelected", 0);
        heightFromLentsNumberPickerSelected = sharedPref.getInt("heightFromLentsNumberPickerSelected", 12);
        weights[0] = Double.longBitsToDouble(sharedPref.getLong(WEIGHT_LOGISTIC_R, 1));
        weights[1] = Double.longBitsToDouble(sharedPref.getLong(WEIGHT_LOGISTIC_G, 1));
        weights[2] = Double.longBitsToDouble(sharedPref.getLong(WEIGHT_LOGISTIC_B, 1));
        weights[3] = Double.longBitsToDouble(sharedPref.getLong(WEIGHT_LOGISTIC_GRAY, 1));
        weights[4] = Double.longBitsToDouble(sharedPref.getLong(WEIGHT_LOGISTIC_MEANB, 1));
        error = sharedPref.getLong("error", 0);
    }


    /***
     * Class that will be used to train with the logistic functions
     */
    private class logisticPixels extends AsyncTask<Void, String, Integer> {

        private Context context;
        private Logistic logistic;
        private double equal;
        private double n;
        private ProgressDialog progress;

        @Override
        protected Integer doInBackground(Void... params) {
            //List<Instance> instances = readDataSet("dataset.txt");
            publishProgress("Trainning...");
            HashMap<String, double[]> trainLogistic = Logistic.trainLogisticModel(context);
            weights = trainLogistic.get(Logistic.WEIGHT);
            double[] precision = trainLogistic.get(Logistic.PRECISION);
            n = precision[0];
            equal = precision[1];

            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            progress.setMessage(params[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context = TelaConfiguracao.this;
            progress = new ProgressDialog(context);
            progress.setMessage("Starting...");
            progress.show();

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progress.dismiss();
            error = Double.doubleToLongBits(equal/ n);
            percentageErrorLogisticTextView.setText("TRAINED");
            saveScreen();
        }
    }

}
