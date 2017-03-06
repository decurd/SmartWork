package com.i2max.i2smartwork.common.conference;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.IntentUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shlee on 15. 9. 3..
 */
public class ConferenceOnlineListRecyclerViewAdapter extends RecyclerView.Adapter<ConferenceOnlineListRecyclerViewAdapter.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private Context mContext;
    private List<LinkedTreeMap<String, String>> mValues;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public String mCfrcId, mCrtUsrID;
        public final CircleImageView mCivCrtUsrPhoto;
        public final TextView mTvCrtUsrNm, mTvCrtDttm, mTvCfrcTtl, mTvTerm, mTvCfrcRoomTpNm, mTvCfrcTpNm, mTvCfrcStNm, mTvCfrcConvRslt;
        public final Button mBtStartCfrc;

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
            mTvCfrcConvRslt = (TextView) view.findViewById(R.id.tv_cfrc_file_conv_rslt);
            mBtStartCfrc = (Button) view.findViewById(R.id.btn_start_cfrc);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public LinkedTreeMap<String, String> getValueAt(int position) {
        return mValues.get(position);
    }

    public ConferenceOnlineListRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, String>> items) {
        mContext = context;
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_conference_online_room, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        if(TextUtils.isEmpty(holder.mCfrcId)) return;

        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LinkedTreeMap<String, String> item = mValues.get(position);

        final String cfrcId = FormatUtil.getStringValidate(item.get("cfrc_id"));
        if(TextUtils.isEmpty(cfrcId)) {
            holder.mView.setVisibility(View.GONE);
            return;
        } else {
            holder.mCfrcId = cfrcId;
            holder.mCrtUsrID = FormatUtil.getStringValidate(item.get("crt_usr_id"));
            holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(item.get("crt_usr_nm")));
            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("crt_usr_photo"))))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);
            holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(item.get("crt_usr_nm")));
            //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
            String modDttm = FormatUtil.getStringValidate(item.get("mod_dttm"));
            if ("".equals(modDttm)) {
                holder.mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("crt_dttm")))); //만든시간 표시
            } else {
                holder.mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("mod_dttm")))); //수정시간 표시
            }
            holder.mTvCfrcTtl.setText(FormatUtil.getStringValidate(item.get("cfrc_ttl")));
            holder.mTvTerm.setText(FormatUtil.getFormattedDate4(FormatUtil.getStringValidate(item.get("cfrc_dt"))) + " " +
                    FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("start_tm"))) + "~" +
                    FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("end_tm"))));
            holder.mTvCfrcRoomTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_room_tp_nm")));
            holder.mTvCfrcTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_tp_nm")));
            holder.mTvCfrcStNm.setText(FormatUtil.getStringValidate(item.get("cfrc_st_nm")));

            String cfrc_st = FormatUtil.getStringValidate(item.get("cfrc_st"));
            String attachFileCnt = ""+(int) Float.parseFloat(FormatUtil.getStringValidate(item.get("attach_cnt")));
            int convFileCnt = (int) Float.parseFloat(FormatUtil.getStringValidate(item.get("conv_yn_cnt")));
            String btnText = "종료";
            String result = "변환미완료";
            if(convFileCnt <= 0) {
                result = "변환완료";
            }
            String convFileRslt = attachFileCnt+"개 파일   "+ result;
            holder.mTvCfrcConvRslt.setText(convFileRslt);

            int btColor = mContext.getResources().getColor(R.color.material_grey_600);
            if("RDY".equals(cfrc_st) || "ING".equals(cfrc_st)) {
//                if(convFileCnt > 0) {
//                    btnText = "회의문서변환중...";
//                    btColor =  mContext.getResources().getColor(R.color.md_red_400);
//                } else {
                    btnText = "회의참석";
                    btColor =  mContext.getResources().getColor(R.color.colorPrimary);
                    holder.mBtStartCfrc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("onBindViewHolder", FormatUtil.getStringValidate(item.get("cfrc_id")));
                            try {
                                Intent intent = IntentUtil.getI2ConferenceIntent(cfrcId);
                                mContext.startActivity(intent);
                            } catch (ActivityNotFoundException e ) {
                                e.printStackTrace();
                                DialogUtil.showConfirmDialog(mContext, "알림", "I2Conference앱이 설치되어 있지않습니다.\n다운로드를 진행합니다.",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Toast.makeText(mContext, "I2Conference앱 설치 APK를 다운로드를 시작합니다", Toast.LENGTH_LONG).show();
                                                String downloadURL = I2UrlHelper.I2App.getI2conferenceAppDownloadUrl();
                                                //토큰 인증처리
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                                Bundle bundle = new Bundle();
                                                bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization());
                                                intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                                Log.d("", "intent:" + intent.toString());
                                            }
                                        });
                            }

                        }
                    });
//                }
            }

            holder.mBtStartCfrc.setText(btnText);
            holder.mBtStartCfrc.setBackgroundColor(btColor);
            holder.mBtStartCfrc.setTextColor(Color.WHITE);






        }
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void removeAt(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }
}
