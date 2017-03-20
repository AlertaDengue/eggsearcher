package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;

/**
 * Created by leon on 11/11/15.
 */
public class TelaResultado extends Activity {

    int numberOfEggs;
    int totalNumberOfEggs;
    int sampleNumber;
    int areaTotal;
    String TAG = "TelaResultado";
    Intent intent;
    int number = 1;
    Set<String> numberOfEggsSamples = new HashSet<String>(1);


    LinearLayout linearLayoutSamples;
    NumberPicker totalNumberOfEggsView;
    EditText codeResult;
    EditText descriptionResult;


    DBHelper db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_resultado);
        loadScreen();

        codeResult = (EditText) findViewById(R.id.codeResult);
        descriptionResult = (EditText) findViewById(R.id.descriptionResult);

        descriptionResult.setText(areaTotal + " area\n\n");

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
        totalNumberOfEggsView.setMaxValue(1000000000);
        totalNumberOfEggsView.setMinValue(0);
        totalNumberOfEggsView.setValue(totalNumberOfEggs);


    }

    public void loadScreen(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        numberOfEggs = sharedPref.getInt("numberOfEggs", 0);
        sampleNumber = sharedPref.getInt("sampleNumber", 1);
        areaTotal = sharedPref.getInt("areaTotal", 0);
        numberOfEggsSamples = sharedPref.getStringSet("numberOfEggsSamples", numberOfEggsSamples);
        numberOfEggsSamples.add("" + numberOfEggs);
    }

    public void saveScreenAnotherPicture(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", numberOfEggsSamples);
        editor.commit();
    }

    public void saveScreenResult(){
        db = new DBHelper(this);
        db.insertSample(codeResult.getText().toString(),totalNumberOfEggsView.getValue(),descriptionResult.getText().toString());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", new HashSet<String>(1));
        editor.putInt("sampleNumber", sampleNumber + 1);
        editor.commit();

    }

    public void saveScreenBackPress(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("numberOfEggsSamples", new HashSet<String>(1));
        editor.commit();

    }

    public void anotherPicture(View view){
        saveScreenAnotherPicture();
        intent = new Intent(this,TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    public void saveResult(View view){
        saveScreenResult();
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
