package org.multibluetooth.multibluetooth.MainMenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Facebook.FacebookLogin;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;

public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	// VIEW
	private Button driving_btn;
	private TextView btDeviceName;
	private Button btConnect;
	private ImageButton btCheck;

	public static BluetoothConnection btLaserCon;
	public static BluetoothConnection btOBDCon;

	private static final int BLUETOOTH_CONNECTING = 1000;
	public static final int BLUETOOTH_LASER_CONNECT = 1010;
	public static final int BLUETOOTH_OBD_CONNECT = 1020;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_activity);

		faccebookLoginCheck();
		// VIEW 연결
		driving_btn = (Button) findViewById(R.id.driving_btn);

		btDeviceName = (TextView) findViewById(R.id.bt_device_name);
		btConnect = (Button) findViewById(R.id.bt_connect);
		btCheck = (ImageButton) findViewById(R.id.bt_check);

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

	// 블루투스 연결상태 확인
	public void checkBtStatus(View v) {
		Intent intent = new Intent(MainMenuActivity.this, BluetoothCheckDialog.class);
		if (btLaserCon != null)
			intent.putExtra("LASER", btLaserCon.getConnectionStatus());
		if (btOBDCon != null)
			intent.putExtra("OBD", btOBDCon.getConnectionStatus());
		startActivityForResult(intent, BLUETOOTH_CONNECTING);
	}

	// 불루투스 연결
	public void onBluetoothConnect(int connectDevice) {
		if (connectDevice == BLUETOOTH_LASER_CONNECT) {
			btLaserCon = new LaserScanner(this);
			btLaserCon.conn();
		} else if (connectDevice == BLUETOOTH_OBD_CONNECT) {
			btOBDCon = new OBDScanner(this);
			btOBDCon.conn();
		}
	}

	// 디바이스 메세지 전달
	public void setChangeText(String message) {
		btDeviceName.setText(message);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case LaserScanner.REQUEST_CONNECT_DEVICE_SECURE_BY_LASER:
				btLaserCon.onActivityResult(BluetoothConnection.REQUEST_CONNECT_DEVICE_SECURE, resultCode, data);
				break;
			case BluetoothConnection.REQUEST_CONNECT_DEVICE_INSECURE:
				btLaserCon.onActivityResult(BluetoothConnection.REQUEST_CONNECT_DEVICE_INSECURE, resultCode, data);
				break;
			case LaserScanner.REQUEST_ENABLE_BT_BY_LASER:
				btLaserCon.onActivityResult(BluetoothConnection.REQUEST_ENABLE_BT, resultCode, data);
				break;
			case OBDScanner.REQUEST_CONNECT_DEVICE_SECURE_BY_OBD:
				btOBDCon.onActivityResult(BluetoothConnection.REQUEST_CONNECT_DEVICE_SECURE, resultCode, data);
				break;
			case OBDScanner.REQUEST_ENABLE_BT_BY_OBD:
				btOBDCon.onActivityResult(BluetoothConnection.REQUEST_ENABLE_BT, resultCode, data);
				break;
			case BLUETOOTH_CONNECTING:
				if (resultCode == BLUETOOTH_LASER_CONNECT) {
					onBluetoothConnect(BLUETOOTH_LASER_CONNECT);
				} else if (resultCode == BLUETOOTH_OBD_CONNECT)
				 	onBluetoothConnect(BLUETOOTH_OBD_CONNECT);
				break;
		}
	}
}
