package com.example.qkx.translator.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.qkx.translator.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.img_conversation)
    void startConversation() {
        startActivity(new Intent(this, ConversationActivity.class));
    }

    @OnClick(R.id.img_orc)
    void startOrc() {
        startActivity(new Intent(this, OrcActivity.class));
    }

    @OnClick(R.id.img_setting)
    void startSetting() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    @OnClick(R.id.img_simultaneous)
    void startSimultaneous() {
        startActivity(new Intent(this, SimultaneousActivity.class));
    }
}
