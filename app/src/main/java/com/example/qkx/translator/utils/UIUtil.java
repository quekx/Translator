package com.example.qkx.translator.utils;

import android.view.View;

/**
 * Created by qkx on 17/3/10.
 */

public class UIUtil {
    public static <T extends View> T getView(View container, int id) {
        return (T) container.findViewById(id);
    }
}
