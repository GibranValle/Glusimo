package app.proyectoterminal.upibi.glusimo.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.classes.DataBaseManager;

public class Lista extends Fragment implements AdapterView.OnItemSelectedListener {
    // LIST VIEW
    private ListView lista;

    // SPINNER
    Spinner año, mes, dia;
    ArrayAdapter adapteraño, adaptermes, adapterdia;

    // BASE DE DATOS
    private SimpleCursorAdapter adaptador;
    private Cursor cursor;
    String TAG ="Interfaz";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

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

        // METODOS PARA DATABASE
        cursor = new DataBaseManager(getContext()).posicionCero();
        // CREAR EL ARREGLO DE INICIO
        String [] from = {DataBaseManager.C_FECHA, DataBaseManager.C_AÑO, DataBaseManager.C_MES,
                DataBaseManager.C_DIA, DataBaseManager.C_TIEMPO, DataBaseManager.C_CONCENTRACIÓN};

        // CREAR EL ARREGLO DE ID
        int [] to = {R.id.out_elemento, R.id.out_precio, R.id.out_extra, R.id.out_precio_extra, R.id.out_inventario};
        adaptador = new SimpleCursorAdapter(this, R.layout.detalles_lista, cursor, from, to);
        lista.setAdapter(adaptador);



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

}
