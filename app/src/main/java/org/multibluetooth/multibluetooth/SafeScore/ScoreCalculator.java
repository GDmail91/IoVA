package org.multibluetooth.multibluetooth.SafeScore;

import android.content.Context;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.Model.DriveInfo;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

import java.util.LinkedList;

/**
 * Created by YS on 2016-09-19.
 */
    public class ScoreCalculator {
    private static final String TAG = "ScoreCalculator";

    private static int drive_id;
    private boolean FAST_FLAG = false;
    private boolean SLOW_FLAG = false;
    private boolean START_FLAG = false;
    private boolean STOP_FLAG = false;
    private static int FAST_COUNT = 0;
    private static int SLOW_COUNT = 0;
    private static int START_COUNT = 0;
    private static int STOP_COUNT = 0;
    private static int SPEEDING_COUNT = 0;

    private static int queueLength = 0;
    private static LinkedList<DriveInfo> mQueue = new LinkedList<>();
    private SafeScore safeScore;
    private SafeScoreModel safeScoreModel;

    private Context mContext;

    public ScoreCalculator(Context mContext, int drive_id) {
        this.drive_id = drive_id;
        this.mContext = mContext;
        init();
    }

    public void init() {
        FAST_COUNT = 0;
        SLOW_COUNT = 0;
        START_COUNT = 0;
        STOP_COUNT = 0;
        SPEEDING_COUNT = 0;

        FAST_FLAG = false;
        SLOW_FLAG = false;
        START_FLAG = false;
        STOP_FLAG = false;

        queueLength = 0;
        mQueue.clear();
        // TODO 반드시 close하고 끝내도록 짤것
        safeScoreModel = new SafeScoreModel(mContext, "DriveInfo.db", null);
        safeScoreModel.insert(new SafeScore(drive_id,0,0,0,0,0,"",""));
    }

    // 데이터 RECIVE 프로세스
    public void putOBDData(DriveInfo driveInfo) {
        Log.d(TAG, driveInfo.toString());
        mQueue.add(driveInfo);

        // 큐가 5개 이상일시 부터 운행안전정보 기록
        if(mQueue.size() >= 5) {
            // 안전점수 계산
            safeScore = doCalculateScore(mQueue);
            // 점수 DB 삽입
            safeScoreModel.update(safeScore);

            // dequeue
            mQueue.removeFirst();
        }
    }

    public SafeScore doCalculateScore(LinkedList<DriveInfo> mQueue) {
        queueLength = mQueue.size() - 1;
        int safeDistanceCount = getSafeDistance(mQueue);
        int speedingCount = getSpeedingCount(mQueue);
        int fastAccCount = getFastAccCount(mQueue);
        int fastBreakCount = getFastBreakCount(mQueue);
        int suddenStartCount = getSuddenStartCount(mQueue);
        int suddenStopCount = getSuddenStopCount(mQueue);

        return new SafeScore(drive_id, speedingCount, fastAccCount, fastBreakCount, suddenStartCount, suddenStopCount,"","");
    }

    /**
     * Safe Distance calculator
     * @param driveData
     * @return safe distance
     */
    public int getSafeDistance(LinkedList<DriveInfo> driveData) {
        int speed = driveData.get(queueLength).getFrontDistance();

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
    public int getFastAccCount(LinkedList<DriveInfo> driveData) {
        // 3초 이내 급가속 건수
        if ((driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-1).getVehicleSpeed()) > 11
        || (driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-2).getVehicleSpeed()) > 11
        || (driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-3).getVehicleSpeed()) > 11) {
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
    public int getFastBreakCount(LinkedList<DriveInfo> driveData) {
        // 1초 이내 급감속 건수
        if ((driveData.get(queueLength-1).getVehicleSpeed() - driveData.get(queueLength).getVehicleSpeed()) > 7) {
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
    public int getSuddenStartCount(LinkedList<DriveInfo> driveData) {

        // 정지에서 초당 11km/h 이상 가속한경우
        if (driveData.get(queueLength-1).getVehicleSpeed() == 0
        && driveData.get(queueLength).getVehicleSpeed() > 11) {
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
    public int getSuddenStopCount(LinkedList<DriveInfo> driveData) {

        // 초당 7km/h 이상 감속하여 속도가 0이 된경우
        if ((driveData.get(queueLength-1).getVehicleSpeed() - driveData.get(queueLength).getVehicleSpeed()) > 7
        && driveData.get(queueLength).getVehicleSpeed() == 0) {
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

    public int getSpeedingCount(LinkedList<DriveInfo> driveData) {

        // 속도가 120 이상으로 달린 초
        if (driveData.get(queueLength).getVehicleSpeed() > 120) {
            SPEEDING_COUNT++;
        }

        return SPEEDING_COUNT;
    }

    public void clearQueue() {
        mQueue.clear();
    }
}
