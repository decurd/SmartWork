package com.i2max.i2smartwork.common.plan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.sns.SNSDetailGroupActivity;
import com.i2max.i2smartwork.common.sns.SNSDetailProfileActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PlanDetailActivity extends BaseAppCompatActivity {
    protected static String TAG = PlanDetailActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String PLAN_ID = "plan_id";
    public static final String PLAN_DATE = "plan_date";

    protected String mTarObjTp, mTarObjId, mTarObjTtl, mTarPlanID;
    protected TextView tvUsrNm, tvStartDttm, tvEndDttm, tvPlanTtl, tvTarObjTtl, tvPlace, tvPlanOpenYN, tvPlanDtlCntn;
    protected LinearLayout llBottomBtns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        Intent intent = getIntent();
        mTarPlanID = intent.getStringExtra(PLAN_ID);
        String planDate = intent.getStringExtra(PLAN_DATE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(planDate + " 상세일정");

        tvUsrNm = (TextView) findViewById(R.id.tv_usr_nm);
        tvStartDttm = (TextView) findViewById(R.id.tv_start_dttm);
        tvEndDttm = (TextView) findViewById(R.id.tv_end_dttm);
        tvPlanTtl = (TextView) findViewById(R.id.tv_plan_ttl);
        tvTarObjTtl = (TextView) findViewById(R.id.tv_tar_obj_ttl);
        tvPlace = (TextView) findViewById(R.id.tv_place);
        tvPlanOpenYN = (TextView) findViewById(R.id.tv_plan_open_yn);
        tvPlanDtlCntn = (TextView) findViewById(R.id.tv_plan_dtl_cntn);
        llBottomBtns = (LinearLayout) findViewById(R.id.ll_bottom_btns);

        Button btnDelete = (Button) findViewById(R.id.btn_plan_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.showConfirmDialog(PlanDetailActivity.this, "삭제", "일정을 삭제하시겠습니까?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        I2ConnectApi.requestJSON(PlanDetailActivity.this, I2UrlHelper.Plan.deleteSnsPlan(mTarPlanID))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JSONObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPlan onCompleted");
                                        DialogUtil.showInformationDialog(PlanDetailActivity.this, "일정이 삭제되었습니다.",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        finish();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPlan onError");
                                        //Error dialog 표시
                                        e.printStackTrace();
                                        DialogUtil.showErrorDialogWithValidateSession(PlanDetailActivity.this, e);
                                    }

                                    @Override
                                    public void onNext(JSONObject jsonObject) {
                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPlan onNext");
                                        if (!I2ResponseParser.checkReponseStatus(jsonObject)) {
                                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }
        });

        Button btnModify = (Button) findViewById(R.id.btn_plan_modify);
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent planIntent = new Intent(PlanDetailActivity.this, PlanCreateActivity.class);
                planIntent.putExtra(PlanCreateActivity.MODE, PlanCreateActivity.MODE_MODIFY);
                planIntent.putExtra(PlanCreateActivity.PLAN_ID, mTarPlanID);
                planIntent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                planIntent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
                planIntent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                startActivity(planIntent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPlanDetail();
    }

    public void loadPlanDetail() {
        I2ConnectApi.requestJSON(PlanDetailActivity.this, I2UrlHelper.Plan.getViewSnsPlan(mTarPlanID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsPlan onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsPlan onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(PlanDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            try {
                                final String userNm = FormatUtil.getStringValidate(statusInfo.getString("usr_nm"));
                                tvUsrNm.setText(userNm);
                                tvStartDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMMSS(statusInfo.getString("start_dttm")));
                                tvEndDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMMSS(statusInfo.getString("end_dttm")));
                                if (!statusInfo.isNull("plan_ttl")) {
                                    tvPlanTtl.setText(statusInfo.getString("plan_ttl"));
                                }
                                if (!statusInfo.isNull("place")) tvPlace.setText(statusInfo.getString("place"));
                                if (statusInfo.getString("plan_open_yn").equals("Y"))
                                    tvPlanOpenYN.setText("공개");
                                else
                                    tvPlanOpenYN.setText("비공개");
                                if (!statusInfo.isNull("plan_dtl_cntn")) tvPlanDtlCntn.setText(statusInfo.getString("plan_dtl_cntn"));


                                mTarObjTp = FormatUtil.getStringValidate(statusInfo.getString("tar_obj_tp_cd"));
                                mTarObjId = FormatUtil.getStringValidate(statusInfo.getString("tar_obj_id"));

                                // 대상
                                if(CodeConstant.TYPE_USER.equals(mTarObjTp)) {
                                    mTarObjTtl = userNm;
                                    tvTarObjTtl.setText(userNm);
                                    tvTarObjTtl.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(PlanDetailActivity.this, SNSDetailProfileActivity.class);
                                            intent.putExtra(SNSDetailProfileActivity.USR_ID, mTarObjId);
                                            intent.putExtra(SNSDetailProfileActivity.USR_NM, userNm);
                                            startActivity(intent);
                                        }
                                    });
                                } else if(CodeConstant.TYPE_GROUP.equals(mTarObjTp)) {
                                    loadGetGroupNm(mTarObjId);
                                }

                                if(statusInfo.getString("usr_id").equals(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID)) ) {
                                    llBottomBtns.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

    public void loadGetGroupNm(String groupId) {
        I2ConnectApi.requestJSON(PlanDetailActivity.this, I2UrlHelper.SNS.getViewSnsGroup(groupId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(PlanDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                final String grpNm = statusInfo.getString("grp_nm");
                                mTarObjTtl = grpNm;
                                tvTarObjTtl.setText(grpNm);
                                tvTarObjTtl.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(PlanDetailActivity.this, SNSDetailGroupActivity.class);
                                        intent.putExtra(SNSDetailGroupActivity.GRP_ID, mTarObjId);
                                        intent.putExtra(SNSDetailGroupActivity.GRP_NM, grpNm);
                                        startActivity(intent);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onError(e);
                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
