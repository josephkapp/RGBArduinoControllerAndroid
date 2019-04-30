/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.rgbarduinocontrollerandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int SELECT_ANIMATION = 1;
    public static final String TAG = "RGB ARDUINO ANDROID";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> listAdapter;
    private TextView statusMessage;
    private String selAnimationText;
    private int selAnimationVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PaintSeekBar seekBar = findViewById(R.id.redSeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        statusMessage = findViewById(R.id.statusTextView);

        //Set start up defaults
        selAnimationVal = 0;
        selAnimationText = "Static Color";
        TextView textView = findViewById(R.id.selAnimationTextView);
        textView.setText(selAnimationText);
        findViewById(R.id.directionSwitch).setEnabled(false);
        findViewById(R.id.speedSeekBar).setEnabled(false);

        service_init();

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                statusMessage.setText("Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.disconnect) {
            if (mDevice!=null)
            {
                mService.disconnect();
                Log.i(TAG, "Disconnected from device");
                statusMessage.setText("Disconnected from device");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSelectAnimationButtonPress(View view)
    {
        Intent i = new Intent(this, AnimationSelection.class);
        startActivityForResult(i,SELECT_ANIMATION);
    }

    public void onConnectToBluetoothButtonPress(View view)
    {
        Intent i = new Intent(this, DeviceListActivity.class);
        startActivityForResult(i,REQUEST_SELECT_DEVICE);
    }

    public void onTurnOffButtonPress(View view)
    {
        String sendString = assembleMessage(0,0,0,0,0,0);
        sendMessage(sendString);
    }

    public void onSubmitButtonPress(View view)
    {
        String sendString = assembleMessage();
        sendMessage(sendString);
    }

    //Build the arduino command.
    //Example Data Steam: A7G0B0R255S50D0T
    //A = Animation
    //R = Red
    //G = Green
    //B = Blue
    //w = White
    //S = Delay speed
    //D = Animation Direction
    //T = End of Input
    public String assembleMessage()
    {
        int dirVal;
        int redVal;
        int greenVal;
        int blueVal;
        int speedVal;

        redVal = ((SeekBar) findViewById(R.id.redSeekBar)).getProgress();
        greenVal = ((SeekBar) findViewById(R.id.greenSeekBar)).getProgress();
        blueVal = ((SeekBar) findViewById(R.id.blueSeekBar)).getProgress();
        speedVal = ((SeekBar) findViewById(R.id.speedSeekBar)).getProgress();

        if (((Switch) findViewById(R.id.directionSwitch)).isChecked() == true)
            dirVal = 1;
        else
            dirVal = 0;

        return assembleMessage(selAnimationVal,redVal,greenVal,blueVal,speedVal,dirVal);
    }

    public String assembleMessage(int selAnimation, int red, int green, int blue, int speed, int direction)
    {
        StringBuilder message = new StringBuilder();

        message.append("A").append(selAnimation).append("R").append(red)
                .append("G").append(green).append("B").append(blue)
                .append("S").append(speed).append("D").append(direction)
                .append("T");

        Log.i(TAG,"Message to be sent to the arduino:" + message.toString());

        return message.toString();
    }

    public void sendMessage(String msg)
    {
        if (mDevice == null)
        {
            showMessage("Connect to a device before sending a command");
            Log.w(TAG,"Need to be connected to a bluetooth device first before sending a command");
            return;
        }


        byte[] value;
        try {
            //send data to service
            value = msg.getBytes("UTF-8");
            mService.writeRXCharacteristic(value);
            //Update the log with time stamp
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_ANIMATION) {
            selAnimationText = data.getStringExtra("selectedAnimationText");
            selAnimationVal = data.getIntExtra("selectedAnimationVal",0);
            TextView textView = findViewById(R.id.selAnimationTextView);
            textView.setText(selAnimationText);

            switch (selAnimationVal){
                case 0: //Static Color
                {
                    findViewById(R.id.redSeekBar).setEnabled(true);
                    findViewById(R.id.greenSeekBar).setEnabled(true);
                    findViewById(R.id.blueSeekBar).setEnabled(true);
                    findViewById(R.id.speedSeekBar).setEnabled(false);
                    findViewById(R.id.directionSwitch).setEnabled(false);
                    break;
                }
                case 1: //Fade Color
                {
                    findViewById(R.id.redSeekBar).setEnabled(false);
                    findViewById(R.id.greenSeekBar).setEnabled(false);
                    findViewById(R.id.blueSeekBar).setEnabled(false);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(false);
                    break;
                }
                case 2: //Rainbow Cycle
                {
                    findViewById(R.id.redSeekBar).setEnabled(false);
                    findViewById(R.id.greenSeekBar).setEnabled(false);
                    findViewById(R.id.blueSeekBar).setEnabled(false);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(false);
                    break;
                }
                case 3: //Rainbow Chase
                {
                    findViewById(R.id.redSeekBar).setEnabled(false);
                    findViewById(R.id.greenSeekBar).setEnabled(false);
                    findViewById(R.id.blueSeekBar).setEnabled(false);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(true);
                    break;
                }
                case 4: //Theater Chase
                {
                    findViewById(R.id.redSeekBar).setEnabled(true);
                    findViewById(R.id.greenSeekBar).setEnabled(true);
                    findViewById(R.id.blueSeekBar).setEnabled(true);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(true);
                    break;
                }
                case 5: //Random Color Chase
                {
                    findViewById(R.id.redSeekBar).setEnabled(false);
                    findViewById(R.id.greenSeekBar).setEnabled(false);
                    findViewById(R.id.blueSeekBar).setEnabled(false);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(true);
                    break;
                }
                case 6: //Aurora Glow
                {
                    findViewById(R.id.redSeekBar).setEnabled(false);
                    findViewById(R.id.greenSeekBar).setEnabled(false);
                    findViewById(R.id.blueSeekBar).setEnabled(false);
                    findViewById(R.id.speedSeekBar).setEnabled(true);
                    findViewById(R.id.directionSwitch).setEnabled(false);
                    break;
                }
            }

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_DEVICE)
        {

            statusMessage.setText("Connecting...");


            String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

            Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
            boolean success = mService.connect(deviceAddress);

            if(success)
                statusMessage.setText("Successfully connected to: " + mDevice.getName() );
            else
                statusMessage.setText("Failed to connect to: " + mDevice.getName());
        }
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("["+currentDateTimeString+"] RX: "+text);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("RGB Arduino is running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}