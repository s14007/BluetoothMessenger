package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = null;
    private final int REQUEST_CONNECT_DEVICE = 1000;
    private final int REQUEST_ENABLE_BT = 2000;
    private final int MENU_TOGGLE_CONNECT = Menu.FIRST;
    private final int MENU_TOGGLE_DISCOVERABLE = Menu.FIRST + 1;
    private final int MENU_TOGGLE_NAMEOPTION = Menu.FIRST + 2;
    private final int MENU_QUIT = Menu.FIRST + 3;
//    private  ArrayAdapter<String> mCandidateServers;
/*    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("Log:", "ACTION_FOUND");
                // デバイスが見つかった場合、Intent から BluetoothDevice を取り出す
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 名前とアドレスを所定のフォーマットで ArrayAdapter に格納
                mCandidateServers.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("Log:", "ACTION_DISCOVERY_FINISHED");
                // デバイス検出が終了した場合は、BroadcastReceiver を解除
                context.unregisterReceiver(mReceiver);
            }
        }
    };*/

    private Menu myMenu;
    private ProgressDialog connectingProgressDialog;
    private BluetoothServerThread serverThread = null;
    private BluetoothClientThread clientThread = null;
    private Handler myHandler;
    private Handler bstHandler;
    private DeviceListActivity listActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        NameOption nameOption = new NameOption();
        String name = nameOption.getName();
        TextView hostName = (TextView)findViewById(R.id.member_view);
        hostName.setText(name);

        if (mBluetoothAdapter != null) {
            Toast.makeText(this, "Bluetooth is supported", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_LONG).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
//            if (mChatService == null) setupChat();
            Log.v("bt :", "on");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetoothを有効にしました", Toast.LENGTH_LONG).show();
                    startServerThread();
                    startClientThread();
                }
                break;
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Bluetoothを有効にしました", Toast.LENGTH_LONG).show();
//                        selectNxt();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Bluetoothを有効にする必要があります", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    default:
                        Toast.makeText(this, "Bluetoothが接続を開始できません", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
        }
    }


    @Override
        public boolean onCreateOptionsMenu (Menu menu){
            myMenu = menu;
            menu.add(0, MENU_TOGGLE_CONNECT, 1, "connect / disconnect");
            menu.add(0, MENU_TOGGLE_DISCOVERABLE, 2, "discoverable");
            menu.add(0, MENU_TOGGLE_NAMEOPTION, 3, "名前設定");
            menu.add(0, MENU_QUIT, 4, "終了");
            return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_TOGGLE_CONNECT:
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                break;
            case MENU_TOGGLE_DISCOVERABLE:
                ensureDiscoverable();
                break;
            case MENU_TOGGLE_NAMEOPTION:
                Intent nameOption = new Intent(this, NameOption.class);
                startActivity(nameOption);
                break;
            case MENU_QUIT:
                finish();
                break;
        }
        return true;
    }

/*    private void detection() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }*/

    private void ensureDiscoverable() {
//        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
    }

    public void startServerThread() {
        connectingProgressDialog =
                ProgressDialog.show(this, "", getResources().getString(R.string.connecting_wait, true));

        if (serverThread == null) {
            createServerThread();
        }

        serverThread.start();
    }

    public void createServerThread() {
        serverThread = new BluetoothServerThread(this, myHandler, BluetoothAdapter.getDefaultAdapter());
        bstHandler = serverThread.getHandler();
    }

    public void startClientThread() {
        connectingProgressDialog =
                ProgressDialog.show(this, "", getResources().getString(R.string.connecting_wait, true));

        if (clientThread == null) {
            createClientThread();
        }
        clientThread.start();
    }

    public void createClientThread() {
        clientThread = new BluetoothClientThread(this, myHandler, listActivity.mDevice, BluetoothAdapter.getDefaultAdapter());

    }
}
