package com.example.qkx.hello;

import android.util.Log;

/**
 * Created by qkx on 17/3/5.
 */

public class DebugLog {
    private static final String TAG = "DEFAULT LOG TAG";

    private static final boolean isLogOpen = true;

    public static void d(String tag, String log) {
        if (isLogOpen) {
            Log.d(tag, log);
        }
    }

    public static void d(String log) {
        d(TAG, log);
    }

    public static void e(String tag, String log) {
        if (isLogOpen) {
            Log.e(tag, log);
        }
    }

    public static void e(String log) {
        e(TAG, log);
    }
}
