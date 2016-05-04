package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.InterpolacionLagrange;

public class Tendencias extends Fragment implements View.OnClickListener {

    TextView consola_tendencias, info_tendencias;
    ImageView espacio,ejeY;
    Timer timer;
    TimerTask timerTask;
    EventBus bus = EventBus.getDefault();
    SharedPreferences respaldo;
    SharedPreferences.Editor editor;
    InterpolacionLagrange poly;

    int puntos = 0;
    float gain = 2.5f;

    public Bitmap bitmap;
    public Canvas canvas;
    public Paint paint;
    int alto, ancho;

    public Bitmap bitmap2;
    public Canvas canvas2;
    public Paint paint2;
    int alto2, ancho2;


    String TAG = "Tendencias";
    final Handler handler = new Handler();  // VARIABLE PARA TIMER

    float xo,xf, yo, yf, pss;
    double[] coeficientes;

    private static final int curvaGlucosaNormal[] = {84, 130, 127, 100, 85, 80};
    private static final int curvaGlucosaPrediabetes[] = {80, 189, 127, 134, 143, 99};
    private static final int curvaGlucosaDiabetes[] = {84, 160, 220, 200, 186, 150};
    private static final int tiempo[] = {0, 30, 60, 90, 120, 150};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG,"Creando vista de Tendencias");
        poly = new InterpolacionLagrange();
        // REGISTRAR SI NO SE HA REGISTRADO
        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus  el fragment Tendencias");
            bus.register(this);
        }

        return inflater.inflate(R.layout.fragment_tendencias, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.v(TAG,"OnActivityCreated Tendencias");

        consola_tendencias = (TextView) getActivity().findViewById(R.id.consola_tendencias);
        info_tendencias = (TextView) getActivity().findViewById(R.id.tendencias_info);
        ejeY = (ImageView) getActivity().findViewById(R.id.eje_concentracion);
        espacio = (ImageView) getActivity().findViewById(R.id.canvas);
        espacio.setOnClickListener(this);

        info_tendencias.setText(R.string.info_tendencias);

        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        gain = respaldo.getFloat("gain",2.5f);


        ViewTreeObserver vto = espacio.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                // CONSIGUE LOS DATOS DE ANCHO Y ALTO HASTA ESTE PUNTO
                alto = espacio.getHeight();
                ancho = espacio.getWidth();
                pss = (float) ancho/tiempo.length;
                Log.i(TAG,"paso calculado: "+pss);

                //
                alto2 = ejeY.getHeight();
                ancho2 = ejeY.getWidth();

                //Log.d(TAG, "Height = " + espacio.getHeight() + " Width = " + espacio.getWidth());
                ViewTreeObserver obs = espacio.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                Log.i(TAG,"esperando que sirva este metodo alto: "+alto+" ancho: "+ancho);

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

                // PARA HACER LA GRÁFICA
                bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
                espacio.setImageBitmap(bitmap);
                canvas = new Canvas(bitmap);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                empezarCanvas();

                // calcular el paso en X
                ///////////// ASIGNACIONES PARA AGREGAR CANVAS ////////////////
            }
        });
    }


    void graficarLagrange()
    {
        if( xo == 0 )
        {
            puntos = 0;
            empezarCanvas();
            paint.setColor(getResources().getColor(R.color.colorLetraClara));
            paint.setStrokeWidth(4);
            Log.i(TAG,"iniciando grafica");
            // iniciar valores
            yo = (float) (alto-(gain*poly.calcularPunto(coeficientes,puntos)));
            xf = xo + pss;
            yf = (float) (alto-(gain*poly.calcularPunto(coeficientes,puntos)));
        }
        if(xf >= ancho)
        {
            terminarGrafica();
            Log.i(TAG,"parando timer");
        }
        else
        {
            puntos = puntos + 1;
            // aumentar paso
            xf = xo + pss;
            yf = (float) (alto-(gain*poly.calcularPunto(coeficientes,puntos)));
            Log.i(TAG,"graficando punto xo: "+xo+" xf: "+xf +" yo: "+yo +" yf: "+yf);
            canvas.drawLine(xo,yo,xf,yf,paint);
            // USAR ESTE METODO PARA ACTUALIZAR PANTALLA
            espacio.invalidate();
            // desplazar pasos
            xo = xf;
            yo = yf;
        }

    }

    /*
    void graficarDemo(int[] x)
    {
        if(xo == 0)
        {
            puntos = 0;
            empezarCanvas();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(4);
            Log.i(TAG,"iniciando grafica");
            // iniciar valores
            yo = (float) (alto-(gain*poly.calcularPunto(coeficientes,xo)));
        }

        if(xf >= ancho || puntos >= x.length-1)
        {
            //Log.i(TAG,"parando timer");
            xo = 0;
            terminarGrafica();
        }
        else
        {
            // aumentar paso
            puntos = puntos + 1;
            xf = xo + pss;
            yf = alto-(gain*curvaGlucosaDiabetes[puntos]);

            Log.i(TAG,"graficando punto xo: "+xo+" xf: "+xf +" yo: "+yo +" yf: "+yf);
            canvas.drawLine(xo,yo,xf,yf,paint);
            // USAR ESTE METODO PARA ACTUALIZAR PANTALLA
            espacio.invalidate();

            //
            xo = xf;
            yo = yf;
        }
    }
    */

    public void empezarGrafica(int[] x, int[] y, int periodo)
    {
        coeficientes = poly.polyLagrange(x,y);
        int largo = x.length;
        int tfinal = x[largo-1];
        Log.i(TAG,"valor final tiempo: "+tfinal);
        pss = (float) ancho/tfinal;
        Log.i(TAG,"coeficientes encontrados: "+ Arrays.toString(coeficientes));
        Log.i(TAG,"largo: "+largo + " paso: "+pss);

        // reiniciar valores
        xo = 0;
        puntos = 0;

        //instanciar nuevo timer
        timer = new Timer();
        //inicializar el timer
        initializeTimerTask2();
        //esperar 0ms para empezar, repetir cada 100ms
        timer.schedule(timerTask, 0, periodo); //
    }

    public void terminarGrafica() {
        //parar el timer, si no esta vacio
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void initializeTimerTask2() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run()
                    {
                        //graficarDemo(x,y);
                        graficarLagrange();
                    }
                });
            }
        };
    }

    @Override
    public void onClick(View v)
    {
        vibrar(100);
        empezarGrafica(tiempo,curvaGlucosaDiabetes, 3);
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

    private void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        Log.i(TAG,"numero recibido en bus tendencias: "+event.numero);
    }
}
