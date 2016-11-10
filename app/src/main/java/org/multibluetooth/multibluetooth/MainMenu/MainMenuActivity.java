package org.multibluetooth.multibluetooth.MainMenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import org.json.JSONArray;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.BluetoothConnection;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.OBDScan.OBDScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.DeviceListActivity;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Service.BluetoothService;
import org.multibluetooth.multibluetooth.Driving.DrivingActivity;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.Facebook.FacebookLogin;
import org.multibluetooth.multibluetooth.History.HistoryActivity;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;
import org.multibluetooth.multibluetooth.Setting.SettingActivity;
import org.multibluetooth.multibluetooth.retrofit.RetrofitService;
import org.multibluetooth.multibluetooth.retrofit.format.DTOInsertCallback;
import org.multibluetooth.multibluetooth.retrofit.format.DTOLastIndexCallback;
import org.multibluetooth.multibluetooth.retrofit.format.DTOLastIndexCallbackData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	// VIEW
	private TextView btDeviceName;
	private ImageView btconnectSign;

	public static BluetoothConnection btLaserCon;
	public static BluetoothConnection btOBDCon;
	private ConnectivityManager connectivityManager;
	private BroadcastReceiver receiver;

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

		// WiFi 연결 확인
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
				&& connectivityManager.getActiveNetworkInfo() != null){
					if(connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI
							&& connectivityManager.getActiveNetworkInfo().isConnected()){
						// TODO 업로드 시나리오
						Log.d("TEST", "WIFI 연결됨");

						getLastIndexAndUpload();
					}

					if(connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE
							&& connectivityManager.getActiveNetworkInfo().isConnected()){
						Log.d("TEST", "MOBILE 연결됨");
					}
				} else {
					Log.d("TEST", "network 없음");
				}
			}
		};
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

		registerReceiver(receiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (btLaserCon == null) {
			btLaserCon = new LaserScanner(this);
		}
		((LaserScanner) btLaserCon).setupService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	public void onMenuClick(View v) {
		Log.d(TAG, v.toString());

		switch (v.getId()) {
			case R.id.driving_btn:	// 운전하기
				if ((btLaserCon != null && btLaserCon.getConnectionStatus() == BluetoothService.STATE_CONNECTED)
				&& (btOBDCon != null && btOBDCon.getConnectionStatus() == BluetoothService.STATE_CONNECTED)) {
					Intent drivingIntent = new Intent(MainMenuActivity.this, DrivingActivity.class);
					drivingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(drivingIntent);
				} else if (btLaserCon == null || btLaserCon.getConnectionStatus() != BluetoothService.STATE_CONNECTED) {
					Toast.makeText(getApplicationContext(), "Laser 연결이 필요합니다.", Toast.LENGTH_LONG).show();
				} else if (btOBDCon == null || btOBDCon.getConnectionStatus() != BluetoothService.STATE_CONNECTED) {
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
				&& btLaserCon.getConnectionStatus() == BluetoothService.STATE_CONNECTED
				&& (btOBDCon == null || btOBDCon.getConnectionStatus() != BluetoothService.STATE_CONNECTED)) {
			btOBDCon = new OBDScanner(this);
			Log.d(TAG, "OBD 연결 실행");
			btOBDCon.conn();
			Log.d(TAG, "OBD 연결 실행2");
		}
		if (btLaserCon != null && btOBDCon != null
				&& btLaserCon.getConnectionStatus() == BluetoothService.STATE_CONNECTED
				&& btOBDCon.getConnectionStatus() == BluetoothService.STATE_CONNECTED) {
			btconnectSign.setImageResource(R.drawable.green_dot);
		}
	}

	private void getLastIndexAndUpload() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(getResources().getString(R.string.baseURL))
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RetrofitService service = retrofit.create(RetrofitService.class);

		SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
		Call<DTOLastIndexCallback> call = service.getLastIndex(
				pref.getString("access_token", ""));
		call.enqueue(new Callback<DTOLastIndexCallback>() {
			@Override
			public void onResponse(Response<DTOLastIndexCallback> response) {
				if (response.isSuccess() && response.body() != null) {
					DTOLastIndexCallbackData lastIndexData = response.body().getData();

					// 인덱스에 해당하는 데이터 가져옴
					DriveInfoModel driveInfoModel = new DriveInfoModel(getApplicationContext(), "DriveInfo.db", null);
					SafeScoreModel safeScoreModel = new SafeScoreModel(getApplicationContext(), "SafeScore.db", null);
					JSONArray driveList;
					JSONArray safeScoreList;
					if (lastIndexData != null) {
						driveList = new JSONArray(driveInfoModel.getAfterData(lastIndexData.getLastDriveId(), lastIndexData.getLastRequestId()));
						safeScoreList = new JSONArray(safeScoreModel.getAfterData(lastIndexData.getLastDriveId()));
					} else {
						driveList = new JSONArray(driveInfoModel.getAfterData(0, 0));
						safeScoreList = new JSONArray(safeScoreModel.getAfterData(0));
					}

					Log.d("TEST",safeScoreList.toString());
					if (driveList.length() > 0) {
						// 가져온 데이터 임시파일변환
						File driveFile = generateTempFile("tempDrive", driveList.toString());
						File scoreFile = generateTempFile("tempScore", safeScoreList.toString());

						// 파일 업로드
						onServerUpload(driveFile, scoreFile);
					}
				}
			}

			@Override
			public void onFailure(Throwable t) {
				Toast.makeText(getApplicationContext(), "서버 연결 실패", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void onServerUpload(File driveFile, File scoreFile) {

		// create upload service client
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(getResources().getString(R.string.baseURL))
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RetrofitService service = retrofit.create(RetrofitService.class);

		Map<String, RequestBody> driveMap = new HashMap<>();
		RequestBody driveFileBody = RequestBody.create(MediaType.parse("json"), driveFile);
		driveMap.put("raw_drive_data\"; filename=\"" + driveFile.getName(), driveFileBody);

		Map<String, RequestBody> scoreMap = new HashMap<>();
		RequestBody scoreFileBody = RequestBody.create(MediaType.parse("json"), scoreFile);
		scoreMap.put("raw_score_data\"; filename=\"" + scoreFile.getName(), scoreFileBody);

		// 서버 업로드
		SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
		Call<DTOInsertCallback> call = service.uploadNativeData(
				pref.getString("access_token", ""),
				driveMap,
				scoreMap);
		call.enqueue(new Callback<DTOInsertCallback>() {
			@Override
			public void onResponse(Response<DTOInsertCallback> response) {
				if (response.isSuccess() && response.body() != null) {
					Toast.makeText(getApplicationContext(), "동기화 완료", Toast.LENGTH_LONG).show();
					Log.d("TEST"," 동기화 완료");
				}

				Log.d("TEST", response.raw().toString());
			}

			@Override
			public void onFailure(Throwable t) {
				Log.d("TEST",t.toString());
				Toast.makeText(getApplicationContext(), "서버 연결 실패", Toast.LENGTH_LONG).show();
			}
		});
	}

	private File generateTempFile(String fileName, String writeData) {
		File file = new File(getFilesDir(), fileName);

		FileOutputStream outputStream;

		try{
			outputStream = openFileOutput( fileName, Context.MODE_PRIVATE);
			outputStream.write( writeData.getBytes());
			outputStream.close();
		}catch( Exception e){
			e.printStackTrace();
		}

		return file;
	}

}
