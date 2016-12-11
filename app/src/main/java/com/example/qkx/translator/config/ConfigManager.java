package com.example.qkx.translator.config;

import com.example.qkx.translator.MyApp;
import com.example.qkx.translator.Constants;
import com.example.qkx.translator.Speech.SpeechManager;
import com.example.qkx.translator.utils.PreferenceUtil;

/**
 * Created by qkx on 16/12/6.
 */

public class ConfigManager {
    private static ConfigManager sInstance;

    public static ConfigManager getInstance() {
        if (sInstance == null) {
            synchronized (SpeechManager.class) {
                if (sInstance == null) {
                    sInstance = new ConfigManager();
                }
            }
        }
        return sInstance;
    }

    private ConfigManager(){}

    // 发音人
    public String getVoiceName() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_VOICE_NAME,
                Constants.VALUE_DEFAULT_VOICE_NAME);
    }

    // 音量
    public String getVoiceVolume() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_VOICE_VOLUME,
                Constants.VALUE_DEFAULT_VOICE_VOLUME);
    }

    // 语速
    public String getVoiceSpeed() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_VOICE_SPEED,
                Constants.VALUE_DEFAULT_VOICE_SPEED);
    }

    // 前端超时
    public String getAvdBos() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_AVD_BOS,
                Constants.VALUE_DEFAULT_AVD_BOS);
    }

    // 后端超时
    public String getAvdEos() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_AVD_EOS,
                Constants.VALUE_DEFAULT_AVD_EOS);
    }

    // 应用领域
    public String getDomain() {
        return PreferenceUtil.getString(MyApp.getAppInstance(), Constants.KEY_DOMAIN,
                Constants.VALUE_DEFAULT_DOMAIN);
    }
}
