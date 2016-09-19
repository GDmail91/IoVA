package org.multibluetooth.multibluetooth.Driving;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.ScoreCalculator;

/**
 * Created by YS on 2016-09-13.
 */
public class DrivingActivity extends AppCompatActivity {

    // VIEW
    private TextView curSpeedView;
    private TextView safeDistanceView;


    private LocationManager lm;
    private LocationListener ll;
    double mySpeed, maxSpeed;
    private int TAG_CODE_PERMISSION_LOCATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driving_activity);

        // VIEW 연결
        curSpeedView = (TextView) findViewById(R.id.cur_speed);
        safeDistanceView = (TextView) findViewById(R.id.safe_distance);

        maxSpeed = mySpeed = 0;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // GPS 값 가져오는 리스너
        ll = new LocationListener() {
            public void onLocationChanged(Location location) {
                //여기서 위치값이 갱신되면 이벤트가 발생한다.
                //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
                if (location != null) {
                    // 현재 속도 가져옴
                    mySpeed = location.getSpeed() * 3.6;
                    if (mySpeed > maxSpeed) {
                        maxSpeed = mySpeed;
                    }
                    curSpeedView.setText("\nCurrent Speed : " + mySpeed + " km/h, Max Speed : "
                            + maxSpeed + " km/h");

                    // 안전거리 계산
                    // 도로교통공단 기준 80 km/h 미만일 경우 현재속도 - 15
                    // 80 km/h 이상 또는 고속도로일 경우 현재속도로 한다.
                    // 여기에 날씨정보를 포함하여
                    // 비가올경우 x1.5
                    // 눈이올경우 x3 을한다.
                    safeDistanceView.setText("Safe Distance : " + ScoreCalculator.scoreCalculator.getSafeDistance(mySpeed));
                }

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                    double longitude = location.getLongitude();    //경도
                    double latitude = location.getLatitude();         //위도
                    float accuracy = location.getAccuracy();        //신뢰도


                    // 도로별 속도 제한을 파악하기위해 T map API 도로 타입을 가져온다.
                    // roadType을 기준으로 고속도로, 도시고속도로의 경우 고속도로 제한속도를 적용한다.
                    // 편도 갯수에 따라 달라지지만 우선 2개 이상인 경우로 판단할것

                    // 만약 T map api 중 경로API 에서 turnType 의 정보중 191 번이 제한속도일경우 경로 API 사용도 가능할 듯
                    // roadType(1:고속, 2:자동차전용, 3~:일반도로)이 바뀌었을때 turnType에대한 정보가 없다면 기본 제한속도를 roadType으로 결정
                    // nodeType이 POINT 인 것중 turnType이 191 이면 제한속도 변경
                } else {
                    //Network 위치제공자에 의한 위치변화
                    //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                }
            }
            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };


        registerLocationUpdates();
    }

    private void registerLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //두번째는 1/1000초마다, 세번째는 1미터마다 해당 값을 갱신한다는 뜻으로, 딜레이마다 호출하기도 하지만
            //위치값을 판별하여 일정 미터단위 움직임이 발생 했을 때에도 리스너를 호출 할 수 있다.
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, ll);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, ll);
        } else {
            Toast.makeText(this, "퍼미션없뜸", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    TAG_CODE_PERMISSION_LOCATION);
        }
    }
}
