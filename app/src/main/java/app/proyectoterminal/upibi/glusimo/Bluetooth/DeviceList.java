/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package app.proyectoterminal.upibi.glusimo.Bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

import app.proyectoterminal.upibi.glusimo.R;

@SuppressLint("NewApi")
public class DeviceList extends Activity {
    // Debugging
    private static final String TAG = "DeviceList";
    private static final boolean D = true;

    // Member fields
    private BluetoothAdapter BTadaptador;
    private Button boton;
    private ArrayAdapter<String> arrayDevicesVinculados;
    private ArrayAdapter<String> arrayDevicesEncontrados;
    private IntentFilter filtro;
    private Set<BluetoothDevice> pairedDevices;
    private ProgressBar bolita;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"DeviceList creada");
        
        // Setup the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lista_devices);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // RECUPERAR LOS XML DE LOS ELEMENTOS
        boton = (Button) findViewById(R.id.boton_lista_devices);
        bolita = (ProgressBar)findViewById(R.id.bolita);

        // Recuperar String de boton,
        String stringBuscar = getIntent().getStringExtra("scan_for_devices");
        if(stringBuscar == null)
        	stringBuscar = "BUSCAR DOSIFICADOR";
        // Cargar el string en el boton
        boton.setText(stringBuscar);
        // Habilitar el discovery si no se encontraron dosificador
        boton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        // inicializa los arreglos para devices vinculados y encontrados
        arrayDevicesVinculados = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        arrayDevicesEncontrados = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // agrega los resources desde xml
        ListView pairedListView = (ListView) findViewById(R.id.lista_encontrados);
        ListView newDevicesListView = (ListView) findViewById(R.id.lista_nuevos);

        // setea los adaptadores necesarios
        pairedListView.setAdapter(arrayDevicesVinculados);
        newDevicesListView.setAdapter(arrayDevicesEncontrados);

        // agrega el responsable del listener
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Crea IntentFilter y registra los broadcast para cada accion respectivamente
        filtro = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filtro);
        filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filtro);

        // abrir el adaptador local
        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        //carga los dispositivos vinculados
        pairedDevices = BTadaptador.getBondedDevices();

        // si encuentra dispositivos vinculados, los agrega al array
        if (pairedDevices.size() > 0) {
            findViewById(R.id.titulo).setVisibility(View.VISIBLE); //hace visible el array
            for (BluetoothDevice device : pairedDevices) {
                arrayDevicesVinculados.add(device.getName() + "\n" + device.getAddress()); //agregando al array
            }
        } else {
            // si no existen dispositivos vinculados, carga un string
            String noDevices = getResources().getText(R.string.no_vinculados).toString();
            arrayDevicesVinculados.add(noDevices);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (BTadaptador != null) {
            BTadaptador.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        this.finish();
    }

    // Start device discover with the BluetoothAdapter
	private void doDiscovery()
    {
        if (D) Log.d(TAG, "doDiscovery()");

        // carga el progress bar
        bolita.setVisibility(View.VISIBLE);

        // cancelar el discovery si se esta efectuando
        if (BTadaptador.isDiscovering()) {
            BTadaptador.cancelDiscovery();
        }

        // empieza el discovery
        BTadaptador.startDiscovery();

    }

    //crea el listener para todos los listviews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            Log.i(TAG,"ListView Click");

            // Cancel discovery because it's costly and we're about to connect
            if(BTadaptador.isDiscovering())
                BTadaptador.cancelDiscovery();
            // recupera la direccion MAC del dispositivo, son los ultimos 17 caracteres del view
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0,info.length() - 18);
            Log.i(TAG, name);

            // crea el intent y guarda la direccion MAC
            Intent intent = new Intent();
            intent.putExtra(BluetoothState.EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(BluetoothState.EXTRA_DEVICE_NAME, name);

            // Setea el result y termina la activity
            setResult(Activity.RESULT_OK, intent);
            Log.i(TAG,"Activity Result Ok");
            finish();
        }
    };

    // El broadcast que encuentra los dispositivos y los agrega al array cuando termina el discovery
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // carga la vista de dispositivos encontrados
            findViewById(R.id.subtitulo).setVisibility(View.VISIBLE);
            // Si se encuentra un device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //recupera el objeto device del intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // saltar si ya esta vinculado
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    arrayDevicesEncontrados.add(device.getName() + "\n" + device.getAddress());
                }
                //cuando termina el discovery, cambia la vista
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                bolita.setVisibility(View.GONE);
                if (arrayDevicesEncontrados.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.no_encontrados).toString();
                    arrayDevicesEncontrados.add(noDevices);
                }
            }
        }
    };

}
