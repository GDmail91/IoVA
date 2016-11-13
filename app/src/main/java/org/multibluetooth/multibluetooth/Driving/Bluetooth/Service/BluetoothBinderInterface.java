package org.multibluetooth.multibluetooth.Driving.Bluetooth.Service;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by YS on 2016-11-12.
 */
public interface BluetoothBinderInterface {
    public void init(Handler handler);
    public int getState();
    public void start();
    public void stop();
    public void connect(BluetoothDevice device, boolean secure);
    public void write(Bundle out);
}
