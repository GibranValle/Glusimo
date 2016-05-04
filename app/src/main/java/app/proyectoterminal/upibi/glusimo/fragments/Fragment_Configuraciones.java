package app.proyectoterminal.upibi.glusimo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.proyectoterminal.upibi.glusimo.R;

/**
 * ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class Fragment_Configuraciones extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    Button aceptar, cancelar;
    SharedPreferences respaldo;
    SharedPreferences.Editor editor;
    TextView titulo;
    EditText editMax, editHipo, editHiper, editSuperHiper;
    CheckBox demo_medicion;
    Intent i;
    int posicion, hipoglucemia, hiperglucemia, hiperglucemia_severa, max;
    boolean dm;
    String texto;
    FrameLayout frame_medicion, frame_registro, frame_curva;

    final static String TAG = "Configuracion";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_configuracion_detalles);

        // cargar los resources del XML
        aceptar = (Button) findViewById(R.id.button_aceptar);
        cancelar = (Button) findViewById(R.id.button_cancelar);
        frame_medicion = (FrameLayout) findViewById(R.id.frame_medicion);
        frame_registro = (FrameLayout) findViewById(R.id.frame_registro);
        frame_curva = (FrameLayout) findViewById(R.id.frame_curva);
        titulo = (TextView) findViewById(R.id.titulo_fragment_config);
        demo_medicion = (CheckBox) findViewById(R.id.cb_medicion);
        editMax = (EditText) findViewById(R.id.edit_max);
        editHipo = (EditText) findViewById(R.id.edit_hipo);
        editHiper = (EditText) findViewById(R.id.edit_hiper);
        editSuperHiper = (EditText) findViewById(R.id.edit_hiper_max);

        // recuperar la configuracion previa
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);

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

                // cargar solo los datos necesarios
                dm = respaldo.getBoolean("demo_medicion",false);
                hipoglucemia = respaldo.getInt("hipo",70);
                hiperglucemia = respaldo.getInt("hiper",120);
                hiperglucemia_severa = respaldo.getInt("hiper_severa",200);
                max = respaldo.getInt("max", 250);

                // asignar los datos cargados
                demo_medicion.setChecked(dm);
                editMax.setText(""+max);
                editHipo.setText(""+hipoglucemia);
                editHiper.setText(""+hiperglucemia);
                editSuperHiper.setText(""+hiperglucemia_severa);
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


        // Asignar los listeners
        aceptar.setOnClickListener(this);
        cancelar.setOnClickListener(this);
        demo_medicion.setOnCheckedChangeListener(this);

        // carga los datos en el edit text

    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if(id == R.id.button_aceptar)
        {
            switch (posicion)
            {
                case 0:
                    // tomar los datos del edittext y guardarlos
                    dm = demo_medicion.isChecked();
                    hipoglucemia = Integer.parseInt(editHipo.getText().toString());
                    hiperglucemia = Integer.parseInt(editHiper.getText().toString());
                    hiperglucemia_severa = Integer.parseInt(editSuperHiper.getText().toString());
                    max = Integer.parseInt(editMax.getText().toString());
                    editor = respaldo.edit();
                    editor.putInt("max", max);
                    editor.putInt("hipo", hipoglucemia);
                    editor.putInt("hiper", hiperglucemia);
                    editor.putInt("hiper_severa", hiperglucemia_severa);
                    editor.putBoolean("demo_medicion", dm);
                    if(editor.commit())
                    {
                        Log.d(TAG,"guardado");
                        Toast.makeText(this,R.string.exito, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            finish();
        }
        if(id == R.id.button_aceptar)
        {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {

    }
}
