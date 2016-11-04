package org.multibluetooth.multibluetooth.Driving.retrofit;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;

/**
 * Created by YS on 2016-07-22.
 */
public interface FacebookService {
    @GET("/app/app_link_hosts")
    Call<JSONObject> appLinkId(
            @Field("access_token") String access_token,
            @Field("name") String name,
            @Field("android") String android,
            @Field("web") String web
    );
}
