package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSGroupJoinApplyListActivity extends BaseAppCompatActivity {
    static String TAG = SNSGroupJoinApplyListActivity.class.getSimpleName();

    public static final String GRP_ID = "tar_grp_id";

    protected String mTarGrpID;

    protected List<JSONObject> mJoinApplyList;
    protected GJALRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_group_join_apply_list);

        Intent intent = getIntent();
        mTarGrpID = intent.getStringExtra(GRP_ID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("가입신청목록");

        mRV = (RecyclerView) findViewById(R.id.rv_group_join_apply);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        mJoinApplyList = new ArrayList<>();
        mAdapter = new GJALRecyclerViewAdapter(SNSGroupJoinApplyListActivity.this, mJoinApplyList);
        mRV.setAdapter(mAdapter);

        loadListJoinApply();
    }

    public void loadListJoinApply() {
        mJoinApplyList.clear();
        I2ConnectApi.requestJSON(SNSGroupJoinApplyListActivity.this, I2UrlHelper.SNS.getListJoinApply(mTarGrpID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListJoinApply onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListJoinApply onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupJoinApplyListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListJoinApply onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            List<JSONObject> statusInfoList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);
                            mJoinApplyList.addAll(statusInfoList);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class GJALRecyclerViewAdapter
            extends RecyclerView.Adapter<GJALRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;

        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mGrpID, mUsrID;

            public final View mView;
            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvUsrNm, mTvJoinDttm;
            public final ImageView mIvExcept;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvUsrNm = (TextView) view.findViewById(R.id.tv_usr_nm);
                mTvJoinDttm = (TextView) view.findViewById(R.id.tv_join_dttm);
                mIvExcept = (ImageView) view.findViewById(R.id.iv_except);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public JSONObject getValueAt(int position) {
            return mValues.get(position);
        }

        public GJALRecyclerViewAdapter(Context context, List<JSONObject> items) {
            mContext = context;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_join_apply, parent, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                TypedValue outValue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                view.setBackgroundResource(outValue.resourceId);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.get(position);

                holder.mGrpID = jsonObject.getString("grp_id");
                holder.mUsrID = jsonObject.getString("usr_id");
                holder.mTvUsrNm.setText(jsonObject.getString("usr_nm"));
                holder.mTvJoinDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMMSS(jsonObject.getString("join_dttm")));

                Glide.with(holder.mCivCrtUsrPhoto.getContext())
                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(jsonObject.getString("crt_usr_photo"))))
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

                holder.mIvExcept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogUtil.showConfirmDialog(mContext, "안내", holder.mTvUsrNm.getText() +  "님을 가입승인하시겠습니까?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.joinApproval(holder.mGrpID, holder.mUsrID))
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<JSONObject>() {
                                            @Override
                                            public void onCompleted() {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApproval onCompleted");
                                                DialogUtil.showInformationDialog(mContext, "가입승인 완료되었습니다.");
                                                ((SNSGroupJoinApplyListActivity) mContext).loadListJoinApply();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApproval onError");
                                                //Error dialog 표시
                                                DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                            }

                                            @Override
                                            public void onNext(JSONObject jsonObject) {
                                                Log.d(TAG, "I2UrlHelper.SNS.joinApproval onNext");
                                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                                } else {
                                                    Toast.makeText(mContext, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
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

}
