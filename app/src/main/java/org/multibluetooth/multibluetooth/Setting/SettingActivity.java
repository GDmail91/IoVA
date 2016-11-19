package org.multibluetooth.multibluetooth.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.multibluetooth.multibluetooth.Facebook.FacebookLogin;
import org.multibluetooth.multibluetooth.R;

/**
 * Created by YS on 2016-09-29.
 */
public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";

    // VIEW
    private TextView facebookId;
    private Switch wifiSetting;
    private Switch speakerSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        wifiSetting = (Switch) findViewById(R.id.wifi_setting);
        speakerSetting = (Switch) findViewById(R.id.speaker_setting);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        wifiSetting.setChecked(pref.getBoolean("wifi_setting", true));
        speakerSetting.setChecked(pref.getBoolean("speaker_setting", true));

        facebookId = (TextView) findViewById(R.id.facebook_id);

        facebookId.setText(getFacebookName());
    }

    private String getFacebookName() {
        // Facebook login check
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("user_name", "-");
    }

    public void onDataSync(View v) {

    }

    public void onSetting(View v) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        switch (v.getId()) {
            case R.id.wifi_setting:
                if (wifiSetting.isChecked()) editor.putBoolean("wifi_setting", true);
                else editor.putBoolean("wifi_setting", false);
                editor.apply();
                break;
            case R.id.speaker_setting:
                if (speakerSetting.isChecked()) editor.putBoolean("speaker_setting", true);
                else editor.putBoolean("speaker_setting", false);
                editor.apply();
                break;

        }
    }

    public void onLogout(View v) {
        // access_token 초기화
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("access_token", "");
        editor.putString("user_name", "");
        editor.apply();

        // 페북 연결 해제
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();

        // 로그인 화면으로 이동
        Intent logoutIntent = new Intent(SettingActivity.this, FacebookLogin.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoutIntent);
    }
}
