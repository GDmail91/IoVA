package org.multibluetooth.multibluetooth.Driving.Bluetooth.Service;

/**
 * Created by YS on 2016-09-25.
 */

import android.os.Bundle;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public interface ConnectedThread {

    void write(Bundle cmdInfo);
    void cancel();
}
