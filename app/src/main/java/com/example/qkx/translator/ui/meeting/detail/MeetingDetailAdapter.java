package com.example.qkx.translator.ui.meeting.detail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.qkx.translator.R;
import com.example.qkx.translator.utils.DebugLog;
import com.example.qkx.translator.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qkx on 17/3/10.
 */

public class MeetingDetailAdapter extends RecyclerView.Adapter {

    private String mLocale = "zh";
    private Context mContxet;
    private LayoutInflater mInflater;

    private List<MeetingDetailMessage> mData = new ArrayList<>();

    public MeetingDetailAdapter(Context context) {
        this.mContxet = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DebugLog.d("onCreateViewHolder() =======");
        View view = mInflater.inflate(R.layout.item_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DetailViewHolder) {
            DebugLog.d("onBindViewHolder() =======");
            DetailViewHolder detailViewHolder = (DetailViewHolder) holder;
            detailViewHolder.bind(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setLocale(String locale) {
        this.mLocale = locale;
    }

    public void addData(MeetingDetailMessage data) {
        DebugLog.d("addData() =======");
        mData.add(data);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    private class DetailViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvMessageHint;
        private TextView mTvMessageFrom;

        private DetailViewHolder(View itemView) {
            super(itemView);
            mTvMessageHint = UIUtil.getView(itemView, R.id.tv_message_detail);
            mTvMessageFrom = UIUtil.getView(itemView, R.id.tv_message_from);
        }

        private void bind(MeetingDetailMessage data) {
            DebugLog.d("bind() =======");
            final String sourceInfo = data.getSourceInfo();
            mTvMessageFrom.setText(sourceInfo);

            mTvMessageHint.setText(data.getMsg());

//            String text = data.getText();
//            MyMessage myMessage = GsonUtil.parseJson(text);
//
//            if ("zh".equals(mLocale) && "en".equals(myMessage.src)) {
//                RestSource.getInstance().queryCh(myMessage.text, new RestSource.TranslateCallback() {
//                    @Override
//                    public void onProcessResult(ResultBean resultBean) {
//                        DebugLog.d("bind() :: queryCh() =======");
//                        mTvMessageHint.setText(resultBean.trans_result.get(0).dst);
//                    }
//                });
//            } else if ("en".equals(mLocale) && "zh".equals(myMessage.src)) {
//                RestSource.getInstance().queryEn(myMessage.text, new RestSource.TranslateCallback() {
//                    @Override
//                    public void onProcessResult(ResultBean resultBean) {
//                        DebugLog.d("bind() :: queryEn() =======");
//                        mTvMessageHint.setText(resultBean.trans_result.get(0).dst);
//                    }
//                });
//            } else {
//                DebugLog.d("bind() =======");
//                mTvMessageHint.setText(myMessage.text);
//            }
        }
    }
}
