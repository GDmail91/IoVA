package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan;

/**
 * Created by YS on 2016-10-12.
 */
public class LaserCommand {
    public static byte[] getDistance() {
        return "AA01".getBytes();
    }

    public static byte[] getLeftScan() {
        return "L".getBytes();
    }

    public static byte[] getRightScan() {
        return "R".getBytes();
    }

    public static byte[] stopScan() {
        return "S".getBytes();
    }
}
