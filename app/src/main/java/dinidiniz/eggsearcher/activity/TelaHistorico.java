package dinidiniz.eggsearcher.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import dinidiniz.eggsearcher.Consts;
import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.SQL.DBHelper;
import dinidiniz.eggsearcher.adapters.HistoricAdapter;
import dinidiniz.eggsearcher.adapters.HistoricCal;

/**
 * Created by leon on 14/11/15.
 */
public class TelaHistorico extends Activity {

    private DBHelper db;
    private ListView adapterListView;
    String TAG = "TelaHistorico";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_historico);

        db = new DBHelper(this);
        adapterListView = (ListView) findViewById(R.id.historicListView);

        //Starts adapter
        HistoricAdapter adapterHistoric = new HistoricAdapter(db.getAllSamples(), this);
        adapterListView.setAdapter(adapterHistoric);

    }

    public void sendEmail(View view){
        Resources res = getResources();
        DBHelper db = new DBHelper(this);
        File attachment = db.exportDB();

        String subject = res.getString(R.string.email_subject);
        String text = res.getString(R.string.email_text);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));

        startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public void deleteTableSamples(View view){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DBHelper db = new DBHelper(getApplicationContext());
                        db.deleteAllPixelsFromTable(db.SAMPLES_TABLE_NAME);
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.deleted_data), Toast.LENGTH_LONG).show();

                        //Restarts adapter
                        HistoricAdapter adapterHistoric = new HistoricAdapter(db.getAllSamples(), TelaHistorico.this);
                        adapterListView.setAdapter(adapterHistoric);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage(getResources().getText(R.string.delete_question)).setPositiveButton(getResources().getText(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getText(R.string.no), dialogClickListener).show();


    }
}
