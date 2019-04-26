package com.example.rgbarduinocontrollerandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ConnectBluetooth extends AppCompatActivity implements BluetoothDialog.BluetoothDialogListener {

    private BluetoothAdapter mBluetoothAdapter;
    //private BroadcastReceiver mReceiver;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
    private int selectedIndex;
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bluetooth);

        //Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Connect to Bluetooth");
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Setup bluetooth scanners
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
                    new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        lv = (ListView) findViewById(R.id.listView);

        pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            list.add(device);
        }
        
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedIndex = position;
                BluetoothDevice device = (BluetoothDevice)parent.getItemAtPosition(position);
                openDialog(device);


            }});

    }

    public void openDialog(BluetoothDevice btDevice){
        Bundle args = new Bundle();
        args.putString("btName",btDevice.getName());
        args.putString("btAddress", btDevice.getAddress());
        args.putString("btBondState", String.valueOf(btDevice.getBondState()));
        BluetoothDialog dialog = new BluetoothDialog();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "Bluetooth Connection Dialog");
    }

    @Override
    public void onConnectClick()
    {
        BluetoothDevice device = (BluetoothDevice)lv.getItemAtPosition(selectedIndex);
        Intent intent = new Intent();
        intent.putExtra("selectedBluetoothDeviceAddress", device.getAddress());
        setResult(RESULT_OK, intent);
        finish();
    }

}
