package dinidiniz.eggsearcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by leon on 23/08/15.
 */
public class TelaInicial extends AppCompatActivity {

    Intent intent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);

    }

    public void tirarFoto(View view){
        intent = new Intent(this,AtividadePrincipal.class);
        startActivity(intent);
    }

    public void upload(View view){
        Intent intent = new Intent(this, FilePicker.class);
        startActivity(intent);
    }
}
