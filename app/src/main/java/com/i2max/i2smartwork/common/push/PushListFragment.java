package com.i2max.i2smartwork.common.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
import com.i2max.i2smartwork.component.EndlessRecyclerOnScrollListener;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PushListFragment extends Fragment {
    static String TAG = PushListFragment.class.getSimpleName();

    public static final int LIST_UNREAD_SNS_MSG = 0;
    public static final int LIST_SNS_MSG = 1;

    public boolean checkLoading = false;
    protected int mListMode, mListPage, mTotalCnt;
    public void setPostMode(int listMode) {
        mListMode = listMode;
    }

    protected List<JSONObject> mPushDataArray;
    protected PushListRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;
    protected TextView mTvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        mRV = (RecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_sns_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);
        mRV.addItemDecoration(new SimpleDividerItemDecoration(mRV.getContext()));
        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                if (Math.abs(dy) > CodeConstant.SCROLL_OFFSET) {
//                    if (dy > 0) {
//                        ((PushListActivity)getActivity()).setVisibleFabButton(true);
//                    } else {
//                        ((PushListActivity)getActivity()).setVisibleFabButton(false);
//                    }
//                }
            }

            @Override
            public void onLoadMore(int current_page) {
                if (!checkLoading) {

                    if (mTotalCnt > mPushDataArray.size()) {
                        mListPage++;

                        loadRecyclerView();
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mPushDataArray = new ArrayList<>();
        mAdapter = new PushListRecyclerViewAdapter(getActivity(), mPushDataArray);
        mRV.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView() {

        if(mTotalCnt > 0 && mTotalCnt <= mPushDataArray.size() ) return;

        String receiveConfirm = "";
        if (mListMode == LIST_UNREAD_SNS_MSG) {
            receiveConfirm = "not_received";
        } else {
            receiveConfirm = "received";
        }

        I2ConnectApi.requestJSON(getActivity(), I2UrlHelper.Push.getListSnsMessage(String.format("%d",mListPage), receiveConfirm))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsMessage onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsMessage onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsMessage onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            List<JSONObject> statusInfoList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);

                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfoList.get(0).getString("total_cnt"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                setEmptyResult(mTotalCnt);
                                if (mTotalCnt > 0) {
                                    mPushDataArray.addAll(statusInfoList);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }

    public static class PushListRecyclerViewAdapter
            extends RecyclerView.Adapter<PushListRecyclerViewAdapter.ViewHolder> {
        protected final String TYPE_WORK = "WORK";
        protected final String TYPE_MEMO = "MEMO";
        protected final String TYPE_TASK = "TASK";
        protected final String TYPE_CFRC = "CFRC";
        protected final String TYPE_CUMM = "CUMM";
        protected final String TYPE_MILE = "MILE";
        protected final String TYPE_USER = "USER";


        protected Context mContext;

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public String mRecvID;
            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvMsgCntn;
            public final ImageView mIvBizTpCd;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvMsgCntn = (TextView) view.findViewById(R.id.tv_msg_cntn);
                mIvBizTpCd = (ImageView) view.findViewById(R.id.iv_biz_tp_cd);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public JSONObject getValueAt(int position) throws JSONException {
            return mValues.get(position);
        }

        public PushListRecyclerViewAdapter(Context context, List<JSONObject> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mContext = context;
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_push, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.get(position);

                holder.mRecvID = jsonObject.getString("recv_id");
                holder.mTvMsgCntn.setText(jsonObject.getString("msg_cntn"));

                final String typeStr = jsonObject.getString("biz_tp_cd");

                if (TYPE_WORK.equals(typeStr)) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_work));
                } else if (TYPE_MEMO.equals(typeStr)) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_memo));
                } else if (TYPE_CFRC.equals(typeStr)) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_conference));
                } else if (TYPE_TASK.equals(typeStr) ) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_task));
                } else if (TYPE_MILE.equals(typeStr) ) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_mile));
                } else if (TYPE_USER.equals(typeStr) ) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_type_user));
                } else if (TYPE_CUMM.equals(typeStr) ) {
                    holder.mIvBizTpCd.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_push_reply));
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            linkTypeActivity(typeStr, jsonObject);
                            Log.d("onBindViewHolder", jsonObject.getString("link_url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Glide.with(holder.mCivCrtUsrPhoto.getContext())
                        .load(I2UrlHelper.File.getUsrImage(jsonObject.getString("crt_usr_photo")))
                        .error(R.drawable.ic_no_usr_photo)
                        .fitCenter()
                        .into(holder.mCivCrtUsrPhoto);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void linkTypeActivity(String typeStr, JSONObject infoMap) {
            Intent intent = null;

            try {
                String url = infoMap.getString("link_url");
                String id = "";
                if(!TextUtils.isEmpty(url)) {
                    id = url.substring(url.lastIndexOf("id=")+3);
                }

                if (TYPE_WORK.equals(typeStr)) {
                    Toast.makeText(mContext, "과제관리는 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
                } else if (TYPE_MEMO.equals(typeStr) ) {
                    Toast.makeText(mContext, "메모보고는 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
                } else if (TYPE_MILE.equals(typeStr) ) {
                    Toast.makeText(mContext, "마일스톤 일정은 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
                } else if (TYPE_CFRC.equals(typeStr)) {
                    intent = new Intent(mContext, ConferenceDetailActivity.class);
                    intent.putExtra(CodeConstant.TITLE, mContext.getString(R.string.cfrc_detail));
                    intent.putExtra(CodeConstant.CUR_OBJ_TP, infoMap.getString("biz_tp_cd"));
                    intent.putExtra(CodeConstant.CUR_OBJ_ID, id);
                    intent.putExtra(CodeConstant.CRT_USR_ID, "");
                    mContext.startActivity(intent);
                } else if (TYPE_TASK.equals(typeStr) ) {
                    intent = new Intent(mContext, TaskDetailActivity.class);
                    intent.putExtra(CodeConstant.TITLE, mContext.getString(R.string.task_detail));
                    intent.putExtra(CodeConstant.CUR_OBJ_TP, infoMap.getString("biz_tp_cd"));
                    intent.putExtra(CodeConstant.CUR_OBJ_ID, id);
                    intent.putExtra(CodeConstant.CRT_USR_ID, "");
                    mContext.startActivity(intent);
                } else if (TYPE_CUMM.equals(typeStr) ) {
                    Toast.makeText(mContext, "마일스톤 일정은 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
//                    intent = new Intent(mContext, SNSDetailPostActivity.class);
//                    intent.putExtra(CodeConstant.TITLE, mContext.getString(R.string.task_detail));
//                    intent.putExtra(CodeConstant.OBJECT_ID, id);
//                    intent.putExtra(CodeConstant.CRT_USR_ID, "");
                    mContext.startActivity(intent);
                } else if (TYPE_USER.equals(typeStr) ) {
                    //?
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }



    public void setEmptyResult(int dataSize) {
        if(dataSize < 1) {
            mRV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mRV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);

        }
    }
}
