package org.multibluetooth.multibluetooth.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-07-18.
 */
public class DTOFacebookLogin {
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    long user_id;

    public String getMsg() {
        return msg;
    }

    public long getUserId()
    {
        return user_id;
    }

}
