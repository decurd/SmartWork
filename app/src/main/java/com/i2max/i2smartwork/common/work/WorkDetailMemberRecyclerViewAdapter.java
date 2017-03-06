package com.i2max.i2smartwork.common.work;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.common.sns.SNSDetailProfileActivity;
import com.i2max.i2smartwork.utils.FormatUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shlee on 15. 9. 11..
 */
public class WorkDetailMemberRecyclerViewAdapter
        extends RecyclerView.Adapter<WorkDetailMemberRecyclerViewAdapter.ViewHolder> {

    protected Context mContext;
    private List<LinkedTreeMap<String, String>>  mValues;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public String mUsrID;
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

    public LinkedTreeMap<String, String> getValueAt(int position) {
        return mValues.get(position);
    }

    public WorkDetailMemberRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, String>> items) {
        mContext = context;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_member, parent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TypedValue outValue = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            view.setBackgroundResource(outValue.resourceId);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LinkedTreeMap<String, String> item = mValues.get(position);

        holder.mUsrID = FormatUtil.getStringValidate(item.get("usr_id"));
        holder.mTvUsrNm.setText(FormatUtil.getStringValidate(item.get("usr_nm")));

        String tpNm = FormatUtil.getStringValidate(item.get("usr_tp"));
        String fnlApprYn = FormatUtil.getStringValidate(item.get("fnl_appr_yn"));
        if("CRTR".equals(tpNm)) {
            tpNm = "담당자";
        } else if("REFE".equals(tpNm)) {
            if("Y".equals(fnlApprYn)) {
                tpNm = "책임자";
            } else {
                tpNm = "참가자";
            }
        }
        holder.mTvSelfIntro.setText(tpNm);

        Glide.with(holder.mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("crt_usr_photo"))))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(holder.mCivCrtUsrPhoto);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SNSDetailProfileActivity.class);

                intent.putExtra(SNSDetailProfileActivity.USR_ID, holder.mUsrID);
                intent.putExtra(SNSDetailProfileActivity.USR_NM, holder.mTvUsrNm.getText().toString());

                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}