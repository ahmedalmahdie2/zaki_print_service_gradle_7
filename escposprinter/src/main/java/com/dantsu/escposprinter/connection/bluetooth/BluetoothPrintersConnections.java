package com.dantsu.escposprinter.connection.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class BluetoothPrintersConnections extends BluetoothConnections {

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @return a EscPosPrinterCommands instance
     */
    @Nullable
    public static BluetoothConnection selectFirstPaired() {
        BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
        BluetoothConnection[] bluetoothPrinters = printers.getList();

        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            Log.d("printers count", bluetoothPrinters.length + " bluetooth printers");
            for (BluetoothConnection printer : bluetoothPrinters) {
                try {
                    Log.d("printers count", bluetoothPrinters.length + " bluetooth printers");

                    return printer.connect();
                } catch (EscPosConnectionException e) {
                    Log.d("can't connect to", " bluetooth printer");
                    e.printStackTrace();
                }
            }
        }
        else
        {
            Log.d("selectFirstPaired", "we still don't have bluetooth printers!");
        }
        return null;
    }

    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */
    @SuppressLint("MissingPermission")
    @Nullable
    public BluetoothConnection[] getList() {
        BluetoothConnection[] bluetoothDevicesList = super.getList();


        if (bluetoothDevicesList == null) {
//            Log.d("Bluetooth", ", we don't have bluetooth printers!");
            return null;
        }
        else
        {
//            Log.d("Bluetooth", ", we still have bluetooth printers!");
        }

//        int i = 0;
//        BluetoothConnection[] printersTmp = new BluetoothConnection[bluetoothDevicesList.length];
//        for (BluetoothConnection bluetoothConnection : bluetoothDevicesList) {
//            BluetoothDevice device = bluetoothConnection.getDevice();
//
//            int majDeviceCl = device.getBluetoothClass().getMajorDeviceClass(),
//                    deviceCl = device.getBluetoothClass().getDeviceClass();
//
//            if (majDeviceCl == BluetoothClass.Device.Major.IMAGING && (deviceCl == 1664 || deviceCl == BluetoothClass.Device.Major.IMAGING)) {
//                printersTmp[i++] = new BluetoothConnection(device);
//            }
//        }
        Log.d("getList", "printers count: " + bluetoothDevicesList.length);

//        System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i);
        return bluetoothDevicesList;
    }

}
