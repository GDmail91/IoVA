package org.multibluetooth.multibluetooth.Driving.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-07-18.
 */
public class DTOFacebookLogin {
    @SerializedName("status")
    boolean status;
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    DTOLoginData data;

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public long getData()
    {
        return data.getUserId();
    }

    class DTOLoginData{
        @SerializedName("user_id")
        long userId;

        public long getUserId() {
            return userId;
        }

        public void setUserId(long user_id) {
            this.userId = user_id;
        }
    }
}
