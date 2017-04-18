package com.example.qkx.hello;

/**
 * Created by qkx on 16/7/13.
 */
public class Constants {
    public static final String APPID = "56b0105c";

    //Baidu Translation Api
    public static final String appId = "20160713000025153";
    public static final String secret = "CW2ZYy9sHjSPzsaadZpe";
    public static final String baseUrl = "http://api.fanyi.baidu.com/api/trans/vip/translate/";

    //Request Code
    public static final int REQUEST_PHOTO_GALLERY_CROP = 0;
    public static final int REQUEST_PHOTO_GALLERY = 1;
    public static final int REQUEST_PHOTO_CAMERA_CROP = 2;
    public static final int REQUEST_PHOTO_CAMERA = 3;

    //SharePreference Key
    public static final String KEY_VOICE_NAME = "voice_name";
    public static final String KEY_VOICE_SPEED = "voice_speed";
    public static final String KEY_VOICE_VOLUME = "voice_volume";
    public static final String KEY_AVD_BOS = "avd_bos";
    public static final String KEY_AVD_EOS = "avd_eos";
    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_SOUND_CONTROL = "sound_control";

    // Config Default value
    public static final String VALUE_DEFAULT_VOICE_NAME = "xiaoyan"; // 发音人
    public static final String VALUE_DEFAULT_VOICE_SPEED = "50";     // 语速
    public static final String VALUE_DEFAULT_VOICE_VOLUME = "80";    // 音量
    public static final String VALUE_DEFAULT_AVD_BOS = "4000";       // 前端超时
    public static final String VALUE_DEFAULT_AVD_EOS = "1000";       // 后端超时
    public static final String VALUE_DEFAULT_DOMAIN = "iat";         // 应用领域，默认日常用语
}
