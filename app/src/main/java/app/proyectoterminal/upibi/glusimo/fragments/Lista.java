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

public class Lista extends Fragment implements AdapterView.OnItemSelectedListener {
    // LIST VIEW
    private ListView lista;

    // SPINNER
    Spinner mes, dia;
    ArrayAdapter adaptermes, adapterdia;

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
        mes = (Spinner) getActivity().findViewById(R.id.filtro_mes);
        dia = (Spinner) getActivity().findViewById(R.id.filtro_dia);

        // adapters
        adaptermes = ArrayAdapter.createFromResource(getContext(),R.array.meses,
                R.layout.custom_spinner_layout);
        adapterdia = ArrayAdapter.createFromResource(getContext(),R.array.dia,
                R.layout.custom_spinner_layout);

        mes.setAdapter(adaptermes);
        dia.setAdapter(adapterdia);

        // MOVER EL CURSOR A CERO
        //cursor = new DataBaseManager(getContext()).posicionCero();


        // FUNCIONES PARA EL CLICK
        mes.setOnItemSelectedListener(this);
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
