package dinidiniz.eggsearcher.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.adapters.HistoricCal;

/**
 * Created by leon on 25/05/16.
 */
public class TelaHistoricoCal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_cal);

        final DBHelper dbHelper = new DBHelper(this);
        final ListView listView = (ListView) findViewById(R.id.listViewHistoricCal);
        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButtonHistoricalCal);


        HistoricCal adapterHistoric = new HistoricCal(dbHelper.getAllPixeis(), this);
        listView.setAdapter(adapterHistoric);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton.isChecked()) {
                    HistoricCal adapterHistoric = new HistoricCal(dbHelper.getAllContour(), TelaHistoricoCal.this);
                    listView.setAdapter(adapterHistoric);
                } else {
                    HistoricCal adapterHistoric = new HistoricCal(dbHelper.getAllPixeis(), TelaHistoricoCal.this);
                    listView.setAdapter(adapterHistoric);
                }
            }
        });

    }
}
