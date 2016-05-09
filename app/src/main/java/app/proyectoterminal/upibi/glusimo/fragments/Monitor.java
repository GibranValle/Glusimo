package app.proyectoterminal.upibi.glusimo.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;

public class Monitor extends Fragment
{
    Spinner spFechaInicial, spFechaFinal;
    TextView escala;

    // VARIABLES PARA GRAFICAR
    ImageView espacio;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    int alto, ancho;
    ImageView ejeY;
    Bitmap bitmap2;
    Canvas canvas2;
    Paint paint2;
    int alto2, ancho2;

    EventBus bus = EventBus.getDefault();
    String TAG = "Monitor";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.v(TAG,"Creando vista de Medición");
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

        Log.v(TAG,"OnActivityCreated Medición");

        escala = (TextView) getActivity().findViewById(R.id.monitor_escala);
        spFechaInicial = (Spinner) getActivity().findViewById(R.id.fecha_inicio);
        spFechaFinal = (Spinner) getActivity().findViewById(R.id.fecha_final);
        espacio = (ImageView) getActivity().findViewById(R.id.canvas_monitor);
        ejeY = (ImageView) getActivity().findViewById(R.id.ejeY_monitor);

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
                canvas2.translate(ancho2/10, alto2/1.8f);
                //canvas2.drawText("Concentracion [mg/mL]",0,0,paint2);
                canvas2.translate(-ancho2/3.5f, 0);
                canvas2.rotate(-90,ancho2/1.2f,0);
                // cambiar este parametro, da mucho ruido
                paint2.setTextSize(40f);
                paint2.setFakeBoldText(true);
                canvas2.drawText(getResources().getString(R.string.eje_Y),0,0,paint2);

                // PARA HACER LA GRÁFICA
                bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                espacio.setImageBitmap(bitmap);
                canvas = new Canvas(bitmap);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                ///////////// ASIGNACIONES PARA AGREGAR CANVAS ////////////////
            }
        });
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
