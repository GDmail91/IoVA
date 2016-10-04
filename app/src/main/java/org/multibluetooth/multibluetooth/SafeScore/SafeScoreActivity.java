package org.multibluetooth.multibluetooth.SafeScore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

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

        Bundle bundle = getIntent().getExtras();
        SafeScore safeScore;
        if (bundle == null) {

            // 안전점수 가져와서 뿌려줌
            SafeScoreModel safeScoreModel = new SafeScoreModel(this, "DriveInfo.db", null);
            safeScore = safeScoreModel.getScoreData();
        } else {
            safeScore = (SafeScore) bundle.getSerializable("drive_item");
        }
        Log.d(TAG, "Drive ID: "+safeScore.getDriveId());

        safeDistanceScore.setText("" + safeScore.getSafeDistanceCount());
        speedingScore.setText("" + safeScore.getSpeedingCount());
        fastAccScore.setText("" + safeScore.getFastAccCount());
        fastBreakScore.setText("" + safeScore.getFastBreakCount());
        suddenStartScore.setText("" + safeScore.getSuddenStartCount());
        suddenStopScore.setText("" + safeScore.getSuddenStopCount());

        // 전체 지표의 평균 계산
        avgScoreView.setText("" + safeScore.getAvgScore());
    }
}
