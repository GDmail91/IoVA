package org.multibluetooth.multibluetooth.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-11-09.
 */
public class DTOLastIndexCallback {
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    DTOLastIndexCallbackData lastInsertData;

    public DTOLastIndexCallback(String msg, DTOLastIndexCallbackData lastInsertData) {
        this.msg = msg;
        this.lastInsertData = lastInsertData;
    }

    public String getMsg() {
        return msg;
    }

    public DTOLastIndexCallbackData getData() {
        return lastInsertData;
    }
}
