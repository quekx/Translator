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
import com.example.qkx.translator.utils.NetworkUtil;
import com.example.qkx.translator.utils.SpeechUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
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
        mKeywords.add("对话");
        mKeywords.add("同声");
        mKeywords.add("图片");
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
        SpeechManager.getInstance().stopSpeechSynthesizing();
    }

    private void startHintSynthesizing() {
        BaseSynthesizerListener listener = new BaseSynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                doOnSynthesizeCompleted(error);
            }
        };
        SpeechManager.getInstance().synthesizeSpeech(WELCOME_TEXT, listener);
    }

    // 合成结束回调
    private void doOnSynthesizeCompleted(SpeechError error) {
        KLog.e(TAG, "" + error.getErrorDescription());
        //开始识别
        startKeywordRecognizing();
    }

    // 识别相关
    private void stopSpeechRecognizing() {
        SpeechManager.getInstance().stopSpeechRecognizing();
    }

    private void startKeywordRecognizing() {
        BaseRecognizerListener listener = new BaseRecognizerListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                doOnKeywordResult(recognizerResult, isLast);
            }
        };
        SpeechManager.getInstance().recognizeChinese(listener);
    }

    // 识别结果回调
    private void doOnKeywordResult(RecognizerResult recognizerResult, boolean isLast) {
        String json = recognizerResult.getResultString();
        String ret = SpeechUtil.parseJsonResult(json);

        KLog.d(TAG, "keyword: res >> " + ret);
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

    private void processKeyword(int index) {
        switch (index) {
            case 0:
                ToastUtil.showToastShort(this, "你好，进入对话翻译");
                startConversation();
                break;
            case 1:
                ToastUtil.showToastShort(this, "你好，进入同声翻译，请说开始");
                startSimultaneous();
                break;
            case 2:
                ToastUtil.showToastShort(this, "你好，进入图片翻译");
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
        if (NetworkUtil.isOnline()) {
            startHintSynthesizing();
        } else {
            ToastUtil.showToastShort(this, "请连接网络");
        }
//            mIsFirstOpen = false;
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopSpeechSynthesizing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SpeechManager.getInstance().unInit();
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
