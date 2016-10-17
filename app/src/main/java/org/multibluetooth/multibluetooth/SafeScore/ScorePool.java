package org.multibluetooth.multibluetooth.SafeScore;

import android.content.Context;

import org.multibluetooth.multibluetooth.Driving.Model.DriveInfo;

import java.util.LinkedList;

/**
 * Created by YS on 2016-10-14.
 */
public class ScorePool extends LinkedList<DriveInfo> {

    private static ScorePool scorePool = new ScorePool();   // 삽입할 Pool
    private static LinkedList<Integer> searchCount = new LinkedList<>();    // 조회수 카운터

    private ScorePool() {

    }

    public static ScorePool getInstance() {
        return scorePool;
    }

    public void init() {
        scorePool.clear();
    }

    /**
     * Pool 속에 새 DriveInfo 삽입
     * @param driveInfo 삽입할 DriveInfo
     * @return 현재 Pool 크기
     */
    public int intoPool(DriveInfo driveInfo) {
        scorePool.add(driveInfo);
        searchCount.add(0);

        return scorePool.size();
    }

    /**
     * Pool 에서 같은 Drive ID가 있는지 검색
     * @param sensingId 검색할 Sensing ID
     * @return 검색된 결과가 있으면 0 이상, 없으면 -1
     */
    public int searchPool(int sensingId, Context mContext) {
        for (int i=0; i<scorePool.size(); i++) {
            if (scorePool.get(i).getId() == sensingId) {
                searchCount.set(i, searchCount.get(i) + 1);
                return i;
            }
            if (searchCount.get(i) > 3) {
                // TODO 삭제시 중간값 계산 알고리즘 삽입
                removeDriveInfo(i, mContext);
            }
        }

        return -1;
    }

    private void removeDriveInfo(int i, Context mContext) {
        searchCount.remove(i);
        DriveInfo driveInfo = scorePool.remove(i);

        // TODO 유실값에 대한 판단
        // TODO 어떤 알고리즘 적용? 칼빈?
        //DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, "DriveInfo.db", null);
        //driveInfoModel.getData(driveInfo.getId()-1);
        //driveInfoModel.getData(driveInfo.getId()-2);
    }

    /**
     * Pool 에서 DriveInfo 꺼냄
     * @param index 꺼내올 Pool 의 Index
     * @return 꺼내온 DriveInfo
     */
    public DriveInfo getDriveInfo(int index) {
        searchCount.remove(index);
        return scorePool.remove(index);
    }
}
