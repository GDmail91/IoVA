package org.multibluetooth.multibluetooth.retrofit.format;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YS on 2016-11-09.
 */
public class DTOInsertCallback {
    @SerializedName("msg")
    String msg;
    @SerializedName("data")
    InsertData insertData;

    public DTOInsertCallback(String msg, InsertData insertData) {
        this.msg = msg;
        this.insertData = insertData;
    }

    public String getMsg() {
        return msg;
    }

    public InsertData getData() {
        return insertData;
    }

    class InsertData {
        @SerializedName("insert_length")
        int insertLength;

        public int getLength() {
            return insertLength;
        }
    }
}
