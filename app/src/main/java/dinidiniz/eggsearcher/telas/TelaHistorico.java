package dinidiniz.eggsearcher.telas;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;

/**
 * Created by leon on 14/11/15.
 */
public class TelaHistorico extends Activity {

    DBHelper db;
    ArrayList<ArrayList> allSamples;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_historico);

        LinearLayout headHistorico = (LinearLayout) findViewById(R.id.headHistorico);

        LinearLayout.LayoutParams lParamsLinearLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsTextView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsTextViewCode = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        db = new DBHelper(this);
        allSamples = db.getAllSamples();
        Collections.reverse(allSamples);

        for (ArrayList sample:allSamples){
            LinearLayout linearLayoutSingleSample = new LinearLayout(this);
            linearLayoutSingleSample.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutSingleSample.setBackgroundResource(R.drawable.border);
            linearLayoutSingleSample.setLayoutParams(lParamsLinearLayout);
            linearLayoutSingleSample.setPadding(12, 12, 12, 12);

            TextView textSingleSampleId = new TextView(this);
            textSingleSampleId.setLayoutParams(lParamsTextView);
            textSingleSampleId.setText((String) sample.get(0));
            textSingleSampleId.setTextColor(Color.BLACK);
            textSingleSampleId.setTextSize(21);
            linearLayoutSingleSample.addView(textSingleSampleId);

            TextView textSingleSampleCode = new TextView(this);
            textSingleSampleCode.setLayoutParams(lParamsTextViewCode);
            textSingleSampleCode.setText((String) sample.get(1));
            textSingleSampleCode.setTextColor(Color.BLACK);
            textSingleSampleCode.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            textSingleSampleCode.setGravity(Gravity.CENTER);
            textSingleSampleCode.setTextSize(21);
            linearLayoutSingleSample.addView(textSingleSampleCode);

            TextView textSingleSampleEggs = new TextView(this);
            textSingleSampleEggs.setLayoutParams(lParamsTextView);
            textSingleSampleEggs.setText((String) sample.get(2));
            textSingleSampleEggs.setTextColor(Color.BLACK);
            textSingleSampleEggs.setTextSize(21);
            linearLayoutSingleSample.addView(textSingleSampleEggs);

            headHistorico.addView(linearLayoutSingleSample);
        }
    }
}
