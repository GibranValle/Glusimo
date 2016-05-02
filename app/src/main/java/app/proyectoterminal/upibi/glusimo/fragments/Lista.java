package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class Lista extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    // LIST VIEW
    private ListView lista;

    // SPINNER
    Spinner sp_año, sp_mes, sp_dia;
    ArrayAdapter adapteraño, adaptermes, adapterdia;

    // COMUNICACION ENTRE FRAGMENTS
    EventBus bus = EventBus.getDefault();
    Intent i;

    // BASE DE DATOS
    private SimpleCursorAdapter adaptador;
    private Cursor cursor;
    private DataBaseManager manager;
    String TAG ="Interfaz";

    int conteo = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.v(TAG,"Creando vista de Lista");
        // REGISTRAR SI NO SE HA REGISTRADO
        if (!bus.isRegistered(this))
        {
            Log.v(TAG,"Registrando en bus el fragment Lista");
            bus.register(this);
        }
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.v(TAG,"OnActivityCreated Lista");

        // recuperar recurso xml
        lista = (ListView) getActivity().findViewById(R.id.list_registro);
        sp_año = (Spinner) getActivity().findViewById(R.id.filtro_año);
        sp_mes = (Spinner) getActivity().findViewById(R.id.filtro_mes);
        sp_dia = (Spinner) getActivity().findViewById(R.id.filtro_dia);

        // adapters
        adapteraño = ArrayAdapter.createFromResource(getContext(),R.array.años,
                R.layout.custom_spinner_layout);
        adaptermes = ArrayAdapter.createFromResource(getContext(),R.array.meses,
                R.layout.custom_spinner_layout);
        adapterdia = ArrayAdapter.createFromResource(getContext(),R.array.dias,
                R.layout.custom_spinner_layout);

        sp_año.setAdapter(adapteraño);
        sp_mes.setAdapter(adaptermes);
        sp_dia.setAdapter(adapterdia);

        // FUNCIONES PARA EL CLICK
        sp_año.setOnItemSelectedListener(this);
        sp_mes.setOnItemSelectedListener(this);
        sp_dia.setOnItemSelectedListener(this);

        manager = new DataBaseManager(getContext());
        actualizarListView();

        // AGREGAR LISTENERS
        lista.setOnItemClickListener(this);
        lista.setOnItemLongClickListener(this);

        /* PRUEBA DE LISTVIEW
        String[] meses = {"Enero","Feberero","Marzo","Abril","Mayo"};
        String data = manager.recuperarTodos();
        Log.i(TAG,"recuperados: "+data);
        // adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.custom_listview_layout,meses);
        // asignar el adapter a la lista para cargar datos
        lista.setAdapter(adapter);
        */
    }


    /** //////////////////////// METODOS DE PERSONALIZADOS  /////////////////////////////**/
    void actualizarListView()
    {
        //METODOS PARA DATABASE
        // MOVER EL CURSOR A CERO
        cursor = manager.posicionCero();
        // CREAR EL ARREGLO DE INICIO
        String [] from = {DataBaseManager.C_FECHA_CORTA, DataBaseManager.C_TIEMPO,
                DataBaseManager.C_CONCENTRACIÓN};
        // CREAR EL ARREGLO DE ID
        int [] to = {R.id.out_fecha, R.id.out_tiempo, R.id.out_concentracion};
        adaptador = new SimpleCursorAdapter(getContext(),
                R.layout.activity_lista_layout, cursor, from, to);
        lista.setAdapter(adaptador);
        /*
        // CREAR EL ARREGLO DE INICIO
        String [] from = {DataBaseManager.C_AÑO, DataBaseManager.C_MES,
                DataBaseManager.C_DIA,DataBaseManager.C_TIEMPO,
                DataBaseManager.C_CONCENTRACIÓN};
        // CREAR EL ARREGLO DE ID
        int [] to = {R.id.out_año, R.id.out_mes, R.id.out_dia, R.id.out_tiempo, R.id.out_concentracion};
        adaptador = new SimpleCursorAdapter(getContext(),
                R.layout.activity_lista_layout, cursor, from, to);
        lista.setAdapter(adaptador);
        */
    }
    void agregarMedicion(String fecha, int valor)
    {
        // comprobar que no exista la misma medición
        String fecha_original = fecha;
        Log.i(TAG,"fecha original: "+fecha_original);
        String comprobando = manager.comprobar(fecha);
        // SI REGRESA VACIO, NO EXISTE EN LA BASE DE DATOS, ENTONCES, CONTINUAR AGREGANDOLO
        if (comprobando.equals(""))
        {
            Log.i(TAG,"separando datos");

            // index de " " para cortar los datos
            int indexslash = fecha.indexOf(" ");
            // formar el string año
            String nombre_dia = fecha.substring(0,indexslash);
            // recortar el año de la cadane original
            fecha = fecha.replace(nombre_dia+" ","");
            Log.i(TAG,"dia: "+nombre_dia);

            indexslash = fecha.indexOf("/");
            String dia = fecha.substring(0,indexslash);
            fecha = fecha.replace(dia+"/","");
            Log.i(TAG,"dia: "+dia);

            // index de "/" para cortar los datos
            indexslash = fecha.indexOf("/");
            // formar el string año
            String mes = fecha.substring(0,indexslash);
            // recortar el año de la cadane original
            fecha = fecha.replace(mes+"/","");
            Log.i(TAG,"mes: "+mes);

            // encontrar el proximo "-"
            indexslash = fecha.indexOf("-");
            // formar el string de mes
            String año = fecha.substring(0,indexslash);
            // recortar el mes de la cadena original
            fecha = fecha.replace(año+"-","");
            Log.i(TAG,"año: "+año);

            String tiempo = fecha;
            Log.i(TAG,"tiempo: "+tiempo);

            String concentración = String.valueOf(valor);
            Log.i(TAG,"concentración: "+concentración);
            // INSERTAR DATOS EN TABLA, RECUPERANDO EL ID SE CONOCE EL ESTADO DE LA EDICION
            long id = manager.insertarDatos(fecha_original , año, mes, dia, nombre_dia,
                    tiempo, concentración);
            // SI EL VALOR REGRESADO ES -1 EL METODO NO SE LLEVO A CABO CORRECTAMENTE
            if (id < 0)
            {
                Toast.makeText(getContext(), R.string.error_db, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getContext(), R.string.exito_db, Toast.LENGTH_SHORT).show();
            }
        }
    }
    /** //////////////////////// METODOS DE PERSONALIZADOS  /////////////////////////////**/




    /**  ///////////////////////   METODOS DE SPINNER    /////////////////////////**/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
    /**  ///////////////////////   METODOS DE SPINNER    /////////////////////////**/




    /** //////////////////////// METODOS DE BUS EVENT  /////////////////////////////**/
    @Subscribe
    public void onEvent(EnviarIntEvent event)
    {
        DateFormat df = new SimpleDateFormat("EEEE dd/MMMM/yyyy-HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i(TAG,"numero recibido en bus Lista: "+event.numero);
        Log.i(TAG,"hora de evento: "+date);
        // agregar elemento
        agregarMedicion(date,event.numero);
        actualizarListView();
    }
    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        String dato = event.mensaje;
        if(dato.equals("DL1"))
        {
            actualizarListView();
        }
    }
    /** //////////////////////// METODOS DE BUS EVENT  /////////////////////////////**/



    /** //////////////////////// METODOS DE LISTVIEW  /////////////////////////////**/
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(TAG," "+id);
        i = new Intent(getContext(),DetallesLista.class);
        //recupera el ID del item seleccionado
        i.putExtra("id",id);
        startActivity(i);
        i.putExtra("fragment",1);
        return true;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(TAG," "+id);
        i = new Intent(getContext(),DetallesLista.class);
        //recupera el ID del item seleccionado y lo pasa a detalleslista
        i.putExtra("id",id);
        i.putExtra("fragment",1);
        startActivity(i);
    }
    /** //////////////////////// METODOS DE LISTVIEW  /////////////////////////////**/
}
