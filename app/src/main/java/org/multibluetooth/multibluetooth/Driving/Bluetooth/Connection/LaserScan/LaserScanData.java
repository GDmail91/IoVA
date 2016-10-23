package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan;

/**
 * Created by YS on 2016-10-17.
 */
public class LaserScanData {
    private float frontDistance;
    private float backDistance;
    private float sideDistance;

    public LaserScanData() {
        this.frontDistance = 0;
        this.backDistance = 0;
        this.sideDistance = 0;
    }

    public LaserScanData(float frontDistance, float backDistance, float sideDistance) {
        this.frontDistance = frontDistance;
        this.backDistance = backDistance;
        this.sideDistance = sideDistance;
    }

    public float getFrontDistance() {
        return frontDistance;
    }

    public float getSideDistance() {
        return sideDistance;
    }

    public float getBackDistance() {
        return backDistance;
    }

    public void setFrontDistance(float frontDistance) {
        this.frontDistance = frontDistance;
    }

    public void setBackDistance(float backDistance) {
        this.backDistance = backDistance;
    }

    public void setSideDistance(float sideDistance) {
        this.sideDistance = sideDistance;
    }

    @Override
    public String toString() {
        return ""+frontDistance + backDistance + sideDistance;
    }
}
