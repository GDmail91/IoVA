package org.multibluetooth.multibluetooth.Driving.Model;

import android.location.Location;

/**
 * Created by YS on 2016-09-26.
 */
public class DriveInfo {
    private int _id = 0;
    private int drive_id;
    private int vehicle_speed;
    private float front_distance;
    private float back_distance;
    private float side_distance;
    private double gps_latitude;
    private double gps_longitude;
    private String measure_time;

    public DriveInfo() {}

    public DriveInfo(int _id, int drive_id, int vehicle_speed, float front_distance, float back_distance, float side_distance, double gps_latitude, double gps_longitude, String measure_time) {
        this._id = _id;
        this.drive_id = drive_id;
        this.vehicle_speed = vehicle_speed;
        this.front_distance = front_distance;
        this.back_distance = back_distance;
        this.side_distance = side_distance;
        this.gps_latitude = gps_latitude;
        this.gps_longitude = gps_longitude;
        this.measure_time = measure_time;
    }

    public int getId() { return _id; }

    public int getDriveId() { return drive_id; }

    public int getVehicleSpeed() { return vehicle_speed; }

    public float getFrontDistance() { return front_distance; }

    public float getBackDistance() { return back_distance; }

    public float getSideDistance() { return side_distance; }

    public double getGpsLatitude() { return gps_latitude; }

    public double getGpsLongitude() { return gps_longitude; }

    public void setOBDSensor(int _id, int vehicle_speed) {
        this._id = _id;
        this.vehicle_speed = vehicle_speed;
    }

    public void setOBDSensor(DriveInfo driveInfo) {
        this._id = driveInfo.getId();
        this.drive_id = driveInfo.getDriveId();
        this.vehicle_speed = driveInfo.getVehicleSpeed();
    }

    public void setLaserSensor(DriveInfo driveInfo) {
        this._id = driveInfo.getId();
        this.drive_id = driveInfo.getDriveId();
        this.front_distance = driveInfo.getFrontDistance();
        this.back_distance = driveInfo.getBackDistance();
    }

    // 평소 스캔
    public void setDistance(int _id, float front_distance, float back_distance) {
        this._id = _id;
        this.front_distance = front_distance;
        this.back_distance = back_distance;
    }

    // 옆차선 스캔
    public void setDistance(int _id, float front_distance, float back_distance, float side_distance) {
        this._id = _id;
        this.front_distance = front_distance;
        this.back_distance = back_distance;
         this.side_distance = side_distance;
    }

    public void setLocation(Location location) {
        this.gps_latitude = location.getLatitude();
        this.gps_longitude = location.getLongitude();
    }

    public String toString() {
        return "{\"request_id\": \""+_id + "\", \"drive_id\": \""+drive_id+"\", " +
                "\"vehicle_speed\": \""+vehicle_speed+"\", \"front_distance\": \""+front_distance+"\", " +
                "\"back_distance\": \""+back_distance + "\", \"side_distance\": \"" + side_distance +"\", " +
                "\"gps_latitude\": \""+gps_latitude+"\", \"gps_longitude\": \""+gps_longitude+"\", " +
                "\"measure_time\": \""+measure_time+"\"}";
    }

    /*public JSONObject toString() {
        try {
            return new JSONObject("{\"ID\": \""+_id + "\", \"DR_ID\": \""+drive_id+"\", \"VS\": \""+vehicle_speed+"\", \"FD\": \""+front_distance+"\", \"BD\": \""+back_distance + "\", \"SD\": \"" + side_distance +"\", \"LAT\": \""+gps_latitude+"\", \"LON\": \""+gps_longitude+"\", \"MT\": \""+measure_time+"\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
