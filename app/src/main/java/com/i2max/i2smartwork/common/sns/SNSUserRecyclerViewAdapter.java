package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.Intent;
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
public class SNSUserRecyclerViewAdapter
        extends RecyclerView.Adapter<SNSUserRecyclerViewAdapter.ViewHolder> {
    static String TAG = SNSUserRecyclerViewAdapter.class.getSimpleName();

    protected Context mContext;
    protected int mMode;

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<JSONObject> mValues;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mUsrID, mFollowYN;

        public final View mView;
        public final CircleImageView mCivCrtUsrPhoto;
        public final TextView mTvUsrNm, mTvDeptNm, mTvSelfIntro;
        public final ImageButton mIBtnFollow;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
            mTvUsrNm = (TextView) view.findViewById(R.id.tv_usr_nm);
            mTvDeptNm = (TextView) view.findViewById(R.id.tv_dept_nm);
            mTvSelfIntro = (TextView) view.findViewById(R.id.tv_self_intro);
            mIBtnFollow = (ImageButton) view.findViewById(R.id.ibtn_follow);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public JSONObject getValueAt(int position) {
        return mValues.get(position);
    }

    public SNSUserRecyclerViewAdapter(Context context, List<JSONObject> items, int mode) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mContext = context;
        mBackground = mTypedValue.resourceId;
        mValues = items;
        mMode = mode;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_friend, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            final JSONObject jsonObject = mValues.get(position);

            final String userID, userNM, followYN, photoImgPath;
            if (mMode == CodeConstant.MODE_FOLLOWER) { //팔로워
                userID = "fllw_usr_id";
                userNM = "usr_nm";
                followYN = "fllw_yn"; //값이 없음
                photoImgPath = "usr_photo_url";
            } else { // 디폴트 팔로윙
                if (!jsonObject.has("usr_id")) {
                    userID = "tar_usr_id"; //following list
                } else {
                    userID = "usr_id"; //nomal 전체검색
                }
                userNM = "usr_nm";
                followYN = "follow_yn"; //값이 없음
                photoImgPath = "usr_photo_url";
            }

            switch (mMode) {
                case CodeConstant.MODE_LINK_ADD:
                case CodeConstant.MODE_CFRC_MEMBER_ADD:
                case CodeConstant.MODE_TASK_MEMBER_ADD:
                case CodeConstant.MODE_MEMO_MEMBER_ADD:
                case CodeConstant.MODE_WORK_MEMBER_ADD:
                    holder.mIBtnFollow.setVisibility(View.GONE);
                    final String usrID = jsonObject.getString(FormatUtil.getStringValidate(userID));
                    final String usrNM = jsonObject.getString(FormatUtil.getStringValidate(userNM));
                    final String usrImg = jsonObject.getString(FormatUtil.getStringValidate(photoImgPath));

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SNSPersonSearchActivity) mContext).setResultFinish(usrID, usrNM, usrImg);
                        }
                    });
                    break;

            }

            holder.mUsrID = jsonObject.getString(userID);
            holder.mTvUsrNm.setText(jsonObject.getString(userNM));
            if (!jsonObject.isNull("dept_nm"))
                holder.mTvDeptNm.setText(jsonObject.getString("dept_nm"));
            if (!jsonObject.isNull("self_intro"))
                holder.mTvSelfIntro.setText(jsonObject.getString("self_intro"));

            if (jsonObject.isNull(followYN)) {
                holder.mFollowYN = "Y"; // listSnsFollowing.json 호출 시 왜 follow_yn은 없을지..?
            } else {
                holder.mFollowYN = jsonObject.getString(followYN);
            }

            if (holder.mFollowYN.equals("Y")) {
                holder.mIBtnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_following_del));
            } else {
                holder.mIBtnFollow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.btn_following_add));
            }

            holder.mIBtnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.toggleSnsFollow(holder.mUsrID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JSONObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onCompleted");
                                    String msg = "";

                                    for (int i = 0; i < mValues.size(); i++) {
                                        try {
                                            if (mValues.get(i).getString(userID).equals(holder.mUsrID)) {
                                                if (holder.mFollowYN.equals("Y")) {
                                                    mValues.get(i).put(followYN, "N");
                                                    msg = "팔로잉을 취소하였습니다.";
                                                } else {
                                                    mValues.get(i).put(followYN, "Y");
                                                    msg = "팔로잉 하였습니다.";
                                                }

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
                                    Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onError");
                                    //Error dialog 표시
                                    e.printStackTrace();
                                    DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                }

                                @Override
                                public void onNext(JSONObject jsonObject) {
                                    Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onNext");
                                    if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                    } else {
                                        Toast.makeText(mContext, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(jsonObject.getString(photoImgPath))))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);

            holder.mCivCrtUsrPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SNSDetailProfileActivity.class);

                    intent.putExtra(SNSDetailProfileActivity.USR_ID, holder.mUsrID);
                    intent.putExtra(SNSDetailProfileActivity.USR_NM, holder.mTvUsrNm.getText().toString());

                    mContext.startActivity(intent);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

}