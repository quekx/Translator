package com.example.qkx.translator.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.qkx.translator.R;
import com.example.qkx.translator.Speech.BaseRecognizerListener;
import com.example.qkx.translator.Speech.BaseSynthesizerListener;
import com.example.qkx.translator.Speech.SpeechManager;
import com.example.qkx.translator.ui.conversation.ConversationActivity;
import com.example.qkx.translator.ui.setting.SettingActivity;
import com.example.qkx.translator.ui.simultaneous.SimultaneousActivity;
import com.example.qkx.translator.ui.orc.OrcActivity;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.socks.library.KLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String WELCOME_TEXT = "您好，进入声控模式，请说，对话翻译，同声翻译，或者图片翻译";

    private boolean mIsFirstOpen = false;
    private int mRecognizeTime = 0;

    private List<String> mKeywords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        mIsFirstOpen = true;
        SpeechManager.getInstance().init();

        mKeywords = new ArrayList<>();
        mKeywords.add("对话翻译");
        mKeywords.add("同声翻译");
        mKeywords.add("图片翻译");
    }

    @OnClick(R.id.img_conversation)
    void startConversation() {
        startActivity(new Intent(this, ConversationActivity.class));
    }

    @OnClick(R.id.img_orc)
    void startOrc() {
        startActivity(new Intent(this, OrcActivity.class));
    }

    @OnClick(R.id.img_setting)
    void startSetting() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    @OnClick(R.id.img_simultaneous)
    void startSimultaneous() {
        startActivity(new Intent(this, SimultaneousActivity.class));
    }

    // 合成相关
    private void stopSpeechSynthesizing() {
        SpeechSynthesizer tts = SpeechSynthesizer.getSynthesizer();
        if (tts != null && tts.isSpeaking()) {
            tts.stopSpeaking();
        }
    }

    private void startHintSynthesizing() {
        BaseSynthesizerListener listener = new BaseSynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                //开始识别
                startKeywordRecognizing();
            }
        };
        SpeechManager.getInstance().synthesizeSpeech(WELCOME_TEXT, listener);
    }

    // 识别相关
    private void stopSpeechRecognizing() {
        SpeechRecognizer iat = SpeechRecognizer.getRecognizer();
        if (iat != null && iat.isListening()) {
            iat.stopListening();
        }
    }

    private void startKeywordRecognizing() {
        BaseRecognizerListener listener = new BaseRecognizerListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                String json = recognizerResult.getResultString();
                String ret = parseJson(json);

                KLog.d(TAG, "outcome >> " + ret);
                mRecognizeTime++;
                if (mRecognizeTime > 3) {
                    stopSpeechRecognizing();
                    return;
                }
                for (int i = 0; i < mKeywords.size(); i++) {
                    if (ret.contains(mKeywords.get(i))) {
                        // 停止识别
                        stopSpeechRecognizing();

                        processKeyword(i);
                        return;
                    }
                }
            }
        };
        SpeechManager.getInstance().recognizeChinese(listener);
    }

    private void processKeyword(int index) {
        switch (index) {
            case 0:
                startConversation();
                break;
            case 1:
                startSimultaneous();
                break;
            case 2:
                startOrc();
                break;
            default:
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecognizeTime = 0;

//        if (mIsFirstOpen) {
        startHintSynthesizing();
//            mIsFirstOpen = false;
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SpeechManager.getInstance().unInit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopSpeechSynthesizing();
    }

    private String parseJson(String json) {
        StringBuilder ret = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject object = items.getJSONObject(0);
                ret.append(object.getString("w"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret.toString();
    }
}
