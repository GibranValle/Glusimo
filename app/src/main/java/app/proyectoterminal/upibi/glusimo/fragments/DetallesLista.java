package app.proyectoterminal.upibi.glusimo.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class DetallesLista extends Activity implements View.OnClickListener{

    private Button actualizar, eliminar, cancelar;
    private TextView detalles;
    private String datos, fecha, fecha_corta, hora, concentración;
    private Long id_recuperado;
    private Intent i;
    private ObjectAnimator animador;
    Bundle bundle;
    private String TAG = "Interfaz";
    private Boolean conectado = false;
    private DataBaseManager manager;
    private int fragment = 0;
    EventBus bus = EventBus.getDefault();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.v(TAG,"Creando vista de DetallesLista");
        // REGISTRAR SI NO SE HA REGISTRADO
        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus el fragment detalles lista");
            bus.register(this);
        }

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.detalles_lista_layout);

        eliminar =(Button)findViewById(R.id.boton_eliminar);
        actualizar = (Button)findViewById(R.id.boton_actualizar);
        cancelar = (Button)findViewById(R.id.boton_cancelar);
        detalles = (TextView)findViewById(R.id.titulo_detalles_lista);


        actualizar.setOnClickListener(this);
        eliminar.setOnClickListener(this);
        cancelar.setOnClickListener(this);

        //recuperar id
        //comando antes del bundle
        bundle = getIntent().getExtras();
        id_recuperado = bundle.getLong("id");
        conectado = bundle.getBoolean("conectado",false);
        fragment = bundle.getInt("fragment",0);

        manager = new DataBaseManager(this);
        //recupera fecha + concentración
        datos = manager.recuperarPorId(id_recuperado);
        Log.d("DETALLES","SE CARGO EL USUARIO "+datos);
        //extrae el nombre de fecha corta de la cadena original
        fecha = datos.substring(0,datos.indexOf("|"));
        //extrae el nombre de fecha corta de la cadena original
        fecha_corta = datos.substring(datos.indexOf("¿")+1,datos.indexOf("&"));
        //extrae el nombre de fecha de la cadena original
        hora = datos.substring(datos.indexOf("&")+1,datos.indexOf("!"));
        // extrae la concentración de la cadena original
        concentración = datos.substring(datos.indexOf("!")+1,datos.length()-1);
        Log.d(TAG,"fecha: "+fecha_corta +" Hora: "+hora+" []: "+concentración);

        detalles.setText("Fecha:\n"+fecha_corta+" Hora: "+hora+" Glucemia: "+concentración+" mg/dL");

    }

    public void animarBoton (Button boton)
    {
        int inicio = getResources().getColor(R.color.colorPrimaryDark);
        int ff = getResources().getColor(R.color.colorLight);;

        if (animador != null)
        {
            animador.cancel();
        }
        animador = ObjectAnimator.ofInt(boton, "backgroundColor", inicio, ff, inicio);
        animador.setDuration(100);
        animador.setInterpolator(new DecelerateInterpolator());
        animador.setEvaluator(new ArgbEvaluator());
        animador.start();
    }

    @Override
    public void onClick(View v)
    {
        Log.d(TAG, "PUSH");
        int id = v.getId();

        if(id==R.id.boton_eliminar)
        {
            animarBoton(eliminar);  // metodo para iluminar

            Log.d(TAG,"ELIMINANDO");
            int borrado = manager.eliminarDatos(fecha);
            if (borrado>0)  // se borraron mas de 0 elementos
            {
                Toast.makeText(this,"Registro del: "+ fecha +" eliminado correctamente", Toast.LENGTH_SHORT).show();
                Log.d(TAG,""+borrado);
                bus.post(new EnviarStringEvent("DL1"));
                finish();
                /*
                i = new Intent(this,Interfaz.class);
                // envia el estado de conexión y el fragment donde se quedó
                i.putExtra("conectado",conectado);
                i.putExtra("fragment",fragment);
                startActivity(i);
                */
            }
            else
            {
                Toast.makeText(this,"Fecha: "+ fecha +" Error al intentar elminar", Toast.LENGTH_SHORT).show();
                Log.d(TAG,""+borrado);
            }
        }

        if(id==R.id.boton_actualizar)
        {
            Log.d(TAG,"ACTUALIZANDO");
            animarBoton(actualizar);  // metodo para iluminar
            finish();
            //animarBoton(cancelar);  // metodo para iluminar
            //startActivity( i = new Intent(this,Interfaz.class));
        }

        if(id==R.id.boton_cancelar)
        {
            Log.d(TAG,"CANCELANDO");
            animarBoton(cancelar);  // metodo para iluminar
            finish();
            /*
            i = new Intent(this,Interfaz.class);
            // envia el estado de conexión y el fragment donde se quedó
            i.putExtra("conectado",conectado);
            i.putExtra("fragment",fragment);
            startActivity(i);
            */
        }
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event) {
        Log.d(TAG,"numero recibido en bus: "+event.numero);
    }
}
