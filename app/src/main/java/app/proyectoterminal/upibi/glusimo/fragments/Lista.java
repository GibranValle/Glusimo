package app.proyectoterminal.upibi.glusimo.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class Lista extends Fragment implements AdapterView.OnItemSelectedListener {
    // LIST VIEW
    private ListView lista;

    // SPINNER
    Spinner año, mes, dia;
    ArrayAdapter adapteraño, adaptermes, adapterdia;

    // COMUNICACION ENTRE FRAGMENTS
    EventBus bus = EventBus.getDefault();

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
        año = (Spinner) getActivity().findViewById(R.id.filtro_año);
        mes = (Spinner) getActivity().findViewById(R.id.filtro_mes);
        dia = (Spinner) getActivity().findViewById(R.id.filtro_dia);

        // adapters
        adapteraño = ArrayAdapter.createFromResource(getContext(),R.array.años,
                R.layout.custom_spinner_layout);
        adaptermes = ArrayAdapter.createFromResource(getContext(),R.array.meses,
                R.layout.custom_spinner_layout);
        adapterdia = ArrayAdapter.createFromResource(getContext(),R.array.dia,
                R.layout.custom_spinner_layout);

        año.setAdapter(adapteraño);
        mes.setAdapter(adaptermes);
        dia.setAdapter(adapterdia);

        // FUNCIONES PARA EL CLICK
        año.setOnItemSelectedListener(this);
        mes.setOnItemSelectedListener(this);
        dia.setOnItemSelectedListener(this);

        //METODOS PARA DATABASE
        manager = new DataBaseManager(getContext());
        manager.posicionCero();
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
        Log.i(TAG,"numero recibido en bus Lista: "+event.numero);
    }
    /** //////////////////////// METODOS DE BUS EVENT  /////////////////////////////**/
}
