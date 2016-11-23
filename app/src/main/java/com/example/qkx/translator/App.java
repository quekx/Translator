package com.example.qkx.translator;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.socks.library.KLog;

/**
 * Created by qkx on 16/11/22.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + Constants.APPID);
        KLog.i("SpeechUtility init()");
    }
}
