package org.multibluetooth.multibluetooth.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-11-09.
 */
public class DTOLastIndexCallbackData {
    @SerializedName("drive_id")
    int lastDriveId;

    @SerializedName("request_id")
    int lastRequestId;

    public int getLastDriveId() {
        return lastDriveId;
    }

    public int getLastRequestId() {
        return lastRequestId;
    }
}
