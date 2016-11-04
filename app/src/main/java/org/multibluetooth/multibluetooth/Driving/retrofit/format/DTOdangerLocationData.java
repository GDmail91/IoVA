package org.multibluetooth.multibluetooth.Driving.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-11-03.
 */
public class DTOdangerLocationData {
    @SerializedName("zone_name")
    String zone_name;
    @SerializedName("level")
    int level;

    public DTOdangerLocationData(String zone_name, int level) {
        this.zone_name = zone_name;
        this.level = level;
    }

    public String getZoneName() {
        return zone_name;
    }

    public int getLevel() {
        return level;
    }
}
