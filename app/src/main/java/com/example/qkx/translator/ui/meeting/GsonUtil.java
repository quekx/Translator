package com.example.qkx.translator.ui.meeting;

import com.example.qkx.translator.ui.meeting.model.MeetingMessage;
import com.google.gson.Gson;

/**
 * Created by qkx on 17/3/5.
 */

public class GsonUtil {
    private static Gson gson = new Gson();

    public static MeetingMessage parseJson(String json) {
        return gson.fromJson(json, MeetingMessage.class);
    }

    public static String toJson(String src, String text) {
        MeetingMessage myMessage = new MeetingMessage(src, text);
        return gson.toJson(myMessage);
    }
}
