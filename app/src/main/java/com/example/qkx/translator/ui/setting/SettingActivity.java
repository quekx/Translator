package com.example.qkx.translator.ui.setting;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.qkx.translator.Constants;
import com.example.qkx.translator.R;
import com.example.qkx.translator.config.ConfigManager;
import com.example.qkx.translator.ui.base.BaseDetailActivity;
import com.example.qkx.translator.utils.PreferenceUtil;
import com.example.qkx.translator.utils.ToastUtil;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.socks.library.KLog;

public class SettingActivity extends BaseDetailActivity {
    private static final String TAG = SettingActivity.class.getSimpleName();

    @Bind(R.id.edt_test_voice)
    EditText edtTest;

    private String[] mBosTimeMillis = {"1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000"};
    private String[] mBosTimes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    private String mDefaultAvdBosMillis;
    private String mDefaultAvdEosMillis;
    private String mDefaultName;
    private String mDefaultSpeed;
    private String mDefaultVolume;
    private String mDefaultDomain;

    private String[] mEosTimeMillis = {"1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000"};
    private String[] mEosTimes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    private String[] mSpeeds = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};

    @Bind(R.id.spinner_bos)
    Spinner mSpinnerBos;
    @Bind(R.id.spinner_eos)
    Spinner mSpinnerEos;
    @Bind(R.id.spinner_name)
    Spinner mSpinnerName;
    @Bind(R.id.spinner_speed)
    Spinner mSpinnerSpeed;
    @Bind(R.id.spinner_volume)
    Spinner mSpinnerVolume;

    @Bind(R.id.checkbox_sound_control)
    CheckBox mCheckBoxSoundControl;

    private MySynthesizerListener mSynListener;
    private SpeechSynthesizer mTts;
    private String[] mVoiceDisplayNames = {"青年女声(小燕)", "青年男声(小峰)", "中年男声(老孙)", "女声播音员(小筠)"};
    private String[] mVoiceNames = {"xiaoyan", "xiaofeng", "vils", "aisjying"};
    private String[] mVolumes = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};

    private void init() {
        mDefaultName = ConfigManager.getInstance().getVoiceName();
        mDefaultSpeed = ConfigManager.getInstance().getVoiceSpeed();
        mDefaultVolume = ConfigManager.getInstance().getVoiceVolume();
        mDefaultAvdBosMillis = ConfigManager.getInstance().getAvdBos();
        mDefaultAvdEosMillis = ConfigManager.getInstance().getAvdEos();
        mDefaultDomain = ConfigManager.getInstance().getDomain();
    }

    private void initTts() {
        this.mSynListener = new MySynthesizerListener();
        this.mTts = SpeechSynthesizer.createSynthesizer(this, null);
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

    private void saveConfig() {
        PreferenceUtil.putString(this, Constants.KEY_VOICE_NAME, mDefaultName);
        PreferenceUtil.putString(this, Constants.KEY_VOICE_SPEED, mDefaultSpeed);
        PreferenceUtil.putString(this, Constants.KEY_VOICE_VOLUME, mDefaultVolume);
        PreferenceUtil.putString(this, Constants.KEY_AVD_BOS, mDefaultAvdBosMillis);
        PreferenceUtil.putString(this, Constants.KEY_AVD_EOS, mDefaultAvdEosMillis);
        PreferenceUtil.putString(this, Constants.KEY_DOMAIN, mDefaultDomain);
        PreferenceUtil.putBoolean(this, Constants.KEY_SOUND_CONTROL, mCheckBoxSoundControl.isChecked());

        ToastUtil.showToastShort(this, "保存成功");
        finish();
    }

    private void setupViews() {
        // 发音人
        ArrayAdapter<String> adapterVoiceName = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mVoiceDisplayNames);
        adapterVoiceName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerName.setAdapter(adapterVoiceName);
        mSpinnerName.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultName = mVoiceNames[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int index = getConfigIndex(mVoiceNames, Constants.KEY_VOICE_NAME);
        mSpinnerName.setSelection(index != -1 ? index : 0);

        // 语速
        ArrayAdapter<String> adapterSpeed = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mSpeeds);
        adapterSpeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSpeed.setAdapter(adapterSpeed);
        mSpinnerSpeed.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultSpeed = mSpeeds[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        index = getConfigIndex(mSpeeds, Constants.KEY_VOICE_SPEED);
        mSpinnerSpeed.setSelection(index != -1 ? index : 4);

        // 音量
        ArrayAdapter<String> adapterVolume = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mVolumes);
        adapterVolume.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerVolume.setAdapter(adapterVolume);
        mSpinnerVolume.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultVolume = mVolumes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        index = getConfigIndex(mVolumes, Constants.KEY_VOICE_VOLUME);
        mSpinnerVolume.setSelection(index != -1 ? index : 7);

        // 静音超时
        ArrayAdapter<String> adapterBos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mBosTimes);
        adapterBos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBos.setAdapter(adapterBos);
        mSpinnerBos.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultAvdBosMillis = mBosTimeMillis[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        index = getConfigIndex(mBosTimeMillis, Constants.KEY_AVD_BOS);
        mSpinnerBos.setSelection(index != -1 ? index : 3);

        // 说话超时
        ArrayAdapter<String> adapterEos = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mEosTimes);
        adapterEos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerEos.setAdapter(adapterEos);
        mSpinnerEos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultAvdEosMillis = mEosTimeMillis[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        index = getConfigIndex(mEosTimeMillis, Constants.KEY_AVD_EOS);
        mSpinnerEos.setSelection(index != -1 ? index : 0);

        mCheckBoxSoundControl.setChecked(ConfigManager.getInstance().isSoundControlOpen());
    }

    private int getConfigIndex(String[] values, String key) {
        String config = PreferenceUtil.getString(this, key);
        if (config == null) return -1;

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(config)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ButterKnife.bind(this);
        setTitle(getResources().getString(R.string.title_setting));
        init();
        initTts();
        setupViews();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveConfig();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected int provideContentViewId() {
        return R.layout.activity_setting;
    }

    @OnClick(R.id.btn_test_voice)
    void testVoice() {
        String str = this.edtTest.getText().toString();
        KLog.i(TAG, "test str >> " + str);
        if (str.length() != 0) {
            read(str, this.mDefaultName, this.mDefaultSpeed, this.mDefaultVolume);
        }
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

