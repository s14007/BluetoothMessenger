package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by s14007 on 16/01/25.
 */
public class BluetoothServerThread extends Thread {
    public static int STATE_CONNECTED = 1001;

    //UUIDの生成
    public static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter myServerAdapter;
    //サーバー側の処理
    //UUID：Bluetoothプロファイル毎に決められた値
    private final BluetoothServerSocket serverSocket;
//    public String myNumber;
    private Context mContext;
    private Handler myHandler;


    //コンストラクタの定義
    public BluetoothServerThread(Context context, BluetoothAdapter btAdapter) {
        //各種初期化
        mContext = context;
        BluetoothServerSocket tmpServerSocket = null;
        this.myServerAdapter = btAdapter;

        try {
            //自デバイスのBluetoothサーバーソケットの取得
            tmpServerSocket = myServerAdapter.listenUsingRfcommWithServiceRecord("BlueToothSample03", SERIAL_PORT_SERVICE_CLASS_UUID);

        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = tmpServerSocket;
    }

    public void run() {
        BluetoothSocket receivedSocket = null;
        while (true) {
            try {
                //クライアント側からの接続要求待ち。ソケットが返される。
                Log.e("ServerSocket", "接続待ち");
                receivedSocket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }

            if (receivedSocket != null) {
                //ソケットを受け取れていた(接続完了時)の処理
                //RwClassにmanageSocketを移す
                Log.e("BluetoothServerThread :", "Socketを受け取りました");
                ReadWriteModel rw = new ReadWriteModel(mContext, receivedSocket);
                rw.start();

                try {
                    //処理が完了したソケットは閉じる。
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
