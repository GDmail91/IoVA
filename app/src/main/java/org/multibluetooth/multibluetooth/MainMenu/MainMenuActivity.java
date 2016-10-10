package org.multibluetooth.multibluetooth.MainMenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothLaserService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Facebook.FacebookLogin;
import org.multibluetooth.multibluetooth.History.HistoryActivity;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;
import org.multibluetooth.multibluetooth.Setting.SettingActivity;

public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	// VIEW
	private TextView btDeviceName;
	private ImageView btconnectSign;

	public static BluetoothConnection btLaserCon;
	public static BluetoothConnection btOBDCon;

	private static final int BLUETOOTH_CONNECTING = 1000;
	public static final int BLUETOOTH_LASER_CONNECT = 1010;
	public static final int BLUETOOTH_OBD_CONNECT = 1020;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_activity);

		// 페이스북으로 로그인 됬는지 확인
		faccebookLoginCheck();

		// VIEW 연결
		btDeviceName = (TextView) findViewById(R.id.bt_device_name);
		btconnectSign = (ImageView) findViewById(R.id.bt_connect_sign);

		// Bluetooth connection check
		setBtConnectSign();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (btLaserCon == null) {
			btLaserCon = new LaserScanner(this);
		}
		((LaserScanner) btLaserCon).setupService();
	}

	public void onMenuClick(View v) {
		Log.d(TAG, v.toString());

		switch (v.getId()) {
			case R.id.driving_btn:	// 운전하기
				if ((btLaserCon != null && btLaserCon.getConnectionStatus() == BluetoothLaserService.STATE_CONNECTED)
				|| (btOBDCon != null && btOBDCon.getConnectionStatus() == BluetoothLaserService.STATE_CONNECTED)) {
					Intent drivingIntent = new Intent(MainMenuActivity.this, DrivingActivity.class);
					startActivity(drivingIntent);
				} else {
					Toast.makeText(getApplicationContext(), "OBD 연결이 필요합니다.", Toast.LENGTH_LONG).show();
				}
				break;

			case R.id.safe_score_btn:	// 안전점수 보기
				Intent scoreIntent = new Intent(MainMenuActivity.this, SafeScoreActivity.class);
				startActivity(scoreIntent);
				break;

			case R.id.history_btn: // 운전 기록 보기
				Intent historyIntent = new Intent(MainMenuActivity.this, HistoryActivity.class);
				startActivity(historyIntent);
				break;

			case R.id.fb_logout:	// 페이스북 로그아웃
				// 설정 창으로 이동
				Intent settingIntent = new Intent(MainMenuActivity.this, SettingActivity.class);
				startActivity(settingIntent);
				break;
		}
	}

	// 페이스북 로그인 확인
	public void faccebookLoginCheck() {
		// Facebook login check
		FacebookSdk.sdkInitialize(getApplicationContext());
		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
		// TODO 페이스북에 해당토큰이 아직 유효한지 질의
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
			SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
			SharedPreferences.Editor prefEdit = pref.edit();
			prefEdit.putString(DeviceListActivity.EXTRA_DEVICE_ADDRESS, "");
			prefEdit.apply();

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
			case LaserScanner.REQUEST_ENABLE_BT_BY_LASER:
				btLaserCon.onActivityResult(BluetoothConnection.REQUEST_ENABLE_BT, resultCode, data);
				Log.d(TAG, "이떄 연결됬는지 확인?");
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

	public void setBtConnectSign() {
		if (btLaserCon != null
				&& btLaserCon.getConnectionStatus() == BluetoothLaserService.STATE_CONNECTED
				&& (btOBDCon == null || btOBDCon.getConnectionStatus() != BluetoothLaserService.STATE_CONNECTED)) {
			btOBDCon = new OBDScanner(this);
			Log.d(TAG, "OBD 연결 실행");
			btOBDCon.conn();
			Log.d(TAG, "OBD 연결 실행2");
		}
		if (btLaserCon != null && btOBDCon != null
				&& btLaserCon.getConnectionStatus() == BluetoothLaserService.STATE_CONNECTED
				&& btOBDCon.getConnectionStatus() == BluetoothLaserService.STATE_CONNECTED) {
			btconnectSign.setImageResource(R.drawable.green_dot);
		}
	}
}
