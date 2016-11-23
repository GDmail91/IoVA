package org.multibluetooth.multibluetooth.SafeScore;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;

import java.util.ArrayList;

/**
 * Created by YS on 2016-09-19.
 */
public class SafeScoreActivity extends ScoreBase {
    private static final String TAG = "SafeScoreActivity";

    // VIEW
    private TextView avgScoreView;

    private SafeScore avgSafeScore;

    private RadarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_radarchart_noseekbar);


        Bundle bundle = getIntent().getExtras();
//        Log.d(TAG, "bundle: "+bundle.toString());
        SafeScore safeScore;
        if (bundle == null) {

            // 안전점수 가져와서 뿌려줌
            SafeScoreModel safeScoreModel = new SafeScoreModel(this, SafeScoreModel.DB_NAME, null);
            safeScore = safeScoreModel.getScoreData();
            safeScoreModel.close();

        } else {
            safeScore = (SafeScore) bundle.getSerializable("drive_item");
        }

        Log.d(TAG, "내용 : "+safeScore.getAvgScore(this));
        avgScoreView = (TextView) findViewById(R.id.avg_score);
        avgScoreView.setTypeface(mTfLight);
        avgScoreView.setTextColor(Color.WHITE);
        avgScoreView.setBackgroundResource(R.color.metroBackground);

        // 전체 지표의 평균 계산
        avgScoreView.setText("" + safeScore.getAvgScore(this));

        LinearLayout tvg = (LinearLayout) findViewById(R.id.textViewGroup);
        tvg.setBackgroundResource(R.color.metroBackground);
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setTypeface(mTfLight);
        tv.setTextColor(Color.WHITE);

        mChart = (RadarChart) findViewById(R.id.chart1);
        mChart.setBackgroundResource(R.color.metroBackground);

        mChart.getDescription().setEnabled(false);

        mChart.setWebLineWidth(1f);
        mChart.setWebColor(Color.LTGRAY);
        mChart.setWebLineWidthInner(1f);
        mChart.setWebColorInner(Color.LTGRAY);
        mChart.setWebAlpha(100);


        setData(safeScore);

        mChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(13f);
        xAxis.setYOffset(0f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private String[] mActivities = new String[]{"안전거리 미확보", "과속", "급가속", "급감속", "급출발", "급정거"};

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mActivities[(int) value % mActivities.length];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        xAxis.setTextColor(Color.WHITE);

        YAxis yAxis = mChart.getYAxis();
        yAxis.setTypeface(mTfLight);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(80f);
        yAxis.setDrawLabels(true);
        yAxis.setDrawTopYLabelEntry(false);

        Legend l = mChart.getLegend();
        l.setDrawInside(false);
        l.setEnabled(false);

        for (IDataSet<?> set : mChart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());

        if (mChart.isRotationEnabled())
            mChart.setRotationEnabled(false);

        mChart.invalidate();

    }


    public void setData(SafeScore safeScore) {

        ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();

        entries1.add(new RadarEntry(safeScore.getPercentSafeDistance(this)));
        entries1.add(new RadarEntry(safeScore.getPercentSpeeding(this)));
        entries1.add(new RadarEntry(safeScore.getPercentFastAcc(this)));
        entries1.add(new RadarEntry(safeScore.getPercentFastBreak(this)));
        entries1.add(new RadarEntry(safeScore.getPercentSuddenStart(this)));
        entries1.add(new RadarEntry(safeScore.getPercentSuddenStop(this)));


        RadarDataSet set2 = new RadarDataSet(entries1, "평균 안전점수");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set2);

        RadarData data = new RadarData(sets);
        data.setValueTypeface(mTfLight);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);
        mChart.invalidate();
    }
}
