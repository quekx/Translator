package com.example.qkx.hello;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by qkx on 17/3/5.
 */

public class ToastUtil {
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String msg) {
        Toast.makeText(DemoApp.getAppContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
