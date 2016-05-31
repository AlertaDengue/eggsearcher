package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.util.List;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.functions.CameraPreview;

/**
 * Created by leon on 15/11/15.
 */
public class TelaConfiguracao extends Activity{

    private Spinner thresholdSpinner;
    private Spinner processSpinner;
    private Spinner resolutionSpinner;
    private NumberPicker heightFromLentsNumberPicker;
    private int thresholdSpinnerSelected;
    private int processSpinnerSelected;
    private int heightFromLentsNumberPickerSelected;
    private int resolutionSpinnerSelected;
    private Resources res;
    private CheckBox flashCheckBox;
    private Boolean flashChackBoxSelected;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_configuracao);

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


        //HEIGHT NUMBER PICKER

        heightFromLentsNumberPicker = (NumberPicker) findViewById(R.id.heightFromLents);
        heightFromLentsNumberPicker.setMaxValue(15);
        heightFromLentsNumberPicker.setMinValue(8);
        heightFromLentsNumberPicker.setValue(heightFromLentsNumberPickerSelected);


    }

    @Override
    protected void onPause(){
        super.onPause();
        saveScreen();
    }

    public void saveScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("thresholdSpinnerSelected", thresholdSpinner.getSelectedItemPosition());
        editor.putInt("processSpinnerSelected", processSpinner.getSelectedItemPosition());
        editor.putInt("heightFromLentsNumberPickerSelected", heightFromLentsNumberPicker.getValue());
        editor.putInt("resolutionSpinnerSelected", resolutionSpinner.getSelectedItemPosition());
        editor.commit();
    }

    public void loadScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        thresholdSpinnerSelected = sharedPref.getInt("thresholdSpinnerSelected", 0);
        processSpinnerSelected = sharedPref.getInt("processSpinnerSelected", 0);
        resolutionSpinnerSelected = sharedPref.getInt("resolutionSpinnerSelected", 0);
        heightFromLentsNumberPickerSelected = sharedPref.getInt("heightFromLentsNumberPickerSelected", 12);
    }

}
