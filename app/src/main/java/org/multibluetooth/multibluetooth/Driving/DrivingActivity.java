package org.multibluetooth.multibluetooth.Driving;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.MainMenu.MainMenuActivity;
import org.multibluetooth.multibluetooth.R;

/**
 * Created by YS on 2016-09-13.
 */
public class DrivingActivity extends AppCompatActivity {

    // VIEW
    private TextView forwardDistance;
    private TextView backDistance;
    private TextView btDeviceName;

    double mySpeed, maxSpeed;

    private static final int DRIVE_START_FLAG = 1;
    public static final int FORWARD_MESSAGE = 100;
    public static final int BACK_MESSAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driving_activity);

        Intent intent = new Intent(DrivingActivity.this, DriveStartDialog.class);
        startActivityForResult(intent, DRIVE_START_FLAG);

        // VIEW 연결
        forwardDistance = (TextView) findViewById(R.id.forward_distance);
        backDistance = (TextView) findViewById(R.id.back_distance);
        btDeviceName = (TextView) findViewById(R.id.bt_device_name);

        maxSpeed = mySpeed = 0;

        if (MainMenuActivity.btLaserCon != null)
            MainMenuActivity.btLaserCon.setChangeContext(this);
        if (MainMenuActivity.btOBDCon != null)
            MainMenuActivity.btOBDCon.setChangeContext(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainMenuActivity.btLaserCon != null)
            MainMenuActivity.btLaserCon.serviceConn();
        if (MainMenuActivity.btOBDCon != null)
            MainMenuActivity.btOBDCon.serviceConn();
    }

    // 디바이스 메세지 전달
    public void setChangeText(String message) {
        btDeviceName.setText(message);
    }

    // 전방 데이터 전달
    public void setForwardText(String message) {
        forwardDistance.setText(message);
    }

    // 후방 데이터 전달
    public void setBackText(String message) {
        backDistance.setText(message);
    }

    // 운행 시작 버튼 클릭
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DRIVE_START_FLAG:
                // 운행 시작
                new DriveThread(mHandler).start();
                break;
        }
    }

    // 운행 쓰레드에서 Message를 받기위한 핸들러
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    switch (msg.arg1) {
                        case FORWARD_MESSAGE:
                            Bundle fBundle = msg.getData();
                            setForwardText(fBundle.getString("message"));
                            break;
                        case BACK_MESSAGE:
                            Bundle bBundle = msg.getData();
                            setBackText(bBundle.getString("message"));
                            break;
                    }
                    break;
            }
        }
    };
}
