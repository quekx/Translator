package com.example.qkx.translator.ui.simultaneous;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.R;
import com.example.qkx.translator.config.ConfigManager;
import com.example.qkx.translator.data.ResultBean;
import com.example.qkx.translator.rest.RestSource;
import com.example.qkx.translator.ui.ResultCallback;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.FileUtil;
import com.example.qkx.translator.utils.SpeechUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SynthesizerListener;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SimultaneousActivity extends BaseDetailActivity {
    private static final String TAG = SimultaneousActivity.class.getSimpleName();
    private static final String DIVIDER = "--------------------------------------------";

    private static final int MODE_CH = 0;
    private static final int MODE_EN = 1;
    private int mCurrentMode = MODE_CH;

    private Set<Character> mCharFilter; // 处理标点符号

    @Bind(R.id.btn_start_speak_syc)
    Button mBtnStartSpeak;
    @Bind(R.id.btn_stop_speak_syc)
    Button mBtnStopSpeak;
    @Bind(R.id.btn_switch)
    Button mBtnSwitch;

    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private SpeechRecognizer mIat;
    private InitListener mInitListener;
    private RestSource mRestSource;

    // 目录路径
    private String mSycRecordDirPath = null;
    // 文本文件路径
    private String mSycRecordTextPath = null;

    @Bind(R.id.tv_res_syc)
    TextView mTvResSyc;
    //    @Bind(R.id.tv_syc_record_hint)
//    TextView mTvSycRecordHint;
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
        FileUtil.addStringToFile(String.format("%s\n%s\n\n", src, dst),
                mSycRecordTextPath);
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
//        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + Constants.APPID);
        this.mInitListener = new InitListener() {
            public void onInit(int code) {
                KLog.d(TAG, "SpeechRecognizer init() code = " + code);
                if (code != 0) {
                    showTip("初始化失败，错误码：" + code);
                }
            }
        };
        this.mIat = SpeechRecognizer.createRecognizer(this, this.mInitListener);
    }

    // 添加缺少的标点
    private String processDst(String origin, String dst) {
        if (dst == null || dst.length() == 0) return "";
        if (origin == null || origin.length() == 0) return dst;

        if (!mCharFilter.contains(origin.charAt(origin.length() - 1)) &&
                !mCharFilter.contains(dst.charAt(0))) return String.format(",%s", dst);

        return dst;
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

    private void recognizeChinese() {
        recognizeSpeechSyc(new ResultCallback() {
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
                        if (mSycRecordTextPath != null) {
                            addSycStringToFile(src, dst);
                        }
                    }
                });
            }
        }, "zh_cn");
    }

    private void recognizeEnglish() {
        recognizeSpeechSyc(new ResultCallback() {
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
                        if (mSycRecordTextPath != null) {
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
    private void recognizeSpeechSyc(final ResultCallback callback, String language) {
        // 场景
        mIat.setParameter(SpeechConstant.DOMAIN, ConfigManager.getInstance().getDomain());

        mIat.setParameter(SpeechConstant.LANGUAGE, language);
        if (language.equals("zh_cn")) {
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        } else {
            mIat.setParameter(SpeechConstant.ACCENT, null);
        }
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "30000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "30000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
//        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//
//        if (mSycRecordDirPath != null) {
//            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//                    String.format("%s/%s.wav", mSycRecordDirPath, FileUtil.getCurrentTime()));
//        } else {
//            mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//                    Environment.getExternalStorageDirectory() + "/msc/iat.wav");
//        }
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, null);

        int ret = mIat.startListening(new com.iflytek.cloud.RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
                saveAudioBytesToFile(bytes);
            }

            @Override
            public void onBeginOfSpeech() {
                KLog.d(TAG, "onBeginOfSpeech");
                setSpeakButtonEnabled(true);
            }

            // 主动停止时不会回调
            @Override
            public void onEndOfSpeech() {
                KLog.d(TAG, "onEndOfSpeech");
//                setSpeakButtonEnabled(false);

                // 停止记录
//                stopSycRecord();

                // 超时一分钟停止时，开始下一段识别
                speak();
            }

            @Override
            public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean isLast) {
                String json = recognizerResult.getResultString();
                String ret = SpeechUtil.parseJsonResult(json);

                KLog.d(TAG, "res >> " + ret);
                callback.onProcessResult(ret);
            }

            @Override
            public void onError(SpeechError speechError) {
                String dep = speechError.getPlainDescription(true);
                KLog.d(TAG, "speech error is " + dep);
//                showTip(dep);

//                stopSpeak();
                setSpeakButtonEnabled(false);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
//            showTip("请开始说话");
            KLog.d(TAG, "没有错误，请开始说话");
        }
    }

    private String mAudioPath = null;

    private void saveAudioBytesToFile(byte[] bytes) {
        if (mAudioPath == null) return;

        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(mAudioPath), true);
            os.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    //    @OnClick(R.id.btn_syc_start_record)
    void startSycRecord() {
        String date = FileUtil.getCurrentTime();
        mSycRecordDirPath = Environment.getExternalStorageDirectory() + "/record/同声翻译/" + date;
        KLog.d(TAG, "mSycRecordDirPath >> " + mSycRecordDirPath);
        File dir = new File(mSycRecordDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // audio path
        mAudioPath = String.format("%s/%s.pcm", mSycRecordDirPath, date);

        mSycRecordTextPath = String.format("%s/%s.txt", mSycRecordDirPath, date);
        FileUtil.addStringToFile(String.format("文本创建于%s\n%s\n\n", date, DIVIDER),
                mSycRecordTextPath);
//        mTvSycRecordHint.setText(String.format("开始记录，记录保存至目录%s", mSycRecordDirPath));
        ToastUtil.showToastShort(this, String.format("开始记录，记录保存至目录%s", mSycRecordDirPath));

//        setRecordButtonEnabled(true);
    }

    private void speak() {
        KLog.d(TAG, "speak start");
        switch (mCurrentMode) {
            case MODE_CH:
                recognizeChinese();
                break;
            case MODE_EN:
                recognizeEnglish();
                break;
        }
    }

    @OnClick(R.id.btn_start_speak_syc)
    void startSpeak() {
        mTvResSyc.setText("");
        mTvTranslationSyc.setText("");
        showTip("请开始说话");
        startSycRecord();

        speak();
    }

    //    @OnClick(R.id.btn_syc_stop_record)
    void stopSycRecord() {
        if (mSycRecordDirPath != null) {
//            ToastUtil.showToastShort(this, String.format("停止记录，记录保存至目录%s", mSycRecordDirPath));
            mSycRecordDirPath = null;
            mSycRecordTextPath = null;

            savePcmAsWav();
            mAudioPath = null;
        }
//        setRecordButtonEnabled(false);
    }

    private void savePcmAsWav() {
        if (mAudioPath == null) return;

        String newAudioPath = mAudioPath.replace("pcm", "wav");
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(mAudioPath);
            out = new FileOutputStream(newAudioPath);

            long totalAudioLen = in.getChannel().size();
            long totalDataLen = totalAudioLen + 36;
            long longSampleRate = 16000L;
            int channels = 1;
            long byteRate = 16 * longSampleRate * channels / 8;
            addHeaderToWavFile(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addHeaderToWavFile(FileOutputStream out, long totalAudioLen, long totalDataLen,
                                    long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    @OnClick(R.id.btn_stop_speak_syc)
    void stopSpeak() {
        if (mIat != null && mIat.isListening()) {
            mIat.stopListening();

            stopSycRecord();
        }
        setSpeakButtonEnabled(false);
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

    @OnClick(R.id.btn_take_photo)
    void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (mSycRecordDirPath != null) {
            String filePath = String.format("%s/%s.png", mSycRecordDirPath, FileUtil.getCurrentTime());
            Uri uri = Uri.fromFile(new File(filePath));

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("return-data", false);
        }
        startActivity(intent);
    }

    class MySynthesizerListener implements SynthesizerListener {
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
