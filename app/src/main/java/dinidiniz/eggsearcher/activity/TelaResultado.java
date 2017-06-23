package dinidiniz.eggsearcher.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import dinidiniz.eggsearcher.Consts;
import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.helper.Coordinates;

/**
 * Created by leon on 11/11/15.
 */
public class TelaResultado extends Activity {

    private int numberOfEggs;
    private int totalNumberOfEggs;
    private int sampleNumber;
    private int areaTotal;
    private long dateOnField;
    private int resolutionHeight = 0;
    private int resolutionWidth = 0;
    private int height = 0;
    private final String TAG = TelaResultado.class.getName();
    private Intent intent;
    private int number = 1;
    private Set<String> numberOfEggsSamples = new HashSet<String>(1);
    private String filePath;


    private LinearLayout linearLayoutSamples;
    private NumberPicker totalNumberOfEggsView;
    private EditText codeResult;
    private EditText descriptionResult;
    private EditText dateEditText;
    private CalendarView resultCalendarView;




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_resultado);
        loadScreen();

        codeResult = (EditText) findViewById(R.id.codeResult);
        descriptionResult = (EditText) findViewById(R.id.descriptionResult);
        resultCalendarView = (CalendarView) findViewById(R.id.resultCalendarView);

        //Set Calendar
        Calendar c = Calendar.getInstance();
        resultCalendarView.setDate(c.getTimeInMillis(), false, true);
        resultCalendarView.setMaxDate(c.getTimeInMillis());
        resultCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView arg0, int year, int month,
                                            int date) {
                Calendar cal1 = Calendar.getInstance();
                cal1.set(year, month, date);
                dateOnField = cal1.getTimeInMillis();
            }
        });


        descriptionResult.setText(areaTotal + " area");

        TextView textSampleNumber = (TextView) findViewById(R.id.textSampleNumber);
        textSampleNumber.setText("Sample Number " + sampleNumber);

        //Adding every photo database
        linearLayoutSamples = (LinearLayout) findViewById(R.id.samplesResults);
        LinearLayout.LayoutParams lParamsLinearLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsTextView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsNumberPicker = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(R.dimen.number_picker_height));
        LinearLayout.LayoutParams lParamsButton = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.button_width),(int) getResources().getDimension(R.dimen.button_width));

        for(String sample:numberOfEggsSamples) {
            LinearLayout linearLayoutSingleSample = new LinearLayout(this);
            linearLayoutSingleSample.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutSingleSample.setLayoutParams(lParamsLinearLayout);
            linearLayoutSingleSample.setGravity(Gravity.CENTER);
            linearLayoutSingleSample.setPadding(24, 24, 24, 24);

            final Button destroyButton = new Button(this);
            destroyButton.setLayoutParams(lParamsButton);
            destroyButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    LinearLayout parent = (LinearLayout) v.getParent();
                    NumberPicker numberPicker = (NumberPicker) parent.getChildAt(2);
                    numberOfEggsSamples.remove("" + numberPicker.getValue());
                    totalNumberOfEggsView.setValue(totalNumberOfEggsView.getValue() - numberPicker.getValue());
                    linearLayoutSamples.removeView((LinearLayout) v.getParent());
                }

            });
            destroyButton.setText("-");
            destroyButton.setGravity(Gravity.CENTER);
            linearLayoutSingleSample.addView(destroyButton);

            TextView textSingleSampleNumber = new TextView(this);
            textSingleSampleNumber.setLayoutParams(lParamsTextView);
            textSingleSampleNumber.setText("Number of eggs [" + number + "]:");
            textSingleSampleNumber.setTextColor(Color.BLACK);
            textSingleSampleNumber.setTextSize(21);
            linearLayoutSingleSample.addView(textSingleSampleNumber);

            NumberPicker numberPickerSingleSample = new NumberPicker(this);
            numberPickerSingleSample.setLayoutParams(lParamsNumberPicker);
            numberPickerSingleSample.setMaxValue(10000000);
            numberPickerSingleSample.setMinValue(0);
            numberPickerSingleSample.setBackgroundResource(R.drawable.edittextborder);
            numberPickerSingleSample.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            numberPickerSingleSample.setValue(Integer.parseInt(sample));
            linearLayoutSingleSample.addView(numberPickerSingleSample);

            linearLayoutSamples.addView(linearLayoutSingleSample);

            number += 1;
            totalNumberOfEggs += Integer.parseInt(sample);

        }

        totalNumberOfEggsView = (NumberPicker) findViewById(R.id.totalNumberOfEggs);
        totalNumberOfEggsView.setMaxValue(1000000);
        totalNumberOfEggsView.setMinValue(0);
        totalNumberOfEggsView.setValue(totalNumberOfEggs);


    }

    public void loadScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        numberOfEggs = sharedPref.getInt("numberOfEggs", 0);
        sampleNumber = sharedPref.getInt("sampleNumber", 1);
        areaTotal = sharedPref.getInt("areaTotal", 0);
        numberOfEggsSamples = sharedPref.getStringSet("numberOfEggsSamples", numberOfEggsSamples);
        filePath = sharedPref.getString(Consts.imagepath, "/");
        getHeightAndMP(filePath);
        numberOfEggsSamples.add("" + numberOfEggs);
    }

    private void getHeightAndMP(String filePath){
        String fileName = filePath.split("/")[filePath.split("/").length -1];
        String[] fileInfos = fileName.split("_");
        Log.i(TAG, fileName + " lenght: " + fileInfos.length );
        if (fileInfos.length  == 6) {
            Log.i(TAG, "parse: "  + Integer.parseInt(fileInfos[2]));
            height = Integer.parseInt(fileInfos[3]);
            resolutionHeight = Integer.parseInt(fileInfos[1]);
            resolutionWidth = Integer.parseInt(fileInfos[2]);
        }


    }

    public void saveScreenAnotherPicture(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", numberOfEggsSamples);
        editor.apply();
    }

    public void saveScreenBackPress(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", new HashSet<String>(1));
        editor.apply();

    }

    public void anotherPicture(View view){
        saveScreenAnotherPicture();
        intent = new Intent(this,TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    /***
     * Send to get coordinates and save in the db
     */
    public void getCoordinates(){
        new Coordinates(this, codeResult.getText().toString(),
                totalNumberOfEggsView.getValue(),descriptionResult.getText().toString(),dateOnField , sampleNumber, areaTotal, height, resolutionHeight, resolutionWidth);
    }

    public void saveResult(View view){
        getCoordinates();
        intent = new Intent(this,TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    public void onBackPressed() {
        saveScreenBackPress();
        intent = new Intent(this, TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
