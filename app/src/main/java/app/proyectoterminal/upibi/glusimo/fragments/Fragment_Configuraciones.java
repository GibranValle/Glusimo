package app.proyectoterminal.upibi.glusimo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

/**
 * ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class Fragment_Configuraciones extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private Button aceptar, cancelar, eliminar;
    private SharedPreferences respaldo;
    private SharedPreferences.Editor editor;
    private TextView titulo;
    private EditText editMax, editHipo, editHiper, editSuperHiper, editFrec;
    private EditText editLA, editLB, editLC, editLD, editLE, editLF, editLG, editAjuste;
    private CheckBox demo_medicion, monitorizar_cb, ajustar;
    private Intent i;
    int posicion, hipoglucemia, hiperglucemia, hiperglucemia_severa, max, frec;
    int LA, LB, LC, LD, LE, LF, LG, ajuste;
    boolean dm, monitorizar;
    private String texto;
    private FrameLayout frame_medicion, frame_registro, frame_curva, frame_monitor, frame_dosificador;
    private Spinner spinner;
    int posicionSpinner;
    private DataBaseManager manager;
    EventBus bus = EventBus.getDefault();
    SeekBar barra;
    boolean ajustando;

    private static final int curvaGlucosaNormal[] = {84, 130, 127, 100, 85, 82, 80};
    private static final int curvaGlucosaPrediabetes[] = {90, 169, 160, 134, 143, 121,99};
    private static final int curvaGlucosaDiabetes[] = {84, 160, 220, 200, 186, 168, 150};

    final static String TAG = "Configuracion";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus  el fragment medición");
            bus.register(this);
        }

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_configuracion_detalles);
        manager = new DataBaseManager(this);

        // cargar los resources del XML
        aceptar = (Button) findViewById(R.id.button_aceptar);
        cancelar = (Button) findViewById(R.id.button_cancelar);
        eliminar = (Button) findViewById(R.id.boton_eliminar_db);

        frame_medicion = (FrameLayout) findViewById(R.id.frame_medicion);
        frame_registro = (FrameLayout) findViewById(R.id.frame_registro);
        frame_curva = (FrameLayout) findViewById(R.id.frame_curva);
        frame_monitor = (FrameLayout) findViewById(R.id.frame_monitor);
        frame_dosificador = (FrameLayout) findViewById(R.id.frame_dosificador);
        titulo = (TextView) findViewById(R.id.titulo_fragment_config);

        demo_medicion = (CheckBox) findViewById(R.id.cb_medicion);
        monitorizar_cb = (CheckBox) findViewById(R.id.cb_monitor);
        ajustar = (CheckBox) findViewById(R.id.cb_ajuste);

        barra = (SeekBar) findViewById(R.id.slider);

        editMax = (EditText) findViewById(R.id.edit_max);
        editHipo = (EditText) findViewById(R.id.edit_hipo);
        editHiper = (EditText) findViewById(R.id.edit_hiper);
        editSuperHiper = (EditText) findViewById(R.id.edit_hiper_max);
        editLA = (EditText) findViewById(R.id.edit_lecturaA);
        editLB = (EditText) findViewById(R.id.edit_lecturaB);
        editLC = (EditText) findViewById(R.id.edit_lecturaC);
        editLD = (EditText) findViewById(R.id.edit_lecturaD);
        editLE = (EditText) findViewById(R.id.edit_lecturaE);
        editLF = (EditText) findViewById(R.id.edit_lecturaF);
        editLG = (EditText) findViewById(R.id.edit_lecturaG);
        editFrec = (EditText) findViewById(R.id.edit_frec);
        editAjuste = (EditText) findViewById(R.id.edit_ajuste);
        spinner = (Spinner) findViewById(R.id.spinner_curvas);

        // recuperar la configuracion previa
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);

        // recuperar datos desde el activity
        i = getIntent();
        posicion = i.getIntExtra("linea",-1);

        // Creación del spinner
        List<String> mediciones = new ArrayList<>();
        ArrayAdapter<String> adapter;
        // agregar elementos al spinner
        mediciones.add("Seleccione una curva:");
        mediciones.add(getResources().getString(R.string.curvadiabetes));
        mediciones.add(getResources().getString(R.string.curvapre));
        mediciones.add(getResources().getString(R.string.curvasano));
        mediciones.add(getResources().getString(R.string.curvapersonalizada));
        // agregar listas y layouts al adapter
        adapter = new ArrayAdapter<>
                (this, R.layout.custom_drop_spinner_layout,mediciones);
        adapter.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        // cargar el adapter al spinner
        spinner.setAdapter(adapter);
        // agregar listener al spinner
        spinner.setOnItemSelectedListener(this);

        switch (posicion)
        {
            case 0: // MEDICION
                Log.i(TAG,"medicion");
                frame_dosificador.setVisibility(View.GONE);
                frame_medicion.setVisibility(View.VISIBLE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.GONE);
                frame_monitor.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
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
            case 1: // REGISTRO
                Log.i(TAG,"reg");
                frame_dosificador.setVisibility(View.GONE);
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.VISIBLE);
                frame_curva.setVisibility(View.GONE);
                frame_monitor.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                texto = getResources().getString(R.string.titulo_registro);
                titulo.setText(texto);

                break;
            case 2: // CURVA
                Log.i(TAG,"curva");
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.VISIBLE);
                frame_monitor.setVisibility(View.GONE);
                frame_dosificador.setVisibility(View.GONE);

                texto = getResources().getString(R.string.titulo_curva);
                titulo.setText(texto);
                break;
            case 3: // MONITOR
                frame_dosificador.setVisibility(View.GONE);
                Log.i(TAG,"monitor");
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.GONE);
                frame_monitor.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                texto = getResources().getString(R.string.titulo_monitor_config);
                titulo.setText(texto);

                // cada 5 minutos = 60/12 = 5 mins
                frec = respaldo.getInt("frec", 12);
                monitorizar = respaldo.getBoolean("monitorizar",true);
                editFrec.setText(""+frec);
                monitorizar_cb.setChecked(monitorizar);

            case 4: // DOSIFICADOR
                Log.i(TAG,"dosificador");
                frame_dosificador.setVisibility(View.VISIBLE);
                frame_medicion.setVisibility(View.GONE);
                frame_registro.setVisibility(View.GONE);
                frame_curva.setVisibility(View.GONE);
                frame_monitor.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                texto = getResources().getString(R.string.titulo_dosificador);
                titulo.setText(texto);
                ajustar.setVisibility(View.GONE);
                barra.setVisibility(View.GONE);

                // cada 5 minutos = 60/12 = 5 mins
                ajuste = respaldo.getInt("ajuste", 1);
                ajustando = respaldo.getBoolean("ajustando",false);
                editFrec.setText(""+frec);
                ajustar.setChecked(ajustando);
                barra.setProgress(ajuste*10);
        }


        // Asignar los listeners
        aceptar.setOnClickListener(this);
        cancelar.setOnClickListener(this);
        eliminar.setOnClickListener(this);
        demo_medicion.setOnCheckedChangeListener(this);
        barra.setOnSeekBarChangeListener(this);
        // carga los datos en el edit text

    }

    @Override
    public void onClick(View v)
    {
        vibrar(100);
        int id = v.getId();
        if(id == R.id.button_aceptar)
        {
            // switch para saber que panel de configuracion usar
            switch (posicion)
            {
                case 0:
                    Log.i(TAG,"configuracion de medicion");
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
                    bus.post(new EnviarStringEvent("F0"));
                    finish();
                    break;



                case 1:
                    Log.i(TAG,"configuracion de registro");
                    bus.post(new EnviarStringEvent("F1"));
                    finish();
                    break;

                case 2:
                    Log.i(TAG,"configuracion de curva");
                    editor = respaldo.edit();
                    switch (posicionSpinner)
                    {
                        case 0:
                            Toast.makeText(this, getResources().getString(R.string.error_spinner),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Log.i(TAG,"guardando curva diabetico");
                            // Copiar los valores del editText
                            LA = Integer.parseInt(editLA.getText().toString());
                            LB = Integer.parseInt(editLB.getText().toString());
                            LC = Integer.parseInt(editLC.getText().toString());
                            LD = Integer.parseInt(editLD.getText().toString());
                            LE = Integer.parseInt(editLE.getText().toString());
                            LF = Integer.parseInt(editLF.getText().toString());
                            LG = Integer.parseInt(editLG.getText().toString());
                            // guardar los datos en respaldo
                            editor.putInt("posicionSpinner", posicionSpinner);
                            editor.putInt("CDL0",LA);
                            editor.putInt("CDL1",LB);
                            editor.putInt("CDL2",LC);
                            editor.putInt("CDL3",LD);
                            editor.putInt("CDL4",LE);
                            editor.putInt("CDL5",LF);
                            editor.putInt("CDL6",LG);
                            if(editor.commit())
                            {
                                bus.post(new EnviarStringEvent("F3"));
                                Toast.makeText(this, R.string.exito, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            break;
                        case 2:
                            Log.i(TAG,"guardando curva prediabetico");
                            // Copiar los valores del editText
                            LA = Integer.parseInt(editLA.getText().toString());
                            LB = Integer.parseInt(editLB.getText().toString());
                            LC = Integer.parseInt(editLC.getText().toString());
                            LD = Integer.parseInt(editLD.getText().toString());
                            LE = Integer.parseInt(editLE.getText().toString());
                            LF = Integer.parseInt(editLF.getText().toString());
                            LG = Integer.parseInt(editLG.getText().toString());
                            // guardar los datos en respaldo
                            editor.putInt("posicionSpinner", posicionSpinner);
                            editor.putInt("CPDL0",LA);
                            editor.putInt("CPDL1",LB);
                            editor.putInt("CPDL2",LC);
                            editor.putInt("CPDL3",LD);
                            editor.putInt("CPDL4",LE);
                            editor.putInt("CPDL5",LF);
                            editor.putInt("CPDL6",LG);
                            if(editor.commit())
                            {
                                bus.post(new EnviarStringEvent("F3"));
                                Toast.makeText(this, R.string.exito, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            break;
                        case 3:
                            Log.i(TAG,"guardando curva sano");
                            // Copiar los valores del editText
                            LA = Integer.parseInt(editLA.getText().toString());
                            LB = Integer.parseInt(editLB.getText().toString());
                            LC = Integer.parseInt(editLC.getText().toString());
                            LD = Integer.parseInt(editLD.getText().toString());
                            LE = Integer.parseInt(editLE.getText().toString());
                            LF = Integer.parseInt(editLF.getText().toString());
                            LG = Integer.parseInt(editLG.getText().toString());
                            // guardar los datos en respaldo
                            editor.putInt("posicionSpinner", posicionSpinner);
                            editor.putInt("CSL0",LA);
                            editor.putInt("CSL1",LB);
                            editor.putInt("CSL2",LC);
                            editor.putInt("CSL3",LD);
                            editor.putInt("CSL4",LE);
                            editor.putInt("CSL5",LF);
                            editor.putInt("CSL6",LG);
                            if(editor.commit())
                            {
                                bus.post(new EnviarStringEvent("F3"));
                                Toast.makeText(this, R.string.exito, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            break;
                        case 4:
                            Log.i(TAG,"guardando curva personalizada");
                            // Copiar los valores del editText
                            LA = Integer.parseInt(editLA.getText().toString());
                            LB = Integer.parseInt(editLB.getText().toString());
                            LC = Integer.parseInt(editLC.getText().toString());
                            LD = Integer.parseInt(editLD.getText().toString());
                            LE = Integer.parseInt(editLE.getText().toString());
                            LF = Integer.parseInt(editLF.getText().toString());
                            LG = Integer.parseInt(editLG.getText().toString());
                            // guardar los datos en respaldo
                            editor.putInt("posicionSpinner", posicionSpinner);
                            editor.putInt("CPL0",LA);
                            editor.putInt("CPL1",LB);
                            editor.putInt("CPL2",LC);
                            editor.putInt("CPL3",LD);
                            editor.putInt("CPL4",LE);
                            editor.putInt("CPL5",LF);
                            editor.putInt("CPL6",LG);
                            if(editor.commit())
                            {
                                Toast.makeText(this, R.string.exito, Toast.LENGTH_SHORT).show();
                                bus.post(new EnviarStringEvent("F3"));
                                finish();
                            }
                            break;
                    }
                    break;

                case 3:
                    Log.i(TAG,"configuracion de monitor");
                    monitorizar = monitorizar_cb.isChecked();
                    frec = Integer.parseInt(editFrec.getText().toString());
                    editor = respaldo.edit();
                    editor.putInt("frec", frec);
                    editor.putBoolean("monitorizar", monitorizar);
                    if(editor.commit())
                    {
                        Log.d(TAG,"guardado");
                        Toast.makeText(this,R.string.exito, Toast.LENGTH_SHORT).show();
                        bus.post(new EnviarStringEvent("F2"));
                        finish();
                    }
                    break;

            }
        }
        if(id == R.id.button_cancelar)
        {
            finish();
        }
        if(id == R.id.boton_eliminar_db)
        {
            Log.i(TAG,"Eliminando todos los registros");
            int b = manager.eliminarTodo();
            Log.i(TAG,"id: "+b);
            if (b > 0)
            {
                Toast.makeText(this, R.string.exito_eliminar_db, Toast.LENGTH_SHORT).show();
                bus.post(new EnviarStringEvent("F1"));
                finish();
            }
            else if(b == 0)
            {
                Toast.makeText(this, R.string.error_eliminar_db2, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, R.string.error_eliminar_db, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(buttonView.getId() == R.id.cb_medicion)
        {
            dm = isChecked;
        }

        if(buttonView.getId() == R.id.cb_ajuste)
        {
            if(isChecked)
            {
                ajustar.setVisibility(View.VISIBLE);
                barra.setVisibility(View.VISIBLE);
            }
            else
            {
                ajustar.setVisibility(View.GONE);
                barra.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        posicionSpinner = position;
        Log.i(TAG,"posicion: "+position);

        switch (posicionSpinner)
        {
            case 0:
                Log.i(TAG, "Seleccione algo, ocultando todo");
                frame_curva.setVisibility(View.INVISIBLE);
                break;
            case 1:
                Log.i(TAG, "Seleccionada una opcion valida monstando todo");
                frame_curva.setVisibility(View.VISIBLE);
                // cargar los datos la curva seleccionada
                Log.i(TAG, "Cargando curva diabetico");
                LA = respaldo.getInt("CDL0", curvaGlucosaDiabetes[0]);
                LB = respaldo.getInt("CDL1", curvaGlucosaDiabetes[1]);
                LC = respaldo.getInt("CDL2", curvaGlucosaDiabetes[2]);
                LD = respaldo.getInt("CDL3", curvaGlucosaDiabetes[3]);
                LE = respaldo.getInt("CDL4", curvaGlucosaDiabetes[4]);
                LF = respaldo.getInt("CDL5", curvaGlucosaDiabetes[5]);
                LG = respaldo.getInt("CDL6", curvaGlucosaDiabetes[6]);

                editLA.setText("" + LA);
                editLB.setText("" + LB);
                editLC.setText("" + LC);
                editLD.setText("" + LD);
                editLE.setText("" + LE);
                editLF.setText("" + LF);
                editLG.setText("" + LG);
                break;
            case 2:
                Log.i(TAG, "Seleccionada una opcion valida monstando todo");
                frame_curva.setVisibility(View.VISIBLE);
                Log.i(TAG, "Cargando curva pre diabetico");
                // cargar los datos la curva seleccionada
                LA = respaldo.getInt("CPDL0", curvaGlucosaPrediabetes[0]);
                LB = respaldo.getInt("CPDL1", curvaGlucosaPrediabetes[1]);
                LC = respaldo.getInt("CPDL2", curvaGlucosaPrediabetes[2]);
                LD = respaldo.getInt("CPDL3", curvaGlucosaPrediabetes[3]);
                LE = respaldo.getInt("CPDL4", curvaGlucosaPrediabetes[4]);
                LF = respaldo.getInt("CPDL5", curvaGlucosaPrediabetes[5]);
                LG = respaldo.getInt("CPDL6", curvaGlucosaPrediabetes[6]);

                editLA.setText("" + LA);
                editLB.setText("" + LB);
                editLC.setText("" + LC);
                editLD.setText("" + LD);
                editLE.setText("" + LE);
                editLF.setText("" + LF);
                editLG.setText("" + LG);
                break;
            case 3:
                Log.i(TAG, "Seleccionada una opcion valida monstando todo");
                frame_curva.setVisibility(View.VISIBLE);
                Log.i(TAG, "Cargando curva sano");
                // cargar los datos la curva seleccionada
                LA = respaldo.getInt("CSL0", curvaGlucosaNormal[0]);
                LB = respaldo.getInt("CSL1", curvaGlucosaNormal[1]);
                LC = respaldo.getInt("CSL2", curvaGlucosaNormal[2]);
                LD = respaldo.getInt("CSL3", curvaGlucosaNormal[3]);
                LE = respaldo.getInt("CSL4", curvaGlucosaNormal[4]);
                LF = respaldo.getInt("CSL5", curvaGlucosaNormal[5]);
                LG = respaldo.getInt("CSL6", curvaGlucosaNormal[6]);

                editLA.setText("" + LA);
                editLB.setText("" + LB);
                editLC.setText("" + LC);
                editLD.setText("" + LD);
                editLE.setText("" + LE);
                editLF.setText("" + LF);
                editLG.setText("" + LG);
                break;
            case 4:
                Log.i(TAG, "Seleccionada una opcion valida monstando todo");
                frame_curva.setVisibility(View.VISIBLE);
                Log.i(TAG, "Cargando curva personalizada");
                // cargar los datos la curva seleccionada
                LA = respaldo.getInt("CPL0", curvaGlucosaNormal[0]);
                LB = respaldo.getInt("CPL1", curvaGlucosaNormal[0]);
                LC = respaldo.getInt("CPL2", curvaGlucosaNormal[0]);
                LD = respaldo.getInt("CPL3", curvaGlucosaNormal[0]);
                LE = respaldo.getInt("CPL4", curvaGlucosaNormal[0]);
                LF = respaldo.getInt("CPL5", curvaGlucosaNormal[0]);
                LG = respaldo.getInt("CPL6", curvaGlucosaNormal[0]);

                editLA.setText("" + LA);
                editLB.setText("" + LB);
                editLC.setText("" + LC);
                editLD.setText("" + LD);
                editLE.setText("" + LE);
                editLF.setText("" + LF);
                editLG.setText("" + LG);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        Log.i(TAG, "mensaje recibido en bus fragmnet config: " + event.mensaje);
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        Log.i(TAG, "numero recibido en bus fragmnet config: " + event.numero);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        editAjuste.setText(""+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

