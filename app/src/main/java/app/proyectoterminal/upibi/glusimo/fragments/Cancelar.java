package app.proyectoterminal.upibi.glusimo.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.Audio;

/**
 * ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class Cancelar extends Activity implements View.OnClickListener {

    Button aceptar, pausar;
    TextView titulo, texto;
    Audio audio;
    Intent i;
    int alarma, glucemia;
    String TAG = "Notificacion";
    SharedPreferences respaldo;
    String fecha;
    ImageView icono;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.notification_fragment);

        // recuperar datos desde el activity
        i = getIntent();
        alarma = i.getIntExtra("alerta",-1);
        glucemia = i.getIntExtra("glucemia",-1);
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        fecha = respaldo.getString("fecha"," ");

        titulo = (TextView) findViewById(R.id.titulo_notificacion);
        texto = (TextView) findViewById(R.id.text_notificacion);
        aceptar = (Button) findViewById(R.id.boton_not_aceptar);
        pausar = (Button) findViewById(R.id.boton_not_pausar);
        icono = (ImageView) findViewById(R.id.notificacion_imagen);

        titulo.setText(R.string.titulo_notificacion);
        switch (alarma)
        {
            case 0:
                texto.setText("Niveles de glucosa normales\nConcentración:\n"+glucemia+" mg/dL");
                icono.setVisibility(View.GONE);
                break;
            case 1:
                texto.setText("Precaución: glucemia en aumento\nConcentración:\n"+glucemia+" mg/dL");
                icono.setVisibility(View.GONE);
                break;
            case 2:
                texto.setText("Alerta: glucemia elevada\nConcentración:\n"+glucemia+" mg/dL");
                icono.setImageResource(R.drawable.alarm);
                icono.setVisibility(View.VISIBLE);
                animar();
                break;
            case 3:
                texto.setText("Alerta: sospecha de hipoglucemia\nConcentración:\n"+glucemia+" mg/dL");
                icono.setVisibility(View.VISIBLE);
                icono.setImageResource(R.drawable.alarm);
                animar();
                break;
        }


        aceptar.setOnClickListener(this);
        pausar.setOnClickListener(this);

        audio = new Audio();
    }


    @Override
    public void onClick(View v)
    {
        if (v.getId()==R.id.boton_not_pausar)
        {
            finish();
        }

        if(v.getId()==R.id.boton_not_aceptar)
        {
            vibrar(100);
            if(alarma==3||alarma==2)
            {
                audio.stop();
            }
            cancelNotification(getBaseContext(),0);
            finish();
        }
    }

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    private void animar()
    {
        Log.i(TAG,"animando objeto");

        ObjectAnimator animador;
        AnimatorSet set = new AnimatorSet();
        AnimatorSet set1 = new AnimatorSet();
        AnimatorSet set2 = new AnimatorSet();
        AnimatorSet set3 = new AnimatorSet();

        animador = ObjectAnimator.ofFloat(icono, "alpha", 1, 0.25f, 0.7f, 0.9f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(1000);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(new DecelerateInterpolator());
        set.play(animador);

        animador = ObjectAnimator.ofFloat(icono, "scaleX", 1, 0.9f, 0.93f, 0.96f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(1000);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(new DecelerateInterpolator());
        set1.play(animador);

        animador = ObjectAnimator.ofFloat(icono, "scaleY", 1, 0.9f, 0.93f, 0.96f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(1000);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(new DecelerateInterpolator());
        set2.play(animador);

        set3.playTogether(set, set1, set2);
        set3.start();
    }

    public static void cancelNotification(Context context, int notifyId)
    {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancel(notifyId);
    }
}
