/*******************************************************************************************************
 * THIS CLASS IS CURRENTLY NOT BEING USED
 *******************************************************************************************************/

package com.example.rgbarduinocontrollerandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class BluetoothDialog extends AppCompatDialogFragment {

    private BluetoothDialogListener listener;

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String btName = getArguments().getString("btName");
        String btAddress = getArguments().getString("btAddress");
        String btBondState = getArguments().getString("btBondState");


        builder.setTitle("Bluetooth Connection")
                .setMessage("Name: " + btName + "\n" + "Address: " + btAddress + " \n" + "Bond State: " + btBondState )
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConnectClick();
                    }
                });

        return builder.create();
    }

    public interface BluetoothDialogListener{
        void onConnectClick();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try{
            listener = (BluetoothDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + "Must implement BluetoothDialogListener");
        }
    }
}
