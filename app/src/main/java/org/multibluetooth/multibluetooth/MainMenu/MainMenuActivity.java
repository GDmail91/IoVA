package org.multibluetooth.multibluetooth.MainMenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Facebook.FacebookLogin;
import org.multibluetooth.multibluetooth.R;

public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	// VIEW
	private Button driving_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_activity);

		faccebookLoginCheck();
		// VIEW 연결
		driving_btn = (Button) findViewById(R.id.driving_btn);


	}


	public void onMenuClick(View v) {
		Log.d(TAG, v.toString());

		switch (v.getId()) {
			case R.id.driving_btn:
				Intent drivingIntent = new Intent(MainMenuActivity.this, DrivingActivity.class);
				startActivity(drivingIntent);
				break;
			case R.id.fb_logout:
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("access_token", "");
				editor.apply();

				FacebookSdk.sdkInitialize(getApplicationContext());
				LoginManager.getInstance().logOut();

				Intent logoutIntent = new Intent(MainMenuActivity.this, FacebookLogin.class);
				logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(logoutIntent);
				break;
		}
	}

	public void faccebookLoginCheck() {
		// Facebook login check
		FacebookSdk.sdkInitialize(getApplicationContext());
		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
		if (pref.getString("access_token", "").equals("")) {
			Intent intent = new Intent(MainMenuActivity.this, FacebookLogin.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
