package org.multibluetooth.multibluetooth.Driving;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;

/**
 * Created by YS on 2016-09-23.
 */
public class DriveThread extends Thread {

    private Handler mHandler;
    private int i=0;

    public DriveThread(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                i += 1;
                // TODO 센서에 요청
                // TODO 값 출력
                // 앞쪽 센서 데이터 출력
                Message fMessage = new Message();
                fMessage.what = Constants.MESSAGE_READ;
                fMessage.arg1 = DrivingActivity.FORWARD_MESSAGE;
                Bundle fBundle = new Bundle();
                fBundle.putString("message", "앞"+i);
                fMessage.setData(fBundle);
                mHandler.sendMessage(fMessage);

                // 뒷쪽 센서 데이터 출력
                Message bMessage = new Message();
                bMessage.what = Constants.MESSAGE_READ;
                bMessage.arg1 = DrivingActivity.BACK_MESSAGE;
                Bundle bBundle = new Bundle();
                bBundle.putString("message", "뒤"+i);
                bMessage.setData(bBundle);
                mHandler.sendMessage(bMessage);

                // 1초간 슬립
                sleep(1000);
                if (i>100) break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
