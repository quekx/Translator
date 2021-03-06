package com.example.qkx.translator;

import android.app.Application;

import com.example.qkx.translator.Speech.SpeechManager;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.socks.library.KLog;

/**
 * Created by qkx on 16/11/22.
 */

public class MyApp extends Application {
    private static MyApp sInstance;

    public static MyApp getAppInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + Constants.APPID);
        KLog.i("SpeechUtility >> init");
    }
}
