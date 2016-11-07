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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan.LaserCommand;

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
public class BluetoothLaserService extends BluetoothService {
    private static final String TAG = "BluetoothLaserService";

    protected static final LinkedList<Character> messageStack = new LinkedList<>();
    protected static String inputeMessage = "";
    protected static String frontInputMessage = "";
    protected static String backInputMessage = "";
    protected static String scanInputMessage = "";
    protected static String distance = "";

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothLaserService(Context context, Handler handler) {
        super(context, handler);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param cmdInfo The bytes to write
     * @see ConnectedThread#write(Bundle)
     */
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

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        Message msg2 = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle2 = new Bundle();
        bundle2.putString(Constants.TOAST, "Laser 연결됨");
        msg2.setData(bundle2);
        mHandler.sendMessage(msg2);

        // Set device name
        deviceName = device.getName();

    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    protected class ConnectedChatThread extends Thread implements ConnectedThread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private boolean requestLaser = false;
        private boolean requestScan = false;
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
                    if (requestLaser || requestScan) {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        toMessageStack(new String(buffer, 0, bytes));

                        if (!msgCheck().equals("")) {
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.DEVICE_NAME, deviceName);
                            bundle.putString("MESSAGE1", inputeMessage);
                            //bundle.putString("MESSAGE2", backInputMessage);
                            //bundle.putString("MESSAGE3", scanInputMessage);

                            // 명령마다 구분
                            bundle.putString("CATEGORY", "Laser");
                            bundle.putInt("sensing_id", sensingId);

                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ, inputeMessage.length(), -1, bundle)
                                    .sendToTarget();
                            requestLaser = false;
                            requestScan = false;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothLaserService.this.start();
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
        /*protected String messageCheck() {
            char msgFirst = messageStack.removeFirst();
            char msgSecond = messageStack.removeFirst();
            String msgMode = "";
            String msgBody = "";

            if (msgFirst == 'A' && msgSecond == 'A') {
                for (int i=0; i<8; i++) {
                    if (i<2) {
                        msgMode += messageStack.removeFirst();
                    } else {
                        msgBody += messageStack.removeFirst();
                    }
                }

                return ""+msgFirst+msgSecond+msgMode+msgBody;
            } else {
                messageStack.addFirst(msgSecond);
                Log.d(TAG, "쓰레기값");
                return "";
            }
        }*/

        protected String msgCheck() {
            // 전체를 줘야한다면 inputMessage 사용
            inputeMessage = "";

            // 각각을 구분해야한다면 아래 사용
            frontInputMessage = "";
            backInputMessage = "";
            scanInputMessage = "";
            while (messageStack.size() > 0) {
                char c = messageStack.removeFirst();

                switch (c) {
                    case '{':
                    case '(':
                    case '[':
                        distance = "";
                        break;
                    case '}':
                        distance = distance.trim();
                        while (distance.length() < 6)
                            distance = "0"+distance;
                        frontInputMessage += "AA01" + distance;
                        break;
                    case ')':
                        while (distance.length() < 6)
                            distance = "0"+distance;
                        backInputMessage += "AA02" + distance;
                        break;
                    case ']':
                        while (distance.length() < 6)
                            distance = "0"+distance;
                        scanInputMessage += "AA03" + distance;
                        break;
                    default:
                        distance += c;
                        break;
                }
            }

            return inputeMessage = frontInputMessage + backInputMessage + scanInputMessage;
        }

        /**
         * Write to the connected OutStream.
         *
         * @param cmdInfo The bytes to write
         */
        public void write(Bundle cmdInfo) {
            switch (cmdInfo.getInt("out")) {
                case REQUEST_LASER_SENSOR_DATA:
                    sensingId = cmdInfo.getInt("sensing_id");
                    try {
                        byte[] buffer = LaserCommand.getDistance();
                        mmOutStream.write(buffer);

                        // Share the sent message back to the UI Activity
                        mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }
                    requestLaser = true;
                    break;
                case REQUEST_SCAN_LEFT_SENSOR_DATA:
                    try {
                        byte[] buffer = LaserCommand.getLeftScan();
                        mmOutStream.write(buffer);

                        // Share the sent message back to the UI Activity
                        mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }
                    requestScan = true;
                    break;

                case REQUEST_SCAN_RIGHT_SENSOR_DATA:
                    try {
                        byte[] buffer = LaserCommand.getRightScan();
                        mmOutStream.write(buffer);

                        // Share the sent message back to the UI Activity
                        mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }
                    requestScan = true;
                    break;
                case REQUEST_SCAN_STOP:
                    try {
                        byte[] buffer = LaserCommand.stopScan();
                        mmOutStream.write(buffer);

                        // Share the sent message back to the UI Activity
                        mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                                .sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }
                    requestScan = true;
                    break;

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
