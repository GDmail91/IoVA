package org.multibluetooth.multibluetooth.Driving;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.OBDScan.OBDCommandList;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.MainMenu.MainMenuActivity;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

/**
 * Created by YS on 2016-09-23.
 */
public class DriveThread extends Thread {

    private Context mContext;
    private Handler mHandler;
    private int topDriveNumber;
    private boolean request = true;
    private int i=0;
    OBDCommandList message = new OBDCommandList();

    public DriveThread(Context context, Handler handler, int topDriveNumber) {
        this.mContext = context;
        this.mHandler = handler;
        this.topDriveNumber = topDriveNumber;
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

    public void stopRequest() {
        driveStop();
        request = false;
    }
    @Override
    public void run() {
        super.run();

        if (MainMenuActivity.btOBDCon != null) {
            MainMenuActivity.btOBDCon.queueInit(topDriveNumber);
        }
        while (request) {
            if (MainMenuActivity.btOBDCon != null) {
                // TODO && MainMenuActivity.btLaserCon != null) {
                try {
                    i += 1;
                    // TODO 센서에 요청
                    // TODO 값 출력

                    // TODO 센서 ID 가져와야함
                    DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, "DriveInfo.db", null);
                    int id = driveInfoModel.createIndex(topDriveNumber);
                    driveInfoModel.close();

                    // 앞쪽 센서 데이터 출력
                    Message fMessage = new Message();
                    fMessage.what = Constants.MESSAGE_READ;
                    fMessage.arg1 = DrivingActivity.FORWARD_MESSAGE;

                    Bundle fBundle = new Bundle();
                    fBundle.putString("message", "앞" + i);
                    fMessage.setData(fBundle);
                    mHandler.sendMessage(fMessage);

                    // 뒷쪽 센서 데이터 출력
                    Message bMessage = new Message();
                    bMessage.what = Constants.MESSAGE_READ;
                    bMessage.arg1 = DrivingActivity.BACK_MESSAGE;
                    Bundle bBundle = new Bundle();
                    bBundle.putString("message", "뒤" + i);
                    bMessage.setData(bBundle);
                    mHandler.sendMessage(bMessage);

                    // OBD 데이터 출력
                    ((OBDScanner) MainMenuActivity.btOBDCon).sendMessage(id);

                    // 1초간 슬립
                    sleep(1000);
                    if (i > 100) break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                driveStop();
                request = false;
            }
        }
    }

    private void driveStop() {
        SafeScoreModel safeScoreModel = new SafeScoreModel(mContext, "DriveInfo.db", null);
        safeScoreModel.updateEndOfDrive(topDriveNumber);
        safeScoreModel.close();
    }
}
