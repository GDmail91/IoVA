package org.multibluetooth.multibluetooth.Driving.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by YS on 2016-11-09.
 */
public class DriveInfoList extends LinkedList<JSONObject> {
    public DriveInfoList() {
        super();
    }

    public boolean push(DriveInfo driveInfo) {
        try {
            super.push(new JSONObject(driveInfo.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean add(int index, DriveInfo driveInfo) {
        try {
            super.add(index, new JSONObject(driveInfo.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
