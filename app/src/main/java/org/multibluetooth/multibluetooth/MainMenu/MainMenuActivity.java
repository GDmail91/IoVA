package org.multibluetooth.multibluetooth.MainMenu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.R;

public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	// VIEW
	private Button driving_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_activity);

		// VIEW 연결
		driving_btn = (Button) findViewById(R.id.driving_btn);

	}


	public void onMenuClick(View v) {
		Log.d(TAG, v.toString());

		switch (v.getId()) {
			case R.id.driving_btn:
				Intent intent = new Intent(MainMenuActivity.this, DrivingActivity.class);
				startActivity(intent);
				break;
		}
	}
}
