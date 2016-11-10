package org.multibluetooth.multibluetooth.retrofit;

import org.multibluetooth.multibluetooth.retrofit.format.DTOFacebookLogin;
import org.multibluetooth.multibluetooth.retrofit.format.DTOInsertCallback;
import org.multibluetooth.multibluetooth.retrofit.format.DTOLastIndexCallback;
import org.multibluetooth.multibluetooth.retrofit.format.DTOdangerLocation;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

/**
 * Created by YS on 2016-07-15.
 */
public interface RetrofitService {
    @POST("/users")
    Call<DTOFacebookLogin> setUserInfo(
            @Header("access-token") String access_token
    );

    @GET("/danger_location/{zone_name}")
    Call<DTOdangerLocation> getDangerLevel(
            @Path("zone_name") String zone_name
    );

    @FormUrlEncoded
    @POST("/danger_location/{zone_name}")
    Call<DTOdangerLocation> postDangerLocation(
            @Header("access-token") String access_token,
            @Path("zone_name") String zone_name,
            @Field("lat") double lat,
            @Field("lon") double lon,
            @Field("type") String type
            // TODO add time
    );

    @GET("/drive_info/last")
    Call<DTOLastIndexCallback> getLastIndex(
            @Header("access-token") String access_token
    );

    @Multipart
    @POST("/drive_info")
    Call<DTOInsertCallback> uploadNativeData(
            @Header("access-token") String access_token,
            @PartMap Map<String, RequestBody> raw_drive_data,
            @PartMap Map<String, RequestBody> raw_safe_score
    );
}
