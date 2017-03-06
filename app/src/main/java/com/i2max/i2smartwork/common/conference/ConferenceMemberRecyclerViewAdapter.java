package com.i2max.i2smartwork.common.conference;

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
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shlee on 15. 9. 11..
 */
public class ConferenceMemberRecyclerViewAdapter
        extends UltimateViewAdapter<ConferenceMemberRecyclerViewAdapter.ViewHolder> {

    protected Context mContext;
    private List<LinkedTreeMap<String, String>>  mValues;

    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
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

    public ConferenceMemberRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, String>> items) {
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
    public ViewHolder getViewHolder(View view) {
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LinkedTreeMap<String, String> item = mValues.get(position);

        holder.mUsrID = FormatUtil.getStringValidate(item.get("ref_usr_id"));
        holder.mTvUsrNm.setText(FormatUtil.getStringValidate(item.get("ref_usr_nm")));
        holder.mTvDeptNm.setText(FormatUtil.getStringValidate(item.get("ref_usr_dept_nm")));
        holder.mTvSelfIntro.setText(FormatUtil.getStringValidate(item.get("cfrc_usr_flag_nm")));

        Glide.with(holder.mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("ref_usr_photo"))))
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
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) { return null; }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {}

    @Override
    public int getItemCount() { return mValues.size(); }

    @Override
    public int getAdapterItemCount() {
        return mValues.size();
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }
}