package com.example.qkx.translator.Speech;

import android.content.Context;
import android.text.TextUtils;

import com.example.qkx.translator.MyApp;
import com.example.qkx.translator.config.ConfigManager;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.socks.library.KLog;

/**
 * Created by qkx on 16/12/6.
 */

public class SpeechManager {
    private static final String TAG = SpeechManager.class.getSimpleName();

    private static SpeechManager sInstance;

    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private InitListener mInitListener;

    public static SpeechManager getInstance() {
        if (sInstance == null) {
            synchronized (SpeechManager.class) {
                if (sInstance == null) {
                    sInstance = new SpeechManager();
                }
            }
        }
        return sInstance;
    }

    private SpeechManager() {}

    public void init() {
        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                KLog.d(TAG, "SpeechRecognizer init() code = " + code);
                if (code != 0) {
                    KLog.e("初始化失败，错误码：" + code);
                    ToastUtil.showToastShort(MyApp.getAppInstance(), "初始化失败，错误码：" + code);
                }
            }
        };

        initStt();
        initTts();
    }

    private void initStt() {
        SpeechRecognizer.createRecognizer(MyApp.getAppInstance(), mInitListener);
    }

    private void initTts() {
        SpeechSynthesizer.createSynthesizer(MyApp.getAppInstance(), mInitListener);
    }

    public void unInit() {
        SpeechSynthesizer tts = SpeechSynthesizer.getSynthesizer();
        if (tts != null) {
            tts.destroy();
        }

        SpeechRecognizer iat = SpeechRecognizer.getRecognizer();
        if (iat != null) {
            iat.destroy();
        }
    }

    /******************* tts 语音合成 *******************/
    public void synthesizeSpeech(String text) {
        synthesizeSpeech(text, null, null, null);
    }

    public void synthesizeSpeech(String text, BaseSynthesizerListener listener) {
        synthesizeSpeech(text, null, null, null, listener);
    }

    public void synthesizeSpeech(String text, String voiceName, String voiceSpeed, String voiceVolume) {
        synthesizeSpeech(text, voiceName, voiceSpeed, voiceVolume, null);
    }

    public void synthesizeSpeech(String text, String voiceName, String voiceSpeed, String voiceVolume,
                                 BaseSynthesizerListener listener) {
        if (TextUtils.isEmpty(text)) return;
        SpeechSynthesizer tts = SpeechSynthesizer.getSynthesizer();
        if (tts == null) return;

        if (voiceName == null) {
            voiceName = ConfigManager.getInstance().getVoiceName();
        }
        if (voiceSpeed == null) {
            voiceSpeed = ConfigManager.getInstance().getVoiceSpeed();
        }
        if (voiceVolume == null) {
            voiceVolume = ConfigManager.getInstance().getVoiceVolume();
        }

        tts.setParameter(SpeechConstant.VOICE_NAME, voiceName); //设置发音人
        tts.setParameter(SpeechConstant.SPEED, voiceSpeed);//设置语速
        tts.setParameter(SpeechConstant.VOLUME, voiceVolume);//设置音量,范围 0~100
        tts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        tts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm"); // 音频路径
        tts.startSpeaking(text, listener);
    }

    public void stopSpeechSynthesizing() {
        SpeechSynthesizer tts = SpeechSynthesizer.getSynthesizer();
        if (tts != null && tts.isSpeaking()) {
            tts.stopSpeaking();
        }
    }

    /******************* stt 语音识别 *******************/
    public void recognizeChinese(BaseRecognizerListener listener) {
        recognizeSpeech(listener, "zh_cn", null, null);
    }

    public void reconizeEnglish(BaseRecognizerListener listener) {
        recognizeSpeech(listener, "en_us", null, null);
    }

    public void recognizeSpeech(BaseRecognizerListener listener,
                                String language, String avdBosMillis, String avdEosMillis) {
        recognizeSpeech(listener, language, avdBosMillis, avdEosMillis, null);
    }

    public void recognizeSpeech(BaseRecognizerListener listener,
                                String language, String avdBosMillis, String avdEosMillis, String domain) {
        SpeechRecognizer stt = SpeechRecognizer.getRecognizer();
        if (stt == null) return;

        if (avdBosMillis == null) {
            avdBosMillis = ConfigManager.getInstance().getAvdBos();
        }
        if (avdEosMillis == null) {
            avdEosMillis = ConfigManager.getInstance().getAvdEos();
        }
        if (domain == null) {
            domain = ConfigManager.getInstance().getDomain();
        }

        stt.setParameter(SpeechConstant.DOMAIN, domain);
        stt.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            stt.setParameter(SpeechConstant.ACCENT, "mandarin");
        }
        stt.setParameter(SpeechConstant.RESULT_TYPE, "json");
        stt.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        stt.setParameter(SpeechConstant.VAD_BOS, avdBosMillis); // 设置语音前端点
        stt.setParameter(SpeechConstant.VAD_EOS, avdEosMillis); // 设置语音后端点
        stt.setParameter(SpeechConstant.ASR_PTT, "1"); // 设置标点符号

        int ret = stt.startListening(listener);
        if (ret != ErrorCode.SUCCESS) {
            ToastUtil.showToastShort(MyApp.getAppInstance(), "听写失败,错误码：" + ret);
        } else {
            ToastUtil.showToastShort(MyApp.getAppInstance(), "请开始说话");
        }
    }

    public void stopSpeechRecognizing() {
        SpeechRecognizer iat = SpeechRecognizer.getRecognizer();
        if (iat != null && iat.isListening()) {
            iat.stopListening();
        }
    }
}
