package org.multibluetooth.multibluetooth.Driving;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.Driving.Bluetooth.Connection.LaserScan.LaserScanner;
import org.multibluetooth.multibluetooth.Driving.Bluetooth.Constants;
import org.multibluetooth.multibluetooth.Driving.Model.DriveInfoModel;
import org.multibluetooth.multibluetooth.Driving.ServerConnection.RealtimeConnection;
import org.multibluetooth.multibluetooth.Driving.TTS.DrivingTextToSpeach;
import org.multibluetooth.multibluetooth.Driving.TopActivity.DrivingOnTopService;
import org.multibluetooth.multibluetooth.MainMenu.MainMenuActivity;
import org.multibluetooth.multibluetooth.R;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScore;
import org.multibluetooth.multibluetooth.SafeScore.Model.SafeScoreModel;
import org.multibluetooth.multibluetooth.SafeScore.SafeScoreActivity;

/**
 * Created by YS on 2016-09-13.
 */
public class DrivingActivity extends AppCompatActivity {

    // VIEW
    private RelativeLayout forwardDistanceLayout;
    private TextView forwardDistance;
    private RelativeLayout backDistanceLayout;
    private TextView backDistance;
    private TextView btDeviceName;
    private RelativeLayout sideScanActivity;
    private TextView sideSafeDistance;
    private TextView sideSafeSpeed;
    private TextView sideSafeMsg;
    private LinearLayout loadingDialog;
    private HalfCircleView halfTopView;
    private HalfCircleView halfBottomView;

    double mySpeed, maxSpeed;
    int driveId;

    private DriveThread driveThread;
    private RealtimeConnection realtimeSocket;
    public GpsInfo gpsInfo;
    private SideScanQueue sideScanQueue = new SideScanQueue();

    public static final int DRIVE_START_FLAG = 1;
    public static final int DRIVE_STOP_FLAG = 2;
    public static final int FORWARD_MESSAGE = 100;
    public static final int BACK_MESSAGE = 101;
    public static final int OBD_MESSAGE = 102;
    public static final int SAFE_SPEED_WARNING = 110;
    public static final int SUDDEN_FAST_WARNING = 111;
    public static final int SUDDEN_SLOW_WARNING = 112;
    public static final int SUDDEN_START_WARNING = 113;
    public static final int SUDDEN_STOP_WARNING = 114;
    public static final int DISTANCE_WARNING = 115;
    public static final int DISTANCE_NORMAL = 116;
    public static final int DISTANCE_DANGER = 117;
    public static final int SIDE_DISTANCE_WARNING = 118;
    public static final int SIDE_DISTANCE_GOOD = 119;
    public static final int SIDE_DISTANCE_DANGER = 120;
    public static final int DANGER_LOCATION_IN_MIDDLE = 301;
    public static final int DANGER_LOCATION_IN_WARNING = 302;
    public static final int DANGER_LOCATION_IN_CRITICAL = 303;
    public static final int PERMISSION_GRANTED = 1234;

    // Speech 모듈
    private static DrivingTextToSpeach drTTS;

    // binding with service
    private boolean mIsBound = false;
    private DrivingOnTopService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((DrivingOnTopService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driving_signal_activity);

        Intent intent = new Intent(DrivingActivity.this, DriveStartDialog.class);
        startActivityForResult(intent, DRIVE_START_FLAG);

        // VIEW 연결
        forwardDistanceLayout = (RelativeLayout) findViewById(R.id.forward_distance_layout);
        //forwardDistance = (TextView) findViewById(R.id.forward_distance);
        backDistanceLayout = (RelativeLayout) findViewById(R.id.back_distance_layout);
        //backDistance = (TextView) findViewById(R.id.back_distance);
        btDeviceName = (TextView) findViewById(R.id.bt_device_name);
        sideScanActivity = (RelativeLayout) findViewById(R.id.side_scan_activity);
        sideSafeDistance = (TextView) findViewById(R.id.side_safe_distance);
        sideSafeSpeed = (TextView) findViewById(R.id.side_safe_speed);
        sideSafeMsg = (TextView) findViewById(R.id.side_safe_msg);
        loadingDialog = (LinearLayout) findViewById(R.id.loading_dialog);

        maxSpeed = mySpeed = 0;

        if (MainMenuActivity.btLaserCon != null)
            MainMenuActivity.btLaserCon.setChangeContext(this);
        if (MainMenuActivity.btOBDCon != null)
            MainMenuActivity.btOBDCon.setChangeContext(this);

        // Speech 모듈 생성
        drTTS = DrivingTextToSpeach.getInstance(this);

        // Server socket 연결 쓰레드 생성
        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch(inputMessage.what){
                    case RealtimeConnection.MessageTypeClass.SIMSOCK_DATA :
                        String msg = (String) inputMessage.obj;
                        Log.d("OUT",  msg);
                        // do something with UI
                        break;

                    case RealtimeConnection.MessageTypeClass.SIMSOCK_CONNECTED :
                        // do something with UI
                        break;

                    case RealtimeConnection.MessageTypeClass.SIMSOCK_DISCONNECTED :
                        // do something with UI
                        break;

                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (MainMenuActivity.btLaserCon != null)
            MainMenuActivity.btLaserCon.serviceConn();
        if (MainMenuActivity.btOBDCon != null)
            MainMenuActivity.btOBDCon.serviceConn();*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
        drTTS.onDestroy();
    }

    // 디바이스 메세지 전달
    public void setChangeText(String message) {
        btDeviceName.setText(message);
    }

    public void onAlert(int mode) {
        switch (mode) {
            case DISTANCE_DANGER:
                drTTS.speechingSentence("거리가 가깝습니다. 안전거리를 유지해주세요.");
                setForwardBackground(mode);
                break;
            case DISTANCE_WARNING:
                drTTS.speechingSentence("안전거리 위반입니다. 사고에 주의하세요.");
                setForwardBackground(mode);
                break;
            case SIDE_DISTANCE_DANGER:
                drTTS.speechingSentence("위험합니다. 차선 거리나 속도를 늘려주세요.");
                setSideBackground(mode);
                break;
            case SIDE_DISTANCE_WARNING:
                drTTS.speechingSentence("조금 위험합니다. 옆차량과 거리가 가깝습니다.");
                setSideBackground(mode);
                break;
            case SIDE_DISTANCE_GOOD:
                drTTS.speechingSentence("끼어들어도 좋습니다.");
                setSideBackground(mode);
                break;
            case SAFE_SPEED_WARNING:
                drTTS.speechingSentence("과속 주행중입니다. 속도를 줄여주세요.");
                break;
            case SUDDEN_FAST_WARNING:
                drTTS.speechingSentence("급가속 하였습니다. 안전운전 해주세요.");
                break;
            case SUDDEN_SLOW_WARNING:
                drTTS.speechingSentence("급감속 하였습니다. 안전운전 해주세요.");
                break;
            case SUDDEN_START_WARNING:
                drTTS.speechingSentence("급출발 하였습니다. 안전운전 해주세요.");
                break;
            case SUDDEN_STOP_WARNING:
                drTTS.speechingSentence("급정거 하였습니다. 안전운전 해주세요.");
                break;
            case DANGER_LOCATION_IN_MIDDLE:
                drTTS.speechingSentence("사고유발 지역입니다. 운전에 주의하세요.");
                break;
            case DANGER_LOCATION_IN_WARNING:
                drTTS.speechingSentence("위험행동 다발지역입니다. 방어운전을 해야합니다.");
                break;
            case DANGER_LOCATION_IN_CRITICAL:
                drTTS.speechingSentence("위험한 지역입니다. 주변를 잘살피세요.");
                break;
        }
    }

    // 스캔 관련 버튼 클릭
    public void onScan(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                // 왼쪽 스캔 시작
                sideScanActivity.setVisibility(View.VISIBLE);
                loadingDialog.setVisibility(View.VISIBLE);
                sideScanQueue.init();
                driveThread.scanChange(LaserScanner.SCAN_LEFT);
                break;
            case R.id.right_btn:
                // 오른쪽 스캔 시작
                sideScanActivity.setVisibility(View.VISIBLE);
                loadingDialog.setVisibility(View.VISIBLE);
                sideScanQueue.init();
                driveThread.scanChange(LaserScanner.SCAN_RIGHT);
                break;
            case R.id.end_scan_btn:
                // 스캔 종료
                sideScanActivity.setVisibility(View.GONE);
                sideScanQueue.init();
                driveThread.scanChange(LaserScanner.SCAN_STOP);
                break;
        }
    }

    public void onScan(int side) {
        switch (side) {
            case LaserScanner.SCAN_LEFT:
                // 왼쪽 스캔 시작
                sideScanActivity.setVisibility(View.VISIBLE);
                loadingDialog.setVisibility(View.VISIBLE);
                sideScanQueue.init();
                driveThread.scanChange(side);
                break;
            case LaserScanner.SCAN_RIGHT:
                // 오른쪽 스캔 시작
                sideScanActivity.setVisibility(View.VISIBLE);
                loadingDialog.setVisibility(View.VISIBLE);
                sideScanQueue.init();
                driveThread.scanChange(side);
                break;
            case LaserScanner.SCAN_STOP:
                // 스캔 종료
                sideScanActivity.setVisibility(View.GONE);
                sideScanQueue.init();
                driveThread.scanChange(side);
                break;
        }
    }

    public void setSideDistance(float distance) {
        loadingDialog.setVisibility(View.INVISIBLE);
        sideSafeDistance.setText(distance+"m");
    }

    public void setSideBackground(int mode) {
        // 위험 단계에 따른 알림
        switch (mode) {
            case SIDE_DISTANCE_DANGER:
                sideSafeMsg.setBackgroundResource(R.color.danger);
                sideSafeMsg.setText("위험합니다. \n차선 거리나 속도를 늘려주세요.");
                break;
            case SIDE_DISTANCE_WARNING:
                sideSafeMsg.setBackgroundResource(R.color.warning);
                sideSafeMsg.setText("조금 위험합니다.\n옆차량과 거리가 가깝습니다.");
                break;
            case SIDE_DISTANCE_GOOD:
                sideSafeMsg.setBackgroundResource(R.color.good);
                sideSafeMsg.setText("끼어들어도 좋습니다.");
                break;
        }
    }

    // 전방 데이터 전달
    public void setForwardText(String message) {
        // View 반영
        forwardDistance.setText(message);
        if (mBoundService != null)
            mBoundService.setForwardText(message);
    }

    // 전방 데이터 전달
    public void setForwardText(float distance) {
        // View 반영
        forwardDistance.setText(distance+" m");
        if (mBoundService != null)
            mBoundService.setForwardText("앞 "+distance);
    }

    // 전방 배경색 변경
    public void setForwardBackground(int mode) {
        switch (mode) {
            case DISTANCE_WARNING:
                halfBottomView.setColorByResource(R.color.warning);
                break;
            case DISTANCE_DANGER:
                halfBottomView.setColorByResource(R.color.danger);
                break;
            case DISTANCE_NORMAL:
                halfBottomView.setColorByResource(R.color.good);
                break;
        }
        halfTopView.startDraw();
        // TODO 최상위뷰 색 변경
        // mBoundService.setColor(mode);
    }

    // 후방 데이터 전달
    public void setBackText(String message) {
        // View 반영
        backDistance.setText(message);
        if (mBoundService != null)
            mBoundService.setBackText("뒤 "+message);
    }

    // 후방 데이터 전달
    public void setBackText(float distance) {
        // View 반영
        backDistance.setText(distance+" m");
        if (mBoundService != null)
            mBoundService.setBackText("뒤 "+distance);
    }

    // 후방 배경색 변경
    public void setBackwardBackground(int mode) {
        switch (mode) {
            case DISTANCE_WARNING:
                halfBottomView.setColorByResource(R.color.warning);
                break;
            case DISTANCE_DANGER:
                halfBottomView.setColorByResource(R.color.danger);
                break;
            case DISTANCE_NORMAL:
                halfBottomView.setColorByResource(R.color.good);
                break;
        }
        halfBottomView.startDraw();
    }

    // 운행 시작 버튼 클릭
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DRIVE_START_FLAG:
                gpsInfo = new GpsInfo(this);
                //gpsInfo.showSettingsAlert();
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                                    1);
                } else {
                    startDriving();
                }

                break;
            case PERMISSION_GRANTED:
                // 최상위뷰 연결
                doBindService();
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //  gps functionality
            startDriving();
        } else {
            Toast.makeText(this, "GPS 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // 운행시작 함수
    private void startDriving() {
        // 운행 시작
        DriveInfoModel driveInfoModel = new DriveInfoModel(this, "DriveInfo.db", null);
        driveId = driveInfoModel.getTopNumber();
        driveInfoModel.close();
        driveThread = new DriveThread(this, mHandler, driveId, gpsInfo);
        driveThread.start();

        //realtimeSocket = new RealtimeConnection("128.199.69.84", 8107, mHandler);
        //realtimeSocket.start();
    }

    // 운행 쓰레드에서 Message를 받기위한 핸들러
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    switch (msg.arg1) {
                        case FORWARD_MESSAGE:
                            /*Bundle fBundle = msg.getData();
                            setForwardText(fBundle.getString("message"));*/
                            break;
                        case BACK_MESSAGE:
                            Bundle bBundle = msg.getData();
                            setBackText(bBundle.getString("message"));
                            break;
                        case OBD_MESSAGE:
                            Bundle obdBundle = msg.getData();
                            setChangeText(obdBundle.getString("message"));
                            break;
                        case DRIVE_STOP_FLAG:
                            onDrivingStop();
                            break;
                    }
                    break;
            }
        }
    };

    public void onOptionClick(View v) {
        switch(v.getId()) {
            case R.id.driving_stop:
                onDrivingStop();
                break;
            case R.id.floating_btn:
                onFloatingView();
                break;
        }
    }

    public void onFloatingView() {
        // 마시멜로우 버전이상부터는 PERMISSION 승인이 따로 필요함
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_GRANTED);
            } else {
                // 최상위뷰 연결
                doBindService();
            }
        } else {
            // 최상위뷰 연결
            doBindService();
        }
        //bindService(new Intent(DrivingActivity.this, DrivingOnTopService.class), mConnection, Context.BIND_AUTO_CREATE);
        //startService(new Intent(this, DrivingOnTopService.class));	//서비스 시작
    }

    public void onCloseFloatingView() {
        stopService(new Intent(this, DrivingOnTopService.class));	//서비스 종료
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(DrivingActivity.this,
                DrivingOnTopService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void onDrivingStop() {
        // 플로팅 뷰 닫음
        onCloseFloatingView();

        // 반복 쓰레드 종료
        driveThread.stopRequest();
        SafeScoreModel safeScoreModel = new SafeScoreModel(this, "SafeScore.db", null);
        SafeScore safeScore = safeScoreModel.getData(driveId);
        safeScoreModel.close();
        Log.d("TEST","액티비티가 가지고있는 ID:"+driveId);
        Log.d("TEST","safeScore: "+safeScore.toString());
        // 해당 ID의 운행정보 가져옴
        Intent intent = new Intent(DrivingActivity.this, SafeScoreActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("drive_item", safeScore);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        onDrivingStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.driving_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.driving_stop:
                onDrivingStop();
                return true;
            case R.id.floating_btn:
                onFloatingView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        halfTopView = new HalfCircleView(getApplicationContext(), forwardDistanceLayout.getWidth(), forwardDistanceLayout.getHeight(), HalfCircleView.TOP);
        halfTopView.setFill(true);
        halfTopView.setColorByResource(R.color.good);
        halfTopView.startDraw();

        halfBottomView = new HalfCircleView(getApplicationContext(), backDistanceLayout.getWidth(), backDistanceLayout.getHeight(), HalfCircleView.BOTTOM);
        halfBottomView.setFill(true);
        halfBottomView.setColorByResource(R.color.good);
        halfBottomView.startDraw();

        forwardDistanceLayout.addView(halfTopView);
        //test.addView(halfTopBorder);
        backDistanceLayout.addView(halfBottomView);
        //test2.addView(halfBottomBorder);


        forwardDistance = new TextView(getApplicationContext());
        forwardDistance.setText("- m");
        forwardDistance.setTextSize(90);
        forwardDistance.setTextColor(Color.WHITE);
        backDistance = new TextView(getApplicationContext());
        backDistance.setText("- m");
        backDistance.setTextSize(90);
        backDistance.setTextColor(Color.WHITE);

        forwardDistanceLayout.addView(forwardDistance, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        backDistanceLayout.addView(backDistance, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)forwardDistance.getLayoutParams();
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)backDistance.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        forwardDistance.setLayoutParams(params); //causes layout update
        backDistance.setLayoutParams(params2);
    }
/*
    public void sendTest(View v) {
        switch (v.getId()) {
            *//*case R.id.speed:
                MainMenuActivity.btOBDCon.sendMessage("01 42\r");
                break;*//*
            case R.id.dbdata:
                DriveInfoModel driveInfoModel = new DriveInfoModel(this, "DriveInfo.db", null);
                ArrayList<DriveInfo> testArray = driveInfoModel.getAllData();
                Log.d("TEST", testArray.toString());
                driveInfoModel.close();

                for(DriveInfo dinfo : testArray) {
                    Log.d("TEST", dinfo.toString());
                }

                SafeScoreModel safeScoreModel = new SafeScoreModel(this, "DriveInfo.db", null);
                ArrayList<SafeScore> testArray2 = safeScoreModel.getAllData();
                Log.d("TEST", testArray2.toString());
                safeScoreModel.close();

                for(SafeScore dinfo : testArray2) {
                    Log.d("TEST", dinfo.toString());
                }
                break;
            case R.id.delete_all:
                DriveInfoModel tempModel = new DriveInfoModel(this, "DriveInfo.db", null);
                tempModel.deleteAll();

                SafeScoreModel tempModel2 = new SafeScoreModel(this, "DriveInfo.db", null);
                tempModel2.deleteAll();
                break;
        }
    }*/
}
