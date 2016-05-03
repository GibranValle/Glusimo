package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class Lista extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    // LIST VIEW
    private ListView lista;

    SharedPreferences respaldo; // VARIABLE PARA RECUPERAR DATOS
    SharedPreferences.Editor editor;
    // SPINNER
    Spinner sp_año, sp_mes, sp_dia, sp_salud;

    // COMUNICACION ENTRE FRAGMENTS
    EventBus bus = EventBus.getDefault();
    Intent i;

    int hipoglucemia = 70;
    int hiperglucemia = 120;
    int hiperglucemia_severa = 120;

    // BASE DE DATOS
    private SimpleCursorAdapter adaptador;
    private Cursor cursor;
    private DataBaseManager manager;
    String TAG ="Lista";

    // Listas para el spinner
    List<String> dia = new ArrayList<>();
    List<String> año = new ArrayList<>();
    List<String> mes = new ArrayList<>();
    List<String> salud = new ArrayList<>();

    // crear los string para comprobar
    String stringAños;
    String stringMeses;
    String stringDias;
    String stringSalud;

    ArrayAdapter<String> adapterAño;
    ArrayAdapter<String> adapterMes;
    ArrayAdapter<String> adapterSalud;
    ArrayAdapter<String> adapterDia;


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
        sp_salud = (Spinner) getActivity().findViewById(R.id.filtro_estado);

        // agregar los filtros
        año.add("Año:");
        mes.add("Mes:");
        dia.add("Día:");
        salud.add("Estado:");


        // Crear los adapatadores
        adapterAño = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,año);
        adapterMes = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,mes);
        adapterDia = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,dia);
        adapterSalud = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,salud);

        // crear los layout de despliegue
        adapterAño.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterMes.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterDia.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterSalud.setDropDownViewResource(R.layout.custom_drop_spinner_layout);

        // cargar el adapter al spinner
        sp_año.setAdapter(adapterAño);
        sp_mes.setAdapter(adapterMes);
        sp_dia.setAdapter(adapterDia);
        sp_salud.setAdapter(adapterSalud);

        // FUNCIONES PARA EL CLICK
        sp_año.setOnItemSelectedListener(this);
        sp_mes.setOnItemSelectedListener(this);
        sp_dia.setOnItemSelectedListener(this);
        sp_salud.setOnItemSelectedListener(this);

        manager = new DataBaseManager(getContext());

        // AGREGAR LISTENERS
        lista.setOnItemClickListener(this);
        lista.setOnItemLongClickListener(this);

        // ACTUALIZAR LISTA Y SPINNER
        actualizarListView();
        actualizarSpinner();

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

    @Override
    public void onResume()
    {
        super.onResume();

    }

    /** //////////////////////// METODOS DE PERSONALIZADOS  /////////////////////////////**/
    void actualizarListView()
    {
        Log.i(TAG,"Actualizando list de Lista");
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
    void actualizarSpinner()
    {
        Log.i(TAG,"Actualizando spinner de Lista");
        String añosRecuperados, mesesRecuperados, diasRecuperados, estadosRecuperados;
        añosRecuperados = manager.recuperarTodosAños();
        mesesRecuperados = manager.recuperarTodosMeses();
        diasRecuperados = manager.recuperarTodosDias();
        estadosRecuperados = manager.recuperarTodosEstados();

        /*Log.i(TAG,"datos recuperados: años: "+añosRecuperados+" meses: "+mesesRecuperados+" dias: "
                +diasRecuperados);*/

        // cargar los valores de string
        respaldo = getActivity().getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        stringAños = respaldo.getString("años","");
        stringMeses = respaldo.getString("meses","");
        stringDias = respaldo.getString("dias","");
        stringSalud = respaldo.getString("estado","");

        // crear variables para llenar
        String temporal;
        int indexslash;
        int comprobar = comprobarFinLinea(añosRecuperados);



        Log.i(TAG,"entrando al while años");
        // COMPROBAR QUE AÑOS RECUPERADOS TENGA TEXTO AUN
        while (comprobar>0)
        {
            indexslash = añosRecuperados.indexOf("\n",0);
            //Log.d(TAG,"index: "+indexslash);
            temporal = añosRecuperados.substring(0,indexslash+1);
            //Log.i(TAG,"temporal: "+temporal);
            añosRecuperados = añosRecuperados.replaceFirst(temporal,"");
            //Log.i(TAG,"cadena residual:\n"+añosRecuperados);
            if(stringAños.contains(temporal))
            {
                Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                stringAños = stringAños.concat(temporal);
                Log.i(TAG," año agregado: "+stringAños);
                año.add(temporal);
            }
            comprobar = comprobarFinLinea(añosRecuperados);
        }
        Log.i(TAG,"SALIENDO WHILE AÑOS");




        comprobar = comprobarFinLinea(mesesRecuperados);
        Log.i(TAG,"entrando al while, meses");
        // COMPROBAR QUE AÑOS RECUPERADOS TENGA TEXTO AUN
        while (comprobar>0)
        {
            indexslash = mesesRecuperados.indexOf("\n",0);
            //Log.d(TAG,"index: "+indexslash);
            temporal = mesesRecuperados.substring(0,indexslash+1);
            //Log.i(TAG,"temporal: "+temporal);
            mesesRecuperados = mesesRecuperados.replaceFirst(temporal,"");
            //Log.i(TAG,"cadena residual:\n"+añosRecuperados);
            if(stringMeses.contains(temporal))
            {
                Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                stringMeses = stringMeses.concat(temporal);
                Log.i(TAG," mes agregado: "+stringMeses);
                mes.add(temporal);
            }

            // recomprobar
            comprobar = comprobarFinLinea(mesesRecuperados);
        }
        Log.i(TAG,"SALIENDO WHILE MESES");



        comprobar = comprobarFinLinea(diasRecuperados);
        Log.i(TAG,"entrando al while, dias");
        // COMPROBAR QUE AÑOS RECUPERADOS TENGA TEXTO AUN
        while (comprobar>0)
        {
            indexslash = diasRecuperados.indexOf("\n",0);
            //Log.d(TAG,"index: "+indexslash);
            temporal = diasRecuperados.substring(0,indexslash+1);
            //Log.i(TAG,"temporal: "+temporal);
            diasRecuperados = diasRecuperados.replaceFirst(temporal,"");
            //Log.i(TAG,"cadena residual:\n"+añosRecuperados);

            if(stringDias.contains(temporal))
            {
                Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                stringDias = stringDias.concat(temporal);
                Log.i(TAG," dia agregado: "+stringDias);
                dia.add(temporal);
            }
            // recomprobar
            comprobar = comprobarFinLinea(diasRecuperados);
        }
        Log.i(TAG,"SALIENDO WHILE DIAS");


        comprobar = comprobarFinLinea(estadosRecuperados);
        Log.i(TAG,"entrando al while, estados");
        // COMPROBAR QUE AÑOS RECUPERADOS TENGA TEXTO AUN
        while (comprobar>0)
        {
            indexslash = estadosRecuperados.indexOf("\n",0);
            //Log.d(TAG,"index: "+indexslash);
            temporal = estadosRecuperados.substring(0,indexslash+1);
            //Log.i(TAG,"temporal: "+temporal);
            estadosRecuperados = estadosRecuperados.replaceFirst(temporal,"");
            //Log.i(TAG,"cadena residual:\n"+añosRecuperados);

            if(stringSalud.contains(temporal))
            {
                Log.e(TAG,"Ya contiene este valor");
            }
            else
            {
                stringSalud = stringSalud.concat(temporal);
                Log.i(TAG," estado agregado: "+stringSalud);
                salud.add(temporal);
            }
            // recomprobar
            comprobar = comprobarFinLinea(estadosRecuperados);
        }
        Log.i(TAG,"SALIENDO WHILE, estados");


        Log.i(TAG,"tamaño de size: "+año.size());
        Log.i(TAG,"contiene elemento?"+año.contains("Año:"));

        /*
        String[] array_año = new String[ año.size() ];
        año.toArray( array_año );
        */

        /*
        Log.i(TAG,"ver un list: "+ Arrays.toString(año));
        Log.i(TAG,"ver un list: "+mes);
        Log.i(TAG,"ver un list: "+dia);
        Log.i(TAG,"ver un list: "+salud);
        */

        // guardar los strings
        editor = respaldo.edit();
        editor.putString("años", stringAños);
        editor.putString("meses", stringMeses);
        editor.putString("dias", stringDias);
        editor.putString("estado", stringSalud);

        if (editor.commit())
        {
            Log.i(TAG,"Datos guardados correctamente");
        }
        else
        {
            Log.e(TAG,"Error al guardar datos");
        }

        // RECREAR LOS ADAPTER
        adapterAño = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,año);
        adapterMes = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,mes);
        adapterDia = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,dia);
        adapterSalud = new ArrayAdapter<>
                (getContext(), R.layout.custom_spinner_layout,salud);

        // crear los layout de despliegue
        adapterAño.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterMes.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterDia.setDropDownViewResource(R.layout.custom_drop_spinner_layout);
        adapterSalud.setDropDownViewResource(R.layout.custom_drop_spinner_layout);

        // RECARGAR EL ADAPTER AL SPINNER
        sp_año.setAdapter(adapterAño);
        sp_mes.setAdapter(adapterMes);
        sp_dia.setAdapter(adapterDia);
        sp_salud.setAdapter(adapterSalud);
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
        Log.i(TAG," comprobando fin de linea: "+id);
        return id;
    }
    void agregarMedicion(String fecha, int valor)
    {
        Log.i(TAG,"Agragando medicion en Lista");
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

            // RECUPERAR ESTADO
            String estado= respaldo.getString("estado","Error Medición");
            Log.i(TAG,"estado: "+estado);

            // INSERTAR DATOS EN TABLA, RECUPERANDO EL ID SE CONOCE EL ESTADO DE LA EDICION
            long id = manager.insertarDatos(fecha_original , año, mes, dia, nombre_dia,
                    tiempo, estado, concentración);
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
        // String recibida
        Log.i(TAG," string recibido en lista");
        DateFormat df = new SimpleDateFormat("EEEE dd/MMMM/yyyy-HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i(TAG,"numero recibido en bus Lista: "+event.numero);
        Log.i(TAG,"hora de evento: "+date);
        // agregar elemento
        agregarMedicion(date,event.numero);
        actualizarListView();
        actualizarSpinner();
    }
    @Subscribe
    public void onEvent(EnviarStringEvent event)
    {
        String dato = event.mensaje;
        if(dato.equals("DL1"))
        {
            actualizarListView();
            actualizarSpinner();
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
