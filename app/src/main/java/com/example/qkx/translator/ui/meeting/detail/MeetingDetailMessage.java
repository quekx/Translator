package com.example.qkx.translator.ui.meeting.detail;

import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.ui.meeting.GsonUtil;
import com.example.qkx.translator.ui.meeting.MessageUtil;
import com.example.qkx.translator.ui.meeting.model.MeetingMessage;
import com.example.qkx.translator.utils.DebugLog;
import com.hyphenate.chat.EMMessage;

/**
 * Created by qkx on 17/3/10.
 */

public class MeetingDetailMessage {

    private EMMessage mEMessage;
    private String mText;

    private String mFrom;
    private String mMsg;
    private String mLocale = "zh";

    public MeetingDetailMessage(String msg, String locale) {
        this.mMsg = msg;
        this.mLocale = locale;
        if ("zh".equals(locale)) {
            this.mFrom = "我";
        } else {
            this.mFrom = "me";
        }
    }

    public MeetingDetailMessage(EMMessage message, String locale, final MeetingAddListener listener) {
        this.mEMessage = message;
        this.mText = MessageUtil.getTextFromMessage(message);
        this.mFrom = message.getFrom();
        this.mLocale = locale;

        dealWithText(listener);
    }

    private void dealWithText(final MeetingAddListener listener) {
        MeetingMessage myMessage = GsonUtil.parseJson(mText);

        if ("zh".equals(mLocale) && "en".equals(myMessage.src)) {
            // 异步翻译
            RestSource.getInstance().queryCh(myMessage.text, new RestSource.TranslateCallback() {
                @Override
                public void onProcessResult(ResultBean resultBean) {
                    mMsg = resultBean.trans_result.get(0).dst;
                    DebugLog.d("bind() :: queryCh() ======= msg :: " + mMsg);
                    if (listener != null) {
                        listener.queryDelay(MeetingDetailMessage.this);
                    }
                }
            });
        } else if ("en".equals(mLocale) && "zh".equals(myMessage.src)) {
            // 异步翻译
            RestSource.getInstance().queryEn(myMessage.text, new RestSource.TranslateCallback() {
                @Override
                public void onProcessResult(ResultBean resultBean) {
                    mMsg = resultBean.trans_result.get(0).dst;
                    DebugLog.d("bind() :: queryEn() ======= msg :: " + mMsg);
                    if (listener != null) {
                        listener.queryDelay(MeetingDetailMessage.this);
                    }
                }
            });
        } else {
            mMsg = myMessage.text;
            DebugLog.d("bind() ======= msg :: " + mMsg);
            if (listener != null) {
                listener.addInstant(this);
            }
        }
    }

    public MeetingDetailMessage(EMMessage message, MeetingAddListener listener) {
        this(message, "zh", listener);
    }

    public MeetingDetailMessage(EMMessage message) {
        this(message, "zh", null);
    }

    public String getFrom() {
        return mFrom;
    }

    public String getMsg() {
        return mMsg;
    }

    public String getSourceInfo() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("mFrom : ");
//        builder.append(mEMessage.getFrom());
//        builder.append(", to :");
//        builder.append(mEMessage.getTo());
//        builder.append(", ");
//        return builder.toString();
        return mFrom + " : ";
    }

    public String getText() {
        return mText;
    }
}
