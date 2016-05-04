package app.proyectoterminal.upibi.glusimo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import app.proyectoterminal.upibi.glusimo.R;

public class Configuracion extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final static String TAG = "Configuracion";
    int[] icons = {R.drawable.ic_menu_view,
            R.drawable.ic_menu_today, R.drawable.ic_menu_search};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG,"OnActivityCreated Lista");

        // cargar los recursos del XML
        String[] titulos_tabs = getResources().getStringArray(R.array.titulos_tabs);
        ListView lista = (ListView) getActivity().findViewById(R.id.lista_config);

        /* simple adapter example
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,titulos_tabs);
        lista.setAdapter(adapter);
        */

        //* custom adapter
        // crear el custom adapter
        CustomAdapter adapter = new CustomAdapter(getContext(),titulos_tabs, icons);
        // agregar el adapter a la list
        lista.setAdapter(adapter);
        //*/

        // AGREGAR LISTENERS
        lista.setOnItemClickListener(this);
        lista.setOnItemLongClickListener(this);



    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    class CustomAdapter extends ArrayAdapter<String>
    {
        Context context;
        int images[];
        String[] texts;
        public CustomAdapter(Context context, String[] texts, int images[])
        {
            super(context, R.layout.custom_listimage_layout,R.id.custom_layout_text,texts);
            this.context = context;
            this.images = images;
            this.texts = texts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if( row == null)
            {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.custom_listimage_layout, parent, false);
            }
            ImageView imagen = (ImageView) row.findViewById(R.id.custom_layout_image);
            TextView texto = (TextView) row.findViewById(R.id.custom_layout_text);

            imagen.setImageResource(images[position]);
            texto.setText(texts[position]);

            return row;
        }
    }
}
