package org.multibluetooth.multibluetooth.Driving.Bluetooth.OBDScan;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothLaserService;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothOBDService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfo;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.R;

import java.util.LinkedList;

/**
 * Created by YS on 2016-09-13.
 */
public class OBDScanner extends BluetoothConnection {
    private static final String TAG = "OBDScanner";

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_OBD = 3001;
    public static final int REQUEST_ENABLE_BT_BY_OBD = 3003;

    private static final LinkedList<Character> buffer = new LinkedList<>();

    public OBDScanner(Context context) {
        super(context);
    }

    @Override
    public void conn() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_OBD);
        } else if (mChatService == null) {
            // TODO sendMessage() 를 할 컴포넌트 연결
            // ex) sendMessage(message);
            //setupChat(context);
            setupService();
        }
    }

    @Override
    protected void setupService() {
        mChatService = new BluetoothOBDService(mContext, mHandler);
        mOutStringBuffer = new StringBuffer("");

        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE_BY_OBD);
    }

    /**
     * Sends a message.
     *
     */
    public void sendMessage(int id) {
        // Check that we're actually connected before trying anything
        Log.d(TAG, "message 보낼때 "+ mChatService.getState());
        if (mChatService.getState() != BluetoothLaserService.STATE_CONNECTED) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        // Get the message bytes and tell the BluetoothLaserService to write
        Bundle out = new Bundle();
        out.putInt("sensing_id", id);
        out.putInt("out", BluetoothOBDService.REQUEST_SENSOR_DATA);
        mChatService.write(out);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

    @Override
    protected String messageParse(String message) {
        ((DrivingActivity) mContext).setChangeText(message);

        return message;
    }

    @Override
    protected String updateData(Bundle bundle) {
        String parsedMessage = messageParse(bundle.getString("MESSAGE"));

        Log.d(TAG, parsedMessage);
        String category = bundle.getString("CATEGORY");
        Log.d(TAG, category);
        try {
            switch (category) {
                case "OBD":
                    Log.d(TAG, "OBD 저장");
                    // OBD 센싱된 데이터 DB에 저장
                    int sensingId = bundle.getInt("sensing_id");
                    DriveInfo driveInfo = new DriveInfo();
                    driveInfo.setOBDSensor(sensingId, Integer.valueOf(parsedMessage));
                    mScoreCalculator.putOBDData(driveInfo);

                    DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, "DriveInfo.db", null);
                    driveInfoModel.updateOBD(driveInfo);
                    driveInfoModel.close();
                    break;
                case "Laser":
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return parsedMessage;
    }
}
