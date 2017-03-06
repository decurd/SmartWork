package com.i2max.i2smartwork.common.task;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
public class TaskListRecyclerViewAdapter extends UltimateViewAdapter<TaskListRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<LinkedTreeMap<String, Object>> mValues;
    public String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId;

    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        public final View mView;
        public String mTaskId, tarUsrId, mCrtUsrID;
        public final CircleImageView mCivCrtUsrPhoto;
        public final TextView mTvCrtUsrNm, mTvCrtDttm, mTvTtl, mTvTerm, mTvTaskStNm;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
            mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
            mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
            mTvTtl = (TextView) view.findViewById(R.id.tv_ttl);
            mTvTerm = (TextView) view.findViewById(R.id.tv_term);
            mTvTaskStNm = (TextView) view.findViewById(R.id.tv_task_st_nm);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public LinkedTreeMap<String, Object> getValueAt(int position) {
        return mValues.get(position);
    }

    public TaskListRecyclerViewAdapter(Context context, List<LinkedTreeMap<String, Object>> items) {
        mContext = context;
        mValues = items;
    }

    public void setTarObjInfo(String tarObjTp,String tarObjId, String tarObjTtl, String tarCrtUsrId) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrId = tarCrtUsrId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);
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
        final LinkedTreeMap<String, Object> item = mValues.get(position);

        holder.mTaskId = FormatUtil.getStringValidate(item.get("task_id"));
        holder.mCrtUsrID = FormatUtil.getStringValidate(item.get("crt_usr_id"));
        Glide.with(holder.mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("crt_usr_photo"))))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(holder.mCivCrtUsrPhoto);
        holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(item.get("crt_usr_nm")));
        //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
        String modDttm = FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("mod_dttm")));
        if( "".equals(modDttm)) {
            holder.mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("crt_dttm")))); //만든시간 표시
        } else {
            holder.mTvCrtDttm.setText(modDttm); //수정시간 표시
        }
        holder.mTvTtl.setText(FormatUtil.getStringValidate(item.get("ttl")));
        holder.mTvTerm.setText(FormatUtil.getShortDateFormat(FormatUtil.getStringValidate(item.get("start_dttm")).substring(0,8))
                + " ~ " + FormatUtil.getShortDateFormat(FormatUtil.getStringValidate(item.get("end_dttm")).substring(0, 8)));
        String taskStNm = FormatUtil.getStringValidate(item.get("task_st_cd"));
        if("DLY".equals(taskStNm)) {
            taskStNm = "지연";
        } else {
            taskStNm = FormatUtil.getStringValidate(item.get("task_st_nm"));
        }
        holder.mTvTaskStNm.setText(taskStNm);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onBindViewHolder", FormatUtil.getStringValidate(item.get("task_id")));

                Intent intent = new Intent(mContext, TaskDetailActivity.class);
                intent.putExtra(CodeConstant.TAR_OBJ_TP, FormatUtil.getStringValidate(item.get("tar_obj_tp_cd")));
                intent.putExtra(CodeConstant.TAR_OBJ_ID, FormatUtil.getStringValidate(item.get("tar_obj_id")));
                intent.putExtra(CodeConstant.TAR_CRT_USR_ID, FormatUtil.getStringValidate(item.get("tar_usr_id")));
                intent.putExtra(CodeConstant.CUR_OBJ_TP, CodeConstant.TYPE_TASK);
                intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.getStringValidate(item.get("task_id")));
                intent.putExtra(CodeConstant.TAR_OBJ_TTL, FormatUtil.getStringValidate(item.get("ttl"))); //task 제목
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
