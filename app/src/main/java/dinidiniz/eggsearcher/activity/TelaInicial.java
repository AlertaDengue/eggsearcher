package dinidiniz.eggsearcher.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dinidiniz.eggsearcher.Consts;
import dinidiniz.eggsearcher.R;
import dinidiniz.eggsearcher.helper.Logistic;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * Created by leon on 23/08/15.
 */
public class TelaInicial extends AppCompatActivity {

    private static final String TAG = TelaInicial.class.getName();
    private static final int SELECT_GO_TO_CALIBRATE = 100;
    private static final int SELECT_GO_TO_COUNT = 200;
    private Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

        askPermissions();
    }

    public void askPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION}, 200 );
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION}, 200 );
        }
    }

    public void tirarFoto(View view) {

        intent = new Intent(view.getContext(), TelaFotografia.class);
        intent.putExtra(TelaFotografia.GO_TO_INTENT, TelaFotografia.GO_TO_COUNT);
        startActivity(intent);
    }


    /***
     * Activity result from choosing a picture
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     *
     * ***/
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(TAG, "On activity result" );
        if (resultCode == RESULT_OK) {
        }
        }
            /*
                new AsyncTask<Void, Void, Void>() {
                    private ProgressDialog progressDialog;

                    @Override
                    protected void onPreExecute(){
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(TelaInicial.this);
                        progressDialog.setMessage("Getting data");
                        progressDialog.show();
                    }


                    @Override
                    protected Void doInBackground(Void... params) {

                        InputStream is = null;
                        Bitmap bitmap = null;
                        try {

                            is = getContentResolver().openInputStream(imageReturnedIntent.getData());

                            bitmap = BitmapFactory.decodeStream(is);
                            if (is != null) {
                                    is.close();
                            }


                            File mypath = getFileStreamPath("tempIMG.png");
                            mypath.createNewFile();

                            FileOutputStream fos = new FileOutputStream(mypath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , fos);
                            fos.flush();
                            fos.close();
                            *

                            saveScreen();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressDialog.dismiss();
                        switch (requestCode) {
                            case SELECT_GO_TO_COUNT:
                                intent = new Intent(TelaInicial.this, TelaContagem.class);
                                intent.putExtra(Consts.UPLOADED_PHOTO, true);
                                startActivity(intent);
                                break;
                            case SELECT_GO_TO_CALIBRATE:
                                intent = new Intent(TelaInicial.this, CalibrateActivity.class);
                                intent.putExtra(Consts.UPLOADED_PHOTO, true);
                                startActivity(intent);
                                break;
                        }
                    }

                }.execute();
        }
    }
                    ***/

    public void upload(View view) {

        intent = new Intent(TelaInicial.this, TelaContagem.class);
        intent.putExtra(Consts.UPLOADED_PHOTO, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    public void historic(View view) {
        Intent intent = new Intent(this, TelaHistorico.class);
        startActivity(intent);
    }

    public void configuracoes(View view) {
        Intent intent = new Intent(this, TelaConfiguracao.class);
        startActivity(intent);
    }

    /***
     * Function that determinates what the user do after clicking calibrate
     *
     * @param view
     */
    public void calibrate(final View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        intent = new Intent(view.getContext(), TelaFotografia.class);
                        intent.putExtra(TelaFotografia.GO_TO_INTENT, TelaFotografia.GO_TO_CALIBRATE);
                        startActivity(intent);
                        //Photo button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        intent = new Intent(TelaInicial.this, CalibrateActivity.class);
                        intent.putExtra(Consts.UPLOADED_PHOTO, true);
                        startActivity(intent);
                        //Upload button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("How to procede?").setPositiveButton("Take a picture", dialogClickListener)
                .setNegativeButton("Upload", dialogClickListener).show();

    }

    public void histCalibrate(View view) {
        intent = new Intent(this, TelaHistoricoCal.class);
        startActivity(intent);
    }

    public void saveScreen() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("imagepath", getFileStreamPath("tempIMG.png").getAbsolutePath());
        editor.commit();
    }
}
