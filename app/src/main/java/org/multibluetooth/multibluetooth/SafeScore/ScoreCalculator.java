package org.multibluetooth.multibluetooth.SafeScore;

import org.multibluetooth.multibluetooth.Driving.DriveData;

import java.util.ArrayList;

/**
 * Created by YS on 2016-09-19.
 */
public class ScoreCalculator {

    public static ScoreCalculator scoreCalculator = new ScoreCalculator();

    private boolean FAST_FLAG = false;
    private boolean SLOW_FLAG = false;
    private boolean START_FLAG = false;
    private boolean STOP_FLAG = false;
    private static int FAST_COUNT = 0;
    private static int SLOW_COUNT = 0;
    private static int START_COUNT = 0;
    private static int STOP_COUNT = 0;


    /**
     * Safe Distance calculator
     * @param speed
     * @return safe distance
     */
    public double getSafeDistance(double speed) {
        // TODO 날씨정보 포함시킬것
        // 안전거리 계산
        // 도로교통공단 기준 80 km/h 미만일 경우 현재속도 - 15
        // 80 km/h 이상 또는 고속도로일 경우 현재속도로 한다.
        // 여기에 날씨정보를 포함하여
        // 비가올경우 x1.5
        // 눈이올경우 x3 을한다.
        if (speed < 80) {
            return speed - 15;
        } else {
            return speed;
        }
    }

    // 급 가속 횟수
    public int getSuddenFastCount(ArrayList<DriveData> driveData) {

        int length = driveData.size();
        // 3초 이내 급가속 건수
        if ((driveData.get(length).getSpeed() - driveData.get(length-1).getSpeed()) > 11
        || (driveData.get(length).getSpeed() - driveData.get(length-2).getSpeed()) > 11
        || (driveData.get(length).getSpeed() - driveData.get(length-3).getSpeed()) > 11) {
            if (!FAST_FLAG) {
                FAST_FLAG = true;
                FAST_COUNT++;
            }

            return FAST_COUNT;
        } else {
            if (FAST_FLAG) {
                FAST_FLAG = false;
            }

            return FAST_COUNT;
        }
    }

    // 급 감속 횟수
    public int getSuddenSlowCount(ArrayList<DriveData> driveData) {

        int length = driveData.size();
        // 1초 이내 급감속 건수
        if ((driveData.get(length-1).getSpeed() - driveData.get(length).getSpeed()) > 7) {
            if (!SLOW_FLAG) {
                SLOW_FLAG = true;
                SLOW_COUNT++;
            }

            return SLOW_COUNT;
        } else {
            if (SLOW_FLAG) {
                SLOW_FLAG = false;
            }

            return SLOW_COUNT;
        }
    }

    // 급 출발 횟수
    public int getSuddenStartCount(ArrayList<DriveData> driveData) {

        int length = driveData.size();
        // 정지에서 초당 11km/h 이상으로 가속한경우
        if (driveData.get(length-1).getSpeed() == 0
        && driveData.get(length).getSpeed() > 11) {
            if (!START_FLAG) {
                START_FLAG = true;
                START_COUNT++;
            }

            return START_COUNT;
        } else {
            if (START_FLAG) {
                START_FLAG = false;
            }

            return START_COUNT;
        }
    }

    // 급 정거 횟수
    public int getSuddenStopCount(ArrayList<DriveData> driveData) {

        int length = driveData.size();
        // 초당 7km/h 이상에서 속도가 0이 된경우
        if ((driveData.get(length-1).getSpeed() - driveData.get(length).getSpeed()) > 7
        && driveData.get(length).getSpeed() == 0) {
            if (!STOP_FLAG) {
                STOP_FLAG = true;
                STOP_COUNT++;
            }

            return STOP_COUNT;
        } else {
            if (STOP_FLAG) {
                STOP_FLAG = false;
            }

            return STOP_COUNT;
        }
    }
}
