package org.multibluetooth.multibluetooth.Driving.Bluetooth.LaserScan;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;

import java.util.LinkedList;

/**
 * Created by YS on 2016-09-13.
 */
public class LaserScanner extends BluetoothConnection {
    private static final String TAG = "LaserScanner";
    private static final LinkedList<Character> buffer = new LinkedList<>();

    public LaserScanner(Context context) {
        super(context);
    }

    @Override
    protected void messageCheck(String message) {
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
