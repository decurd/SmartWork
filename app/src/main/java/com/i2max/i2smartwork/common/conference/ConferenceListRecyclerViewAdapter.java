package com.i2max.i2smartwork.common.conference;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shlee on 15. 9. 3..
 */
public class ConferenceListRecyclerViewAdapter extends UltimateViewAdapter<ConferenceListRecyclerViewAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private Context mContext;
    private List<LinkedTreeMap<String, Object>> mValues;

    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        public final View mView;
        public String mCfrcId, mCrtUsrID;
        public final CircleImageView mCivCrtUsrPhoto;
        public final TextView mTvCrtUsrNm, mTvCrtDttm, mTvCfrcTtl, mTvTerm, mTvCfrcRoomTpNm, mTvCfrcTpNm, mTvCfrcStNm;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
            mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
            mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
            mTvCfrcTtl = (TextView) view.findViewById(R.id.tv_cfrc_ttl);
            mTvTerm = (TextView) view.findViewById(R.id.tv_cfrc_term);
            mTvCfrcRoomTpNm = (TextView) view.findViewById(R.id.tv_cfrc_room_tp_nm);
            mTvCfrcTpNm = (TextView) view.findViewById(R.id.tv_cfrc_tp_nm);
            mTvCfrcStNm = (TextView) view.findViewById(R.id.tv_cfrc_st_nm);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public LinkedTreeMap<String, Object> getValueAt(int position) {
        return mValues.get(position);
    }

    public ConferenceListRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, Object>> items) {
        mContext = context;
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_conference, parent, false);
        view.setBackgroundResource(mBackground);
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
        final LinkedTreeMap<String, Object> item = mValues.get(position);

        holder.mCfrcId = FormatUtil.getStringValidate(item.get("cfrc_id"));
        holder.mCrtUsrID = FormatUtil.getStringValidate(item.get("crt_usr_id"));
        holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(item.get("crt_usr_nm")));
        Glide.with(holder.mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("crt_usr_photo"))))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(holder.mCivCrtUsrPhoto);
        holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(item.get("crt_usr_nm")));
        //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
        String crtDttm = FormatUtil.getStringValidate(item.get("mod_dttm"));
        if("".equals(crtDttm)) {
            crtDttm = FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("crt_dttm"))); //만든시간 표시
        } else {
            crtDttm = FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("mod_dttm"))); //수정시간 표시
        }
        holder.mTvCrtDttm.setText(crtDttm);
        holder.mTvCfrcTtl.setText(FormatUtil.getStringValidate(item.get("cfrc_ttl")));
        String cfrcTerm = FormatUtil.getFormattedDate4(FormatUtil.getStringValidate(item.get("cfrc_dt"))) + " "+
                FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("start_tm"))) + "~" +
                FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("end_tm")));
        Log.e("", "cfrcTerm = " + cfrcTerm);
        holder.mTvTerm.setText(cfrcTerm);

        holder.mTvCfrcRoomTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_room_tp_nm")));

        if("오프라인".equals(holder.mTvCfrcRoomTpNm.getText().toString())) holder.mTvCfrcTpNm.setVisibility(View.GONE);
        else holder.mTvCfrcTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_tp_nm")));

        holder.mTvCfrcStNm.setText(FormatUtil.getStringValidate(item.get("cfrc_st_nm")));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onBindViewHolder", FormatUtil.getStringValidate(item.get("cfrc_id")));

                Intent intent = new Intent(mContext, ConferenceDetailActivity.class);
                intent.putExtra(CodeConstant.CUR_OBJ_TP, CodeConstant.TYPE_CFRC);
                intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.getStringValidate(item.get("cfrc_id")));
                intent.putExtra(CodeConstant.TAR_OBJ_TTL, FormatUtil.getStringValidate(item.get("cfrc_ttl")));
                intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.getStringValidate(item.get("crt_usr_id")));
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
