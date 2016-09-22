package org.multibluetooth.multibluetooth.MainMenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothChatService;
import org.multibluetooth.multibluetooth.R;

/**
 * Created by YS on 2016-09-22.
 */
public class BluetoothCheckDialog extends Activity {
    private static final String TAG = "BluetoothCheckDialog";

    private RelativeLayout laserConn;
    private RelativeLayout obdConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_check_dialog);
        setTitle("블루투스 연결상태");
/*
        laserConn = (RelativeLayout) findViewById(R.id.laser_conn);
        obdConn = (RelativeLayout) findViewById(R.id.obd_conn);
        laserConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBluetoothConnecting(v);
            }
        });
        obdConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBluetoothConnecting(v);
            }
        });*/

        Intent intent = getIntent();
        Log.d(TAG, intent.toString());
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            setChangeText(extras.getInt("LASER", BluetoothChatService.STATE_NONE), R.id.laser_state);
            setChangeText(extras.getInt("OBD", BluetoothChatService.STATE_NONE), R.id.obd_state);
        } else {
            setChangeText(BluetoothChatService.STATE_NONE, R.id.laser_state);
            setChangeText(BluetoothChatService.STATE_NONE, R.id.obd_state);
        }
    }

    private void setChangeText(int status, int viewId) {
        TextView v = (TextView) findViewById(viewId);
        switch (status) {
            case BluetoothChatService.STATE_NONE:
                v.setText("연결끊김");
                break;
            case BluetoothChatService.STATE_LISTEN:
                v.setText("연결 시도중");
                break;
            case BluetoothChatService.STATE_CONNECTING:
                v.setText("연결중");
                break;
            case BluetoothChatService.STATE_CONNECTED:
                v.setText("연결됨");
                break;
            default:
                v.setText("연결끊김");
        }
    }

    public void onBluetoothConnecting(View v) {
        switch (v.getId()) {
            case R.id.laser_conn:
                Intent laserIntent = new Intent();
                setResult(MainMenuActivity.BLUETOOTH_LASER_CONNECT, laserIntent);
                finish();
                break;
            case R.id.obd_conn:
                Intent obdIntent = new Intent();
                setResult(MainMenuActivity.BLUETOOTH_OBD_CONNECT, obdIntent);
                finish();
                break;
        }
    }
}
