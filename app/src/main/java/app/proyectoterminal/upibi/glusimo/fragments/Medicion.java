package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;

public class Medicion extends Fragment implements View.OnClickListener
{

    ArcProgress medidor;
    TextView diagnostico;
    EventBus bus = EventBus.getDefault();

    int estado = 0;
    int glucosa = 0;

    public static final String TAG = "Interfaz";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.v(TAG,"Creando vista de medicion");
        // REGISTRAR SI NO SE HA REGISTRADO
        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus  el fragment medicion");
            bus.register(this);
        }
        return inflater.inflate(R.layout.fragment_medicion, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.v(TAG,"OnActivityCreated Medicion");

        diagnostico = (TextView) getActivity().findViewById(R.id.diagnostico);
        medidor = (ArcProgress) getActivity().findViewById(R.id.medidor);
        medidor.setOnClickListener(this);

        // ENVIAR STRING Y ESPERA RECIBIR ESTADO DE CONEXION
        //Log.i(TAG,"enviando mensaje desde medicion");
        //bus.post(new EnviarStringEvent("MC"));
    }

    @Override
    public void onClick(View v)
    {
        vibrar(100);
        Log.i(TAG,"peticion para enviar mensaje por bluetooth");
        bus.post(new EnviarStringEvent("MC"));

        /*
        // SI EXISTE CONEXION PEDIR QUE ENVIE EL TEXTO ELEGIDO
        if (estado == 3)
        {
            vibrar(100);
            bus.post(new EnviarStringEvent("MM"));
        }
        */
    }

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        /*
        String dato;
        Log.i(TAG,"texto recibido en bus: "+event.mensaje);
        if(event.mensaje.startsWith("I"))
        {
            dato = event.mensaje.substring(1,event.mensaje.length());
            Log.i(TAG,"texto corregido en bus: "+dato);
            diagnostico.setText(dato);
        }

        // RECIBIÓ LECTURA DEL GLUCOMETRO
        if(event.mensaje.startsWith("V"))
        {
            dato = event.mensaje.substring(1,event.mensaje.length());
            glucosa = Integer.parseInt(dato);
            Log.i(TAG,"nivel de glucosa medido"+glucosa);
            if(glucosa<500)
            {
                medidor.setMax(glucosa+10);
                ObjectAnimator animation = ObjectAnimator.ofInt (medidor, "progress", 0, glucosa);
                animation.setDuration (500); //in milliseconds
                animation.setInterpolator (new DecelerateInterpolator());
                animation.start ();
            }
        }
        */
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        /*
        Log.i(TAG,"numero recibido en bus: "+event.numero);
        estado = event.numero;
        */
    }
}
