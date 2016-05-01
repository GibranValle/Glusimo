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

public class Lista extends Fragment
{
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
                android.R.layout.simple_dropdown_item_1line);
        adapterdia = ArrayAdapter.createFromResource(getContext(),R.array.dia,
                android.R.layout.simple_dropdown_item_1line);

        mes.setAdapter(adaptermes);
        dia.setAdapter(adapterdia);

        // MOVER EL CURSOR A CERO
        //cursor = new DataBaseManager(getContext()).posicionCero();


        // FUNCIONES PARA EL CLICK
        mes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            }
        });

        dia.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            }
        });
    }
}
