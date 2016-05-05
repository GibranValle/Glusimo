package app.proyectoterminal.upibi.glusimo.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
    TextView diagnostico, titulo;
    EventBus bus = EventBus.getDefault();
    SharedPreferences respaldo;
    SharedPreferences.Editor editor;
    SoundPool soundPool; // VARIABLE PARA SONIDO

    int alert, normal, warning;
    int max = 0;
    int hipoglucemia = 70;
    int hiperglucemia = 120;
    int hiperglucemia_severa = 120;

    // variables para lanzar demo
    boolean dm = false;
    int conteoClicks = 0;
    private static final int glucemias[] = {70, 100, 150, 250};

    public static final String TAG = "Medicion";

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
        return inflater.inflate(R.layout.fragment_medicion, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.v(TAG,"OnActivityCreated Medición");

        titulo = (TextView) getActivity().findViewById(R.id.texto_arco);
        diagnostico = (TextView) getActivity().findViewById(R.id.diagnostico);
        medidor = (ArcProgress) getActivity().findViewById(R.id.medidor);
        medidor.setOnClickListener(this);

        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        max = respaldo.getInt("max",500);
        medidor.setMax(max);

        if(!dm)
        {
            titulo.setText(R.string.texto_arco_demo);
        }
        else
        {
            titulo.setText(R.string.texto_arco);
        }

        ///////////// ASIGNACIONES PARA SONIDO /////////////////
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool sp, int sid, int status)
            {
                Log.d(getClass().getSimpleName(), "Sound is now loaded");
            }
        });
        alert = soundPool.load(getContext(), R.raw.alert, 0);
        normal = soundPool.load(getContext(), R.raw.normal, 0);
        warning = soundPool.load(getContext(),R.raw.warning,0);

        ///////////// ASIGNACIONES PARA SONIDO /////////////////



        // ENVIAR STRING Y ESPERA RECIBIR ESTADO DE CONEXION
        //Log.i(TAG,"enviando mensaje desde medicion");
        //bus.post(new EnviarStringEvent("MC"));
    }

    @Override
    public void onClick(View v)
    {
        vibrar(100);
        Log.i(TAG,"petición para enviar mensaje por bluetooth");
        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        dm = respaldo.getBoolean("demo_medicion",false);
        if(dm)
        {
            if(conteoClicks == 5)
            {
                conteoClicks = 0;
            }
            DatoRecibido(glucemias[conteoClicks]);
            bus.post(new EnviarIntEvent(glucemias[conteoClicks]));
            conteoClicks = conteoClicks + 1;
        }
        else
        {
            bus.post(new EnviarStringEvent("MC"));
        }

        /*
        // SI EXISTE CONEXION PEDIR QUE ENVIE EL TEXTO ELEGIDO
        if (estado == 3)
        {
            vibrar(100);
            bus.post(new EnviarStringEvent("MM"));
        }
        */
    }

    public void playSound(int sound, float volumen)
    {
        soundPool.play(sound, volumen, volumen, 1, 0, 1);
    }

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    private void animar(final int valor)
    {
        Log.i(TAG,"animando objeto");
        ObjectAnimator animation = ObjectAnimator.ofInt (medidor, "progress", 0, valor); // see this max value coming back here, we animale towards that value
        animation.setDuration (1500); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                Log.i(TAG,"animación terminada, lanzar diagnostico");
                diagnosticar(valor);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void diagnosticar(int valor)
    {
        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        hipoglucemia = respaldo.getInt("hipo",70);
        hiperglucemia = respaldo.getInt("hiper",120);
        hiperglucemia_severa = respaldo.getInt("hiper_severa",200);
        if( valor <= hiperglucemia)
        {
            // SOSPECHA DE HIPOGLUCEMIA LANZAR ALARMA
            diagnostico.setText(R.string.paciente_emergency_hipo);
            diagnostico.setBackgroundResource(R.color.colorEmergency);
            playSound(alert,1);
            showNotification();
        }
        if( valor > hipoglucemia && valor <= hiperglucemia)
        {
            // SOSPECHA DE SALUDABLE
            diagnostico.setText(R.string.paciente_saludable);
            diagnostico.setBackgroundResource(R.color.colorOn);
            playSound(normal,0.5f);
        }
        if( valor > hiperglucemia && valor <= hiperglucemia_severa)
        {
            // SOSPECHA DE HIPERGLUCEMIA LANZAR WARNING
            diagnostico.setText(R.string.paciente_warning);
            diagnostico.setBackgroundResource(R.color.colorWarning);
            playSound(warning,0.5f);
        }
        if( valor > hiperglucemia_severa)
        {
            // SOSPECHA DE HIPERGLUCEMIA SEVERA LANZAR ALARMA
            diagnostico.setText(R.string.paciente_emergency);
            diagnostico.setBackgroundResource(R.color.colorEmergency);
            playSound(alert,1);
        }
    }

    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        String dato = event.mensaje;
        if(dato.startsWith("I"))
        {
            Log.i(TAG,"dato recibido desde interfaz: "+dato);
            if(dato.indexOf("C") == 1)
            {
                // LA SEGUNDA LETRA ES C, DISPOSITIVO CONECTADO
                    diagnostico.setText(R.string.sin_medicion);
            }
            else if(dato.indexOf("D")== 1)
            {
                // LA SEGUNDA LETRA ES C, DISPOSITIVO CONECTADO
                diagnostico.setText(R.string.sin_conexion);
            }
        }
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        Log.i(TAG,"numero recibido en bus medición: "+event.numero);
        if(event.numero <= 1000)
        {
            DatoRecibido(event.numero);
        }
    }

    void showNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setContentTitle(getResources().getString(R.string.notificacion_titulo));
        builder.setContentText(getResources().getString(R.string.notificacion_text));
        builder.setTicker(getResources().getString(R.string.notificacion_ticker));

        Intent intent = new Intent(getContext(),AcercaDe.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addParentStack(AcercaDe.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager NM = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NM.notify(0,builder.build());
    }

    void DatoRecibido(int numero)
    {
        diagnostico.setText(R.string.midiendo);
        diagnostico.setBackgroundResource(R.color.colorOff);
        if (numero > max)
        {
            max = numero;
            editor = respaldo.edit();
            editor.putInt("max", max);
            if(editor.commit())
            {
                Log.i(TAG,"maximo encontrado y guardado: " + max);
            }
        }
        animar(numero);
    }
}
