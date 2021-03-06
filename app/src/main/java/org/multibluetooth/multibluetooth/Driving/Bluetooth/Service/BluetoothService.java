package org.multibluetooth.multibluetooth.Driving.Bluetooth.Service;

/**
 * Created by YS on 2016-09-25.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public abstract class BluetoothService extends Service {
    // Debugging
    private static final String TAG = "BluetoothService";

    // Name for the SDP record when creating server socket
    protected static final String NAME_SECURE = "BluetoothChatSecure";
    protected static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    protected static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    protected static String deviceName;

    protected String sendDeviceCheckStr;
    protected String checkDeviceCheckStr;

    // Member fields
    protected BluetoothAdapter mAdapter;
    protected Handler mHandler;
    protected AcceptThread mSecureAcceptThread;
    protected ConnectThread mConnectThread;
    protected ConnectedThread mConnectedThread;
    protected int mState;

    protected LocalBinder mBinder;    // 컴포넌트에 반환되는 IBinder

    private final Random rand = new Random();

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

        public static final int REQUEST_OBD_SENSOR_DATA = 500;  // OBD 센서 데이터 요청
    public static final int REQUEST_LASER_SENSOR_DATA = 501;  // Laser 센서 데이터 요청
    public static final int REQUEST_SCAN_LEFT_SENSOR_DATA = 502;  // 왼쪽 Scan 센서 데이터 요청
    public static final int REQUEST_SCAN_RIGHT_SENSOR_DATA = 503;  // 오른쪽 Scan 센서 데이터 요청
    public static final int REQUEST_SCAN_STOP = 504; // Scan Stop

    public void init(Handler handler) {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        this.mHandler = handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    // 컴포넌트에 반환해줄 IBinder를 위한 클래스
    public class LocalBinder extends Binder implements BluetoothBinderInterface {

        @Override
        public void init(Handler handler) {
            mHandler = handler;
        }

        @Override
        public int getState() {
            return mState;
        }

        @Override
        public void start() {
            Log.d(TAG, "start");

            // Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            setState(STATE_LISTEN);

            // Start the thread to listen on a BluetoothServerSocket
            if (mSecureAcceptThread == null) {
                mSecureAcceptThread = new AcceptThread(true);
                mSecureAcceptThread.start();
            }
        }

        @Override
        public void stop() {
            Log.d(TAG, "stop");

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }

            setState(STATE_NONE);
        }

        @Override
        public void connect(BluetoothDevice device, boolean secure) {
            Log.d(TAG, "connect to: " + device);

            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }

            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device, secure);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }

        @Override
        public void write(Bundle cmdInfo) {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED) return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(cmdInfo);
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    protected synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        if (mHandler != null) {
            // Give the new state to the Handler so the UI Activity can update
            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    protected abstract boolean checkDevice(String checkStr);

    protected abstract String readBuffer(InputStream sin);
/*
    *//**
     * Return the current connection state.
     *//*
    public synchronized int getState() {
        return mState;
    }

    *//**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     *//*
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        // remove insecure accept
        *//*if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }*//*
    }*/

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    /*public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
*/
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public abstract void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType);
/*
    *//**
     * Stop all threads
     *//*
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // remove insecure accept
        *//*
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }*//*
        setState(STATE_NONE);
    }*/

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    //public abstract void write(Bundle out);

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    protected void connectionFailed() {
        if (mHandler != null) {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Unable to connect device");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
        }
        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    protected void connectionLost() {
        if (mHandler != null) {
            // Send a failure message back to the Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Device connection was lost");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
        }
        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    public abstract void start();

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    protected class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            Log.d(TAG, mAdapter.toString());
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    // 10초간 원하는 커넥션인지 파악
                    try {
                        OutputStream sout = socket.getOutputStream();
                        InputStream sin = socket.getInputStream();

                        int timeOut = 0;
                        while(timeOut++ <= 10) {

                            sout.write(sendDeviceCheckStr.getBytes());
                            sout.flush();

                            String testWord = readBuffer(sin);

                            Log.d(TAG, testWord);
                            if (checkDevice(testWord)) {
                                setState(STATE_CONNECTING);
                                break;
                            }

                            setState(STATE_NONE);
                            // 1초간 슬립
                            sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    synchronized (BluetoothService.this) {
                        Log.d(TAG, "AcceptThread mState:"+mState);
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    protected class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // 10초간 원하는 커넥션인지 파악
            try {
                OutputStream sout = mmSocket.getOutputStream();
                InputStream sin = mmSocket.getInputStream();

                setState(STATE_NONE);
                int timeOut = 0;
                while(timeOut++ <= 10) {

                    sout.write(sendDeviceCheckStr.getBytes());
                    sout.flush();

                    String testWord = readBuffer(sin);

                    Log.d(TAG, testWord);
                    if (checkDevice(testWord)) {
                        setState(STATE_CONNECTING);
                        break;
                    }

                    // 1초간 슬립
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            mConnectThread = null;

            Log.d(TAG, "ConnectThread mState:"+mState);
            switch (mState) {
                case STATE_LISTEN:
                case STATE_CONNECTING:
                case STATE_CONNECTED:
                    // Situation normal. Start the connected thread.
                    Log.d(TAG, "여기 실행 되야해");
                    // Start the connected thread
                    connected(mmSocket, mmDevice, mSocketType);
                    break;
                case STATE_NONE:
                    // Either not ready or already connected. Terminate new socket.
                    try {
                        mmSocket.close();
                        connectionFailed();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close unwanted socket", e);
                    }
                    break;

            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }
}

