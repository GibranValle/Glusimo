package app.proyectoterminal.upibi.glusimo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class Interfaz extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String TAG = "Interfaz";
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final boolean D = true;
    private static final int curvaGlucosaNormal[] = {84, 130, 127, 100, 85, 80};
    private static final int curvaGlucosaPrediabetes[] = {80, 189, 127, 134, 143, 99};
    private static final int curvaGlucosaDiabetes[] = {84, 160, 220, 200, 186, 150};
    private static final int tiempo[] = {0, 30, 60, 90, 120, 150};
    /* BLUETOOTH */
    String lectura;
    ViewPager viewPager;
    SharedPreferences respaldo;             // VARIABLE PARA RECUPERAR DATOS
    TextView estado_conexion, estado_paciente;
    Button boton_conexion;
    private StringBuffer outStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter BTadaptador = null;
    // Member object for the chat services
    private BluetoothManager BTservice = null;
    // Name of the connected device
    private String connectedDeviceName = null;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // APLICAR LOS CAMBIOS DE COLOR EN LA INTERFAZ CUANDO DETECTA UN CAMBIO EN ESTADO
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            estado_conexion.setText(R.string.bt_ct);
                            estado_conexion.setBackgroundResource(R.drawable.rect_verde);
                            Log.d(TAG, " BT CONECTADO");
                            Log.i(TAG, "RECIBIENDO DATOS, ESPERANDO ASISTOLIA");
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            //stopTimer();
                            estado_conexion.setText(R.string.bt_cting);
                            estado_conexion.setBackgroundResource(R.drawable.rect_azul);
                            Log.d(TAG, " BT CONECTANDO");
                            break;
                        case BluetoothManager.STATE_LISTEN:
                            estado_conexion.setText(R.string.bt_dc);
                            estado_conexion.setBackgroundResource(R.drawable.rect_gris);
                            Log.d(TAG, " BT DESCONECTADO");
                            break;
                        case BluetoothManager.STATE_NONE:
                            estado_conexion.setBackgroundResource(R.drawable.rect_gris);
                            estado_conexion.setText(R.string.bt_dc);
                            Log.d(TAG, " BT DESCONECTADO");
                            enviarMensaje("F");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    //Log.e(TAG, "mensaje llegando: " + readMessage);
                    int largo = readMessage.length();
                    int inicio = readMessage.indexOf("V");
                    int fin = inicio + 4;
                    int palabrasEnteras = largo / 4;

                    //Log.e(TAG, "largo : " + largo +" inicio: "+inicio +" fin: "+fin);
                    if (largo >= 4 && largo <= 1000 && inicio >= 0) {
                        if (!readMessage.endsWith("\r") || !readMessage.endsWith("\n")) {
                            palabrasEnteras = palabrasEnteras - 1;
                        }
                        //Log.e(TAG, "palabras enteras : " + palabrasEnteras);
                        for (int j = 1; j <= palabrasEnteras; j++) {

                            lectura = readMessage.substring(inicio + 1, fin);
                            //Log.e(TAG, "palabraSeparada : " + lectura+" punto: "+punto);
                            readMessage = readMessage.replaceFirst(readMessage, readMessage);
                            inicio = inicio + 4;
                            fin = fin + 4;
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Conectado a " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfaz);

        estado_conexion = (TextView) findViewById(R.id.consola);
        estado_paciente = (TextView) findViewById(R.id.consola_paciente);
        boton_conexion = (Button) findViewById(R.id.boton_conexion);

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
                vibrar(100);
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                vibrar(100);
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

        /*  BLUETOOTH   */
        // Obtener el adaptador y comprobar soporte de BT
        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        if (BTadaptador == null) {
            Log.e(TAG, "NO SOPORTA BT");
            finish();
        }
        /*  BLUETOOTH   */
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        // carga datos y los despliega en el textview
        cargarDatos();


        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
        if (!BTadaptador.isEnabled())//habilitar si no lo esta
        {
            BTadaptador.enable();
        }
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_NONE) {
                BTservice.start();
                //Toast.makeText(this, "Encendiendo Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        // configurar el servicio de BT
        if (BTservice == null) configurar();

        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "On Destroy");

        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            BTservice.stop();
            Toast.makeText(this, "Apagando Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

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
        if (id == R.id.action_settings) {
            vibrar(100);
            // LA POSICION 3 ES CONFIG
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
            viewPager.setCurrentItem(3);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        vibrar(100);
        if (id == R.id.nav_check) {
            // LA POSICION 0 ES MEDICION
            // ESTE METODO SE DESPLAZA AL FRAGMENT ELEGIDO
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
        } else if (id == R.id.nav_send) {
            Toast.makeText(Interfaz.this, "ESPÉRALO EN FUTURAS VERSIONES", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_acerca) {
            startActivity(new Intent(this, AcercaDe.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * /////////////////////////// METODOS PERSONALIZADOS /////////////////////////////////////
     */
    void cargarDatos() {
        // RECUPERAR LOS DATOS GUARDADOS POR EL USUARIO PREVIAMENTE
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        //paso = Integer.parseInt(respaldo.getString("paso", "2"));
    }

    public void onClick(View v) {
        if (v.getId() == R.id.boton_conexion) {
            vibrar(100);
            //Toast.makeText(this,"conectar", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ABRIR FRAGMENT");
            startActivityForResult(new Intent(this, DeviceList.class), REQUEST_CONNECT_DEVICE_SECURE);
        }
    }

    /**
     * /////////////////////////// METODOS PERSONALIZADOS /////////////////////////////////////
     */

    void vibrar(int ms) {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(ms);
    }

    /**
     * /////////////////////////// METODOS BLUETOOTH /////////////////////////////////////
     */
    private void configurar() {
        Log.d(TAG, "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        BTservice = new BluetoothManager(this, mHandler);
        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer("");
    }// The Handler that gets information back from the BluetoothChatService

    private void enviarMensaje(String mensaje) //recibeel mensaje enviar de tipo string
    {
        //checar la conexion antes de enviar
        if (BTservice.getState() != BluetoothManager.STATE_CONNECTED) {
            Toast.makeText(this, "NO CONECTADO", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NO CONECTADO");
            return;
        }
        //comprobar que haya algo para enviar
        if (mensaje.length() > 0) {
            //convertir a bytes para enviar por serial
            byte[] send = mensaje.getBytes();
            BTservice.write(send);
            Log.d(TAG, "ENVIANDO MENSAJE: " + mensaje);
        }
    }

    private void conectarDevice(Intent data, boolean secure) {
        // RECUPERAR LA DIRECCIÓN MAC
        String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        String nombre = data.getExtras().getString(DeviceList.EXTRA_DEVICE_NAME);
        // Recupera el objeto BluetoothDevice
        BluetoothDevice device = BTadaptador.getRemoteDevice(address);
        // Intentar conectar el device
        BTservice.connect(device, secure);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + device);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + nombre);
        Toast.makeText(this, "Conectando a " + nombre, Toast.LENGTH_LONG).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, true);
                    Log.d(TAG, "CONEXION SEGURA, DISPOSITIVO");
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, false);
                    Log.d(TAG, "CONEXION SEGURA, INSEGURA");
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    configurar();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.e(TAG, "ERROR DE CONEXION");
                    Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_SHORT).show();
                    finish();
                    //
                }
        }
    }
    /**   /////////////////////////// METODOS BLUETOOTH /////////////////////////////////////*/
}
