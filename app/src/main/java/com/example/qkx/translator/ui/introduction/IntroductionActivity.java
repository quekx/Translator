package com.example.qkx.translator.ui.introduction;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.qkx.translator.R;
import com.example.qkx.translator.ui.base.BaseDetailActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by qkx on 17/4/27.
 */

public class IntroductionActivity extends BaseDetailActivity {

    private final static String hint = "<html>\n" +
            "\n" +
            "<body>\n" +
            "<h2>翻译助手使用说明</h2>\n" +
            "<p>主要用于中英互译。</p>\n" +
            "<h3>对话翻译</h3>\n" +
            "<p>语音进入或直接点击进入对话翻译。进入界面后，可看到可操作的三个按钮和一个灰色“停止记录”按钮。中文   英文可将您所说的中文转化成英文显示，英文   中文可以将英文转换成中文显示；点击“开始记录”，可以记录你和对方的谈话；点击“停止记录”后，生成.txt文件并保存于根目录/record/对话翻译。</p>\n" +
            "<h3>同声翻译</h3>\n" +
            "<p>语音进入或直接点击进入同声翻译，显示“切换至英文翻译”时，点击“开始翻译”，即可将您所述的内容转译成中文，期间有需要照片备注时可选择“拍照记录”，点击“停止翻译”后，语音文件及图片信息转存至根目录/record/同声翻译。点击“切换至中文翻译”，可将您所述转译为英文，使用方法同上。\n" +
            "</p>\n" +
            "<h3>图片翻译</h3>\n" +
            "<p>“相册裁减”和“拍照裁剪”实现对照片的裁剪功能，可裁剪出图片常用比例，并存于根目录/record/imgCrop，点击“开始识别”可以识别裁剪后图片上面的英文，使用“翻译”按钮则可以将其转换为中文显示在界面中；也可以通过“相册”和“拍照”选取图片进行识别和翻译，此时图片存于根目录/record/img。</p>\n" +
            "<h3>会议模式</h3>\n" +
            "<p>会议开始前，相关人员进入会议模式，新用户使用会议模式要注册，老用户直接选择登录即可。登录后可收到他人的对话，也可点击“说话”录入自己的意见（语音或拼写），说完后点击“发送”即可输出显示。对话内容会依据手机的默认语言种进行显示，例如我手机默认语言为英文，那么我输出为英文，其他人的对话也会被转化成英文后再由我接收。</p>\n" +
            "<h3>参数设置</h3>\n" +
            "<p><h4>对话翻译设置</h4>\n" +
            "静音超时：在系统提示输入语音至提示未有语音输入的间隔。\n" +
            "<br>说话超时：默认超过时间一秒后自动停止输入。\n" +
            "<br><h4>声控设置</h4>\n" +
            "声控开关：提示语音输入，可选择关闭。\n" +
            "<br><h4>发音设置</h4>\n" +
            "设置声音：有青年女声、青年男声、女声播音员、中年男声四个选项。\n" +
            "<br>设置语速：随着数值的增长，语速越来越慢，可自行调整。\n" +
            "<br>设置音量：随着数值的增长，音量越来越大，可自行调整。\n" +
            "<br>测试发音：用于对正在设置或已经保存的发音设置进行测试，以便于您对发音的调整。</p>\n" +
            "\n" +
            "</body>\n" +
            "</html>\n";

    @Bind(R.id.introduction_container)
    LinearLayout mIntroductionContainer;

    @Bind(R.id.tv_introduction)
    TextView mTvIntroduction;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_introduction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mTvIntroduction.setText(Html.fromHtml(hint));
    }
}
