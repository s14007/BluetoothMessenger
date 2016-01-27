package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * Created by s14007 on 16/01/25.
 */
public class Communicator extends Thread {
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler;
    private  MainActivity mainActivity;
    private BluetoothSocket socket;
    private BluetoothDevice device = null;

    public Communicator(MainActivity main, Handler handler, BluetoothAdapter adapter) {
        this.mainActivity = main;
        this.handler = handler;
        this.bluetoothAdapter = adapter;
    }

    private void connection() {
        if (device == null) {
            Log.e("connection :", "null");
        }

    }
    //TODO:client, server側の処理を別々にする

}
