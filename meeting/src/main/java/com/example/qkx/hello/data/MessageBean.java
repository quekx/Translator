package com.example.qkx.hello.data;

import com.example.qkx.hello.DebugLog;
import com.example.qkx.hello.detail.AddListener;
import com.example.qkx.hello.model.MyMessage;
import com.example.qkx.hello.rest.RestSource;
import com.example.qkx.hello.utils.GsonUtil;
import com.example.qkx.hello.utils.MessageUtil;
import com.hyphenate.chat.EMMessage;

/**
 * Created by qkx on 17/3/10.
 */

public class MessageBean {

    private EMMessage mEMessage;
    private String mText;

    private String mFrom;
    private String mMsg;
    private String mLocale = "zh";

    public MessageBean(String msg, String locale) {
        this.mMsg = msg;
        this.mLocale = locale;
        if ("zh".equals(locale)) {
            this.mFrom = "我";
        } else {
            this.mFrom = "me";
        }
    }

    public MessageBean(EMMessage message, String locale, final AddListener listener) {
        this.mEMessage = message;
        this.mText = MessageUtil.getTextFromMessage(message);
        this.mFrom = message.getFrom();
        this.mLocale = locale;

        dealWithText(listener);
    }

    private void dealWithText(final AddListener listener) {
        MyMessage myMessage = GsonUtil.parseJson(mText);

        if ("zh".equals(mLocale) && "en".equals(myMessage.src)) {
            // 异步翻译
            RestSource.getInstance().queryCh(myMessage.text, new RestSource.TranslateCallback() {
                @Override
                public void onProcessResult(ResultBean resultBean) {
                    mMsg = resultBean.trans_result.get(0).dst;
                    DebugLog.d("bind() :: queryCh() ======= msg :: " + mMsg);
                    if (listener != null) {
                        listener.queryDelay(MessageBean.this);
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
                        listener.queryDelay(MessageBean.this);
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

    public MessageBean(EMMessage message, AddListener listener) {
        this(message, "zh", listener);
    }

    public MessageBean(EMMessage message) {
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
