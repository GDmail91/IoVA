package org.multibluetooth.multibluetooth.Driving;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan.OBDCommandList;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.Driving.ServerConnection.ZoneNameFinder;
import org.multibluetooth.multibluetooth.MainMenu.MainMenuActivity;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;
import org.multibluetooth.multibluetooth.retrofit.RetrofitService;
import org.multibluetooth.multibluetooth.retrofit.format.DTOdangerLocation;
import org.multibluetooth.multibluetooth.retrofit.format.DTOdangerLocationData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by YS on 2016-09-23.
 */
public class DriveThread extends Thread {

    private Context mContext;
    private Handler mHandler;
    private int topDriveNumber;
    private boolean request = true;
    private boolean leftScan = false;
    private boolean rightScan = false;
    private int i=0;
    private String mZoneName = "";
    OBDCommandList message = new OBDCommandList();

    // GpsInfo 객체를 얻어온다
    GpsInfo gpsInfo;

    public DriveThread(Context context, Handler handler, int topDriveNumber, GpsInfo gpsInfo) {
        this.mContext = context;
        this.mHandler = handler;
        this.topDriveNumber = topDriveNumber;
        this.gpsInfo = gpsInfo;
        this.gpsInfo.startLocation();
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

    public int stopRequest() {
        driveStop();
        request = false;
        return topDriveNumber;
    }
    @Override
    public void run() {
        super.run();

        if (MainMenuActivity.btOBDCon != null) {
            MainMenuActivity.btOBDCon.queueInit(topDriveNumber);
        }
        if (MainMenuActivity.btLaserCon != null) {
            MainMenuActivity.btLaserCon.queueInit(topDriveNumber);
        }

        while (request) {
            if (MainMenuActivity.btOBDCon != null) {
                // TODO && MainMenuActivity.btLaserCon != null) {
                try {
                    if (gpsInfo.getLocation() != null) {
                        i += 1;
                        // TODO 센서에 요청
                        // TODO 값 출력

                        // TODO 센서 ID 가져와야함
                        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, "DriveInfo.db", null);
                        int id = driveInfoModel.createIndex(topDriveNumber);
                        Location temp = gpsInfo.getLocation();
                        Log.d("TEST", temp.toString());
                        driveInfoModel.updateGps(id, temp);    // GPS 위치 저장
                        driveInfoModel.close();

                        /*Message fMessage = new Message();
                        fMessage.what = Constants.MESSAGE_READ;
                        fMessage.arg1 = DrivingActivity.FORWARD_MESSAGE;

                        Bundle fBundle = new Bundle();
                        fBundle.putString("message", "앞" + i);
                        fMessage.setData(fBundle);
                        mHandler.sendMessage(fMessage);*/

                                // 뒷쪽 센서 데이터 출력
                        /*Message bMessage = new Message();
                        bMessage.what = Constants.MESSAGE_READ;
                        bMessage.arg1 = DrivingActivity.BACK_MESSAGE;
                        Bundle bBundle = new Bundle();
                        bBundle.putString("message", "뒤" + i);
                        bMessage.setData(bBundle);
                        mHandler.sendMessage(bMessage);*/

                        // OBD 데이터 출력
                        ((OBDScanner) MainMenuActivity.btOBDCon).sendMessage(id);

                        if (leftScan) {
                            // 앞쪽 센서 데이터 출력
                            ((LaserScanner) MainMenuActivity.btLaserCon).sendScan(LaserScanner.SCAN_LEFT);
                        } else if (rightScan) {
                            // 앞쪽 센서 데이터 출력
                            ((LaserScanner) MainMenuActivity.btLaserCon).sendScan(LaserScanner.SCAN_RIGHT);
                        } else {
                            // 앞쪽 센서 데이터 출력
                            ((LaserScanner) MainMenuActivity.btLaserCon).sendMessage(id);
                        }

                        // TODO 뒷쪽 데이터 출력

                        // 현재 위치(zone) 확인
                        String tempZoneName = ZoneNameFinder.getKoZNF(gpsInfo.getLocation());
                        if (!mZoneName.equals(tempZoneName)) {
                            mZoneName = tempZoneName;
                            loadDangerLevel(tempZoneName);
                        }
                    }
                    // 1초간 슬립
                    sleep(1000);

                    // 자동 종료가 필요하다면 추가
                    /*if (i > 100) {
                        Message endMessage = new Message();
                        endMessage.what = Constants.MESSAGE_READ;
                        endMessage.arg1 = DrivingActivity.DRIVE_STOP_FLAG;
                        mHandler.sendMessage(endMessage);
                        break;
                    }*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                driveStop();
                request = false;
            }
        }
    }

    public void scanChange(int scanType) {
        switch (scanType) {
            case LaserScanner.SCAN_LEFT:
                leftScan = true;
                rightScan = false;
                break;
            case LaserScanner.SCAN_RIGHT:
                leftScan = false;
                rightScan = true;
                break;
            case LaserScanner.SCAN_STOP:
                leftScan = false;
                rightScan = false;
                break;
        }
    }

    private void driveStop() {
        SafeScoreModel safeScoreModel = new SafeScoreModel(mContext, "SafeScore.db", null);
        safeScoreModel.updateEndOfDrive(topDriveNumber);
        safeScoreModel.close();
    }

    private void loadDangerLevel(String zoneName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mContext.getResources().getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        Call<DTOdangerLocation> call = service.getDangerLevel(zoneName);
        call.enqueue(new Callback<DTOdangerLocation>() {
            @Override
            public void onResponse(Response<DTOdangerLocation> response) {
                if (response.isSuccess() && response.body() != null) {

                    DTOdangerLocation responseBody= response.body();
                    DTOdangerLocationData dangerLocation = responseBody.getData();

                    // TODO level 변화
                    switch (dangerLocation.getLevel()) {
                        case 1:
                            ((DrivingActivity) mContext).onAlert(DrivingActivity.DANGER_LOCATION_IN_MIDDLE);
                            break;
                        case 2:
                            ((DrivingActivity) mContext).onAlert(DrivingActivity.DANGER_LOCATION_IN_WARNING);
                            break;
                        case 3:
                            ((DrivingActivity) mContext).onAlert(DrivingActivity.DANGER_LOCATION_IN_CRITICAL);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext.getApplicationContext(), "서버 연결 실패", Toast.LENGTH_LONG).show();
            }
        });
    }
}
