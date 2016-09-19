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
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;

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
			case R.id.driving_btn:	// 운전중 센서 스캔
				Intent drivingIntent = new Intent(MainMenuActivity.this, DrivingActivity.class);
				startActivity(drivingIntent);
				break;

			case R.id.safe_score_btn:	// 안전점수 보기
				Intent scoreIntent = new Intent(MainMenuActivity.this, SafeScoreActivity.class);
				startActivity(scoreIntent);
				break;

			case R.id.fb_logout:	// 페이스북 로그아웃
				// access_token 초기화
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("access_token", "");
				editor.apply();

				// 페북 연결 해제
				FacebookSdk.sdkInitialize(getApplicationContext());
				LoginManager.getInstance().logOut();

				// 로그인 화면으로 이동
				Intent logoutIntent = new Intent(MainMenuActivity.this, FacebookLogin.class);
				logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(logoutIntent);
				break;
		}
	}

	// 페이스북 로그인 확인
	public void faccebookLoginCheck() {
		// Facebook login check
		FacebookSdk.sdkInitialize(getApplicationContext());
		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
		if (pref.getString("access_token", "").equals("")) {
			// 미 로그인시 로그인 화면으로 이동
			Intent intent = new Intent(MainMenuActivity.this, FacebookLogin.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
