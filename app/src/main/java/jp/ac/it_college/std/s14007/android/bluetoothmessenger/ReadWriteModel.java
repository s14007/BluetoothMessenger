package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by s14007 on 16/01/26.
 */
public class ReadWriteModel extends Thread {
    //ソケットに対するI/O処理

    public static InputStream in;
    public static OutputStream out;
//    private String sendNumber;
    private Handler myHandler;
    private Context mContext;
    public String string;
    private Context mainContext;
    private String message = "test";


    //コンストラクタの定義
    public ReadWriteModel(Context context, BluetoothSocket socket){
        mContext = context;

        try {
            //接続済みソケットからI/Oストリームをそれぞれ取得
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public ReadWriteModel(Context context, String msg) {
        mainContext = context;
        message = msg;
        TextView textView = (TextView)((MainActivity) mainContext).findViewById(R.id.member_view);
        textView.setText(message);
    }

    public void write(byte[] buf){
        //Outputストリームへのデータ書き込み
        try {
            out.write(buf);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] buf = new byte[1024];
//        string = "test"
        byte[] byteData = message.getBytes();
        String rcvStr = null;
        int tmpBuf = 0;

        Log.e("mainMessage :", message);

        write(byteData);

        while(true){
            try {
                tmpBuf = in.read(buf);
                Log.e("tmpBuf :", String.valueOf(tmpBuf));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(tmpBuf!=0){
                try {
                    rcvStr = new String(buf, "UTF-8");
                    Log.e("read :", rcvStr);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            Intent i = new Intent(mContext, MainActivity.class);
            i.putExtra("message", rcvStr);
            mContext.startActivity(i);
        }
    }

    public void setString(String string) {
        this.string = string;
    }
}
