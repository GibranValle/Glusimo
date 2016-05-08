package app.proyectoterminal.upibi.glusimo.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.Audio;

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
        Log.w(TAG,"click 1");
        vibrar(100);
        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        dm = respaldo.getBoolean("demo_medicion",false);
        Log.i(TAG,"demo mode: "+dm);
        if(dm)
        {
            if(conteoClicks == 4)
            {
                conteoClicks = 0;
            }
            DatoRecibido(glucemias[conteoClicks]);
            Log.i(TAG,"enviando en bus para guardar");
            bus.post(new EnviarIntEvent(glucemias[conteoClicks]));
            conteoClicks = conteoClicks + 1;
        }
        else
        {
            Log.i(TAG,"petición para enviar mensaje por bluetooth");
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
        animation.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                Log.i(TAG,"empezando animacion");
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
        animation.start();
    }

    private void diagnosticar(int valor)
    {
        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        hipoglucemia = respaldo.getInt("hipo",70);
        hiperglucemia = respaldo.getInt("hiper",120);
        hiperglucemia_severa = respaldo.getInt("hiper_severa",200);
        Log.w(TAG,"VALOR: "+valor);
        int notificacion;
        if(valor >= hiperglucemia_severa)
        {
            // SOSPECHA DE HIPERGLUCEMIA SEVERA LANZAR ALARMA
            diagnostico.setText(R.string.paciente_emergency);
            diagnostico.setBackgroundResource(R.color.colorEmergency);
            Log.e(TAG,"HGS");
            notificacion = 2;
        }
        else if(valor >= hiperglucemia)
        {
            // SOSPECHA DE HIPERGLUCEMIA LANZAR WARNING
            diagnostico.setText(R.string.paciente_warning);
            diagnostico.setBackgroundResource(R.color.colorWarning);
            Log.w(TAG,"HG");
            notificacion = 1;
        }
        else if(valor > hipoglucemia)
        {
            // SOSPECHA DE SALUDABLE
            diagnostico.setText(R.string.paciente_saludable);
            diagnostico.setBackgroundResource(R.color.colorOn);
            Log.d(TAG,"sano");
            notificacion = 0;
        }
        else if(valor >= 0)
        {
            // SOSPECHA DE HIPOGLUCEMIA LANZAR ALARMA
            diagnostico.setText(R.string.paciente_emergency_hipo);
            diagnostico.setBackgroundResource(R.color.colorEmergency);
            Log.e(TAG,"HipoG");
            notificacion = 3;
        }
        else
        {
            notificacion = -1;
        }
        showNotification(notificacion,valor);
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
        if(!dm)
        {
            Log.i(TAG,"numero recibido en bus medición: "+event.numero);
            if(event.numero <= 1000)
            {
                DatoRecibido(event.numero);
            }
        }
    }

    void showNotification(int cualAlerta, int glucemia)
    {
        Log.i(TAG,"notificacion: "+cualAlerta +" glucemia: "+glucemia);
        /* lista de eventos
        0 = notificacion
        1 = warning hiperglucemia
        2 = alarma hiperglucemia
        3 = alarma hipoglucemia
        */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setContentTitle(getResources().getString(R.string.notificacion_titulo));
        builder.setAutoCancel(false);
        String info;
        info = getResources().getString(R.string.notificacion_info);
        info = info.concat(""+glucemia);
        DateFormat df = new SimpleDateFormat("EEEE dd/MMMM/yyyy-HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        info = info.concat("\n"+date);
        long[] pattern = new long[0];
        Audio audio = new Audio();
        //MediaPlayer mp;
        // Sets up the Snooze and Dismiss action buttons that will appear in the

        switch (cualAlerta)
        {
            case 0: // notificacion
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(info));
                builder.setTicker(getResources().getString(R.string.notificacion_ticker));
                builder.setContentInfo(getString(R.string.notificacion_text));
                //alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                //mp= MediaPlayer.create(getContext(),R.raw.normal);
                audio.load(getContext(),R.raw.normal,false);
                builder.setLights(Color.RED, 250, 250);
                pattern = new long[]{125, 125, 125};
                break;
            case 1: // warning
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(info));
                builder.setContentInfo(getString(R.string.warning_ticker));
                builder.setTicker(getResources().getString(R.string.warning_ticker));
                //mp= MediaPlayer.create(getContext(), R.raw.warning);
                audio.load(getContext(),R.raw.warning,false);
                builder.setLights(Color.RED, 250, 250);
                pattern = new long[]{150, 150, 150,150};
                break;
            case 2:
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(info));
                builder.setContentInfo(getString(R.string.alarm_hiper_ticker));
                builder.setTicker(getResources().getString(R.string.alarm_hiper_ticker));
                //mp= MediaPlayer.create(getContext(), R.raw.alert);
                audio.load(getContext(),R.raw.alert,true);
                builder.setLights(Color.RED, 250, 250);
                pattern = new long[]{200, 200, 200,200,200};
                break;
            case 3:
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(info));
                builder.setContentInfo(getString(R.string.alarm_hipo_ticker));
                builder.setTicker(getResources().getString(R.string.alarm_hipo_ticker));
                //mp= MediaPlayer.create(getContext(), R.raw.alert);
                audio.load(getContext(),R.raw.alert,true);
                builder.setLights(Color.RED, 250, 250);
                pattern = new long[]{250, 250, 250,250, 250};
                //mp.setLooping(true);
                break;
            default:
        }


        builder.setVibrate(pattern);

        Intent intent = new Intent(getContext(),Cancelar.class);
        intent.putExtra("alerta",cualAlerta);
        intent.putExtra("glucemia",glucemia);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(),
                0, intent,PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(contentIntent);
        NotificationManager NM = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        audio.play();
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
