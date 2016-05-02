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

import app.proyectoterminal.upibi.glusimo.Interfaz;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class DetallesLista extends Activity implements View.OnClickListener{

    Button actualizar, eliminar, cancelar;
    TextView detalles;
    String datos, fecha, concentración;
    Long id_recuperado;
    Intent i;
    ObjectAnimator animador;
    Bundle bundle;
    String TAG = "Interfaz";
    private DataBaseManager manager;

    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"DETALLES INICIADO");
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.detalles_lista_layout);

        eliminar =(Button)findViewById(R.id.boton_eliminar);
        actualizar = (Button)findViewById(R.id.boton_actualizar);
        cancelar = (Button)findViewById(R.id.boton_cancelar);
        detalles = (TextView)findViewById(R.id.detalles_usuario);


        actualizar.setOnClickListener(this);
        eliminar.setOnClickListener(this);
        cancelar.setOnClickListener(this);

        //recuperar id
        //comando antes del bundle
        bundle = getIntent().getExtras();
        id_recuperado = bundle.getLong("id");

        manager = new DataBaseManager(this);
        //recupera fecha + concentración
        datos = manager.recuperarPorId(id_recuperado);
        Log.d("DETALLES","SE CARGO EL USUARIO "+datos);
        //extrae el nombre de fecha de la cadena original
        fecha = datos.substring(0,datos.indexOf("|"));
        // extrae la concentración de la cadena original
        concentración = datos.substring(datos.indexOf("|||||")+5,datos.length()-1);
        Log.d(TAG,"fecha: "+fecha +" []: "+concentración);
        detalles.setText("Fecha: \n"+ fecha+"\n\nConcentración:\n"+concentración+" mg/dL");
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
                Toast.makeText(this,"Elemento: "+ fecha +" eliminado correctamente", Toast.LENGTH_SHORT).show();
                Log.d(TAG,""+borrado);
                startActivity(i = new Intent(this,Interfaz.class));
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
            //animarBoton(cancelar);  // metodo para iluminar
            //startActivity( i = new Intent(this,Interfaz.class));
        }

        if(id==R.id.boton_cancelar)
        {
            Log.d(TAG,"CANCELANDO");
            animarBoton(cancelar);  // metodo para iluminar
            startActivity( i = new Intent(this,Interfaz.class));
        }
    }
}
