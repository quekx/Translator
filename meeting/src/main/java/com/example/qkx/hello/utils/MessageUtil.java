package com.example.qkx.hello.utils;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

/**
 * Created by qkx on 17/3/5.
 */

public class MessageUtil {
    public static String getTextFromMessage(EMMessage message) {
        if (message == null) return null;

        if (message.getType() == EMMessage.Type.TXT) {
            EMTextMessageBody body = (EMTextMessageBody) message.getBody();
            return body.getMessage();
        }
        return null;
    }
}
