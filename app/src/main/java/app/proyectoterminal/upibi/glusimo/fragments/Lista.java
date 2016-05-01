package app.proyectoterminal.upibi.glusimo.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import app.proyectoterminal.upibi.glusimo.R;

public class Lista extends Fragment
{
    // LIST VIEW
    private ListView lista;

    // SPINNER


    // BASE DE DATOS
    private SimpleCursorAdapter adaptador;
    private Cursor cursor;
    String TAG ="UNOLISTA";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);


    }
}
