package com.example.qkx.translator.ui.meeting.register;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.example.qkx.translator.MyApp;
import com.example.qkx.translator.R;
import com.example.qkx.translator.utils.DebugLog;
import com.example.qkx.translator.utils.ToastUtil;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MeetingRegisterActivity extends AppCompatActivity {

    @Bind(R.id.edt_username)
    EditText mEditUsername;
    @Bind(R.id.edt_password)
    EditText mEditPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_register)
    void register() {
        DebugLog.d("register click!");
        final String username = mEditUsername.getText().toString();
        final String password = mEditPassword.getText().toString();

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

        doRegister(username, password);
    }

    private void doRegister(final String username, final String password) {
        new Thread() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(username, password);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    DebugLog.e("register::: register failed!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToastShort(MyApp.getAppInstance(), "register failed!");
                        }
                    });
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToastShort(MyApp.getAppInstance(), "register success!");
                    }
                });
                finish();
            }
        }.start();
    }

}
