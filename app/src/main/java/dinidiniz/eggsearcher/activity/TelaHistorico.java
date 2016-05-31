package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.database.Cursor;
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
    int setTextSize = 21;
    String TAG = "TelaHistorico";
    private TextView textSingleDescription;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_historico);

        LinearLayout headHistorico = (LinearLayout) findViewById(R.id.headHistorico);

        final LinearLayout.LayoutParams lParamsLinearLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsTextView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lParamsTextViewCode = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        db = new DBHelper(this);
        allSamples = db.getAllSamples();
        Collections.reverse(allSamples);

        for (ArrayList sample:allSamples){
            LinearLayout linearLayoutSingleSampleTotal = new LinearLayout(this);
            linearLayoutSingleSampleTotal.setOrientation(LinearLayout.VERTICAL);
            linearLayoutSingleSampleTotal.setBackgroundResource(R.drawable.border);
            linearLayoutSingleSampleTotal.setLayoutParams(lParamsLinearLayout);
            linearLayoutSingleSampleTotal.setPadding(12, 12, 12, 12);
            linearLayoutSingleSampleTotal.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    LinearLayout thisView = (LinearLayout) view;
                    if (thisView.getChildCount() == 1) {
                        LinearLayout linearLayout = (LinearLayout) thisView.getChildAt(0);
                        TextView thisIdView = (TextView) linearLayout.getChildAt(0);
                        int thisIdInt = Integer.parseInt(thisIdView.getText().toString());
                        Cursor thisResult = db.getData(thisIdInt);
                        String thisDescription = "";
                        if (thisResult.moveToFirst()) {
                            thisDescription = thisResult.getString(thisResult.getColumnIndex("description"));
                        }
                        thisResult.close();

                        textSingleDescription = new TextView(getApplicationContext());
                        textSingleDescription.setLayoutParams(lParamsLinearLayout);
                        textSingleDescription.setText("Description: \n   " + thisDescription);
                        textSingleDescription.setTextColor(Color.BLACK);
                        textSingleDescription.setTextSize(setTextSize);
                        textSingleDescription.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        textSingleDescription.setGravity(Gravity.LEFT);

                        thisView.addView(textSingleDescription);
                    } else {
                        TextView thisTextView = (TextView) thisView.getChildAt(1);
                        thisView.removeView(thisTextView);
                    }

                }
            });


            LinearLayout linearLayoutSingleSample = new LinearLayout(this);
            linearLayoutSingleSample.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutSingleSample.setLayoutParams(lParamsLinearLayout);
            linearLayoutSingleSample.setOrientation(LinearLayout.HORIZONTAL);
            linearLayoutSingleSample.setPadding(12, 12, 12, 12);

            TextView textSingleSampleId = new TextView(this);
            textSingleSampleId.setLayoutParams(lParamsTextView);
            textSingleSampleId.setText((String) sample.get(0));
            textSingleSampleId.setTextColor(Color.BLACK);
            textSingleSampleId.setTextSize(setTextSize);
            linearLayoutSingleSample.addView(textSingleSampleId);

            TextView textSingleSampleCode = new TextView(this);
            textSingleSampleCode.setLayoutParams(lParamsTextViewCode);
            textSingleSampleCode.setText((String) sample.get(1));
            textSingleSampleCode.setTextColor(Color.BLACK);
            textSingleSampleCode.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            textSingleSampleCode.setGravity(Gravity.CENTER);
            textSingleSampleCode.setTextSize(setTextSize);
            linearLayoutSingleSample.addView(textSingleSampleCode);

            TextView textSingleSampleEggs = new TextView(this);
            textSingleSampleEggs.setLayoutParams(lParamsTextView);
            textSingleSampleEggs.setText((String) sample.get(2));
            textSingleSampleEggs.setTextColor(Color.BLACK);
            textSingleSampleEggs.setTextSize(setTextSize);
            linearLayoutSingleSample.addView(textSingleSampleEggs);

            linearLayoutSingleSampleTotal.addView(linearLayoutSingleSample);

            headHistorico.addView(linearLayoutSingleSampleTotal);
        }
    }
}
