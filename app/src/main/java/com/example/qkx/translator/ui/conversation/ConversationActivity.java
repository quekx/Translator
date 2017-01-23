package com.example.qkx.translator.ui.conversation;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.R;
import com.example.qkx.translator.config.ConfigManager;
import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.rest.RestSource.TranslateCallback;
import com.example.qkx.translator.speech.SpeechManager;
import com.example.qkx.translator.ui.ResultCallback;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.FileUtil;
import com.example.qkx.translator.utils.SpeechUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.socks.library.KLog;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ConversationActivity extends BaseDetailActivity {
    private static final String DIVIDER = "--------------------------------------------";
    private static final String TAG = ConversationActivity.class.getSimpleName();
    @Bind(R.id.btn_rt_start_record)
    Button mBtnRtStartRecord;
    @Bind(R.id.btn_rt_stop_record)
    Button mBtnRtStopRecord;
    private StringBuffer mBuffer;
    private String mDefaultAvdBosMillis;
    private String mDefaultAvdEosMillis;
    private String mDefaultName;
    private String mDefaultSpeed;
    private String mDefaultVolume;
    private String mEngineType = "cloud";

    // stt
    private SpeechRecognizer mIat;
    private InitListener mInitListener;

    private String mRtRecordTextPath = null;
    private String mRtRecordDirPath = null;

    private SpeechSynthesizer mTts;
    @Bind(R.id.tv_rt)
    TextView tvRt;
    @Bind(R.id.tv_rt_en)
    TextView tvRtEn;
    @Bind(R.id.tv_rt_record_hint)
    TextView tvRtRecordHint;

    private void init() {
        mBuffer = new StringBuffer();

        this.mDefaultAvdBosMillis = ConfigManager.getInstance().getAvdBos();
        this.mDefaultAvdEosMillis = ConfigManager.getInstance().getAvdEos();
        this.mDefaultName = ConfigManager.getInstance().getVoiceName();
        this.mDefaultSpeed = ConfigManager.getInstance().getVoiceSpeed();
        this.mDefaultVolume = ConfigManager.getInstance().getVoiceVolume();
    }

    private void initStt() {
//        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + Constants.APPID);
        mInitListener = new InitListener() {
            public void onInit(int code) {
                KLog.d(TAG, "SpeechRecognizer init() code = " + code);
                if (code != 0) {
                    showTip("初始化失败，错误码：" + code);
                }
            }
        };
        mIat = SpeechRecognizer.createRecognizer(this, this.mInitListener);
    }

    private void initTts() {
        this.mTts = SpeechSynthesizer.createSynthesizer(this, null);
    }

    private void read(String str) {
        read(str, this.mDefaultName, this.mDefaultSpeed, this.mDefaultVolume);
    }

    private void read(String str, String voiceName, String speed, String volume) {
        SpeechManager.getInstance().synthesizeSpeech(str, voiceName, speed, volume);
    }

    private void setRecordButtonEnabled(boolean isRecording) {
        if (isRecording) {
            if (mBtnRtStartRecord.isEnabled()) {
                mBtnRtStartRecord.setEnabled(false);
            }
            if (!mBtnRtStopRecord.isEnabled()) {
                mBtnRtStopRecord.setEnabled(true);
            }
        } else {
            if (!mBtnRtStartRecord.isEnabled()) {
                mBtnRtStartRecord.setEnabled(true);
            }
            if (mBtnRtStopRecord.isEnabled()) {
                mBtnRtStopRecord.setEnabled(false);
            }
        }
    }

    private void showTip(String str) {
        ToastUtil.showToastShort(this, str);
    }

    private void speechToText(ResultCallback callback, String str) {
        speechToText(callback, str, this.mDefaultAvdBosMillis, this.mDefaultAvdEosMillis);
    }

    /**
     * 识别结束后整段文本处理
     *
     * @param callback     结果回调接口
     * @param language     语言种类
     * @param avdBosMillis 说话未开始时的超时时间
     * @param avdEosMillis 说话停止后的超时时间
     */
    private void speechToText(final ResultCallback callback, String language, String avdBosMillis, String avdEosMillis) {
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, avdBosMillis);

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, avdEosMillis);

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");

//        if (mRtRecordDirPath != null) {
//            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//                    String.format("%s/%s.wav", mRtRecordDirPath, FileUtil.getCurrentTime()));
//        } else {
//            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//                    Environment.getExternalStorageDirectory() + "/msc/iat.wav");
//        }
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, null);


        int ret = mIat.startListening(new com.iflytek.cloud.RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
                if (mAudioPath != null) {
                    FileUtil.saveBytesToFile(mAudioPath, bytes);
                }
            }

            @Override
            public void onBeginOfSpeech() {
                showTip("speech start!");

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                String json = recognizerResult.getResultString();
//                KLog.d(TAG, "speech result >> " + json);
                String ret = SpeechUtil.parseJsonResult(json);

                KLog.d(TAG, "onResult: res >> " + ret);

                mBuffer.append(ret);
                if (isLast) {
                    callback.onProcessResult(mBuffer.toString());
                    mBuffer.delete(0, mBuffer.length());
                }
            }

            @Override
            public void onError(SpeechError speechError) {
//                String dep = speechError.getPlainDescription(true);
                String dep = speechError.getErrorDescription();
                KLog.d(TAG, "onError: speech error is " + dep);
                showTip(dep);
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

    private void speechToTextCh(ResultCallback callback) {
        speechToText(callback, "zh_cn");
    }

    private void speechToTextEn(ResultCallback callback) {
        speechToText(callback, "en_us");
    }

    private void translateCh(String q, TranslateCallback callback) {
        RestSource.getInstance().queryCh(q, callback);
    }

    private void translateEn(String q, TranslateCallback callback) {
        RestSource.getInstance().queryEn(q, callback);
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        ButterKnife.bind(this);
        setTitle(getResources().getString(R.string.title_conversation));
        init();
        initStt();
        initTts();
    }

    protected int provideContentViewId() {
        return R.layout.activity_conversation;
    }

    @OnClick(R.id.btn_rt)
    void rtTrans() {
        speechToTextCh(new ResultCallback() {
            public void onProcessResult(String result) {
                tvRt.setText(result);
                translateEn(result, new RestSource.TranslateCallback() {
                    public void onProcessResult(ResultBean resultBean) {
                        String str = tvRt.getText().toString();
                        String dst = resultBean.trans_result.get(0).dst;
                        str = str + '\n' + dst;
                        tvRt.setText(str);
                        read(dst);
                        if (mRtRecordTextPath != null) {
                            FileUtil.addStringToFile(String.format("%s\n%s\n", str, DIVIDER),
                                    mRtRecordTextPath);
                        }
                    }
                });
            }
        });
    }

    @OnClick(R.id.btn_rt_en)
    void rtTransEn() {
        speechToTextEn(new ResultCallback() {
            public void onProcessResult(String result) {
                tvRtEn.setText(result);
                translateCh(result, new RestSource.TranslateCallback() {
                    public void onProcessResult(ResultBean resultBean) {
                        String str = tvRtEn.getText().toString();
                        String dst = resultBean.trans_result.get(0).dst;
                        str = str + '\n' + dst;
                        tvRtEn.setText(str);
                        read(dst);
                        if (mRtRecordTextPath != null) {
                            FileUtil.addStringToFile(String.format("%s\n%s\n", str, DIVIDER),
                                    mRtRecordTextPath);
                        }
                    }
                });
            }
        });
    }

    private String mAudioPath = null;

    @OnClick(R.id.btn_rt_start_record)
    void startRtRecord() {
        String date = FileUtil.getCurrentTime();
        mRtRecordDirPath = Environment.getExternalStorageDirectory().getPath() + "/record/对话翻译/" + date;
        File dir = new File(mRtRecordDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // text path
        mRtRecordTextPath = mRtRecordDirPath + "/" + date + ".txt";
        // audio path
        mAudioPath = mRtRecordDirPath + "/" + date + ".pcm";

        FileUtil.addStringToFile(String.format("文件创建于%s\n%s\n\n", date, DIVIDER),
                mRtRecordTextPath);
//        tvRtRecordHint.setText(String.format("开始记录，记录保存至%s", mRtRecordDirPath));
        tvRtRecordHint.setText(String.format("开始记录，记录保存至根目录%s", "/record/对话翻译/" + date));
        setRecordButtonEnabled(true);
    }

    @OnClick(R.id.btn_rt_stop_record)
    void stopRtRecord() {
        if (mRtRecordDirPath != null) {
            tvRtRecordHint.setText(String.format("停止记录，记录保存至%s", mRtRecordDirPath));
            mRtRecordDirPath = null;
            mRtRecordTextPath = null;

            if (mAudioPath != null) {
                FileUtil.savePcmAsWav(mAudioPath);
                mAudioPath = null;
            }
        }
        setRecordButtonEnabled(false);
    }
}
