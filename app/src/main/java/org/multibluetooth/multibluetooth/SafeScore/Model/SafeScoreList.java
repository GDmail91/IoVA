package org.multibluetooth.multibluetooth.SafeScore.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by YS on 2016-11-09.
 */
public class SafeScoreList extends ArrayList<JSONObject> {
    public SafeScoreList() {
        super();
    }

    public boolean add(SafeScore safeScore) {
        try {
            super.add(new JSONObject(safeScore.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean add(int index, SafeScore safeScore) {
        try {
            super.add(index, new JSONObject(safeScore.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
