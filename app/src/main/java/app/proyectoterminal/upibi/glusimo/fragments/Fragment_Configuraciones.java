package app.proyectoterminal.upibi.glusimo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.proyectoterminal.upibi.glusimo.R;

/**
 * ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class Fragment_Configuraciones extends Activity implements View.OnClickListener {

    Button aceptar, cancelar;
    SharedPreferences respaldo;
    TextView titulo;
    EditText b;
    CheckBox c;
    Intent i;
    int posicion;
    String texto;
    FrameLayout frame_medicion, frame_registro, frame_curva;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_configuracion_detalles);

        // cargar xml
        frame_medicion = (FrameLayout) findViewById(R.id.frame_medicion);
        frame_registro = (FrameLayout) findViewById(R.id.frame_registro);
        frame_curva = (FrameLayout) findViewById(R.id.frame_curva);
        titulo = (TextView) findViewById(R.id.titulo_fragment_config);


        // recuperar datos desde el activity
        i = getIntent();
        posicion = i.getIntExtra("linea",-1);

        switch (posicion)
        {
            case 0:
                frame_medicion.setVisibility(View.VISIBLE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.GONE);
                texto = getResources().getString(R.string.titulo_medicion);
                titulo.setText(texto);
                break;
            case 1:
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.VISIBLE);
                frame_curva.setVisibility(View.GONE);
                texto = getResources().getString(R.string.titulo_registro);
                titulo.setText(texto);
                break;
            case 2:
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.VISIBLE);
                texto = getResources().getString(R.string.titulo_medicion);
                titulo.setText(texto);
                break;
        }

        // cargar los resources del XML
        aceptar = (Button) findViewById(R.id.button_aceptar);
        cancelar = (Button) findViewById(R.id.button_cancelar);

        aceptar.setOnClickListener(this);
        cancelar.setOnClickListener(this);

        // carga los datos en el edit text

    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if(id == R.id.button_aceptar)
        {
            finish();
        }
        if(id == R.id.button_aceptar)
        {
            finish();
        }
    }
}
