package app.proyectoterminal.upibi.glusimo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

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
    public static final int curvaGlucosaNormal[] = {84, 130, 127, 100, 85, 80};
    public static final int curvaGlucosaPrediabetes[] = {80, 189, 127, 134, 143, 99};
    public static final int curvaGlucosaDiabetes[] = {84, 160, 220, 200, 186, 150};
    public static final int tiempo[] = {0, 30, 60, 90, 120, 150};
    /** /////////////////////////// CONSTANTES  ///////////////////////////////////*/

    /** ///////////////////// VARIABLES GLOBALES ///////////////////////////////////*/
    ViewPager viewPager;
    SharedPreferences respaldo;
    SharedPreferences.Editor editor;

    TextView estado_conexion, estado_paciente;
    Button boton_conexion;
    EventBus bus = EventBus.getDefault();
    LinearLayout niveles;
    BluetoothSPP bt;
    String address, name;
    ImageView bateria, reservorio;
    // TIMER
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();  // VARIABLE PARA TIMER

    boolean conectado = false;
    boolean monitorizar;
    int frecuencia_monitoreo;
    boolean monitoreando;

    /** ///////////////////// VARIABLES GLOBALES ///////////////////////////////////*/




    /** -------------------- METODOS DE ETAPAS --------------------------------*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interfaz_layout_uno);

        Log.v(TAG, "On Create");

        bus.register(this);
        bateria = (ImageView) findViewById(R.id.bateria);
        reservorio = (ImageView) findViewById(R.id.farmaco);
        estado_conexion = (TextView) findViewById(R.id.consola);
        estado_paciente = (TextView) findViewById(R.id.consola_paciente);
        boton_conexion = (Button) findViewById(R.id.boton_conexion);
        niveles = (LinearLayout) findViewById(R.id.niveles);

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
                Log.d(TAG,"mensaje completo: "+message);
                //textRead.append(message + "\n");

                if(message.startsWith("B"))
                {
                    //MEDICION DE BATERIA
                    Log.d(TAG,"mensaje recibido: "+message+" largo: "+message.length());
                    dato = message.substring(1,message.length());
                    valor = Integer.parseInt(dato);
                    // modificar valor para obtener algo real.
                    Log.d(TAG,"conversion correcta a entero: "+valor);
                    // logica de valor
                    if(valor > 90)
                    {
                        Log.d(TAG,"bateria llena");
                        // poner imagen de bateria llena 91-100%
                        bateria.setImageResource(R.drawable.bat_100);
                    }

                    else if(valor > 70)
                    {
                        Log.d(TAG,"bateria al 71%%");
                        // poner imagen de bateria llena 71-91%
                        bateria.setImageResource(R.drawable.bat_80);
                    }

                    else if(valor > 50)
                    {
                        Log.d(TAG,"bateria 51%");
                        // poner imagen de bateria llena 51-70%
                        bateria.setImageResource(R.drawable.bat_60);
                    }

                    else if(valor > 30)
                    {
                        Log.d(TAG,"bateria 31%");
                        // poner imagen de bateria llena 31-50%
                        bateria.setImageResource(R.drawable.bat_40);
                    }

                    else if(valor > 15)
                    {
                        // poner imagen de bateria llena 16-30%
                        Log.d(TAG,"bateria 16%");
                        bateria.setImageResource(R.drawable.bat_20);
                    }

                    else if(valor >= 0)
                    {
                        // poner imagen de bateria llena 0-15%
                        Log.d(TAG,"bateria 0%");
                        bateria.setImageResource(R.drawable.bat_0);
                    }
                }

                else if(message.startsWith("D"))
                {
                    //MEDICION DE DOSIFICADOR (FARMACO)
                    Log.d(TAG,"mensaje recibido: "+message+" largo: "+message.length());
                    dato = message.substring(1,message.length());
                    valor = Integer.parseInt(dato);
                    Log.d(TAG,"conversion correcta a entero: "+valor);
                    // logica de valor
                    if(valor > 90)
                    {
                        Log.d(TAG,"dosificador lleno");
                        // poner imagen de dosificador lleno 91-100%
                        reservorio.setImageResource(R.drawable.dos_100);
                    }

                    else if(valor > 70)
                    {
                        Log.d(TAG,"dosificador al 71%");
                        // poner imagen de dosificador lleno 71-90%
                        reservorio.setImageResource(R.drawable.dos_80);
                    }

                    else if(valor > 50)
                    {
                        Log.d(TAG,"dosificador al 51%");
                        // poner imagen de dosificador lleno 51-70%
                        reservorio.setImageResource(R.drawable.dos_60);
                    }

                    else if(valor > 30)
                    {
                        Log.d(TAG,"dosificador al 31%");
                        // poner imagen de dosificador lleno 31-50%
                        reservorio.setImageResource(R.drawable.dos_40);
                    }

                    else if(valor > 15)
                    {
                        Log.d(TAG,"dosificador al 16%");
                        // poner imagen de dosificador lleno 66-30%
                        reservorio.setImageResource(R.drawable.dos_20);
                    }

                    else if(valor >= 0)
                    {
                        Log.d(TAG,"dosificador 0%");
                        // poner imagen de beteria vacia   0 - 15%
                        reservorio.setImageResource(R.drawable.dos_0);
                    }
                }

                else if(message.startsWith("G"))
                {
                    //MEDICION DE GLUCEMIA
                    Log.d(TAG,"mensaje recibido: "+message+" largo: "+message.length());
                    dato = message.substring(1,message.length());
                    valor = Integer.parseInt(dato);
                    Log.d(TAG,"conversion correcta a entero: "+valor);
                    // modificar valor para obtener algo real.
                    bus.post(new EnviarIntEvent(valor));
                    Log.d(TAG,"enviando glucemia: "+valor);
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
                niveles.setVisibility(View.GONE);
                Log.e(TAG,"SE PERDIÓ LA CONEXION");
                bus.post(new EnviarStringEvent("ID"));
                monitoreando = false;
                pararMonitoreo();
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
                editor = respaldo.edit();
                editor.putBoolean("conectado", conectado);
                if(editor.commit())
                {
                    Log.d(TAG,"conectado guardado");
                }
                monitoreando = false;
                pararMonitoreo();
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
                    niveles.setVisibility(View.VISIBLE);
                    estado_conexion.setText(R.string.bt_ct);
                    estado_conexion.setBackgroundResource(R.color.colorOn);
                    // ESCONDER BOTON DE CONEXION
                    boton_conexion.setVisibility(View.GONE);
                    Toast.makeText(Interfaz.this, getResources().
                            getString(R.string.bt_exito)+" "+name,
                            Toast.LENGTH_SHORT).show();
                    conectado = true;
                    editor = respaldo.edit();
                    editor.putBoolean("conectado", conectado);
                    if(editor.commit())
                    {
                        Log.d(TAG,"conectado guardado");
                    }
                    if(monitorizar)
                    {
                        // DECIRLE AL DEVICE QUE SE ESTABLECIÓ LA CONEXION Y SE QUIERE MONITOREAR
                        bt.send("O",true);
                        monitoreando = true;
                        long periodo = frecuencia_monitoreo*60*1000;
                        empezarMonitore(periodo);
                    }
                    else
                    {
                        // AVISAR AL DEVICE QUE SE ESTABLECIÓ LA CONEXION
                        bt.send("C",true);
                        // MONITOREO ONLINE
                    }
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
    protected void onResume()
    {
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
            viewPager.setCurrentItem(4);
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
        }
        else if (id == R.id.nav_reg)
        {
            // LA POSICION 0 ES tendencias
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(1);
        }
        else if (id == R.id.nav_monitor)
        {
            // LA POSICION 0 ES lista
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(2);
        }
        else if (id == R.id.nav_curva)
        {
            // LA POSICION 0 ES config
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(3);
        }
        else if (id == R.id.nav_config)
        {
            // LA POSICION 0 ES config
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(4);
        }
        else if (id == R.id.nav_share)
        {
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
        monitorizar = respaldo.getBoolean("monitorizar",true);
        frecuencia_monitoreo = respaldo.getInt("frec",12);
    }
    void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(ms);
    }
    public void empezarMonitore(long periodo)
    {
        //instanciar nuevo timer
        timer = new Timer();
        //inicializar el timer
        MonitoreoTask();
        //esperar 0ms para empezar, repetir cada 100ms
        timer.schedule(timerTask, 0, periodo); //
    }
    public void pararMonitoreo() {
        //parar el timer, si no esta vacio
        if (timer != null)
        {
            timer.purge();
            timer.cancel();
            timer = null;
            Log.w(TAG,"timer cancelado");
        }
    }

    public void MonitoreoTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run()
                    {
                        //graficarDemo(x,y);
                        monitoreo();
                    }
                });
            }
        };
    }

    public void monitoreo()
    {
        bt.send("O",true);
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

        if(id.equals("M"))
        {
            String mensaje = datos.substring(1,datos.length());
            Log.d(TAG,"Mensaje desde medición "+mensaje);

            if(mensaje.startsWith("M"))
            {
                if(conectado)
                {
                    bt.send("M",true);
                    Log.d(TAG,"SE HA ENVIADO UN MENSAJE DESDE INTERFAZ");
                }
                else
                {
                    Log.d(TAG,"sin conexion, mensaje no enviado");
                    Toast.makeText(Interfaz.this, getResources().getString(R.string.conectar),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else if(mensaje.startsWith("D"))
            {
                if(conectado)
                {
                    // EL MODO DEMO ESTA ACTIVADO
                    bt.send("D", true);
                    Log.d(TAG, "SE HA ENVIADO UN MENSAJE DESDE INTERFAZ, COMO MODO DEMO");
                }
                else
                {
                    Log.d(TAG,"sin conexion, mensaje no enviado");
                    Toast.makeText(Interfaz.this, getResources().getString(R.string.conectar),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        // realizar tarea segun el mensaje desde medicion
        else if(id.equals("D"))
        {
            String mensaje = datos.substring(1,datos.length());
            if(mensaje.startsWith("L"))
            {
                Log.d(TAG,"Mensaje desde Detalles Lista "+mensaje);
                // LANZAR EL FRAGMENT DONDE SE QUEDÓ
                viewPager.setCurrentItem(1);
            }
        }

        else if(id.equals("F"))
        {
            Log.d(TAG,"Mensaje desde Config Fragment");
            String mensaje = datos.substring(1,datos.length());
            if(mensaje.startsWith("0"))
            {
                // LANZAR EL FRAGMENT DONDE SE QUEDÓ
                viewPager.setCurrentItem(0);
            }
            else if(mensaje.startsWith("1"))
            {
                // LANZAR EL FRAGMENT DONDE SE QUEDÓ
                viewPager.setCurrentItem(1);
            }
            else if(mensaje.startsWith("2"))
            {
                // LANZAR EL FRAGMENT DONDE SE QUEDÓ
                viewPager.setCurrentItem(2);
            }
            else if(mensaje.startsWith("3"))
            {
                // LANZAR EL FRAGMENT DONDE SE QUEDÓ
                viewPager.setCurrentItem(3);
            }
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
