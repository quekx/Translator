package com.example.qkx.translator.ui.conversation;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.Constants;
import com.example.qkx.translator.R;
import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.rest.RestSource.TranslateCallback;
import com.example.qkx.translator.ui.ResultCallback;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.FileUtil;
import com.example.qkx.translator.utils.PreferenceUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

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

    private SynthesizerListener mSynListener;
    private SpeechSynthesizer mTts;
    @Bind(R.id.tv_rt)
    TextView tvRt;
    @Bind(R.id.tv_rt_en)
    TextView tvRtEn;
    @Bind(R.id.tv_rt_record_hint)
    TextView tvRtRecordHint;

    private void init() {
        this.mBuffer = new StringBuffer();
        this.mDefaultAvdBosMillis = PreferenceUtil.getString(this, Constants.KEY_AVD_BOS, "4000");
        this.mDefaultAvdEosMillis = PreferenceUtil.getString(this, Constants.KEY_AVD_EOS, "1000");
        this.mDefaultName = PreferenceUtil.getString(this, Constants.KEY_VOICE_NAME, "xiaoyan");
        this.mDefaultSpeed = PreferenceUtil.getString(this, Constants.KEY_VOICE_SPEED, "50");
        this.mDefaultVolume = PreferenceUtil.getString(this, Constants.KEY_VOICE_VOLUME, "80");
    }

    private void initStt() {
//        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + Constants.APPID);
        mInitListener = new InitListener() {
            public void onInit(int code) {
                Log.d(TAG, "SpeechRecognizer init() code = " + code);
                if (code != 0) {
                    showTip("初始化失败，错误码：" + code);
                }
            }
        };
        mIat = SpeechRecognizer.createRecognizer(this, this.mInitListener);
    }

    private void initTts() {
        this.mSynListener = new MySynthesizerListener();
        this.mTts = SpeechSynthesizer.createSynthesizer(this, null);
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

    private void read(String str) {
        read(str, this.mDefaultName, this.mDefaultSpeed, this.mDefaultVolume);
    }

    private void read(String str, String voiceName, String speed, String volume) {
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
//        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置,详见《MSC Reference Manual》SpeechSynthesizer 类
        //设置发音人(更多在线发音人,用户可参见 附录13.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, voiceName); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, speed);//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, volume);//设置音量,范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        // 仅支持保存为 pcm 和 wav 格式,如果不需要保存合成音频,注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //3.开始合成
//        mTts.startSpeaking("科大讯飞,让世界聆听我们的声音", mSynListener);
        mTts.startSpeaking(str, mSynListener);
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

        if (mRtRecordDirPath != null) {
            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                    String.format("%s/%s.wav", mRtRecordDirPath, FileUtil.getCurrentTime()));
        } else {
            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                    Environment.getExternalStorageDirectory() + "/msc/iat.wav");
        }


        int ret = mIat.startListening(new com.iflytek.cloud.RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

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
//                Log.d(TAG, "speech result >> " + json);
                String ret = parseJson(json);

                Log.d(TAG, "outcome >> " + ret);

                mBuffer.append(ret);
                if (isLast) {
                    callback.onProcessResult(mBuffer.toString());
                    mBuffer.delete(0, mBuffer.length());
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                String dep = speechError.getPlainDescription(true);
                Log.d(TAG, "speech error is " + dep);
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

    @OnClick(R.id.btn_rt_start_record)
    void startRtRecord() {
        mRtRecordDirPath = Environment.getExternalStorageDirectory().getPath() + "/record/对话翻译";
        File dir = new File(mRtRecordDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date = FileUtil.getCurrentTime();
        mRtRecordTextPath = mRtRecordDirPath + "/" + date + ".txt";
        FileUtil.addStringToFile(String.format("文件创建于%s\n%s\n\n", date, DIVIDER),
                mRtRecordTextPath);
        tvRtRecordHint.setText(String.format("开始记录，记录保存至%s", mRtRecordDirPath));
        setRecordButtonEnabled(true);
    }

    @OnClick(R.id.btn_rt_stop_record)
    void stopRtRecord() {
        if (mRtRecordDirPath != null) {
            tvRtRecordHint.setText(String.format("停止记录，记录保存至%s", mRtRecordTextPath));
            mRtRecordDirPath = null;
            mRtRecordTextPath = null;
        }
        setRecordButtonEnabled(false);
    }

    class MySynthesizerListener implements SynthesizerListener {
        MySynthesizerListener() {
        }

        //会话结束回调接口,没有错误时,error为null
        public void onCompleted(SpeechError error) {
        }

        //缓冲进度回调
        //percent为缓冲进度0~100,beginPos为缓冲音频在文本中开始位置,endPos表示缓冲音频在文本中结束位置,info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    }
}
