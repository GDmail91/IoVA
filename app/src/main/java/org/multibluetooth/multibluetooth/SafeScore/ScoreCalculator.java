package org.multibluetooth.multibluetooth.SafeScore;

import android.content.Context;
import android.util.Log;

import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
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
    private boolean DISTANCE_ALERT_WAIT = false;
    private boolean SAFE_ALERT_WAIT = false;

    private static int CLOSE_COUNT = 0;
    private static int FAST_COUNT = 0;
    private static int SLOW_COUNT = 0;
    private static int START_COUNT = 0;
    private static int STOP_COUNT = 0;
    private static int SPEEDING_COUNT = 0;
    public static final int OBD_DATA = 1;
    public static final int LASER_DATA = 2;

    private static int queueLength = 0;
    private static long distanceAlertTime;
    private static long safeAlertTime;
    private static LinkedList<DriveInfo> mQueue = new LinkedList<>();
    //private static LinkedList<DriveInfo> laserQueue = new LinkedList<>();

    private SafeScore safeScore;
    private SafeScore safeDistance;
    private SafeScoreModel safeScoreModel;
    private ScorePool scorePool = ScorePool.getInstance();

    private Context mContext;

    public ScoreCalculator(Context mContext, int drive_id) {
        this.mContext = mContext;
        init(drive_id);
    }

    public void init(int drive_id) {
        this.drive_id = drive_id;
        FAST_COUNT = 0;
        SLOW_COUNT = 0;
        START_COUNT = 0;
        STOP_COUNT = 0;
        SPEEDING_COUNT = 0;
        CLOSE_COUNT = 0;

        FAST_FLAG = false;
        SLOW_FLAG = false;
        START_FLAG = false;
        STOP_FLAG = false;
        DISTANCE_ALERT_WAIT = false;

        queueLength = 0;
        mQueue.clear();
        scorePool.init();

        // TODO 반드시 close하고 끝내도록 짤것
        safeScoreModel = new SafeScoreModel(mContext, "DriveInfo.db", null);
        safeScoreModel.insert(new SafeScore(drive_id,0,0,0,0,0,"",""));
    }

    // 데이터 RECIVE 프로세스
    public void putData(int device, DriveInfo driveInfo) {
        switch (device) {
            case OBD_DATA:
                Log.d(TAG, driveInfo.toString());
                mQueue.add(driveInfo);

                // 큐가 5개 이상일시 부터 운행안전정보 기록
                if (mQueue.size() >= 5) {
                    // 안전점수 계산
                    safeScore = doCalculateScore(mQueue);
                    // 점수 DB 삽입
                    safeScoreModel.update(safeScore);

                    // dequeue
                    mQueue.removeFirst();

                }

                // 풀에 같은 Sensing ID값 존재하는지 확인
                int laserIndex = scorePool.searchPool(driveInfo.getId(), mContext);
                if (laserIndex >= 0) {
                    // Laser 에서 먼저 넣은게 있다면 꺼내서 측정값 표시
                    driveInfo.setLaserSensor(scorePool.getDriveInfo(laserIndex));
                    // TODO safe distance 측정
                } else {
                    // 없다면 풀에 삽입
                    scorePool.intoPool(driveInfo);
                }

                break;
            case LASER_DATA:
                // 풀에 같은 Drive ID값 존재하는지 확인
                int obdIndex = scorePool.searchPool(driveInfo.getId(), mContext);
                if (obdIndex >= 0) {
                    // OBD 에서 먼저 넣은게 있다면 꺼내서 측정값 표시
                    driveInfo.setOBDSensor(scorePool.getDriveInfo(obdIndex));
                    // TODO safe distance 측정
                    // 안전거리 계산
                    safeDistance = doCalculateDistance(driveInfo);
                    // 점수 DB에 안전거리 삽입
                    safeScoreModel.updateDistance(safeDistance);
                } else {
                    // 없다면 풀에 삽입
                    scorePool.intoPool(driveInfo);
                }

                break;
        }
    }

    public SafeScore doCalculateScore(LinkedList<DriveInfo> mQueue) {
        queueLength = mQueue.size() - 1;
        int speedingCount = getSpeedingCount(mQueue);
        int fastAccCount = getFastAccCount(mQueue);
        int fastBreakCount = getFastBreakCount(mQueue);
        int suddenStartCount = getSuddenStartCount(mQueue);
        int suddenStopCount = getSuddenStopCount(mQueue);

        return new SafeScore(drive_id, speedingCount, fastAccCount, fastBreakCount, suddenStartCount, suddenStopCount,"","");
    }

    public SafeScore doCalculateDistance(DriveInfo driveInfo) {
        int safeDistanceCount = getSafeDistance(driveInfo);

        return new SafeScore(drive_id, safeDistanceCount);
    }

    /**
     * Safe Distance calculator
     * @param driveInfo
     * @return safe distance
     */
    public int getSafeDistance(DriveInfo driveInfo) {
        //DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, "DriveInfo.db", null);
        int avgSpeed = 0;
        float avgFrontDistance = 0;
        float avgBackDistance = 0;
        /*for (int i=1; i<3; i++) {
            DriveInfo tempInfo = driveInfoModel.getData(driveInfo.getId() - i);
            avgSpeed += tempInfo.getVehicleSpeed();
            avgDistance += tempInfo.getFrontDistance();
        }*/
        //avgSpeed += driveInfoModel.getData(driveInfo.getId()).getVehicleSpeed();
        avgSpeed += driveInfo.getVehicleSpeed();
        avgFrontDistance += driveInfo.getFrontDistance();
        // TODO 후방 거리 계산 추가
        avgBackDistance += driveInfo.getBackDistance();
        //driveInfoModel.close();
/*
        avgSpeed = avgSpeed / 3;
        avgDistance = avgDistance / 3;*/
        Log.d(TAG, "평균 속도: "+avgSpeed);
        Log.d(TAG, "평균 앞 거리: "+avgFrontDistance);

        // TODO 날씨정보 포함시킬것
        // Alert이 한번 울렸을경우 잠깐의 (약 1분)시간을 줌
        // 안전거리 계산
        // 도로교통공단 기준 80 km/h 미만일 경우 현재속도 - 15
        // 80 km/h 이상 또는 고속도로일 경우 현재속도로 한다.
        // 여기에 날씨정보를 포함하여
        // 비가올경우 x1.5
        // 눈이올경우 x3 을한다.
        if (avgSpeed < 80) {
            // 시속 80키로 이내에서 거리가 가까울 경우
            if (isDistanceClose(avgFrontDistance, avgSpeed - 15)) ++CLOSE_COUNT;
            // TODO 후방 거리 계산 추가
            //if (isDistanceClose(avgBackDistance, avgSpeed - 15)) ++CLOSE_COUNT;
        } else {
            // 시속 80키로 이상에서 거리가 가까울 경우
            if (isDistanceClose(avgFrontDistance, avgSpeed)) ++CLOSE_COUNT;
            // TODO 후방 거리 계산 추가
            //if (isDistanceClose(avgBackDistance, avgSpeed)) ++CLOSE_COUNT;
        }
        return CLOSE_COUNT;
    }

    /**
     * 경고음 발생 함수
     * @param curDistance : 현재 거리간격
     * @param safeDistance : 안전거리
     * @return 위험 상황이였는지 반환
     */
    public boolean isDistanceClose(float curDistance, int safeDistance) {
        boolean isClose;

        // 분모가 0이 되지 않기 위해
        if (safeDistance <= 0) safeDistance = 1;

        // 현재 안전 % 값
        double curSafePercent = curDistance/safeDistance;
        Log.d(TAG, "안전 %값 :" +curSafePercent);

        if (curSafePercent < 0.5) {
            // 조금 가까운 경우
            onDistanceAlertBySpeak(DrivingActivity.DISTANCE_WARNING);
            ((DrivingActivity) mContext).setForwardBackgroud(DrivingActivity.DISTANCE_WARNING);
            isClose = true;
        } else if (curSafePercent <= 1) {
            // 너무 가까운경우
            onDistanceAlertBySpeak(DrivingActivity.DISTANCE_DANGER);
            ((DrivingActivity) mContext).setForwardBackgroud(DrivingActivity.DISTANCE_DANGER);
            isClose = true;
        } else {
            // 거리가 멀어졌는데
            if (DISTANCE_ALERT_WAIT) {
                // 알람 대기상태인 경우
                if (System.currentTimeMillis() - distanceAlertTime >= 30000) {
                    // 30초가 지낫으면 다시울리도록
                    DISTANCE_ALERT_WAIT = false;
                }
            }

            ((DrivingActivity) mContext).setForwardBackgroud(DrivingActivity.DISTANCE_NORMAL);
            isClose = false;
        }

        return isClose;
    }

    // 안전거리 알람
    public void onDistanceAlertBySpeak(int type) {
        if (!DISTANCE_ALERT_WAIT) {
            // 알람을 안울렸으면 경보음 발생
            ((DrivingActivity) mContext).onAlert(type);
            DISTANCE_ALERT_WAIT = true;
            distanceAlertTime = System.currentTimeMillis();     // 시간 기록
        } else if (System.currentTimeMillis() - distanceAlertTime >= 60000) {
            // 알람을 울렸을 경우 60초 이후 초기화 (가까우면 다시 울리도록)
            DISTANCE_ALERT_WAIT = false;
        }
    }

    // 안전점수 알람
    public void onSafeDriveAlertBySpeak(int type) {
        if (!SAFE_ALERT_WAIT) {
            // 알람을 안울렸으면 경보음 발생
            ((DrivingActivity) mContext).onAlert(type);
            SAFE_ALERT_WAIT = true;
            safeAlertTime = System.currentTimeMillis();     // 시간 기록
        } else if (System.currentTimeMillis() - safeAlertTime >= 30000) {
            // 알람을 울렸을 경우 30초 이후 초기화 (가까우면 다시 울리도록)
            SAFE_ALERT_WAIT = false;
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
                onSafeDriveAlertBySpeak(DrivingActivity.SUDDEN_FAST_WARNING);
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
                onSafeDriveAlertBySpeak(DrivingActivity.SUDDEN_SLOW_WARNING);
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
                onSafeDriveAlertBySpeak(DrivingActivity.SUDDEN_START_WARNING);
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
                onSafeDriveAlertBySpeak(DrivingActivity.SUDDEN_STOP_WARNING);
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
            onSafeDriveAlertBySpeak(DrivingActivity.SAFE_SPEED_WARNING);
        }

        return SPEEDING_COUNT;
    }

    public void clearQueue() {
        mQueue.clear();
    }
}
