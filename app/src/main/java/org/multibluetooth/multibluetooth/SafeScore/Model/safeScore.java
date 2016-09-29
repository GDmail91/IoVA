package org.multibluetooth.multibluetooth.SafeScore.Model;

/**
 * Created by YS on 2016-09-28.
 */
public class SafeScore {
    private int drive_id;
    private int safe_distance_count;
    private int speeding_count;
    private int fast_acc_count;
    private int fast_break_count;
    private int sudden_start_count;
    private int sudden_stop_count;

    public SafeScore(int drive_id, int safe_distance_count, int speeding_count, int fast_acc_count, int fast_break_count, int sudden_start_count, int sudden_stop_count) {
        this.drive_id = drive_id;
        this.safe_distance_count = safe_distance_count;
        this.speeding_count = speeding_count;
        this.fast_acc_count = fast_acc_count;
        this.fast_break_count = fast_break_count;
        this.sudden_start_count = sudden_start_count;
        this.sudden_stop_count = sudden_stop_count;
    }

    public SafeScore(int drive_id, int speeding_count, int fast_acc_count, int fast_break_count, int sudden_start_count, int sudden_stop_count) {
        this.drive_id = drive_id;
        this.speeding_count = speeding_count;
        this.fast_acc_count = fast_acc_count;
        this.fast_break_count = fast_break_count;
        this.sudden_start_count = sudden_start_count;
        this.sudden_stop_count = sudden_stop_count;
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

    public String toString() {
        return "drive_id: "+drive_id + ", distance_count: "+ safe_distance_count + ", speed_count: "+speeding_count+", FAC: "+fast_acc_count+", FBC: "+fast_break_count+", STC: "+sudden_start_count+", SPC: "+sudden_stop_count;
    }
}
