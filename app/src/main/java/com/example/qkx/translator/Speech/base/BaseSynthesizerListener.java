package com.example.qkx.translator.speech.base;

import android.os.Bundle;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by qkx on 16/12/6.
 */

/**
 * 声音合成时的回调
 * 默认什么都不做
 * 根据需要重写相应方法
 */
public abstract class BaseSynthesizerListener implements SynthesizerListener {
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
