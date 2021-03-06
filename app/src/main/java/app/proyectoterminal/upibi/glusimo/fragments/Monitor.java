package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class Monitor extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    TextView escala, consola;
    // VARIABLES PARA GRAFICAR
    ImageView espacio, jeringa;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    int alto, ancho;
    ImageView ejeY;
    Bitmap bitmap2;
    Canvas canvas2;
    Paint paint2;
    int alto2, ancho2;
    SharedPreferences respaldo;
    Boolean monitorizar;
    List<Long> tiempos = new ArrayList<>();
    List<Integer> glucemias = new ArrayList<>();
    String filterA="", filterB="";
    long idA=0, idB=0;
    long inicio=0, fin=0;
    // BASE DE DATOS
    private SimpleCursorAdapter adaptador;
    private Cursor cursor;
    private DataBaseManager manager;
    Spinner spFechaInicial, spFechaFinal;
    // Listas para el spinner
    List<String> fecha_inicio = new ArrayList<>();
    List<String> fecha_final = new ArrayList<>();
    // array adapters para spinner
    ArrayAdapter<String> adapterInicio;
    ArrayAdapter<String> adapterFinal;

    EventBus bus = EventBus.getDefault();
    String TAG = "Monitor";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.v(TAG,"Creando vista de Monitor");
        // REGISTRAR SI NO SE HA REGISTRADO
        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus  el fragment medición");
            bus.register(this);
        }
        return inflater.inflate(R.layout.fragment_monitor_layout, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.v(TAG,"OnActivityCreated Monitor");

        consola = (TextView) getActivity().findViewById(R.id.consola_monitor);
        escala = (TextView) getActivity().findViewById(R.id.monitor_escala);
        spFechaInicial = (Spinner) getActivity().findViewById(R.id.fecha_inicio);
        spFechaFinal = (Spinner) getActivity().findViewById(R.id.fecha_final);
        espacio = (ImageView) getActivity().findViewById(R.id.canvas_monitor);
        ejeY = (ImageView) getActivity().findViewById(R.id.ejeY_monitor);
        jeringa = (ImageView) getActivity().findViewById(R.id.jeringa);

        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        monitorizar = respaldo.getBoolean("monitorizar",true);
        manager = new DataBaseManager(getContext());

        // Crear los adapatadores para el spinner
        adapterInicio = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_layout,fecha_inicio);
        adapterFinal = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_layout,fecha_final);

        // crear los layout de despliegue del spinner
        adapterInicio.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterFinal.setDropDownViewResource(R.layout.custom_drop_spinner_layout);

        // cargar el adapter al spinner
        spFechaInicial.setAdapter(adapterInicio);
        spFechaFinal.setAdapter(adapterFinal);

        // FUNCIONES PARA EL CLICK
        spFechaInicial.setOnItemSelectedListener(this);
        spFechaFinal.setOnItemSelectedListener(this);
        jeringa.setOnClickListener(this);

        if(monitorizar)
        {
            consola.setText(R.string.titulo_monitor);
            espacio.setOnClickListener(this);
            actualizarSpinner();
            // contruir vectores para graficar
            //contruirVectores();
        }
        else
        {
            consola.setText(R.string.titulo_monitor_off);
        }



        ViewTreeObserver vto = espacio.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                // CONSIGUE LOS DATOS DE ANCHO Y ALTO HASTA ESTE PUNTO
                alto = espacio.getHeight();
                ancho = espacio.getWidth();

                alto2 = ejeY.getHeight();
                ancho2 = ejeY.getWidth();

                Log.i(TAG,"alto: "+alto+" ancho: "+ancho+" alto2: "+alto2+" ancho2: "+ancho2);

                ViewTreeObserver obs = espacio.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

                // NECESARIO PARA ROTAR EL TEXTO 270° -.-!!!!
                bitmap2 = Bitmap.createBitmap(ancho2, alto2, Bitmap.Config.ARGB_8888);
                ejeY.setImageBitmap(bitmap2);
                canvas2 = new Canvas(bitmap2);
                paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint2.setColor(getResources().getColor(R.color.colorLetraClara));
                canvas2.translate(ancho2/10, alto2/1.5f);
                //canvas2.drawText("Concentracion [mg/mL]",0,0,paint2);
                canvas2.translate(-ancho2/3.5f, 0);
                canvas2.rotate(-90,ancho2/1.2f,0);
                // cambiar este parametro, da mucho ruido
                paint2.setTextSize(40f);
                paint2.setFakeBoldText(true);
                canvas2.drawText(getResources().getString(R.string.eje_Y),0,0,paint2);

                if(monitorizar)
                {
                    // PARA HACER LA GRÁFICA
                    bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                    espacio.setImageBitmap(bitmap);
                    canvas = new Canvas(bitmap);
                    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    empezarCanvas();
                    ///////////// ASIGNACIONES PARA AGREGAR CANVAS ////////////////
                }
            }
        });
    }

    void actualizarSpinner()
    {
        Log.i(TAG, "Actualizando spinner de Lista");
        String fechas_Recuperadas;
        fechas_Recuperadas = manager.recuperarTodasFechas();
        Log.i(TAG,"Datos recuperados:\n" + fechas_Recuperadas);

        // crear variables para llenar
        String temporal;
        int indexslash;
        int comprobar = comprobarFinLinea(fechas_Recuperadas);
        String id;

        if(fecha_inicio.size()==0)
        {
            fecha_inicio.add("Fecha Inicio:");
        }
        if(fecha_final.size() == 0)
        {
            fecha_final.add("Fecha Final:");
        }

        //Log.i(TAG,"ENTRANDO AL WHILE AÑOS");
        // COMPROBAR QUE AÑOS RECUPERADOS TENGA TEXTO AUN
        while (comprobar>0)
        {
            indexslash = fechas_Recuperadas.indexOf("\n");
            //Log.d(TAG,"index: "+indexslash);
            temporal = fechas_Recuperadas.substring(0,indexslash);
            //Log.i(TAG,"temporal: "+temporal);
            fechas_Recuperadas = fechas_Recuperadas.replaceFirst(temporal+"\n","");
            //Log.i(TAG,"cadena residual:\n"+añosRecuperados);
            if(fecha_inicio.contains(temporal))
            {
                //Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                Log.i(TAG," fecha agregada: "+temporal);
                fecha_inicio.add(temporal);
            }
            if(fecha_final.contains(temporal))
            {
                //Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                Log.i(TAG," fecha agregada: "+temporal);
                fecha_final.add(temporal);
            }
            comprobar = comprobarFinLinea(fechas_Recuperadas);
        }
        //Log.i(TAG,"SALIENDO WHILE AÑOS");

        // Recrear los adapatadores para el spinner
        adapterInicio = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_layout,fecha_inicio);
        adapterFinal = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_layout,fecha_final);

        // Recrear los layout de despliegue del spinner
        adapterInicio.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterFinal.setDropDownViewResource(R.layout.custom_drop_spinner_layout);

        // Recargar el adapter al spinner
        spFechaInicial.setAdapter(adapterInicio);
        spFechaFinal.setAdapter(adapterFinal);
    }

    int comprobarFinLinea(String dato)
    {
        int id;
        if(dato.contains("\n"))
        {
            id = 1;
        }
        else
        {
            id=-1;
        }
        //Log.i(TAG," comprobando fin de linea: "+id);
        return id;
    }

    void contruirVectores()
    {
        tiempos.clear();
        glucemias.clear();
        Log.i(TAG,"Creando vectores");
        String datos = manager.recuperarDesdeHasta(inicio,fin);
        int largo = datos.length();
        Log.i(TAG,"DATOS RECUPERADOS CON FILTRO: "+datos + " largo: "+largo+"\n");
        int comprobar = comprobarFinLinea(datos);

        while (comprobar>0)
        {
            String fecha = datos.substring(0, datos.indexOf("|"));
            Log.i(TAG, "fecha: " + fecha + " largo: " + fecha.length());
            SimpleDateFormat df = new SimpleDateFormat("EEEE dd/MMMM/yyyy-HH:mm:ss");
            long tiempo;
            long minutos = 0;
            try
            {
                Date d = df.parse(fecha);
                tiempo = d.getTime();
                minutos = tiempo/1000/60;
                //Log.i(TAG,"TIEMPO EN MILIS: "+tiempo+" tiempo en mins: "+minutos);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            datos = datos.replaceFirst(fecha, "");
            datos = datos.substring(1);
            //Log.i(TAG, "cadena restante: " + datos + " largo: " + datos.length());

            String glucemia = datos.substring(0, datos.indexOf("\n"));
            //Log.i(TAG, "glucemia: " + glucemia + " largo: " + glucemia.length());

            datos = datos.replaceFirst(glucemia, "");
            datos = datos.substring(1);
            //Log.i(TAG, "cadena restante: " + datos + " largo: " + datos.length());

            comprobar = comprobarFinLinea(datos);
            tiempos.add(minutos);
            glucemias.add(Integer.valueOf(glucemia));
        }
    }

    public void empezarCanvas()
    {
        Log.d(TAG, "limpiando canvas");
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setColor(0xFF7D0011);
        paint.setStrokeWidth(1);
        canvas.drawLine(1 * ancho / 10, 0, 1 * ancho / 10, alto, paint);
        canvas.drawLine(2 * ancho / 10, 0, 2 * ancho / 10, alto, paint);
        canvas.drawLine(3 * ancho / 10, 0, 3 * ancho / 10, alto, paint);
        canvas.drawLine(4 * ancho / 10, 0, 4 * ancho / 10, alto, paint);
        canvas.drawLine(5 * ancho / 10, 0, 5 * ancho / 10, alto, paint);
        canvas.drawLine(6 * ancho / 10, 0, 6 * ancho / 10, alto, paint);
        canvas.drawLine(7 * ancho / 10, 0, 7 * ancho / 10, alto, paint);
        canvas.drawLine(8 * ancho / 10, 0, 8 * ancho / 10, alto, paint);
        canvas.drawLine(9 * ancho / 10, 0, 9 * ancho / 10, alto, paint);
        // LINEAS HORIZONTALES
        canvas.drawLine(0, 1 * alto / 10 ,ancho, 1 * alto / 10, paint);
        canvas.drawLine(0, 2 * alto / 10 ,ancho, 2 * alto / 10, paint);
        canvas.drawLine(0, 3 * alto / 10 ,ancho, 3 * alto / 10, paint);
        canvas.drawLine(0, 4 * alto / 10 ,ancho, 4 * alto / 10, paint);
        canvas.drawLine(0, 5 * alto / 10 ,ancho, 5 * alto / 10, paint);
        canvas.drawLine(0, 6 * alto / 10 ,ancho, 6 * alto / 10, paint);
        canvas.drawLine(0, 7 * alto / 10 ,ancho, 7 * alto / 10, paint);
        canvas.drawLine(0, 8 * alto / 10 ,ancho, 8 * alto / 10, paint);
        canvas.drawLine(0, 9 * alto / 10 ,ancho, 9 * alto / 10, paint);
    }

    @Override
    public void onClick(View v)
    {

        if(v.getId() == R.id.jeringa)
        {
            vibrar(100);
            Log.i(TAG,"configuracion de medicion "+3);
            Intent i = new Intent(getContext(),Fragment_Configuraciones.class);
            i.putExtra("linea",3);
            startActivity(i);
        }

        if(v.getId() == R.id.canvas) {

            vibrar(100);
            contruirVectores();
            empezarCanvas();
            long offset = 0, size = 0, max = 0, dt = 15;
            float gain = 0;
            long xo, xf;
            float yo, yf;
            Long[] x = new Long[0];
            Integer[] y = new Integer[0];
            Log.i(TAG, "TAMAÑOS: " + tiempos.size() + " " + glucemias.size());
            // encontrar el maximo de un list
            if (tiempos.size() > 0) {
                try {
                    max = Collections.max(glucemias);
                } catch (Exception e) {
                    e.printStackTrace();
                    max = 250;
                }
                // convertir las listas a arrays
                x = tiempos.toArray(new Long[tiempos.size()]);
                y = glucemias.toArray(new Integer[glucemias.size()]);
                Log.i(TAG, "x: " + Arrays.toString(x) + " y: " + Arrays.toString(y));
                offset = x[0];
                size = x.length;
                gain = (float) alto / (max + 10);
                Log.i(TAG, "ganancia calculada: " + gain);
            }


            if (size == 1) {
                Log.i(TAG, "Graficar 1 punto");
                // GRAFIQUE UN PUNTO
                paint.setColor(getResources().getColor(R.color.colorLetraClara));
                paint.setStrokeWidth(5);
                canvas.drawCircle(ancho / 2, alto / 2, 20, paint);
                espacio.invalidate();
                // calcular escala
                escala.setText("Escala:  " + 2 * max / 10 + "mg/dL");
            } else if (size == 2) {
                Log.i(TAG, "Graficar 1 recta");
                // GRAFIQUE UN PUNTO
                paint.setColor(getResources().getColor(R.color.colorLetraClara));
                paint.setStrokeWidth(5);
                xo = 0;
                yo = (alto - (gain * y[0]));
                xf = ancho;
                yf = (alto - (gain * y[1]));
                canvas.drawLine(xo, yo, xf, yf, paint);
                espacio.invalidate();
                dt = x[1] - x[0];
                escala.setText("Escala:  " + max / 10 + "mg/dL  |  " + dt + "min");
            } else if (size >= 3) {
                Log.i(TAG, "Graficar lineas");
                paint.setColor(getResources().getColor(R.color.colorLetraClara));
                paint.setStrokeWidth(5);
                // hacer un for
                for (int c = 0; c < size - 1; c++) {
                    xo = c * ancho / (size - 1);
                    yo = (alto - (gain * y[c]));
                    xf = (c + 1) * ancho / (size - 1);
                    yf = (alto - (gain * y[c + 1]));
                    canvas.drawLine(xo, yo, xf, yf, paint);
                    Log.i(TAG, "puntos: " + xo + " " + yo + " " + xf + " " + yf);
                }
                espacio.invalidate();
                // calcular escala
                dt = x[(int) size - 1] - x[0];
                escala.setText("Escala:  " + max / 10 + "mg/dL  |  " + dt + " min");

                Log.d(TAG, "dt: " + dt);
                if (dt > 60) {
                    dt = dt / 60;
                    escala.setText("Escala:  " + max / 10 + "mg/dL  |  " + dt + " hrs");
                }

                if (dt > 24) {
                    dt = dt / 24;
                    escala.setText("Escala:  " + max / 10 + "mg/dL  |  " + dt + " dias");
                }
                // calcular escala
            }
        }
    }

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner spinner = (Spinner) parent;
        String selected = parent.getItemAtPosition(position).toString();
        if(spinner.getId() == R.id.fecha_inicio)
        {
            Log.w(TAG,"Spinner Fecha inicio"+ " id: "+id);
            idA = id;
            if(idA > 0 )
            {
                filterA = selected;
            }
        }
        else if(spinner.getId() == R.id.fecha_final)
        {
            Log.w(TAG,"Spinner Fecha final" +" id: "+id);
            idB = id;
            if(idB > 0 )
            {
                filterB = selected;
            }
        }

        if(idB >= idA)
        {
            Log.i(TAG,"coso "+selected);

            if(!filterA.equals(""))
            {
                inicio = manager.recuperarId(filterA);
            }
            if(!filterB.equals(""))
            {
                fin = manager.recuperarId(filterB);
            }
            Log.i(TAG,"ids recuperados: "+inicio +" :"+fin);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        String dato = event.mensaje;
        Log.i(TAG,"dato recibido desde interfaz: "+dato);
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        Log.i(TAG,"numero recibido en bus monitor: "+event.numero);
    }
}
