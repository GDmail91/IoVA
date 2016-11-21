package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.MainMenu.MainMenuActivity;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.ScoreCalculator;

/**
 * Created by YS on 2016-09-19.
 */
public abstract class BluetoothConnection implements ServiceConnection, ServiceControl{
    private static final String TAG = "BluetoothConnection";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_LASER = 2001;
    public static final int REQUEST_ENABLE_BT_BY_LASER = 2003;

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_OBD = 3001;
    public static final int REQUEST_ENABLE_BT_BY_OBD = 3003;

    public static final int REQUEST_CONNECT_DEVICE_SECURE_BY_SIDE = 4001;
    public static final int REQUEST_ENABLE_BT_BY_SIDE = 4003;
    public static final int SCAN_STOP = 4010;
    public static final int SCAN_LEFT = 4011;
    public static final int SCAN_RIGHT = 4012;
    /**
     * Bluetooth Device ID
     */
    protected static BluetoothDevice mBTDevice;

    /**
     * Safe Score calculator
     */
    protected static ScoreCalculator mScoreCalculator;

    protected Context mContext;
    /**
     * Local Bluetooth adapter
     */
    protected BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Array adapter for the conversation thread
     */
    protected ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    protected StringBuffer mOutStringBuffer;

    /**
     * Name of the connected device
     */
    protected String mConnectedDeviceName = null;

    public BluetoothConnection(Context context) { init(context); }

    public int init(Context context) {
        mContext = context;

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        return 0;
    }

    protected BluetoothService.LocalBinder binder = null;
    protected boolean mBound = false;    // 서비스 연결 여부

    public boolean isBound() { return mBound; }

    public void queueInit(int topDriveNumber) {
        // make score calculate queue
        if (mScoreCalculator == null) {
            mScoreCalculator = new ScoreCalculator(mContext, topDriveNumber);
        } else {
            mScoreCalculator.init(topDriveNumber);
        }
    }

    public abstract void conn();

    public void setChangeContext(Context context) {
        this.mContext = context;
        if (mScoreCalculator != null)
            mScoreCalculator.setChangeContext(context);
    }

    /*public void serviceConn() {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothLaserService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }*/

    public abstract void bindService();

    public abstract void serviceStop();

    protected void setupStringBuffer() {
        mOutStringBuffer = new StringBuffer("");
    }

    public abstract void setupService();

    public void setupConnect() {
        setupStringBuffer();

        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }

    public abstract int getConnectionStatus();

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public abstract void connectDevice(Intent data, boolean secure);

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        // TODO resource ID로 블투 연결 표시
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        // TODO String 으로 블투 연결 표시
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     *//*
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        Log.d(TAG, "message 보낼때 "+ mChatService.getState());
        if (mChatService.getState() != BluetoothLaserService.STATE_CONNECTED) {
            Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothLaserService to write
            Bundle send = new Bundle();
            send.putBoolean("test", true);
            send.putByteArray("out", message.getBytes());
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            Log.d(TAG, "write: "+message);
        }
    }*/

    /**
     * The Handler that gets information back from the BluetoothLaserService
     */
    protected final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, msg.toString());
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            // String Resource 값 변경
                            setStatus(mContext.getString(R.string.title_connected_to, mConnectedDeviceName));
                            ((MainMenuActivity) mContext).setBtConnectSign();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            ((MainMenuActivity) mContext).setBtConnectSign();
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            ((MainMenuActivity) mContext).setBtConnectSign();
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_LASER_CONNECT:
                    ((MainMenuActivity) mContext).onAutoConnectOBD();
                    break;
                case Constants.MESSAGE_WRITE:
                    if (mContext.getApplicationInfo().className.equals("org.multibluetooth.multibluetooth.Driving.DrivingActivity")) {
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        Toast.makeText(mContext, writeMessage, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "message write: " + writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    Log.d(TAG, "activity name: "+mContext.getClass().getName());
                    if (mContext.getClass().getName().equals("org.multibluetooth.multibluetooth.Driving.DrivingActivity")) {
                        Bundle bundle = (Bundle) msg.obj;
                        /*byte[] readBuf = (byte[]) bundle.getByteArray("MESSAGE");
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);*/

                        // 메세지 파싱
                        // TODO byte[] 로 넘겨줄것
                        updateData(bundle);

                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    /*if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }*/
                    // TODO 지울것
                    //((DrivingActivity) mContext).setChangeText(mConnectedDeviceName);
                    Log.d(TAG, mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != mContext) {
                        Toast.makeText(mContext, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, Constants.TOAST);
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "액티비티 리절트가 안되는거 같음");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When Device
                // ListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    if (mBound)
                        setupConnect();
                    else
                        conn();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    ((Activity) mContext).finish();
                }
        }
    }

    protected String messageParse(String message) {

        ((DrivingActivity) mContext).setChangeText(message);
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

        return message;
    }

    protected String updateData(Bundle bundle) {
        String parsedMessage = messageParse(bundle.getString("MESSAGE"));

        Log.d(TAG, parsedMessage);

        return parsedMessage;
    }
}
