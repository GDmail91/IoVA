package org.multibluetooth.multibluetooth.Driving.Model;

/**
 * Created by YS on 2016-09-26.
 */
public class DriveInfo {
    private int _id = 0;
    private int vehicle_speed;
    private int front_distance;
    private int back_distance;
    private String measure_time;

    public DriveInfo() {}

    public DriveInfo(int _id, int vehicle_speed, int front_distance, int back_distance, String measure_time) {
        this._id = _id;
        this.vehicle_speed = vehicle_speed;
        this.front_distance = front_distance;
        this.back_distance = back_distance;
        this.measure_time = measure_time;
    }

    public int getId() { return _id; }

    public int getVehicleSpeed() { return vehicle_speed; }

    public int getFrontDistance() { return front_distance; }

    public int getBackDistance() { return back_distance; }

    public void setOBDSensor(int _id, int vehicle_speed) {
        this._id = _id;
        this.vehicle_speed = vehicle_speed;
    }

    public String toString() {
        return "ID: "+_id +", VS: "+vehicle_speed+", FD: "+front_distance+", BD: "+back_distance +", MT: "+measure_time;
    }
}