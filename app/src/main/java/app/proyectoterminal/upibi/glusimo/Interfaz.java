package app.proyectoterminal.upibi.glusimo;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.proyectoterminal.upibi.glusimo.Bluetooth.BluetoothSPP;
import app.proyectoterminal.upibi.glusimo.Bluetooth.BluetoothState;
import app.proyectoterminal.upibi.glusimo.Bluetooth.DeviceList;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarIntEvent;
import app.proyectoterminal.upibi.glusimo.Bus.EnviarStringEvent;
import app.proyectoterminal.upibi.glusimo.classes.SampleFragmentPagerAdapter;
import app.proyectoterminal.upibi.glusimo.fragments.AcercaDe;

public class Interfaz extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    /** /////////////////////////// CONSTANTES  ///////////////////////////////////*/
    public static final String TAG = "Interfaz";
    private static final int curvaGlucosaNormal[] = {84, 130, 127, 100, 85, 80};
    private static final int curvaGlucosaPrediabetes[] = {80, 189, 127, 134, 143, 99};
    private static final int curvaGlucosaDiabetes[] = {84, 160, 220, 200, 186, 150};
    private static final int tiempo[] = {0, 30, 60, 90, 120, 150};
    /** /////////////////////////// CONSTANTES  ///////////////////////////////////*/

    /** ///////////////////// VARIABLES GLOBALES ///////////////////////////////////*/
    ViewPager viewPager;
    SharedPreferences respaldo;
    SharedPreferences.Editor editor;
    TextView estado_conexion, estado_paciente;
    Button boton_conexion;
    EventBus bus = EventBus.getDefault();
    BluetoothSPP bt;
    String address, name;
    boolean conectado = false;
    ObjectAnimator animador;
    /** ///////////////////// VARIABLES GLOBALES ///////////////////////////////////*/




    /** -------------------- METODOS DE ETAPAS --------------------------------*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfaz);

        Log.v(TAG, "On Create");

        bus.register(this);

        estado_conexion = (TextView) findViewById(R.id.consola);
        estado_paciente = (TextView) findViewById(R.id.consola_paciente);
        boton_conexion = (Button) findViewById(R.id.boton_conexion);

        //  ----------------- BLUETOOTH -----------------------------------------//
        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable())
        {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }


        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message)
            {
                String dato;
                int valor;
                //textRead.append(message + "\n");
                if(message.startsWith("V"))
                {
                    Log.d(TAG,"mensaje recibido: "+message+" largo: "+message.length());
                    dato = message.substring(1,message.length());
                    valor = Integer.parseInt(dato);
                    Log.d(TAG,"conversion correcta a entero: "+valor);
                    bus.post(new EnviarIntEvent(valor));
                }


            }
        });


        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener()
        {
            public void onDeviceDisconnected()
            {
                // CAMBIAR TEXTO DE CONSOLA CONEXION
                estado_conexion.setText(R.string.bt_dc);
                estado_conexion.setBackgroundResource(R.color.colorOff);
                boton_conexion.setVisibility(View.VISIBLE);
                Log.e(TAG,"SE PERDIÓ LA CONEXION");
                bus.post(new EnviarStringEvent("ID"));
                /*
                textStatus.setText("Status : Not connect");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
                */
            }

            public void onDeviceConnectionFailed()
            {
                // CAMBIAR TEXTO DE CONSOLA CONEXION
                estado_conexion.setText(R.string.bt_dc);
                estado_conexion.setBackgroundResource(R.color.colorOff);
                boton_conexion.setVisibility(View.VISIBLE);
                Toast.makeText(Interfaz.this, getResources().getString(R.string.bt_error),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG,"LISTENER FALLIDO");
                conectado = false;
            }

            public void onDeviceConnected(String name, String address)
            {

            }
        });

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener()
        {
            public void onServiceStateChanged(int state)
            {
                if(state == BluetoothState.STATE_CONNECTED)
                {
                    // Do something when successfully connected
                    // CAMBIAR TEXTO DE CONSOLA CONEXION
                    estado_conexion.setText(R.string.bt_ct);
                    estado_conexion.setBackgroundResource(R.color.colorOn);
                    // ESCONDER BOTON DE CONEXION
                    boton_conexion.setVisibility(View.GONE);
                    Toast.makeText(Interfaz.this, getResources().
                            getString(R.string.bt_exito)+" "+name,
                            Toast.LENGTH_SHORT).show();
                    conectado = true;
                    bt.send("C",true);
                    // CAMBIAR INSTRUCCIÓN DEL FRAGMENT
                    bus.post(new EnviarStringEvent("IC"));
                }
                else if(state == BluetoothState.STATE_CONNECTING)
                {
                    // Do something while connecting
                    estado_conexion.setText(R.string.bt_cting);
                    estado_conexion.setBackgroundResource(R.color.colorConecting);
                    // ESCONDER BOTON DE CONEXION
                    boton_conexion.setVisibility(View.GONE);
                    // ENVIAR MENSAJE PARA AVISAR DE LA CONEXION EXITOSA
                }
                else if(state == BluetoothState.STATE_LISTEN)
                {
                    // Do something when device is waiting for connection
                }
                else if(state == BluetoothState.STATE_NONE)
                {
                    // Do something when device don't have any connection
                }
            }
        });
        //  ----------------- BLUETOOTH -----------------------------------------//


        // medidor
        // AL INICIAR OCULTA LA CONSOLA DE PACIENTE NO EXISTE, OCULTARLA Y MOSTRAR BOTON
        estado_conexion.setVisibility(View.VISIBLE);
        estado_paciente.setVisibility(View.GONE);
        boton_conexion.setVisibility(View.VISIBLE);

        boton_conexion.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.pager);
        SampleFragmentPagerAdapter pagerAdapter =
                new SampleFragmentPagerAdapter(getSupportFragmentManager(), Interfaz.this);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vibrar(50);
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                vibrar(50);
            }
        });

        /* NAVIGATION DRAWER */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /* NAVIGATION DRAWER */
    }
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "On Resume");

        if (!bt.isBluetoothEnabled()) // no habilitado
        {
            // para arrancar el bluetooth sin pedir permiso:
            bt.enable();

            // PARA PEDIR PERMISO PARA REALIZAR LA CONEXION:
            /*
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            */
        }
        else
        {
            if(!bt.isServiceAvailable())
            {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
        cargarDatos();
    }
    protected void onDestroy() {
        super.onDestroy();

        if (bt.isBluetoothEnabled()) // no habilitado
        {
            bt.stopService();
            bt.disconnect();
            bt.disable();
        }
    }
    /** -------------------- METODOS DE ETAPAS --------------------------------*/



    /** -------------------- METODOS DE MENU --------------------------------*/
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_interfaz, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_interfaz) {
            vibrar(100);
            // LA POSICION 3 ES CONFIG
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(3);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /** -------------------- METODOS DE MENU --------------------------------*/




    /** -------------------- METODOS DE NAVIGATION --------------------------------*/
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        vibrar(50);
        if (id == R.id.nav_check)
        {
            // LA POSICION 0 ES MEDICION
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            if(conectado)
            {
                bt.send("M",true);
            }
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_reg) {
            // LA POSICION 0 ES tendencias
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_search) {
            // LA POSICION 0 ES lista
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(2);
        } else if (id == R.id.nav_config) {
            // LA POSICION 0 ES config
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(3);
        } else if (id == R.id.nav_share) {
            Toast.makeText(Interfaz.this, "ESPÉRALO EN FUTURAS VERSIONES", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_send)
        {
            Toast.makeText(Interfaz.this, "ESPÉRALO EN FUTURAS VERSIONES", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_conect)
        {
            // COMO NO ES UN DISPOSITIVO ANDROID USAR:
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            // bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
        else if (id == R.id.nav_acerca) {
            startActivity(new Intent(this, AcercaDe.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /** -------------------- METODOS DE NAVIGATION --------------------------------*/





    /** -------------------- METODOS DE IMPLEMENTS --------------------------------*/
    public void onClick(View v)
    {
        if (v.getId() == R.id.boton_conexion)
        {
            vibrar(50);
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }
    /** -------------------- METODOS DE IMPLEMENTS --------------------------------*/


    /** ////////////////////////// METODOS PERSONALIZADOS ////////////////////////////// */
    void cargarDatos()
    {
        // RECUPERAR LOS DATOS GUARDADOS POR EL USUARIO PREVIAMENTE
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        //conectado = respaldo.getBoolean("conectado",false);
        //paso = Integer.parseInt(respaldo.getString("paso", "2"));
    }
    void vibrar(int ms) {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }
    /** ////////////////////////// METODOS PERSONALIZADOS ////////////////////////////// */

    /** ////////////////////////// METODOS EXTRAS DE BLUETOOTH ////////////////////////////// */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.v(TAG, "onActivityResult " + resultCode);

        switch (requestCode)
        {
            case BluetoothState.REQUEST_CONNECT_DEVICE:
            // DeviceList Regresa información para conectarse a dispositivo
            if (resultCode == Activity.RESULT_OK)
            {
                address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                name = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_NAME);
                Log.v(TAG,"address del dispositivo: "+address);
                bt.connect(data);
                //bt.connect();
                //conectarDevice(data, true);
                //Log.d(TAG, "CONEXION SEGURA, DISPOSITIVO");
            }
        }

        /*
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Log.d(TAG,"Interfaz Activity Result ok");
                bt.connect(data);
            }

        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
            else
            {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth no habilitado"
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        */
    }

    /** ////////////////////////// METODOS EXTRAS DE BLUETOOTH ////////////////////////////// */


    /** ////////////////////////// METODOS PARA COMUNICACION FRAGMENT ////////////////////////////// */
    @Subscribe
    public void onEvent(EnviarStringEvent event) {
        // Esperar a recibir string de algun fragment
        // crear una variable para recibir datos
        String datos = event.mensaje;
        String id = datos.substring(0,1);
        Log.d(TAG,"mensaje recibido en Activity: "+datos+" con id: "+id);
        // Procesar la primera letra para saber de que fragment viene

        // FRAGMENT DE MEDICION
        if(id.equals("M"))
        {
            String mensaje = datos.substring(1,datos.length());
            Log.d(TAG,"Mensaje desde medición "+mensaje);

            if(mensaje.startsWith("C"))
            {
                // Medicion necesita saber el estado de conexion
                if(conectado)
                {
                    bt.send("M",true);
                }
                else
                {
                    Log.d(TAG,"sin conexion, mensaje no enviado");
                    Toast.makeText(Interfaz.this, getResources().getString(R.string.conectar),
                            Toast.LENGTH_SHORT).show();
                }
            }

            // realizar tarea segun el mensaje desde medicion
        }

        /*
        String dato;
        Log.d(TAG,"enviando mensaje: "+event.mensaje);
        if(event.mensaje.startsWith("M"))
        {
            dato = event.mensaje.substring(1,event.mensaje.length());
            Log.d(TAG,"texto corregido en bus: "+dato);
        }
        */
    }

    @Subscribe
    public void onEvent(EnviarIntEvent event) {
        Log.d(TAG,"numero recibido en bus: "+event.numero);
    }
    /** ////////////////////////// METODOS PARA COMUNICACION FRAGMENT ////////////////////////////// */
}
