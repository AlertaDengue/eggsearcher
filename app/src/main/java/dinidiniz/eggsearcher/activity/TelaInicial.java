package dinidiniz.eggsearcher.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dinidiniz.eggsearcher.App;
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
    private Tracker mTracker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

        // Obtain the shared Tracker instance.
        App application = (App) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        askPermissions();

        saveUserData();
    }

    /**
     * Function to ask all permissions needed
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void askPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 200);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 200);
        }

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION}, 200 );
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION}, 200 );
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.CAMERA}, 200 );
        }
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.FLASHLIGHT) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.FLASHLIGHT}, 200 );
        }
    }

    public void tirarFoto(View view) {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.FLASHLIGHT) == PackageManager.PERMISSION_GRANTED ) {
            intent = new Intent(view.getContext(), TelaFotografia.class);
            intent.putExtra(TelaFotografia.GO_TO_INTENT, TelaFotografia.GO_TO_COUNT);
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getText(R.string.permissions_photo), Toast.LENGTH_SHORT).show();
        }
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

    public void sendData(View view) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Send Data")
                .build());


        Intent intent = new Intent(this, TelaResultado.class);
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

    public void saveUserData(){
        //Save user data;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Consts.user_email, "email");
        editor.putString(Consts.user_id, "id");
        editor.putString(Consts.user_name, "name");
        editor.putString(Consts.user_familyname, "family name");
        editor.putString(Consts.user_photourl, "photo");
        editor.apply();
    }

}
