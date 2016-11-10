package org.multibluetooth.multibluetooth.SafeScore.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by YS on 2016-11-09.
 */
public class SafeScoreList extends LinkedList<JSONObject> {
    public SafeScoreList() {
        super();
    }

    public boolean push(SafeScore safeScore) {
        try {
            super.push(new JSONObject(safeScore.toString()));
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
