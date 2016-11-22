package com.example.qkx.translator.ui;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.R;
import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.ui.base.BaseActivity;
import com.example.qkx.translator.utils.FileUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.socks.library.KLog;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SimultaneousActivity extends BaseActivity {
    private static final String DIVIDER = "--------------------------------------------";
    private static final int MODE_CH = 0;
    private static final int MODE_EN = 1;
    private static final String TAG = SimultaneousActivity.class.getSimpleName();
    private Set<Character> mCharFilter;
    @Bind(R.id.btn_syc_start_record)
    Button mBtnStartRecord;
    @Bind(R.id.btn_start_speak_syc)
    Button mBtnStartSpeak;
    @Bind(R.id.btn_syc_stop_record)
    Button mBtnStopRecord;
    @Bind(R.id.btn_stop_speak_syc)
    Button mBtnStopSpeak;
    @Bind(R.id.btn_switch)
    Button mBtnSwitch;
    private int mCurrentMode = 0;
    private String mEngineType = "cloud";
    private SpeechRecognizer mIat;
    private InitListener mInitListener;
    private RestSource mRestSource;
    private String mSycRecordPath = null;
    @Bind(R.id.tv_res_syc)
    TextView mTvResSyc;
    @Bind(R.id.tv_syc_record_hint)
    TextView mTvSycRecordHint;
    @Bind(R.id.tv_translation_syc)
    TextView mTvTranslationSyc;

    // 记录时去除开始的标点
    private void addSycStringToFile(String src, String dst) {
        if (src.length() == 0 || dst.length() == 0) return;

        if (mCharFilter.contains(src.charAt(0))) {
            src = src.substring(1);
        }
        if (mCharFilter.contains(dst.charAt(0))) {
            dst = dst.substring(1);
        }
        FileUtil.addStringToFile(String.format("%s\n%s\n%s\n", src, dst, DIVIDER),
                mSycRecordPath);
    }

    private void init() {
        mRestSource = RestSource.getInstance();

        mCharFilter = new HashSet<>();
        mCharFilter.add(',');
        mCharFilter.add('?');
        mCharFilter.add('.');
        mCharFilter.add('!');
        mCharFilter.add('，');
        mCharFilter.add('？');
        mCharFilter.add('。');
        mCharFilter.add('！');
    }

    private void initStt() {
        SpeechUtility.createUtility(this, "appid=56b0105c");
        this.mInitListener = new InitListener() {
            public void onInit(int code) {
                Log.d(SimultaneousActivity.TAG, "SpeechRecognizer init() code = " + code);
                if (code != 0) {
                    SimultaneousActivity.this.showTip("初始化失败，错误码：" + code);
                }
            }
        };
        this.mIat = SpeechRecognizer.createRecognizer(this, this.mInitListener);
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

    // 添加缺少的标点
    private String processDst(String origin, String dst) {
        if (dst == null || dst.length() == 0) return "";
        if (origin == null ||origin.length() == 0) return dst;

        if (!mCharFilter.contains(origin.charAt(origin.length() - 1)) &&
                !mCharFilter.contains(dst.charAt(0))) return String.format(",%s", dst);

        return dst;
    }

    private void setRecordButtonEnabled(boolean isRecording) {
        if (isRecording) {
            if (mBtnStartRecord.isEnabled()) {
                mBtnStartRecord.setEnabled(false);
            }
            if (!mBtnStopRecord.isEnabled()) {
                mBtnStopRecord.setEnabled(true);
            }
        } else {
            if (!mBtnStartRecord.isEnabled()) {
                mBtnStartRecord.setEnabled(true);
            }
            if (mBtnStopRecord.isEnabled()) {
                mBtnStopRecord.setEnabled(false);
            }
        }
    }

    private void setSpeakButtonEnabled(boolean isSpeaking) {
        if (isSpeaking) {
            if (mBtnStartSpeak.isEnabled()) {
                mBtnStartSpeak.setEnabled(false);
            }
            if (!mBtnStopSpeak.isEnabled()) {
                mBtnStopSpeak.setEnabled(true);
            }
        } else {
            if (!mBtnStartSpeak.isEnabled()) {
                mBtnStartSpeak.setEnabled(true);
            }
            if (mBtnStopSpeak.isEnabled()) {
                mBtnStopSpeak.setEnabled(false);
            }
        }
    }

    private void showTip(String str) {
        ToastUtil.showToastShort(this, str);
    }

    private void speechToTextCh() {
        speechToTextSyc(new RetCallback() {
            @Override
            public void onProcessResult(String result) {
                String str = mTvResSyc.getText().toString();
                mTvResSyc.setText(String.format("%s%s", str, result));

                mRestSource.queryEn(result, new RestSource.TranslateCallback() {
                    @Override
                    public void onProcessResult(ResultBean resultBean) {
                        if ((resultBean.trans_result == null) ||
                                (resultBean.trans_result.size() == 0)) return;

                        ResultBean.TransResult transResult = resultBean.trans_result.get(0);
                        String src = transResult.src;
                        String dst = transResult.dst;

                        String origin = mTvTranslationSyc.getText().toString();
                        dst = processDst(origin, dst);
                        mTvTranslationSyc.setText(String.format("%s%s", origin, dst));

                        // 文件
                        if (mSycRecordPath != null) {
                            addSycStringToFile(src, dst);
                        }
                    }
                });
            }
        }, "zh_cn");
    }

    private void speechToTextEn() {
        speechToTextSyc(new RetCallback() {
            @Override
            public void onProcessResult(String result) {
                String str = mTvResSyc.getText().toString();
                mTvResSyc.setText(String.format("%s%s", str, result));

                mRestSource.queryCh(result, new RestSource.TranslateCallback() {
                    @Override
                    public void onProcessResult(ResultBean resultBean) {
                        if ((resultBean.trans_result == null) ||
                                (resultBean.trans_result.size() == 0)) return;

                        ResultBean.TransResult transResult = resultBean.trans_result.get(0);
                        String src = transResult.src;
                        String dst = transResult.dst;

                        String origin = mTvTranslationSyc.getText().toString();
                        dst = processDst(origin, dst);
                        mTvTranslationSyc.setText(String.format("%s%s", origin, dst));

                        // 文件
                        if (mSycRecordPath != null) {
                            addSycStringToFile(src, dst);
                        }
                    }
                });
            }
        }, "en_us");
    }

    /**
     * 分段文本处理
     *
     * @param callback 结果回调接口
     * @param language 语言种类
     */
    private void speechToTextSyc(final RetCallback callback, String language) {
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "30000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");

        int ret = mIat.startListening(new com.iflytek.cloud.RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                KLog.i(TAG, "onBeginOfSpeech");
                setSpeakButtonEnabled(true);
            }

            @Override
            public void onEndOfSpeech() {
                KLog.i(TAG, "onEndOfSpeech");
                setSpeakButtonEnabled(false);
            }

            @Override
            public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean isLast) {
                String json = recognizerResult.getResultString();
                String ret = parseJson(json);

                Log.d(TAG, "res >> " + ret);
                callback.onProcessResult(ret);
            }

            @Override
            public void onError(SpeechError speechError) {
                String dep = speechError.getPlainDescription(true);
                Log.d(TAG, "speech error is " + dep);
                showTip(dep);

                setSpeakButtonEnabled(false);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
            showTip("请开始说话");
        }
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        ButterKnife.bind(this);
        setTitle(getResources().getString(R.string.title_simultaneous));
        init();
        initStt();
    }

    protected int provideContentViewId() {
        return R.layout.activity_simultaneous;
    }

    @OnClick(R.id.btn_start_speak_syc)
    void speakSyc() {
        mTvResSyc.setText("");
        mTvTranslationSyc.setText("");
        switch (mCurrentMode) {
            case MODE_CH:
                speechToTextCh();
                break;
            case MODE_EN:
                speechToTextEn();
                break;
        }
    }

    @OnClick(R.id.btn_syc_start_record)
    void startSycRecord() {
        String str = Environment.getExternalStorageDirectory().getPath() + "/record/同声翻译";
        File dir = new File(str);
        if (! dir.exists()) {
            dir.mkdirs();
        }

        String date = FileUtil.getCurrentDate();
        mSycRecordPath = str + "/" + date + ".txt";
        FileUtil.addStringToFile(String.format("文件创建于%s\n%s\n\n", date, DIVIDER),
                mSycRecordPath);
        mTvSycRecordHint.setText(String.format("开始记录，记录保存至%s", mSycRecordPath));

        setRecordButtonEnabled(true);
    }

    @OnClick(R.id.btn_stop_speak_syc)
    void stopSpeak() {
        if (this.mIat != null) {
            this.mIat.stopListening();
        }
        setSpeakButtonEnabled(false);
    }

    @OnClick(R.id.btn_syc_stop_record)
    void stopSycRecord() {
        if (this.mSycRecordPath != null) {
            this.mTvSycRecordHint.setText(String.format("停止记录，记录保存至%s", new Object[]{this.mSycRecordPath}));
            this.mSycRecordPath = null;
        }
        setRecordButtonEnabled(false);
    }

    @OnClick(R.id.btn_switch)
    void switchMode() {
        switch (mCurrentMode) {
            case MODE_CH:
                mCurrentMode = MODE_EN;
                showTip("英文模式");
                mBtnSwitch.setText(R.string.switchover_en);
                break;
            case MODE_EN:
                mCurrentMode = MODE_CH;
                showTip("中文模式");
                mBtnSwitch.setText(R.string.switchover_ch);
        }
    }

    class MySynthesizerListener
            implements SynthesizerListener {
        MySynthesizerListener() {
        }

        public void onBufferProgress(int paramInt1, int paramInt2, int paramInt3, String paramString) {
        }

        public void onCompleted(SpeechError paramSpeechError) {
        }

        public void onEvent(int paramInt1, int paramInt2, int paramInt3, Bundle paramBundle) {
        }

        public void onSpeakBegin() {
        }

        public void onSpeakPaused() {
        }

        public void onSpeakProgress(int paramInt1, int paramInt2, int paramInt3) {
        }

        public void onSpeakResumed() {
        }
    }
}
