package org.multibluetooth.multibluetooth.Driving.Bluetooth.LaserScan;

/**
 * Created by YS on 2016-10-12.
 */
public class LaserCommand {
    public static byte[] getDistance() {
        return "AA01".getBytes();
    }

    public static byte[] getScan() {
        return "AA02".getBytes();
    }
}
