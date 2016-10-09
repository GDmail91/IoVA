package org.multibluetooth.multibluetooth.Driving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import org.multibluetooth.multibluetooth.R;

/**
 * Created by YS on 2016-09-23.
 */
public class DriveStartDialog extends Activity {
    private static final String TAG = "DriveStartDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.driving_start_dialog);

    }

    public void onStart(View v) {
        Intent laserIntent = new Intent();
        setResult(DrivingActivity.DRIVE_START_FLAG, laserIntent);
        finish();
    }
}
