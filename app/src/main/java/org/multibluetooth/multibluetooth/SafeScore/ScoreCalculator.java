package org.multibluetooth.multibluetooth.SafeScore;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Driving.GpsInfo;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfo;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.Driving.ServerConnection.ZoneNameFinder;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;
import org.multibluetooth.multibluetooth.retrofit.RetrofitService;
import org.multibluetooth.multibluetooth.retrofit.format.DTOdangerLocation;

import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

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
    private boolean SIDE_GOOD_ALERT_WAIT = false;
    private boolean SIDE_WARN_ALERT_WAIT = false;
    private boolean SIDE_DANGER_ALERT_WAIT = false;
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
    private static int alertCount = 0;
    private static final int maximumCount = 5;

    private static LinkedList<DriveInfo> mQueue = new LinkedList<>();
    private static LinkedList<DriveInfo> laserQueue = new LinkedList<>();

    private SafeScore safeScore;
    private SafeScore safeDistance;
    private ScorePool scorePool = ScorePool.getInstance();

    private Context mContext;
    // GpsInfo 객체를 얻어온다

    public ScoreCalculator(Context mContext, int drive_id) {
        this.mContext = mContext;
        init(drive_id);
    }

    public void init(int drive_id) {
        ScoreCalculator.drive_id = drive_id;
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
        alertCount = 0;

        queueLength = 0;
        mQueue.clear();
        laserQueue.clear();
        scorePool.init();

        // TODO 반드시 close하고 끝내도록 짤것
        SafeScoreModel safeScoreModel = new SafeScoreModel(mContext, SafeScoreModel.DB_NAME, null);
        safeScoreModel.insert(new SafeScore(drive_id,0,0,0,0,0,"",""));
        safeScoreModel.close();
    }

    public void setChangeContext(Context context) {
        this.mContext = context;
    }

    // 데이터 RECIVE 프로세스
    public void putData(int device, DriveInfo driveInfo) {

        SafeScoreModel safeScoreModel = new SafeScoreModel(mContext, SafeScoreModel.DB_NAME, null);
        switch (device) {
            case OBD_DATA:
                mQueue.add(driveInfo);

                // 큐가 5개 이상일시 부터 운행안전정보 기록
                if (mQueue.size() >= 5) {
                    // 안전점수 계산
                    Log.d(TAG, "큐 길이: " + mQueue.size());
                    Log.d(TAG, mQueue.get(0).toString());
                    Log.d(TAG, mQueue.get(1).toString());
                    Log.d(TAG, mQueue.get(2).toString());
                    Log.d(TAG, mQueue.get(3).toString());
                    Log.d(TAG, mQueue.get(mQueue.size()-1).toString());
                    safeScore = doCalculateScore(mQueue);
                    Log.d(TAG, "계산된 점수");
                    Log.d(TAG, safeScore.toString());
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
                    // safe distance 측정
                    laserQueue.add(driveInfo);
                    // 안전거리 계산
                    safeDistance = doCalculateDistance(driveInfo);
                    // 점수 DB에 안전거리 삽입
                    safeScoreModel.updateDistance(safeDistance);
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
                    // safe distance 측정
                    laserQueue.add(driveInfo);
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

        safeScoreModel.close();

        if (driveInfo.getSideDistance() > 0) {
            Log.d("SIDE CAL", driveInfo.toString());
            getSideSafeDistance();
        }
    }

    public SafeScore doCalculateScore(LinkedList<DriveInfo> mQueue) {
        Log.d(TAG, "안전점수 계산 시작");
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
        float backRelSpeed = 0;
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
        backRelSpeed = getRelSpeed(avgBackDistance, avgBackDistance, driveInfo.getVehicleSpeed());
        //driveInfoModel.close();
/*
        avgSpeed = avgSpeed / 3;
        avgDistance = avgDistance / 3;*/
        Log.d(TAG, "평균 속도: "+avgSpeed);
        Log.d(TAG, "평균 앞 거리: "+avgFrontDistance);
        Log.d(TAG, "후방 차량 속도: "+avgSpeed);
        Log.d(TAG, "평균 뒤 거리: "+avgFrontDistance);

        // TODO 날씨정보 포함시킬것
        // Alert이 한번 울렸을경우 잠깐의 (약 1분)시간을 줌
        // 안전거리 계산
        // 도로교통공단 기준 80 km/h 미만일 경우 현재속도 - 15
        // 80 km/h 이상 또는 고속도로일 경우 현재속도로 한다.
        // 여기에 날씨정보를 포함하여
        // 비가올경우 x1.5
        // 눈이올경우 x3 을한다.

        // 전방 거리계산
        short frontDistance;
        if (avgSpeed < 80) {
            // 시속 80키로 이내에서 거리가 가까울 경우
            frontDistance = isDistanceClose(avgFrontDistance, avgSpeed - 15);
        } else {
            // 시속 80키로 이상에서 거리가 가까울 경우
            frontDistance = isDistanceClose(avgFrontDistance, avgSpeed);
        }

        switch (frontDistance) {
            case 0:
                ((DrivingActivity) mContext).setForwardBackground(DrivingActivity.DISTANCE_NORMAL);
                // 거리가 멀어졌는데
                if (DISTANCE_ALERT_WAIT) {
                    // 알람 대기상태인 경우
                    if (System.currentTimeMillis() - distanceAlertTime >= 5000) {
                        // 30초가 지낫으면 다시울리도록
                        DISTANCE_ALERT_WAIT = false;
                        alertCount = 0;
                    }
                }
                break;
            case 1:
                // 조금 가까운 경우
                onDistanceAlertBySpeak(DrivingActivity.DISTANCE_WARNING, driveInfo.getId(), driveInfo.getDriveId());
                ((DrivingActivity) mContext).setForwardBackground(DrivingActivity.DISTANCE_WARNING);
                ++CLOSE_COUNT;
                break;
            case 2:
                // 너무 가까운경우
                onDistanceAlertBySpeak(DrivingActivity.DISTANCE_DANGER, driveInfo.getId(), driveInfo.getDriveId());
                ((DrivingActivity) mContext).setForwardBackground(DrivingActivity.DISTANCE_DANGER);
                ++CLOSE_COUNT;
                break;
        }


        // 후방 거리 계산 추가
        short backDistance;
        if (backRelSpeed < 80) {
            // 시속 80키로 이내에서 거리가 가까울 경우
            backDistance = isDistanceClose(avgBackDistance, (int)backRelSpeed - 15);
        } else {
            // 시속 80키로 이상에서 거리가 가까울 경우
            backDistance = isDistanceClose(avgBackDistance, (int)backRelSpeed);
        }
        switch (backDistance) {
            case 0:
                ((DrivingActivity) mContext).setBackwardBackground(DrivingActivity.DISTANCE_NORMAL);
                break;
            case 1:
                ((DrivingActivity) mContext).setBackwardBackground(DrivingActivity.DISTANCE_WARNING);
                break;
            case 2:
                ((DrivingActivity) mContext).setBackwardBackground(DrivingActivity.DISTANCE_DANGER);
                break;

        }
        return CLOSE_COUNT;
    }

    /**
     * 경고음 발생 함수
     * @param curDistance : 현재 거리간격
     * @param safeDistance : 안전거리
     * @return 위험 상황이였는지 반환
     */
    public short isDistanceClose(float curDistance, int safeDistance) {
        short isClose;

        // 분모가 0이 되지 않기 위해
        if (safeDistance <= 0) safeDistance = 1;

        // 현재 안전 % 값
        double curSafePercent = curDistance/safeDistance;

        if (curSafePercent <= 0.5) {
            // 너무 가까운경우
            isClose = 2;
        } else if (curSafePercent <= 1) {
            // 조금 가까운 경우
            isClose = 1;
        } else {
            // 거리가 멀어진 경우
            isClose = 0;
        }

        return isClose;
    }

    // 안전거리 알람
    public void onDistanceAlertBySpeak(int type, int requestId, int driveId) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        DriveInfo driveInfo = driveInfoModel.getData(requestId, driveId);
        driveInfoModel.close();
        Location location = new Location("gps");
        location.setLatitude(driveInfo.getGpsLatitude());
        location.setLongitude(driveInfo.getGpsLongitude());
        sendDangerBehavior(ZoneNameFinder.getKoZNF(location), location, type);
        if (!DISTANCE_ALERT_WAIT) {
            // 알람을 안울렸으면 경보음 발생
            ((DrivingActivity) mContext).onAlert(type);
            DISTANCE_ALERT_WAIT = true;
            distanceAlertTime = System.currentTimeMillis();     // 시간 기록
        } else if (System.currentTimeMillis() - distanceAlertTime >= 5000) {
            // 알람을 울렸을 경우 5초 이후 초기화 (가까우면 다시 울리도록)
            DISTANCE_ALERT_WAIT = false;
        }
        // 시간 조정전
        /*if (!DISTANCE_ALERT_WAIT) {
            // 알람을 안울렸으면 경보음 발생
            ((DrivingActivity) mContext).onAlert(type);
            DISTANCE_ALERT_WAIT = true;
            distanceAlertTime = System.currentTimeMillis();     // 시간 기록
        } else if (alertCount < maximumCount
            && System.currentTimeMillis() - distanceAlertTime >= 5000) {
                // 알람을 울렸을 경우 5초 이후 초기화 (가까우면 다시 울리도록)
                DISTANCE_ALERT_WAIT = false;
                alertCount++;
        } else if (System.currentTimeMillis() - distanceAlertTime >= 30000) {
            DISTANCE_ALERT_WAIT = false;
            alertCount = 0;
        }*/
    }

    // 안전점수 알람
    public void onSafeDriveAlertBySpeak(int type) {
        GpsInfo gpsInfo = ((DrivingActivity) mContext).gpsInfo;
        sendDangerBehavior(ZoneNameFinder.getKoZNF(gpsInfo.getLocation()), gpsInfo.getLocation(), type);
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
        Log.d(TAG, "급가속: " + (driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-1).getVehicleSpeed())
        + "/" + (driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-2).getVehicleSpeed())
        + "/" + (driveData.get(queueLength).getVehicleSpeed() - driveData.get(queueLength-3).getVehicleSpeed()));
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
        Log.d(TAG, "급감속: " + (driveData.get(queueLength-1).getVehicleSpeed() - driveData.get(queueLength).getVehicleSpeed()));
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
        Log.d(TAG, "급출발: " + driveData.get(queueLength-1).getVehicleSpeed() + "/" + driveData.get(queueLength).getVehicleSpeed());
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
        Log.d(TAG, "급정거: " + (driveData.get(queueLength-1).getVehicleSpeed() - driveData.get(queueLength).getVehicleSpeed()));
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
        Log.d(TAG, "과속: " + driveData.get(queueLength).getVehicleSpeed());
        // 속도가 120 이상으로 달린 초
        if (driveData.get(queueLength).getVehicleSpeed() > 120) {
            SPEEDING_COUNT++;
            onSafeDriveAlertBySpeak(DrivingActivity.SAFE_SPEED_WARNING);
        }

        return SPEEDING_COUNT;
    }

    // 상대방 속도 구하는 공식 (기본 시간단위는 1초 이므로 굳이 계산하지 않음)
    public float getRelSpeed(float startDistance, float endDistance, int avgSpeed) {
        return avgSpeed + ((startDistance - endDistance) * 3600 / 1000);
    }

    public void getSideSafeDistance() {
        if (laserQueue.size() > 2) {
            // 상대속도 구하는 공식
            int avgSpeed = (laserQueue.get(0).getVehicleSpeed() + laserQueue.get(1).getVehicleSpeed())/2;
            float relSpeed = getRelSpeed(laserQueue.get(0).getSideDistance(), laserQueue.get(1).getSideDistance(), avgSpeed);

            // TODO 상대속도로 비교 (뒷차량의 상대속도가 +몇 인지 파악)
            // 뒷차량의 속도가 나보다 15km 이상 빠르고 거리가 30m 이하로 가까운경우
            if (relSpeed - avgSpeed > 15 && laserQueue.get(1).getSideDistance() <= 30) {
                if (!SIDE_DANGER_ALERT_WAIT) {
                    ((DrivingActivity) mContext).onAlert(DrivingActivity.SIDE_DISTANCE_DANGER);
                    SIDE_GOOD_ALERT_WAIT = false;
                    SIDE_WARN_ALERT_WAIT = false;
                    SIDE_DANGER_ALERT_WAIT = true;
                }
            } else if (relSpeed - avgSpeed > 15 || laserQueue.get(1).getSideDistance() <= 30) {
                if (!SIDE_WARN_ALERT_WAIT) {
                    ((DrivingActivity) mContext).onAlert(DrivingActivity.SIDE_DISTANCE_WARNING);
                    SIDE_GOOD_ALERT_WAIT = false;
                    SIDE_WARN_ALERT_WAIT = true;
                    SIDE_DANGER_ALERT_WAIT = false;
                }
            } else if (!SIDE_GOOD_ALERT_WAIT) {
                ((DrivingActivity) mContext).onAlert(DrivingActivity.SIDE_DISTANCE_GOOD);
                SIDE_GOOD_ALERT_WAIT = true;
                SIDE_WARN_ALERT_WAIT = false;
                SIDE_DANGER_ALERT_WAIT = false;
            }

            laserQueue.removeFirst();
        }
    }

    public void clearQueue() {
        mQueue.clear();
    }


    private void sendDangerBehavior(String zoneName, Location location, int type) {
        if (type == DrivingActivity.DISTANCE_WARNING) return;

        String behavior = "";
        switch (type) {
            case DrivingActivity.DISTANCE_DANGER:
                behavior = "distance_danger";
                break;
            case DrivingActivity.SAFE_SPEED_WARNING:
                behavior = "speeding";
                break;
            case DrivingActivity.SUDDEN_FAST_WARNING:
                behavior = "sudden_fast";
                break;
            case DrivingActivity.SUDDEN_SLOW_WARNING:
                behavior = "sudden_slow";
                break;
            case DrivingActivity.SUDDEN_START_WARNING:
                behavior = "sudden_start";
                break;
            case DrivingActivity.SUDDEN_STOP_WARNING:
                behavior = "sudden_stop";
                break;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mContext.getResources().getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Call<DTOdangerLocation> call = service.postDangerLocation(
                pref.getString("access_token", ""),
                zoneName,
                location.getLatitude(),
                location.getLongitude(),
                behavior);
        call.enqueue(new Callback<DTOdangerLocation>() {
            @Override
            public void onResponse(Response<DTOdangerLocation> response) {
                if (response.isSuccess() && response.body() != null) {
                    Toast.makeText(mContext.getApplicationContext(), "위험행동 저장", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext.getApplicationContext(), "서버 연결 실패", Toast.LENGTH_LONG).show();
            }
        });
    }
}
