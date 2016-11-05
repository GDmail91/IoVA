package org.multibluetooth.multibluetooth.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-11-03.
 */
public class DTOdangerLocation {
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    DTOdangerLocationData data;

    public String getMsg() {
        return msg;
    }

    public DTOdangerLocationData getData()
    {
        return data;
    }
}
