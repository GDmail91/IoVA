package org.multibluetooth.multibluetooth.Driving.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothLaserService;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.R;

/**
 * Created by YS on 2016-09-19.
 */
public class BluetoothConnection {
    private static final String TAG = "BluetoothConnection";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;

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
     * Member object for the chat services
     */
    protected BluetoothService mChatService = null;

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

    public void conn() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((AppCompatActivity) mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mChatService == null) {
            // TODO sendMessage() 를 할 컴포넌트 연결
            // ex) sendMessage(message);
            //setupChat(context);
            setupService();
        }
    }

    public void setChangeContext(Context context) {
        this.mContext = context;
    }

    public void serviceConn() {
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
    }

    public void serviceStop() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    protected void setupService() {
        mChatService = new BluetoothLaserService(mContext, mHandler);
        mOutStringBuffer = new StringBuffer("");

        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        ((AppCompatActivity) mContext).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }

    public int getConnectionStatus() {
        return mChatService.getState();
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "커넥 디바이스 실행");
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

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
     */
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
            mOutStringBuffer.setLength(0);
            Log.d(TAG, "write: "+message);
        }
    }

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
                        case BluetoothLaserService.STATE_CONNECTED:
                            // String Resource 값 변경
                            setStatus(mContext.getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothLaserService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothLaserService.STATE_LISTEN:
                        case BluetoothLaserService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
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
                    /*if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }*/
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
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    if (mChatService == null)
                        setupService();
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
