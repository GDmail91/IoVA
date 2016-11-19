package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.enums.AvailableCommandNames;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothOBDService;
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
public class OBDScanner extends BluetoothConnection {
    private static final String TAG = "OBDScanner";

    public boolean AUTO_CONN = true;

    private DriveInfo mDriveInfo = new DriveInfo();;

    private static final LinkedList<Character> buffer = new LinkedList<>();

    public OBDScanner(Context context) {
        super(context);
    }

    public void connMode(boolean autoMode) {
        AUTO_CONN = autoMode;
    }

    @Override
    public void conn() {
        Log.d(TAG, "conn 실행");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBound) {
            bindService();
        }
    }

    @Override
    public void bindService() {
        Log.d(TAG, "bindService()");
        Intent intent = new Intent(mContext, BluetoothOBDService.class);
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
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT_BY_OBD);
        } else {
            if (mBound) {
                Log.d(TAG, "setupService().init()");
                binder.init(mHandler);
                setupStringBuffer();
            }
        }
    }

    @Override
    public void setupConnect() {
        Log.d(TAG, "setupConnect 실행");
        mOutStringBuffer = new StringBuffer("");

        SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String address = pref.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS_OBD, "");
        Log.d(TAG, "뭘가지고 있나: "+address);
        if ("".equals(address) || !AUTO_CONN) {
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
            ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE_BY_OBD);
        } else {
            // Auto connect mode
            // IoVA will served only secure mode
            Intent intent = new Intent();
            intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
            connectDevice(intent, true);
        }
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

        SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = pref.edit();
        prefEdit.putString(DeviceListActivity.EXTRA_DEVICE_ADDRESS_OBD, address);
        prefEdit.apply();

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        binder.connect(device, secure);
    }

    /**
     * Sends a message.
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
        out.putInt("out", BluetoothService.REQUEST_OBD_SENSOR_DATA);
        binder.write(out);

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
        AvailableCommandNames parsedName = AvailableCommandNames.valueOf(bundle.getString("NAME"));

        Log.d(TAG, parsedMessage);
        String category = bundle.getString("CATEGORY");
        Log.d(TAG, category);
        try {
            switch (category) {
                case "OBD":
                    Log.d(TAG, "OBD 저장");
                    // OBD 센싱된 데이터 DB에 저장
                    int sensingId = bundle.getInt("sensing_id");
                    switch (parsedName) {
                        case SPEED:
                            mDriveInfo.setOBDSpeed(sensingId, Integer.valueOf(parsedMessage));
                            break;
                        case ENGINE_RPM:
                            mDriveInfo.setOBDRpm(sensingId, Integer.valueOf(parsedMessage));
                            break;
                    }

                    if (mDriveInfo.isSetOBDData()) {
                        mScoreCalculator.putData(ScoreCalculator.OBD_DATA, mDriveInfo);
                        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
                        driveInfoModel.updateOBD(mDriveInfo);
                        driveInfoModel.close();
                        mDriveInfo.clear();
                    }
                    break;
                case "Laser":
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return parsedMessage;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected()");
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        binder = (BluetoothOBDService.LocalBinder) service;
        mBound = true;
        setupService();
        //setupConnect();
        Log.d(TAG, "OBD 서비스 연결됨");
        Toast.makeText(mContext, "OBD 서비스 연결됨", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        Log.d(TAG, "OBD 서비스 연결 실패");
        Toast.makeText(mContext, "OBD 서비스 연결 실패", Toast.LENGTH_SHORT).show();
    }
}
