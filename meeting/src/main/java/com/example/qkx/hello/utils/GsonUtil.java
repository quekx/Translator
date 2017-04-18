package com.example.qkx.hello.utils;

import com.example.qkx.hello.model.MyMessage;
import com.google.gson.Gson;

/**
 * Created by qkx on 17/3/5.
 */

public class GsonUtil {
    private static Gson gson = new Gson();

    public static MyMessage parseJson(String json) {
        return gson.fromJson(json, MyMessage.class);
    }

    public static String toJson(String src, String text) {
        MyMessage myMessage = new MyMessage(src, text);
        return gson.toJson(myMessage);
    }
}
