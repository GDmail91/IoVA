package org.multibluetooth.multibluetooth.Driving.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.SideScan.SideScanner;

import java.io.IOException;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = BluetoothManager.class.getName();

    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public static boolean isBtOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Instantiates a BluetoothSocket for the remote device and connects it.
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @param dev The remote device to connect to
     * @return The BluetoothSocket
     * @throws IOException
     */
    public static BluetoothConnection getBtConnection(Context mContext, DeviceList.Device dev) {
        if (isBtOn()) {
            switch (dev) {
                case LASER:
                    return new LaserScanner(mContext);
                case OBD:
                    return new OBDScanner(mContext);
                case SIDE_SCANNER:
                    return new SideScanner(mContext);
            }
        }
        return null;
    }
}