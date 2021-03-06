package app.proyectoterminal.upibi.glusimo.classes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.proyectoterminal.upibi.glusimo.R;
import app.proyectoterminal.upibi.glusimo.fragments.Configuracion;
import app.proyectoterminal.upibi.glusimo.fragments.Curva;
import app.proyectoterminal.upibi.glusimo.fragments.Lista;
import app.proyectoterminal.upibi.glusimo.fragments.Medicion;
import app.proyectoterminal.upibi.glusimo.fragments.Monitor;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter{

    final int PAGE_COUNT = 5;
    // METODO IMPLEMENTADO PARA RECUPERAR TITULOS
    String[] titulos = {"Medición","Registro","Monitor","Curva Diagnóstica","Configuración"};
    int[] icons = {R.drawable.ic_menu_view,
            R.drawable.ic_menu_register, R.drawable.ic_monitor, R.drawable.ic_curva,
            R.drawable.ic_menu_manage};

    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public View getTabView(int position) {
        // Given you have a custom layout in `res/layout/custom_navigation_tabsigation_tabs.xml` with a TextView and ImageView
        View v = LayoutInflater.from(context).inflate(R.layout.custom_navigation_tabs, null);
        TextView tv = (TextView) v.findViewById(R.id.tab_text);
        tv.setText(titulos[position]);
        ImageView img = (ImageView) v.findViewById(R.id.tab_image);
        img.setImageResource(icons[position]);
        return v;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position) {
            case 0:
                Medicion medicion = new Medicion();
                return medicion;
            case 1:
                Lista lista = new Lista();
                return lista;
            case 2:
                Monitor monitor = new Monitor();
                return monitor;
            case 3:
                Curva curva = new Curva();
                return curva;
            case 4:
                Configuracion configuracion = new Configuracion();
                return configuracion;
            default:
                return null;
        }
        //return PageFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        Drawable image = context.getResources().getDrawable(icons[position]);
        image.setBounds(0, 0, 75, 75);
        // Replace blank spaces with image icon
        SpannableString sb = new SpannableString("   " + titulos[position]);
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}
