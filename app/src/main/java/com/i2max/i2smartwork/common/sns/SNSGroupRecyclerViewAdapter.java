package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by shlee on 16. 2. 23..
 */
public class SNSGroupRecyclerViewAdapter extends UltimateViewAdapter<SNSGroupRecyclerViewAdapter.SimpleAdapterViewHolder> {
    static String TAG = SNSGroupRecyclerViewAdapter.class.getSimpleName();
    protected Context mContext;

    protected final TypedValue mTypedValue = new TypedValue();
    protected int mBackground;
    protected List<JSONObject> mValues;
    protected int mMode;

    public SNSGroupRecyclerViewAdapter(Context context, List<JSONObject> items, int mode) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mContext = context;
        mBackground = mTypedValue.resourceId;
        mValues = items;
        mMode = mode;
    }

    @Override
    public int getAdapterItemCount() {
        return mValues.size();
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }

    @Override
    public SimpleAdapterViewHolder getViewHolder(View view) {
        return new SimpleAdapterViewHolder(view, false);
    }

    @Override
    public SimpleAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_group, parent, false);
        SimpleAdapterViewHolder vh = new SimpleAdapterViewHolder(v, true);
        return vh;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
    }

    public class SimpleAdapterViewHolder extends UltimateRecyclerviewViewHolder {

        public String mGrpID, mJoinStatus, mOpenYN;

        public View mView;
        public CircleImageView mCivGrpImg;
        public TextView mTvOpenYN, mTvAdminNm, mTvGrpNm, mTvMemberCnt, mTvPostCnt, mTvGrpIntro;
        public ImageButton mIBtnGrpJoin;

        public SimpleAdapterViewHolder(View view, boolean isItem) {
            super(view);

            if (isItem) {
                mView = view;
                mCivGrpImg = (CircleImageView) view.findViewById(R.id.civ_grp_img);
                mTvOpenYN = (TextView) view.findViewById(R.id.tv_open_yn);
                mTvAdminNm = (TextView) view.findViewById(R.id.tv_admin_nm);
                mTvGrpNm = (TextView) view.findViewById(R.id.tv_grp_nm);
                mTvMemberCnt = (TextView) view.findViewById(R.id.tv_member_cnt);
                mTvPostCnt = (TextView) view.findViewById(R.id.tv_post_cnt);
                mTvGrpIntro = (TextView) view.findViewById(R.id.tv_grp_intro);
                mIBtnGrpJoin = (ImageButton) view.findViewById(R.id.ibtn_grp_join);
            }

        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public JSONObject getItem(int position) {
        if (customHeaderView != null)
            position--;
        if (position < mValues.size())
            return mValues.get(position);
        else return null;
    }

    @Override
    public void onBindViewHolder(final SimpleAdapterViewHolder holder, int pos) {
        //Log.d(TAG, "onBindViewHolder size = " + mPlanList.size() + " / pos = " + pos);

        if (pos < getItemCount() && (customHeaderView != null ? pos <= mValues.size() : pos < mValues.size()) && (customHeaderView != null ? pos > 0 : true)) {
            try {
                final JSONObject jsonObject = mValues.get(pos);

                holder.mGrpID = jsonObject.getString("grp_id");
                holder.mTvGrpNm.setText(jsonObject.getString("grp_nm"));
                if (!jsonObject.isNull("member_cnt"))
                    holder.mTvMemberCnt.setText("회원수 : " + jsonObject.getString("member_cnt"));
                if (!jsonObject.isNull("post_cnt"))
                    holder.mTvPostCnt.setText("글수 : " + jsonObject.getString("post_cnt"));
                if (!jsonObject.isNull("grp_intro"))
                    holder.mTvGrpIntro.setText(jsonObject.getString("grp_intro"));

                //holder.mJoinYN = jsonObject.getString("join_yn");
                Glide.with(holder.mCivGrpImg.getContext())
                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(jsonObject.getString("grp_photo_url"))))
                        .error(R.drawable.ic_no_usr_photo)
                        .fitCenter()
                        .into(holder.mCivGrpImg);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SNSDetailGroupActivity.class);

                        intent.putExtra(SNSDetailGroupActivity.GRP_ID, holder.mGrpID);
                        intent.putExtra(SNSDetailGroupActivity.GRP_NM, holder.mTvGrpNm.getText().toString());

                        mContext.startActivity(intent);
                    }
                });

                if (mMode == CodeConstant.MODE_ALL_GROUP) {
                    holder.mOpenYN = jsonObject.getString("open_yn");
                    holder.mJoinStatus = jsonObject.getString("join_status");
                    holder.mTvAdminNm.setText("관리자 : " + jsonObject.getString("admin_nm"));
                    holder.mTvOpenYN.setVisibility(View.VISIBLE);
                    holder.mTvAdminNm.setVisibility(View.VISIBLE);
                    holder.mIBtnGrpJoin.setVisibility(View.VISIBLE);
                    if (holder.mOpenYN.equals("N")) {
                        holder.mTvOpenYN.setText("비공개 그룹");
                    } else {
                        holder.mTvOpenYN.setText("공개 그룹");
                    }

                    if (holder.mJoinStatus.equals("IN")) {
                        holder.mIBtnGrpJoin.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_group_out));
                    } else if (holder.mJoinStatus.equals("ING")) {
                        holder.mIBtnGrpJoin.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_group_request));
                    } else {
                        holder.mIBtnGrpJoin.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_group_in));
                    }

                    holder.mIBtnGrpJoin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder.mJoinStatus.equals("IN")) {
                                DialogUtil.showConfirmDialog(mContext, "안내", "그룹탈퇴를 하시겠습니까?", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.leaveGroupMember(holder.mGrpID))
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<JSONObject>() {
                                                    @Override
                                                    public void onCompleted() {
                                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onCompleted");
                                                        String msg = "그룹을 탈퇴하였습니다.";

                                                        for (int i = 0; i < mValues.size(); i++) {
                                                            try {
                                                                if (mValues.get(i).getString("grp_id").equals(holder.mGrpID)) {
                                                                    holder.mJoinStatus = "NOT_IN";

                                                                    break;
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        DialogUtil.showInformationDialog(mContext, msg);

                                                        notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onError");
                                                        DialogUtil.showErrorDialog(mContext, e.getMessage());
                                                        //Error dialog 표시
                                                        e.printStackTrace();
                                                        DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                                    }

                                                    @Override
                                                    public void onNext(JSONObject jsonObject) {
                                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onNext");
                                                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                                                        } else {
                                                            Toast.makeText(mContext, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            } else {
                                I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.joinApply(holder.mGrpID))
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<JSONObject>() {
                                            @Override
                                            public void onCompleted() {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApply onCompleted");

                                                for (int i = 0; i < mValues.size(); i++) {
                                                    try {
                                                        if (mValues.get(i).getString("grp_id").equals(holder.mGrpID)) {
                                                            holder.mJoinStatus = "IN";

                                                            break;
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApply onError");
                                                //Error dialog 표시
                                                DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                            }

                                            @Override
                                            public void onNext(JSONObject jsonObject) {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApply onNext");
                                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                                    try {
                                                        DialogUtil.showInformationDialog(mContext, statusInfo.getString("join_msg"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                } else {
                                                    Toast.makeText(mContext, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
