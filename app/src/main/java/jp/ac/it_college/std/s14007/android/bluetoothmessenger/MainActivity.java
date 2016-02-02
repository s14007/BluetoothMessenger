package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CONNECT_DEVICE = 1000;
    private final int REQUEST_ENABLE_BT = 2000;
    private final int MENU_TOGGLE_CONNECT = Menu.FIRST;
    private final int MENU_TOGGLE_DISCOVERABLE = Menu.FIRST + 1;
    private final int MENU_TOGGLE_NAMEOPTION = Menu.FIRST + 2;
    private final int MENU_QUIT = Menu.FIRST + 3;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Menu myMenu;
    private ProgressDialog connectingProgressDialog;
    private BluetoothServerThread serverThread = null;
    private BluetoothClientThread clientThread = null;
    private Handler bstHandler;
    private DeviceListActivity listActivity;
    private NameOption nameOption = new NameOption(this);
    private boolean newDevice;
    private Handler myHandler;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        TextView hostName = (TextView) findViewById(R.id.member_view);
        String name = getString(R.string.host_name);
        hostName.setText(name);

        Button sendMessage = (Button) findViewById(R.id.send_message);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input);
                String message = input.getText().toString();
//                TextView serverView = (TextView)findViewById(R.id.server_log);
//                serverView.setText(message);

                /*LinearLayout layout = (LinearLayout) findViewById(R.id.layout_log);
                View view = getLayoutInflater().inflate(R.layout.activity_main, null);
                layout.addView(view);*/

                /*TextView tv = (TextView)findViewById(R.id.server_log);
                tv.setText(message);*/

                TextView textView = new TextView(MainActivity.this);

                LinearLayout layout = (LinearLayout) findViewById(R.id.layout_log);
                layout.addView(textView);

                Time time = new Time("Asia/Tokyo");
                time.setToNow();
                String date = time.hour + ":" + time.minute;
                textView.setText(message + " " + date);

                input.setText("");


                Intent i = getIntent();
                String readMessage = i.getStringExtra("message");
                if (readMessage != null) {
                    Log.e("readMessage :", readMessage);
                }

                TextView clientMessage = (TextView) findViewById(R.id.client_log);
                clientMessage.setText(readMessage);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                ReadWriteModel readWriteModel;
            }
        });

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
            selectDevice();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetoothを有効にしました", Toast.LENGTH_LONG).show();
                    address =
                            data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    newDevice = data.getExtras().getBoolean(DeviceListActivity.PAIRING);
                    Log.e("onActivityResult :", "Device selected");
                }
                break;
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Bluetoothを有効にしました", Toast.LENGTH_LONG).show();
                        selectDevice();
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

    private void selectDevice() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                selectDevice();
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

    private void updateName() {
        SharedPreferences data = getSharedPreferences("pref", Context.MODE_PRIVATE);
        String name = data.getString("hostKey", "guest");
        Log.e("name :", name);
    }


    private void ensureDiscoverable() {
//        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
    }
}
