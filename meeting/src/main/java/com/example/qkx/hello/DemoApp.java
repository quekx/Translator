package com.example.qkx.hello;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

/**
 * Created by qkx on 17/3/5.
 */

public class DemoApp extends Application {

    private static DemoApp mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;

        initEM();
    }

    private void initEM() {
        EMOptions options = new EMOptions();
        EMClient.getInstance().init(this, options);
    }

    public static DemoApp getAppContext() {
        return mAppContext;
    }
}
