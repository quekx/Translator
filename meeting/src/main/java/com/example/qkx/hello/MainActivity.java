package com.example.qkx.hello;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.qkx.hello.data.ResultBean;
import com.example.qkx.hello.model.MyMessage;
import com.example.qkx.hello.rest.RestSource;
import com.example.qkx.hello.utils.GsonUtil;
import com.example.qkx.hello.utils.MessageUtil;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by qkx on 17/3/5.
 */

public class MainActivity extends AppCompatActivity{

    private String mDefaultSrc = "zh";
    private String[] mSrc = {"zh", "en"};

    private String mTestGroupId = "1488712573499";

    private String mCurrentGroupId;

    @Bind(R.id.edt_create_group)
    EditText mEditCreateGroup;
    @Bind(R.id.edt_join_group)
    EditText mEditJoinGroup;
    @Bind(R.id.edt_send_msg)
    EditText mEditSendMsg;

    @Bind(R.id.tv_receive_msg)
    TextView mTvReceiveMsg;

    @Bind(R.id.spinner_src)
    Spinner mSpinnerSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        initView();

        doJoinGroup(mTestGroupId);
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    private void initView() {
        ArrayAdapter<String> adapterVoiceName = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mSrc);
        adapterVoiceName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSrc.setAdapter(adapterVoiceName);
        mSpinnerSrc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDefaultSrc = mSrc[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerSrc.setSelection(0);
    }

    @OnClick(R.id.btn_create_group)
    void createGroup() {
        String groupName = mEditCreateGroup.getText().toString();
        if (TextUtils.isEmpty(groupName)) {
            ToastUtil.showToast("group name is empty!");
            return;
        }

        doCreate(groupName);
    }

    private void doCreate(final String groupName) {
        new Thread() {
            @Override
            public void run() {
                EMGroupManager.EMGroupOptions options = new EMGroupManager.EMGroupOptions();
                options.maxUsers = 200;
                options.style = EMGroupManager.EMGroupStyle.EMGroupStylePublicOpenJoin;
                // groupName, desc, allMembers, reason, option
                try {
                    EMClient.getInstance().groupManager().createGroup(groupName, "", new String[0], "", options);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    DebugLog.e("create group failed!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast("create failed!");
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("create group " + groupName + " success!");
                    }
                });

                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().logout(true);
    }

    @OnClick(R.id.btn_join_group)
    void joinGroup() {
//        EMClient.getInstance().groupManager()
        String groupId = mEditJoinGroup.getText().toString();
        if (TextUtils.isEmpty(groupId)) {
            ToastUtil.showToast("group id is empty!");
            return;
        }

        doJoinGroup(groupId);
    }

    private void doJoinGroup(final String groupId) {
        new Thread() {
            @Override
            public void run() {
//                List<EMGroup> groups = EMClient.getInstance().groupManager().getAllGroups();
//                if (groups == null) return;
//                EMGroup group = EMClient.getInstance().groupManager().getGroup(groupName);

//                EMClient.getInstance().groupManager().asyncGetPublicGroupsFromServer();

//                final EMGroup group = groups.get(0);
//                if (group == null) {
//                    DebugLog.e("group is null");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtil.showToast("group not exist!");
//                        }
//                    });
//                    return;
//                }

//                DebugLog.d("group name :" + group.getGroupName() + ", id :: " + group.getGroupId());
                DebugLog.d("group id :" + groupId);

                try {
                    EMClient.getInstance().groupManager().joinGroup(groupId);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    DebugLog.e("join group " + groupId + " failed!");
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtil.showToast("join group " + groupId + " failed!");
//                        }
//                    });
                }

                // 接听
                EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
                mCurrentGroupId = groupId;

                DebugLog.d("join group " + groupId + "success!");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onJoinGroupSuccess();
//                        ToastUtil.showToast("join group " + groupId + "success!");
//                    }
//                });
            }
        }.start();
    }

    @OnClick(R.id.btn_send_msg)
    void sendMsg() {
        String msg = mEditSendMsg.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            ToastUtil.showToast("msg is empty!");
            return;
        }

        if (mTestGroupId == null) {
            ToastUtil.showToast("current group not exist!");
            return;
        }

//        EMMessage message = EMMessage.createTxtSendMessage(msg, mCurrentGroupId);
        msg = GsonUtil.toJson(mDefaultSrc, msg);
        EMMessage message = EMMessage.createTxtSendMessage(msg, mTestGroupId);
        message.setChatType(EMMessage.ChatType.GroupChat);
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    @OnClick(R.id.btn_leave_group)
    void leaveGroup() {
        if (mCurrentGroupId == null) return;

        new Thread() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(mCurrentGroupId);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                mCurrentGroupId = null;
            }
        }.start();
    }

    private EMMessageListener mMessageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            if (list == null) return;

            EMMessage message = list.get(0);
            if (message != null) {
                final String text = MessageUtil.getTextFromMessage(message);
                MyMessage myMessage = GsonUtil.parseJson(text);
                switch (myMessage.src) {
                    case "zh":
                        RestSource.getInstance().queryEn(myMessage.text, new RestSource.TranslateCallback() {
                            @Override
                            public void onProcessResult(ResultBean resultBean) {
                                mTvReceiveMsg.setText(resultBean.trans_result.get(0).dst);
                            }
                        });
                        break;
                    case "en":
                        RestSource.getInstance().queryCh(myMessage.text, new RestSource.TranslateCallback() {
                            @Override
                            public void onProcessResult(ResultBean resultBean) {
                                mTvReceiveMsg.setText(resultBean.trans_result.get(0).dst);
                            }
                        });
                        break;
                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTvReceiveMsg.setText("receive:\n" + text);
//                    }
//                });
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
