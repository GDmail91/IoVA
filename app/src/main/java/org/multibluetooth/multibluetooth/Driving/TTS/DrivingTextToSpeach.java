package org.multibluetooth.multibluetooth.Driving.TTS;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by YS on 2016-10-11.
 */
public class DrivingTextToSpeach {

    private static DrivingTextToSpeach mDrivingTextToSpeech = new DrivingTextToSpeach();
    private static TextToSpeech drTTS;

    private DrivingTextToSpeach() {

    }

    public static DrivingTextToSpeach getInstance(Context mContext) {
        drTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    drTTS.setLanguage(Locale.KOREAN);
                }
            }
        });
        return mDrivingTextToSpeech;
    }


    public void speechingSentence(String speechSentence) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(speechSentence);
        } else {
            ttsUnder20(speechSentence);
        }
    }

    public void onDestroy() {
        drTTS.shutdown();
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        drTTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        drTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}