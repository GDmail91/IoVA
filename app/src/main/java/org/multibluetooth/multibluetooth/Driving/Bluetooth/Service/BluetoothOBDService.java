/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.multibluetooth.multibluetooth.Driving.Bluetooth.Service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan.OBDCommandList;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothOBDService extends BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothOBDService";

    private static final LinkedList<String> sendingQueue = new LinkedList<>();

    //public static final int REQUEST_SENSOR_DATA = 500;  // OBD 센서 데이터 요청

    @Override
    public void onCreate() {
        super.onCreate();

        sendDeviceCheckStr = "01 0D\r";
        checkDeviceCheckStr = "Number";
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

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        mBinder.start();
    }

    protected boolean checkDevice(String checkStr) {
        try {
            Log.d(TAG, "checkDevice: "+checkStr);
            Integer.parseInt(checkStr);

            return true;
        } catch (Exception e) {
            return false;
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
        mConnectedThread = new ConnectedOBDThread(socket, socketType);
        ((ConnectedOBDThread) mConnectedThread).start();

        if (mHandler != null) {
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            Message msg2 = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle2 = new Bundle();
            bundle2.putString(Constants.TOAST, "OBD 연결됨");
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
    protected class ConnectedOBDThread extends Thread implements ConnectedThread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private OBDCommandList obdCommandList = new OBDCommandList();
        private boolean requestOBD = false;
        private boolean testOBD = false;
        private int sensingId = 0;

        public ConnectedOBDThread(BluetoothSocket socket, String socketType) {
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

            Log.d(TAG, "ConnectedThread mState:"+mState);
            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    if (requestOBD) {
                        for (org.multibluetooth.multibluetooth.Obd.ObdCommand cmd : obdCommandList.cmdList) {
                            cmd.run(mmInStream, mmOutStream);

                            Log.d(TAG, "결과: "+cmd.getResult() + " / " +cmd.getCalculatedResult() + " / "+ cmd.getResultUnit());
                            String message = cmd.getCalculatedResult();

                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.DEVICE_NAME, deviceName);
                            // 명령마다 구분
                            bundle.putString("CATEGORY", "OBD");
                            bundle.putInt("sensing_id", sensingId);
                            bundle.putString("MESSAGE", message);
                            Log.d(TAG, message);

                            if (mHandler != null) {
                                // Send the obtained bytes to the UI Activity
                                mHandler.obtainMessage(Constants.MESSAGE_READ, message.length(), -1, bundle)
                                        .sendToTarget();
                            }
                        }
                        requestOBD = false;
                    }

                    if (testOBD) {
                        byte[] buffer = new byte[1024];
                        int bytes;
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer); // 010D410037
                        String rawData = new String(buffer);
                        rawData = rawData.replaceAll("\\s", ""); //removes all [ \t\n\x0B\f\r]
                        rawData = rawData.replaceAll("(BUS INIT)|(BUSINIT)|(\\.)", "");
                        Log.d(TAG, "bytes "+bytes);
                        Log.d(TAG, "test "+rawData); // 010410D

                        String message = new String(buffer);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.DEVICE_NAME, deviceName);
                        bundle.putString("MESSAGE", message);

                        if (mHandler != null) {
                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ, message.length(), -1, bundle)
                                    .sendToTarget();
                        }
                        testOBD = false;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothOBDService.this.start();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "connected thread end");
        }

        /**
         * Write to the connected OutStream.
         *
         * @param cmdInfo The bytes to write
         */
        public void write(Bundle cmdInfo) {
            if (cmdInfo.getBoolean("test")) {
                try {
                    byte[] buffer = cmdInfo.getByteArray("out");
                    mmOutStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                testOBD = true;
            } else {
                switch (cmdInfo.getInt("out")) {
                    case REQUEST_OBD_SENSOR_DATA:
                        sensingId = cmdInfo.getInt("sensing_id");
                        requestOBD = true;
                        break;
                }
            }
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
