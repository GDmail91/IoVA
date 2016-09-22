package org.multibluetooth.multibluetooth.Driving.Bluetooth.LaserScan;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothChatService;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;

import java.util.LinkedList;

/**
 * Created by YS on 2016-09-13.
 */
public class LaserScanner extends BluetoothConnection {
    private static final String TAG = "LaserScanner";

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_LASER = 2001;
    public static final int REQUEST_ENABLE_BT_BY_LASER = 2003;

    private static final LinkedList<Character> buffer = new LinkedList<>();

    public LaserScanner(Context context) {
        super(context);
    }

    @Override
    public void conn() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_LASER);
        } else if (mChatService == null) {
            // TODO sendMessage() 를 할 컴포넌트 연결
            // ex) sendMessage(message);
            //setupChat(context);
            setupService();
        }
    }

    @Override
    protected void setupService() {
        mChatService = new BluetoothChatService(mContext, mHandler);
        mOutStringBuffer = new StringBuffer("");

        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE_BY_LASER);
    }

    @Override
    protected void messageParse(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

        message = message.toUpperCase();

        for(char c : message.toCharArray()) {
            Log.d(TAG, ""+c);
            buffer.add(c);
        }

        if (buffer.size() >= 10) {
            String msgStart = buffer.removeFirst().toString() + buffer.removeFirst().toString();
            String msgMode = "";
            String msgBody = "";

            if (msgStart.equals("AA")) {
                for (int i=0; i<8; i++) {
                    if (i<2) {
                        msgMode += buffer.removeFirst();
                    } else {
                        msgBody += buffer.removeFirst();
                    }
                }

                switch (msgMode) {
                    case "01":
                        ((DrivingActivity) mContext).setChangeText("\n\nmode: "+msgMode+"\nbody: "+msgBody);
                        break;
                    case "02":
                        ((DrivingActivity) mContext).setChangeText("mode: "+msgMode+"\nbody: "+msgBody);
                        break;
                }
            } else {
                Log.d(TAG, "쓰레기값");
            }
        }
    }
}
