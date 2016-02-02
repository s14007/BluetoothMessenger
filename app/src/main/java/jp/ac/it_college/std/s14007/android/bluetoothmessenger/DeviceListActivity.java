package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends Activity {

    public static final String OUI_LEGO = "00:16:53";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    static final String PAIRING = "pairing";
    private Intent intent = getIntent();
    private Handler handler;
    private BluetoothClientThread bluetoothClientThread;

    private ArrayAdapter<String> mCandidateServers;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("log:","ACTION_FOUND");
                // デバイスが見つかった場合、Intent から BluetoothDevice を取り出す
                mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 名前とアドレスを所定のフォーマットで ArrayAdapter に格納
                mCandidateServers.add(mDevice.getName() + "\n" + mDevice.getAddress());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothServerThread bluetoothServerThread = new BluetoothServerThread(DeviceListActivity.this, mBluetoothAdapter);
        bluetoothServerThread.start();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mBluetoothAdapter.cancelDiscovery();
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING, parent.getId() == R.id.new_devices);
            intent.putExtras(data);

            setResult(Activity.RESULT_OK, intent);
            Log.e("deviceClickListener :", "getAddress");

            bluetoothClientThread = new BluetoothClientThread(DeviceListActivity.this, address, mBluetoothAdapter);
            bluetoothClientThread.start();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);


        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        mCandidateServers = new ArrayAdapter<>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mCandidateServers);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

// Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

// If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {

                    mPairedDevicesArrayAdapter.add(device.getName() + "-n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);


        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

}
