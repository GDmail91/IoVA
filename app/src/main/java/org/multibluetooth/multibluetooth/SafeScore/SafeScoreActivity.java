package org.multibluetooth.multibluetooth.SafeScore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

import java.util.ArrayList;

/**
 * Created by YS on 2016-09-19.
 */
public class SafeScoreActivity extends AppCompatActivity {
    private static final String TAG = "SafeScoreActivity";

    // VIEW
    private TextView avgScoreView;
    private TextView safeDistanceScore;
    private TextView speedingScore;
    private TextView fastAccScore;
    private TextView fastBreakScore;
    private TextView suddenStartScore;
    private TextView suddenStopScore;

    private SafeScore avgSafeScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safe_score_activity);

        // 각 텍스트뷰 별로 바인딩
        avgScoreView = (TextView) findViewById(R.id.avg_score);
        safeDistanceScore = (TextView) findViewById(R.id.safe_distance_score);
        speedingScore = (TextView) findViewById(R.id.speeding_score);
        fastAccScore = (TextView) findViewById(R.id.fast_acc_score);
        fastBreakScore = (TextView) findViewById(R.id.fast_break_score);
        suddenStartScore = (TextView) findViewById(R.id.sudden_start_score);
        suddenStopScore = (TextView) findViewById(R.id.sudden_stop_score);

        // 안전점수 가져와서 뿌려줌
        SafeScore safeScore = getScoreData();
        safeDistanceScore.setText(""+safeScore.getSafeDistanceCount());
        speedingScore.setText(""+safeScore.getSpeedingCount());
        fastAccScore.setText(""+safeScore.getFastAccCount());
        fastBreakScore.setText(""+safeScore.getFastBreakCount());
        suddenStartScore.setText(""+safeScore.getSuddenStartCount());
        suddenStopScore.setText(""+safeScore.getSuddenStopCount());

        // 전체 지표의 평균 계산
        int avgScore = safeScore.getSafeDistanceCount() +
                safeScore.getSpeedingCount() +
                safeScore.getFastAccCount() +
                safeScore.getFastBreakCount() +
                safeScore.getSuddenStartCount() +
                safeScore.getSuddenStopCount();
        avgScore /= 6;
        avgScoreView.setText(""+avgScore);
    }

    private SafeScore getScoreData() {
        SafeScoreModel safeScoreModel = new SafeScoreModel(this, "DriveInfo.db", null);
        ArrayList<SafeScore> allData = safeScoreModel.getAllData();
        Log.d(TAG, allData.toString());
        int avgDistance = 0;
        int avgSpeeding = 0;
        int avgFastAcc = 0;
        int avgFastBreak = 0;
        int avgSuddenStart = 0;
        int avgSuddenStop = 0;

        int size = allData.size();
        if (size != 0) {
            for (SafeScore safeScore : allData) {
                avgDistance += safeScore.getSafeDistanceCount();
                avgSpeeding += safeScore.getSpeedingCount();
                avgFastAcc += safeScore.getFastAccCount();
                avgFastBreak += safeScore.getFastBreakCount();
                avgSuddenStart += safeScore.getSuddenStartCount();
                avgSuddenStop += safeScore.getSuddenStopCount();
            }

            // TODO 각 지표들의 평균 점수 계산? 혹은 합산 점수 계산 해야함
            avgDistance = avgDistance / size;
            avgSpeeding = avgSpeeding / size;
            avgFastAcc = avgFastAcc / size;
            avgFastBreak = avgFastBreak / size;
            avgSuddenStart = avgSuddenStart / size;
            avgSuddenStop = avgSuddenStop / size;
        }
        return new SafeScore(0, avgDistance, avgSpeeding, avgFastAcc, avgFastBreak, avgSuddenStart, avgSuddenStop);
    }
}
