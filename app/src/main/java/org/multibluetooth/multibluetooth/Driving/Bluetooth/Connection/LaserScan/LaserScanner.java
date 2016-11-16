package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothLaserService;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfo;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.ScoreCalculator;

import java.util.LinkedList;

/**
 * Created by YS on 2016-09-13.
 */
public class LaserScanner extends BluetoothConnection {
    private static final String TAG = "LaserScanner";

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_LASER = 2001;
    public static final int REQUEST_ENABLE_BT_BY_LASER = 2003;
    public static final int SCAN_LEFT = 3001;
    public static final int SCAN_RIGHT = 3002;
    public static final int SCAN_STOP = 3003;

    private static final LinkedList<Character> buffer = new LinkedList<>();

    public LaserScanner(Context context) {
        super(context);
        //bindService();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_LASER);
        }
    }

    @Override
    public void conn() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_LASER);
        } else if (!mBound) {
            bindService();
        }
    }

    @Override
    public void bindService() {
        Log.d(TAG, "bindService()");
        Intent intent = new Intent(mContext, BluetoothLaserService.class);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void serviceStop() {
        if(mBound){
            //binder.stop();
            mContext.unbindService(this);
            mBound = false;
        }
    }

    /**
     * Service init
     */
    public void setupService() {
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_LASER);
        } else {
            if (mBound) {
                Log.d(TAG, "setupService().init()");
                binder.init(mHandler);
                setupStringBuffer();

                //((BluetoothLaserService.LocalBinder) binder).start();
            }
        }
    }

    @Override
    public void setupConnect() {
        Log.d(TAG, "setupConnect()");
        setupStringBuffer();

        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE_BY_LASER);
    }

    @Override
    public int getConnectionStatus() {
        if (binder != null)
            return binder.getState();
        else
            return BluetoothService.STATE_NONE;
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "커넥 디바이스 실행");
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        binder.connect(device, secure);
    }

    /**
     * Sends a request distance.
     *
     */
    public void sendMessage(int id) {
        // Check that we're actually connected before trying anything
        Log.d(TAG, "message 보낼때 "+ binder.getState());
        if (binder.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        // Get the message bytes and tell the BluetoothLaserService to write
        Bundle out = new Bundle();
        out.putInt("sensing_id", id);
        out.putInt("out", BluetoothService.REQUEST_LASER_SENSOR_DATA);
        binder.write(out);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

    /**
     * Sends request to scan.
     *
     */
    public void sendScan(int side) {
        // Check that we're actually connected before trying anything
        if (binder.getState() != BluetoothLaserService.STATE_CONNECTED) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        // Get the message bytes and tell the BluetoothLaserService to write
        Bundle out = new Bundle();
        out.putInt("side", side);
        switch (side) {
            case SCAN_LEFT:
                out.putInt("out", BluetoothService.REQUEST_SCAN_LEFT_SENSOR_DATA);
                break;
            case SCAN_RIGHT:
                out.putInt("out", BluetoothService.REQUEST_SCAN_RIGHT_SENSOR_DATA);
                break;
            case SCAN_STOP:
                out.putInt("out", BluetoothService.REQUEST_SCAN_STOP);
                break;
        }
        binder.write(out);

        // Reset out string buffer to zero and clear the edit text field
        mOutStringBuffer.setLength(0);
    }

    @Deprecated
    @Override
    protected String messageParse(String message) {
        // Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

        message = message.toUpperCase();

        for(char c : message.toCharArray()) {
            Log.d(TAG, ""+c);
            buffer.add(c);
        }

        while (buffer.size() >= 10) {
            String msgStart = buffer.removeFirst().toString() + buffer.removeFirst().toString();
            String msgMode = "";
            String msgBody = "";

            if (msgStart.equals("AA")) {
                for (int i=0; i<8; i++) {
                    if (i<2) {
                        msgMode += buffer.removeFirst();
                    } else {
                        msgBody += buffer.removeFirst();
                    }
                }

                // 모드에 따라서 Driving Activity에 다르게 표시
                switch (msgMode) {
                    case "01":
                        // 전방 측정
                        ((DrivingActivity) mContext).setChangeText(msgBody);
                        ((DrivingActivity) mContext).setForwardText(Float.valueOf(msgBody));
                        // TODO 삭제 전시회용
                        ((DrivingActivity) mContext).setBackText(Float.valueOf(msgBody));
                        break;
                    case "02":
                        // 후방 측정
                        //((DrivingActivity) mContext).setChangeText("mode: "+msgMode+"\nbody: "+msgBody);
                        //((DrivingActivity) mContext).setBackText(msgBody);
                        break;
                    case "03":
                        // 옆차선 측정
                        break;
                }
            } else {
                Log.d(TAG, "쓰레기값");
            }
            return msgBody;
        }
        return message;
    }

    protected LaserScanData laserMessageParse(String message) {
        // Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

        message = message.toUpperCase();
        LaserScanData laserScanData = new LaserScanData();

        for(char c : message.toCharArray()) {
            Log.d(TAG, ""+c);
            buffer.add(c);
        }

        while (buffer.size() >= 10) {
            String msgStart = buffer.removeFirst().toString() + buffer.removeFirst().toString();
            String msgMode = "";
            String msgBody = "";

            if (msgStart.equals("AA")) {
                for (int i=0; i<8; i++) {
                    if (i<2) {
                        msgMode += buffer.removeFirst();
                    } else {
                        msgBody += buffer.removeFirst();
                    }
                }

                // 모드에 따라서 Driving Activity에 다르게 표시
                switch (msgMode) {
                    case "01":
                        // 전방 측정
                        laserScanData.setFrontDistance(Float.valueOf(msgBody));
                        ((DrivingActivity) mContext).setChangeText(msgBody);
                        ((DrivingActivity) mContext).setForwardText(Float.valueOf(msgBody));
                        break;
                    case "02":
                        // 후방 측정
                        laserScanData.setBackDistance(Float.valueOf(msgBody));
                        //((DrivingActivity) mContext).setChangeText("mode: "+msgMode+"\nbody: "+msgBody);
                        ((DrivingActivity) mContext).setBackText(Float.valueOf(msgBody));
                        break;
                    case "03":
                        // 옆차선 측정
                        laserScanData.setSideDistance(Float.valueOf(msgBody));
                        ((DrivingActivity) mContext).setSideDistance(Float.valueOf(msgBody));
                        break;
                }
            } else {
                Log.d(TAG, "쓰레기값");
            }
        }
        return laserScanData;
    }

    @Override
    protected String updateData(Bundle bundle) {
        LaserScanData parsedMessage = laserMessageParse(bundle.getString("MESSAGE1"));

        Log.d(TAG, "전방측정:"+parsedMessage.getFrontDistance());
        Log.d(TAG, "후방측정:"+parsedMessage.getBackDistance());
        String category = bundle.getString("CATEGORY");
        Log.d(TAG, category);
        try {
            switch (category) {
                case "OBD":
                case "Laser":
                    Log.d(TAG, "Laser 저장");
                    // Laser 센싱된 데이터 DB에 저장
                    int sensingId = bundle.getInt("sensing_id");

                    DriveInfo driveInfo = new DriveInfo();
                    if (parsedMessage.getSideDistance() == 0) {
                        driveInfo.setDistance(sensingId,
                                parsedMessage.getFrontDistance(),
                                parsedMessage.getBackDistance());
                    } else {
                        driveInfo.setDistance(sensingId,
                                parsedMessage.getFrontDistance(),
                                parsedMessage.getBackDistance(),
                                parsedMessage.getSideDistance());
                    }
                    mScoreCalculator.putData(ScoreCalculator.LASER_DATA, driveInfo);

                    DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
                    driveInfoModel.updateFrontLaser(driveInfo);
                    driveInfoModel.close();
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return parsedMessage.toString();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected()");
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        binder = (BluetoothLaserService.LocalBinder) service;
        mBound = true;
        setupService();
        //setupConnect();
        Log.d(TAG, "Laser 서비스 연결됨");
        Toast.makeText(mContext, "Laser 서비스 연결됨", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        Log.d(TAG, "Laser 서비스 연결 실패");
        Toast.makeText(mContext, "Laser 서비스 연결 실패", Toast.LENGTH_SHORT).show();
    }
}
