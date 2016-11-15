package org.multibluetooth.multibluetooth.Driving.Bluetooth.Service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Created by YS on 2016-11-13.
 */
public class BluetoothSideService extends BluetoothService {
    private static final String TAG = "BluetoothSideService";

    protected static final LinkedList<Character> messageStack = new LinkedList<>();
    protected static String inputeMessage = "";
    protected static boolean request = false;

    @Override
    public void onCreate() {
        super.onCreate();

        sendDeviceCheckStr = "S";
        checkDeviceCheckStr = "Lamp";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Service Starting");
        mBinder = new LocalBinder();    // 컴포넌트에 반환되는 IBinder
        //mBinder.start();
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void start() {
        mBinder.start();
    }

    @Override
    protected boolean checkDevice(String checkStr) {
        return checkDeviceCheckStr.equals(checkStr);
    }

    @Override
    protected String readBuffer(InputStream sin) {
        try {
            byte[] buffer = new byte[255];
            int length = sin.read(buffer);

            return new String(buffer, 0, length);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Override
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            Log.d(TAG, "connectTh");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            Log.d(TAG, "connectedTh");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            Log.d(TAG, "secAcTh");
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        // remove insecure accept
        /*if (mInsecureAcceptThread != null) {
            Log.d(TAG, "inSecAcTh");
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }*/

        setState(STATE_CONNECTED);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedChatThread(socket, socketType);
        ((ConnectedChatThread) mConnectedThread).start();

        if (mHandler != null) {
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            Message msg2 = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle2 = new Bundle();
            bundle2.putString(Constants.TOAST, "Side 연결됨");
            msg2.setData(bundle2);
            mHandler.sendMessage(msg2);
        }

        // Set device name
        deviceName = device.getName();

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    @Override
    protected void connectionFailed() {
        Log.d(TAG, "connectionFailed()");
        if (mHandler != null) {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Unable to connect device");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    @Override
    protected void connectionLost() {
        Log.d(TAG, "connectionLost()");
        if (mHandler != null) {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Device connection was lost");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    protected class ConnectedChatThread extends Thread implements ConnectedThread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private int sensingId = 0;

        public ConnectedChatThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            Log.d(TAG, "ConnectedThread mState:"+mState);
            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    if (request) {
                        // TODO Something in request sequence
                        request = false;
                    }
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    toMessageStack(new String(buffer, 0, bytes));

                    if (!msgCheck().equals("")) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.DEVICE_NAME, deviceName);
                        bundle.putString("MESSAGE1", inputeMessage);

                        // 명령마다 구분
                        bundle.putString("CATEGORY", "Side");
                        bundle.putInt("sensing_id", sensingId);

                        if (mHandler != null) {
                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ, inputeMessage.length(), -1, bundle)
                                    .sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothSideService.this.start();
                    break;
                }
            }
            Log.d(TAG, "connected thread end");
        }

        protected void toMessageStack(String message) {
            message = message.toUpperCase();

            for(char c : message.toCharArray()) {
                Log.d(TAG, ""+c);
                messageStack.add(c);
            }
        }

        protected String msgCheck() {
            // 전체를 줘야한다면 inputMessage 사용
            inputeMessage = "";

            while (messageStack.size() > 0) {
                char c = messageStack.removeFirst();

                switch (c) {
                    case '{':
                        inputeMessage = "{";
                        break;
                    case '}':
                        if (inputeMessage.length() > 3)
                            inputeMessage = "";
                        else {
                            inputeMessage = inputeMessage.trim();
                            inputeMessage += inputeMessage + "}";
                        }
                        break;
                    default:
                        inputeMessage += c;
                        break;
                }
            }

            return inputeMessage;
        }

        /**
         * Write to the connected OutStream.
         *
         * @param cmdInfo The bytes to write
         */
        public void write(Bundle cmdInfo) {
            sensingId = cmdInfo.getInt("sensing_id");
            try {

                String reqMessage = "something to send";
                byte[] buffer = reqMessage.getBytes();
                mmOutStream.write(buffer);

                if (mHandler != null) {
                    // Share the sent message back to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
            request = true;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
