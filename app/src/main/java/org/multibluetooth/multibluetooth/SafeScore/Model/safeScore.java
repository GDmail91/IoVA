package org.multibluetooth.multibluetooth.SafeScore.Model;

import android.content.Context;

import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;

import java.io.Serializable;

/**
 * Created by YS on 2016-09-28.
 */
public class SafeScore implements Serializable {
    private int drive_id;
    private int safe_distance_count;
    private int speeding_count;
    private int fast_acc_count;
    private int fast_break_count;
    private int sudden_start_count;
    private int sudden_stop_count;
    private String drive_start;
    private String drive_stop;

    private static final int StandardDistanceCount = 1000;
    private static final int StandardSpeedingCount = 300;
    private static final int StandardFastAccCount = 300;
    private static final int StandardFastBreakCount = 300;
    private static final int StandardSuddenStartCount = 300;
    private static final int StandardSuddenStopCount = 300;

    public SafeScore(int drive_id, int safe_distance_count, int speeding_count, int fast_acc_count, int fast_break_count, int sudden_start_count, int sudden_stop_count, String drive_start, String drive_stop) {
        this.drive_id = drive_id;
        this.safe_distance_count = safe_distance_count;
        this.speeding_count = speeding_count;
        this.fast_acc_count = fast_acc_count;
        this.fast_break_count = fast_break_count;
        this.sudden_start_count = sudden_start_count;
        this.sudden_stop_count = sudden_stop_count;
        this.drive_start = drive_start;
        this.drive_stop = drive_stop;
    }

    public SafeScore(int drive_id, int speeding_count, int fast_acc_count, int fast_break_count, int sudden_start_count, int sudden_stop_count, String drive_start, String drive_stop) {
        this.drive_id = drive_id;
        this.speeding_count = speeding_count;
        this.fast_acc_count = fast_acc_count;
        this.fast_break_count = fast_break_count;
        this.sudden_start_count = sudden_start_count;
        this.sudden_stop_count = sudden_stop_count;
        this.drive_start = drive_start;
        this.drive_stop = drive_stop;
    }

    public SafeScore(int drive_id, int safe_distance_count) {
        this.drive_id = drive_id;
        this.safe_distance_count = safe_distance_count;
    }

    public int getDriveId() {return drive_id;}
    public int getSafeDistanceCount() {return safe_distance_count;}
    public int getSpeedingCount() {return speeding_count;}
    public int getFastAccCount() {return fast_acc_count;}
    public int getFastBreakCount() {return fast_break_count;}
    public int getSuddenStartCount() {return sudden_start_count;}
    public int getSuddenStopCount() {return sudden_stop_count;}
    public String getDriveStart() {return drive_start;}
    public String getDriveStop() {return drive_stop;}

    public float getPercentSafeDistance(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)safe_distance_count / StandardCount) * 100;
    }

    public float getPercentSafeDistance(int StandardCount) {
        return 100 - ((float)safe_distance_count / StandardCount) * 100;
    }

    public float getPercentSpeeding(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)speeding_count / StandardCount) * 100;
    }

    public float getPercentSpeeding(int StandardCount) {
        return 100 - ((float)speeding_count / StandardCount) * 100;
    }

    public float getPercentFastAcc(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)fast_acc_count / StandardCount) * 100;
    }

    public float getPercentFastAcc(int StandardCount) {
        return 100 - ((float)fast_acc_count / StandardCount) * 100;
    }

    public float getPercentFastBreak(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)fast_break_count / StandardCount) * 100;
    }

    public float getPercentFastBreak(int StandardCount) {
        return 100 - ((float)fast_break_count / StandardCount) * 100;
    }

    public float getPercentSuddenStart(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)sudden_start_count / StandardCount) * 100;
    }

    public float getPercentSuddenStart(int StandardCount) {
        return 100 - ((float)sudden_start_count / StandardCount) * 100;
    }

    public float getPercentSuddenStop(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();
        return 100 - ((float)sudden_stop_count / StandardCount) * 100;
    }

    public float getPercentSuddenStop(int StandardCount) {
        return 100 - ((float)sudden_stop_count / StandardCount) * 100;
    }

    public int getAvgScore(Context mContext) {
        DriveInfoModel driveInfoModel = new DriveInfoModel(mContext, DriveInfoModel.DB_NAME, null);
        int StandardCount = driveInfoModel.getCountRequestNumber(this.drive_id);
        driveInfoModel.close();

        int avgScore = (int) ((getPercentSafeDistance(StandardCount) +
                getPercentSpeeding(StandardCount) +
                getPercentFastAcc(StandardCount) +
                getPercentFastBreak(StandardCount) +
                getPercentSuddenStart(StandardCount) +
                getPercentSuddenStop(StandardCount)) / 6);
        return avgScore;
    }

    public String toString() {
        return "{\"drive_id\": \""+drive_id+"\", " +
                "\"safe_distance_count\": \""+safe_distance_count+"\", \"speeding_count\": \""+speeding_count+"\", " +
                "\"fast_acc_count\": \""+fast_acc_count + "\", \"fast_break_count\": \"" + fast_break_count +"\", " +
                "\"sudden_start_count\": \""+sudden_start_count+"\", \"sudden_stop_count\": \""+sudden_stop_count+"\", " +
                "\"drive_start\": \""+drive_start+"\", \"drive_stop\": \""+drive_stop+"\"}";
    }

    /*public String toString() {
        return "{drive_id: "+drive_id + ", distance_count: "+ safe_distance_count + ", speed_count: "+speeding_count+", FAC: "+fast_acc_count+", FBC: "+fast_break_count+", STC: "+sudden_start_count+", SPC: "+sudden_stop_count+", TIME: "+drive_start+"/"+drive_stop;
    }*/
}
