package org.multibluetooth.multibluetooth.Driving.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by YS on 2016-11-09.
 */
public class DriveInfoList extends ArrayList<JSONObject> {
    public DriveInfoList() {
        super();
    }

    public boolean add(DriveInfo driveInfo) {
        try {
            super.add(new JSONObject(driveInfo.toString()));
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
