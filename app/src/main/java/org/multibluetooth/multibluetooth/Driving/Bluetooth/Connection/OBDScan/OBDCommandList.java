package org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan;

import org.multibluetooth.multibluetooth.Obd.ObdCommand;
import org.multibluetooth.multibluetooth.Obd.SpeedCommand;

import java.util.ArrayList;

/**
 * Created by YS on 2016-09-24.
 */
public class OBDCommandList {
    // 왜 스피드밖에 필요가 없지?
    public ArrayList<ObdCommand> cmdList = new ArrayList<>();

    /**
     * return cmd list length
     */
    public OBDCommandList() {
        // TODO If need more commnad
        cmdList.add(new SpeedCommand());
    }

}
