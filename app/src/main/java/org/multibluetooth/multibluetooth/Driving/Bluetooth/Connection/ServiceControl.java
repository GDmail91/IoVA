package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;

/**
 * Created by YS on 2016-11-12.
 */
public interface ServiceControl {
    BluetoothService mChatService = null;

    // 스타티드 서비스 실행
    public void setupService();

    // 바인드 서비스 연결
    public void bindService();
}
