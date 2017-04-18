package com.example.qkx.translator.ui.meeting.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;


import com.example.qkx.translator.MyApp;
import com.example.qkx.translator.R;
import com.example.qkx.translator.ui.meeting.detail.MeetingDetailActivity;
import com.example.qkx.translator.ui.meeting.register.MeetingRegisterActivity;
import com.example.qkx.translator.utils.DebugLog;
import com.example.qkx.translator.utils.ToastUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MeetingLoginActivity extends AppCompatActivity {

    @Bind(R.id.edt_username)
    EditText mEditUsername;
    @Bind(R.id.edt_password)
    EditText mEditPassword;

    @Bind(R.id.btn_login)
    Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (EMClient.getInstance().isLoggedInBefore()) {
            EMClient.getInstance().logout(true);
        }
    }

    @OnClick(R.id.btn_register)
    void register() {
        Intent intent = new Intent(this, MeetingRegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_login)
    void login() {
        setLoginButton(false);

        String username = mEditUsername.getText().toString();
        String password = mEditPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            DebugLog.e("username is empty!");
            ToastUtil.showToastShort(MyApp.getAppInstance(), "username is empty!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            DebugLog.e("password is empty!");
            ToastUtil.showToastShort(MyApp.getAppInstance(), "password is empty!");
            return;
        }

        //
        doLogin(username, password);
    }

    private void doLogin(String username, String password) {
        EMClient.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DebugLog.d("登录聊天服务器成功！");
                        ToastUtil.showToastShort(MyApp.getAppInstance(), "登录聊天服务器成功！");
                        setLoginButton(true);
                    }
                });

                startMainPage();
                finish();
            }

            @Override
            public void onError(int i, final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DebugLog.d("登录聊天服务器失败！");
                        ToastUtil.showToastShort(MyApp.getAppInstance(), "登录聊天服务器失败！" + s);
                        setLoginButton(true);
                    }
                });
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    private void setLoginButton(boolean enable) {
        mBtnLogin.setEnabled(enable);
    }

    private void startMainPage() {
//        Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, MeetingDetailActivity.class);
        startActivity(intent);
    }
}
