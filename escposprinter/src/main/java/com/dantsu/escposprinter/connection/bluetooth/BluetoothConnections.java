package com.dantsu.escposprinter.connection.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Set;

public class BluetoothConnections {
    protected BluetoothAdapter bluetoothAdapter;
    
    /**
     * Create a new instance of BluetoothConnections
     */
    public BluetoothConnections() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * Get a list of bluetooth devices available.
     * @return Return an array of BluetoothConnection instance
     */
    @SuppressLint("MissingPermission")
    @Nullable
    public BluetoothConnection[] getList() {
        if (this.bluetoothAdapter == null) {
            return null;
        }
    
        if(!this.bluetoothAdapter.isEnabled()) {
            return null;
        }
        
        Set<BluetoothDevice> bluetoothDevicesList = this.bluetoothAdapter.getBondedDevices();
        BluetoothConnection[] bluetoothDevices = new BluetoothConnection[bluetoothDevicesList.size()];
    
        if (bluetoothDevicesList.size() > 0) {
//            Log.d("Bluetooth", ", we have bluetooth printers!");
            int i = 0;
            for (BluetoothDevice device : bluetoothDevicesList) {
                bluetoothDevices[i++] = new BluetoothConnection(device);
            }
        }
        else
        {
//            Log.d("Bluetooth", ", we don't have bluetooth printers!");
        }
        
        return bluetoothDevices;
    }
}
