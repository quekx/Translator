package com.example.qkx.translator.ui.meeting.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.EditText;

import com.example.qkx.translator.R;
import com.example.qkx.translator.ui.meeting.GsonUtil;
import com.example.qkx.translator.utils.DebugLog;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qkx on 17/3/10.
 */

public class MeetingDetailActivity extends AppCompatActivity {

    private String mTestGroupId = "1488712573499";

    @Bind(R.id.recycleView)
    RecyclerView mRecyclerView;
    @Bind(R.id.edt_send_msg_detail)
    EditText mEditSendMsg;

    private MeetingDetailAdapter mAdapter;

    private String mCurrentLocale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        initViews();

        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);

        doJoinGroup();

        DebugLog.d("onCreate!");
    }

    private void doJoinGroup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().joinGroup(mTestGroupId);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    DebugLog.e("group id : " + mTestGroupId + " failed!");
                    return;
                }

                DebugLog.d("join group " + mTestGroupId + "success!");
            }
        }).start();
    }

    private void initViews() {
        mAdapter = new MeetingDetailAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentLocale = getResources().getConfiguration().locale.getLanguage();
        DebugLog.e("current locale >> " + mCurrentLocale);
        mAdapter.setLocale(mCurrentLocale);
        mAdapter.clear();
    }

    @OnClick(R.id.btn_send_msg_detail)
    void sendMsg() {
        String msg = mEditSendMsg.getText().toString();
        if (TextUtils.isEmpty(msg)) return;

        mEditSendMsg.setText(null);
        addMessageBean(new MeetingDetailMessage(msg, mCurrentLocale));

        msg = GsonUtil.toJson(mCurrentLocale, msg);
        EMMessage message = EMMessage.createTxtSendMessage(msg, mTestGroupId);
        message.setChatType(EMMessage.ChatType.GroupChat);
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    private void addMessageBean(final MeetingDetailMessage messageBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addData(messageBean);
            }
        });
    }

    private EMMessageListener mMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            DebugLog.d("onMessageReceived!");

            if (list == null || list.isEmpty()) return;

//            final EMMessage message = list.get(0);
            DebugLog.d("message list size :: " + list.size());
            for (EMMessage message : list) {
                new MeetingDetailMessage(message, mCurrentLocale, new MeetingAddListener() {
                    @Override
                    public void queryDelay(MeetingDetailMessage messageBean) {
                        addMessageBean(messageBean);
                    }

                    @Override
                    public void addInstant(MeetingDetailMessage messageBean) {
                        addMessageBean(messageBean);
                    }
                });
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageRead(List<EMMessage> list) {

        }

        @Override
        public void onMessageDelivered(List<EMMessage> list) {

        }

        @Override
        public void onMessageChanged(EMMessage emMessage, Object o) {

        }
    };
}
