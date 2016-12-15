package com.example.qkx.translator.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.qkx.translator.MyApp;

/**
 * Created by qkx on 16/12/15.
 */

public class NetworkUtil {

    private static ConnectivityManager connMgr;

    public static boolean isOnline() {
        if (connMgr == null) {
            connMgr = (ConnectivityManager) MyApp.getAppInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
