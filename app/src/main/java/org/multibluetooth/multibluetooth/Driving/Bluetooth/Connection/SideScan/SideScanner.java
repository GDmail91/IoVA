package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.SideScan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothSideService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;

import java.util.LinkedList;

/**
 * Created by YS on 2016-11-13.
 */
public class SideScanner extends BluetoothConnection {
    private static final String TAG = "SideScanner";

    public boolean AUTO_CONN = true;

    private static final LinkedList<Character> buffer = new LinkedList<>();

    public SideScanner(Context context) {
        super(context);
    }

    public void connMode(boolean autoMode) {
        AUTO_CONN = autoMode;
    }

    @Override
    public void conn() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_SIDE);
        } else if (!mBound) {
            bindService();
        }
    }

    @Override
    public void bindService() {
        Log.d(TAG, "bindService()");
        Intent intent = new Intent(mContext, BluetoothSideService.class);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void serviceStop() {
        if(mBound){
            //binder.stop();
            mContext.unbindService(this);
            mBound = false;
        }
    }

    /**
     * Service init
     */
    public void setupService() {
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_SIDE);
        } else {
            if (mBound) {
                Log.d(TAG, "setupService().init()");
                binder.init(mHandler);
                setupStringBuffer();
            }
        }
    }

    @Override
    public void setupConnect() {
        Log.d(TAG, "setupConnect()");
        setupStringBuffer();

        SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String address = pref.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS_SIDE, "");
        Log.d(TAG, "뭘가지고 있나: "+address);
        if ("".equals(address) || !AUTO_CONN) {
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
            ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE_BY_SIDE);
        } else {
            // Auto connect mode
            // IoVA will served only secure mode
            Intent intent = new Intent();
            intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
            connectDevice(intent, true);
        }
    }

    @Override
    public int getConnectionStatus() {
        if (binder != null)
            return binder.getState();
        else
            return BluetoothService.STATE_NONE;
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "커넥 디바이스 실행");
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = pref.edit();
        prefEdit.putString(DeviceListActivity.EXTRA_DEVICE_ADDRESS_SIDE, address);
        prefEdit.apply();

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        binder.connect(device, secure);
    }

    @Override
    protected String updateData(Bundle bundle) {
        String category = bundle.getString("CATEGORY");
        Log.d(TAG, category);
        try {
            switch (category) {
                case "OBD":
                case "Laser":
                case "Side":
                    switch (bundle.getString("MESSAGE1", "")) {
                        case Constants.SIDE_LEFT:
                            ((DrivingActivity) mContext).onScan(SCAN_LEFT);
                            break;
                        case Constants.SIDE_RIGHT:
                            ((DrivingActivity) mContext).onScan(SCAN_RIGHT);
                            break;
                        case Constants.SIDE_STOP:
                            ((DrivingActivity) mContext).onScan(SCAN_STOP);
                            break;
                    }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected()");
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        binder = (BluetoothSideService.LocalBinder) service;
        mBound = true;
        setupService();
        //setupConnect();
        Log.d(TAG, "Side 서비스 연결됨");
        Toast.makeText(mContext, "Side 서비스 연결됨", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        Log.d(TAG, "Side 서비스 연결 실패");
        Toast.makeText(mContext, "Side 서비스 연결 실패", Toast.LENGTH_SHORT).show();
    }
}
