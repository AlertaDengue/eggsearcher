package dinidiniz.eggsearcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import dinidiniz.eggsearcher.R;

/**
 * Created by leon on 23/08/15.
 */
public class TelaInicial extends AppCompatActivity {

    private Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

    }

    public void tirarFoto(View view){
        intent = new Intent(this,TelaFotografia.class);
        intent.putExtra(TelaFotografia.GO_TO_INTENT, TelaFotografia.GO_TO_COUNT);
        startActivity(intent);
    }

    public void upload(View view){
        Intent intent = new Intent(this, TelaFilePicker.class);
        startActivity(intent);
    }

    public void historic(View view){
        Intent intent = new Intent(this, TelaHistorico.class);
        startActivity(intent);
    }

    public void configuracoes(View view){
        Intent intent = new Intent(this, TelaConfiguracao.class);
        startActivity(intent);
    }

    public void calibrate(View view){
        intent = new Intent(this,TelaFotografia.class);
        intent.putExtra(TelaFotografia.GO_TO_INTENT, TelaFotografia.GO_TO_CALIBRATE);
        startActivity(intent);
    }

    public void histCalibrate(View view){
        intent = new Intent(this, TelaHistoricoCal.class);
        startActivity(intent);
    }
}
