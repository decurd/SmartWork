package com.i2max.i2smartwork.common.sns;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.db.chart.Tools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.memo.MemoDetailActivity;
import com.i2max.i2smartwork.common.plan.PlanDetailActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
import com.i2max.i2smartwork.common.work.WorkDetailActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSGroupFunctionActivity extends BaseAppCompatActivity {
    protected static String TAG = SNSGroupFunctionActivity.class.getSimpleName();

    public static final String GRP_ID = "tar_grp_id";
    public static final String GRP_NM = "tar_grp_nm";

    protected final int MAX_CNT = 5;

    protected String mTarGrpID, mTarGrpNm;

    protected LinearLayout llMember;
    protected RelativeLayout rlGroupJoinApply, rlGroupProfile, rlPermitThrow;
    protected LinearLayout llPlan;
    protected TextView tvPlanTtl;
    protected Button btnLeaveGroup;
    protected JSONArray mMemberArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_group_function);

        Intent intent = getIntent();
        mTarGrpID = intent.getStringExtra(GRP_ID);
        mTarGrpNm = intent.getStringExtra(GRP_NM);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mTarGrpNm);

        llMember = (LinearLayout)findViewById(R.id.ll_member);

        Button btnMemberMore = (Button) findViewById(R.id.btn_member_more);
        btnMemberMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSGroupFunctionActivity.this, SNSGroupMemberActivity.class);
                intent.putExtra(SNSGroupMemberActivity.GRP_ID, mTarGrpID);
                intent.putExtra(SNSGroupMemberActivity.MODE, SNSGroupMemberActivity.MODE_MEMBER_LIST);
                startActivity(intent);
            }
        });

        RelativeLayout rlFile = (RelativeLayout)findViewById(R.id.rl_file);
        rlFile.setOnClickListener(rlClickListener);

        //RelativeLayout rlPicture = (RelativeLayout)findViewById(R.id.rl_picture);
       // rlPicture.setOnClickListener(rlClickListener);

//        RelativeLayout rlPlan = (RelativeLayout)findViewById(R.id.rl_plan);
//        rlPlan.setOnClickListener(rlClickListener);
//        rlPlan.setVisibility(View.GONE);
        llPlan = (LinearLayout)findViewById(R.id.ll_plan);
        tvPlanTtl = (TextView) findViewById(R.id.tv_plan_ttl);
        tvPlanTtl.setOnClickListener(rlClickListener);

        rlGroupJoinApply = (RelativeLayout)findViewById(R.id.rl_group_join_apply);
        rlGroupJoinApply.setOnClickListener(rlClickListener);

        //rlPicture.setVisibility(View.GONE);
        rlFile.setVisibility(View.GONE);
        rlGroupJoinApply.setVisibility(View.GONE);

        rlGroupProfile = (RelativeLayout)findViewById(R.id.rl_group_profile);
        rlGroupProfile.setOnClickListener(rlClickListener);

        rlPermitThrow = (RelativeLayout)findViewById(R.id.rl_permit_throw);
        rlPermitThrow.setOnClickListener(rlClickListener);

        btnLeaveGroup = (Button)findViewById(R.id.btn_leave_group);
        btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showConfirmDialog(SNSGroupFunctionActivity.this, "안내", "그룹탈퇴를 하시겠습니까?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        I2ConnectApi.requestJSON(SNSGroupFunctionActivity.this, I2UrlHelper.SNS.leaveGroupMember(mTarGrpID))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JSONObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onCompleted");
                                        String msg = "그룹을 탈퇴하였습니다.";

                                        DialogUtil.showInformationDialog(SNSGroupFunctionActivity.this, msg, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onError");
                                        //Error dialog 표시
                                        e.printStackTrace();
                                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupFunctionActivity.this, e);
                                    }

                                    @Override
                                    public void onNext(JSONObject jsonObject) {
                                        Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onNext");
                                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                                        } else {
                                            Toast.makeText(SNSGroupFunctionActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }
        });

        I2ConnectApi.requestJSON(SNSGroupFunctionActivity.this, I2UrlHelper.SNS.getViewSnsGroup(mTarGrpID))
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
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                if (statusInfo.getString("admin_yn").equals("Y")) {
//                                    rlGroupJoinApply.setVisibility(View.VISIBLE);
                                    rlGroupProfile.setVisibility(View.VISIBLE);
                                    rlPermitThrow.setVisibility(View.VISIBLE);
                                } else {
                                    rlGroupJoinApply.setVisibility(View.GONE);
                                    rlGroupProfile.setVisibility(View.GONE);
                                    rlPermitThrow.setVisibility(View.GONE);
                                    if (statusInfo.getString("join_status").equals("IN")) {
                                        btnLeaveGroup.setVisibility(View.VISIBLE);
                                    }
                                }

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

    @Override
    public void onResume() {
        super.onResume();

        loadMemberList();
        loadListSnsPlan();
    }

    public void loadMemberList() {
        I2ConnectApi.requestJSON(SNSGroupFunctionActivity.this, I2UrlHelper.SNS.getListGroupMember(mTarGrpID, "1", ""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            JSONArray statusInfoArray = I2ResponseParser.getJsonArray(statusInfo, "list_data");
                            mMemberArray = statusInfoArray;

                            addMemberList(mMemberArray);
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void addMemberList(JSONArray array) {
        llMember.removeAllViews();

        for (int i=0; i<array.length(); i++) {
            if (i >= MAX_CNT)
                break;

            try {
                JSONObject jsonObject = array.getJSONObject(i);
                CircleImageView civMember = new CircleImageView(this);
                addCivToLayout(civMember, llMember, jsonObject.getString("usr_photo_url"), i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addCivToLayout(CircleImageView civ, LinearLayout ll, String urlStr, Integer pos) {
        final float scale = getResources().getDisplayMetrics().density;
        int borderWH = (int) (34 * scale + 0.5f); // 34dp
        int civWH = (int) (32 * scale + 0.5f); // 32dp
        int rightMargin = (int) (10 * scale + 0.5f); // 32dp
        //TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics())

        RelativeLayout rl = new RelativeLayout(this);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(borderWH, borderWH );
        rl.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_menu_profile));
        llParams.setMargins(0, 0, rightMargin, 0);

        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(civWH, civWH );
        rlParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        civ.setLayoutParams(rlParams);
        rl.addView(civ);

        rl.setLayoutParams(llParams);

        ll.addView(rl, 0);

        rl.setTag(pos);

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSGroupFunctionActivity.this, SNSDetailProfileActivity.class);
                try {
                    intent.putExtra(SNSDetailProfileActivity.USR_ID, mMemberArray.getJSONObject((Integer) v.getTag()).getString("usr_id"));
                    intent.putExtra(SNSDetailProfileActivity.USR_NM, mMemberArray.getJSONObject((Integer) v.getTag()).getString("usr_nm"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });

        Glide.with(civ.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(urlStr)))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(civ);
    }


    public View.OnClickListener rlClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent;
            switch (v.getId()) {
                case R.id.rl_file :
                    intent = new Intent(SNSGroupFunctionActivity.this, SNSFileListActivity.class);
                    intent.putExtra(SNSFileListActivity.MODE, SNSFileListActivity.MODE_GRP);
                    intent.putExtra(SNSFileListActivity.GRP_ID, mTarGrpID);
                    startActivity(intent);
                    break;
                case R.id.ll_plan :
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                    String now = sdf.format(new Date());
                    Log.d(TAG, "rl_plan Onclick " + now);
//                    intent = new Intent(SNSGroupFunctionActivity.this, PlanListActivity.class);
//                    intent.putExtra(CodeConstant.TAR_OBJ_TP, CodeConstant.TYPE_GROUP);
//                    intent.putExtra(PlanListActivity.MONTH_TERM, 0);
//                    intent.putExtra(PlanListActivity.PLAN_ID, "");
//                    intent.putExtra(PlanListActivity.PLAN_DATE, now.substring(0,12));
//                    startActivity(intent);
                    break;
                case R.id.rl_group_join_apply :
                    intent = new Intent(SNSGroupFunctionActivity.this, SNSGroupJoinApplyListActivity.class);
                    intent.putExtra(SNSGroupJoinApplyListActivity.GRP_ID, mTarGrpID);
                    startActivity(intent);
                    break;
                case R.id.rl_group_profile :
                    intent = new Intent(SNSGroupFunctionActivity.this, SNSConfigGroupProfileActivity.class);
                    intent.putExtra(SNSConfigGroupProfileActivity.GRP_ID, mTarGrpID);
                    intent.putExtra(SNSConfigGroupProfileActivity.GRP_NM, mTarGrpNm);
                    startActivity(intent);
                    break;
                case R.id.rl_permit_throw :
                    intent = new Intent(SNSGroupFunctionActivity.this, SNSGroupMemberActivity.class);
                    intent.putExtra(SNSGroupMemberActivity.GRP_ID, mTarGrpID);
                    intent.putExtra(SNSGroupMemberActivity.MODE, SNSGroupMemberActivity.MODE_PERMIT_THROW);
                    startActivity(intent);
                    break;

            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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

    public void loadListSnsPlan() {

        I2ConnectApi.requestJSON(SNSGroupFunctionActivity.this, I2UrlHelper.Plan.getListSnsPlan(CodeConstant.TYPE_GROUP, mTarGrpID, "0"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                            List<JSONObject> statusInfoList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);
//                            makePlanRow(statusInfoList);
                        } else {
                            Toast.makeText(SNSGroupFunctionActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void makePlanRow(List<JSONObject> jsonList) {
        List<Map<String, Object>> infoMapList = new ArrayList<>();

//        for(int i=0; i<jsonList.size(); i++) {
//            Map<String, Object> infoMap = new Gson().fromJson(jsonList.get(i).toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
//            infoMapList.add(infoMap);
//        }

        // 날짜순 정렬
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                return m1.get("start_dttm").toString().compareTo(m2.get("start_dttm").toString());
            }
        };
        Collections.sort(infoMapList, mapComparator);

        int m = 0;
        llPlan.removeAllViews();
        for(int i=infoMapList.size()-1; i>=0; i--) {

            if (m==3) break;

            LinearLayout llSub = new LinearLayout(SNSGroupFunctionActivity.this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llSub.setPadding((int) Tools.fromDpToPx(50), (int) Tools.fromDpToPx(10), (int) Tools.fromDpToPx(10), (int) Tools.fromDpToPx(10));
            llSub.setOrientation(LinearLayout.HORIZONTAL);

            ImageView ivType = new ImageView(SNSGroupFunctionActivity.this);
            final String typeStr = FormatUtil.checkNullString(infoMapList.get(i).get("obj_tar_tp"));
            final Map<String, Object> infoMap = infoMapList.get(i);

            if (CodeConstant.TYPE_WORK.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_mile));
            } else if (CodeConstant.TYPE_MILE.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_mile));
            } else if (CodeConstant.TYPE_MEMO.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_memo));
            } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_conference));
            } else if (CodeConstant.TYPE_TASK.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_task));
            } else if (CodeConstant.TYPE_GROUP.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_user));
            } else if (CodeConstant.TYPE_USER.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSGroupFunctionActivity.this, R.drawable.ic_type_user));
            }
            LinearLayout.LayoutParams ivP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ivP.setMargins(0,0,(int)Tools.fromDpToPx(10),0);
            llSub.addView(ivType, ivP);

            ContextThemeWrapper newContext1 = new ContextThemeWrapper(SNSGroupFunctionActivity.this, R.style.TextListMediumDarkGray);
            TextView tvDttm = new TextView(newContext1);
            LinearLayout.LayoutParams tvP1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvP1.weight = 0.5f;
            String kYMDE = DateCalendarUtil.getKoreanShortYMDEFromYYYYMMDD(FormatUtil.checkNullString(infoMapList.get(i).get("start_dttm")).substring(0,8));
            String dttm = kYMDE + " " + FormatUtil.checkNullString(infoMapList.get(i).get("stime"));
            tvDttm.setText(dttm);
            llSub.addView(tvDttm, tvP1);

            ContextThemeWrapper newContext2 = new ContextThemeWrapper(SNSGroupFunctionActivity.this, R.style.TextListMedium);
            TextView tvPlanDtlCntn = new TextView(newContext2);
            LinearLayout.LayoutParams tvP2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvP2.weight = 0.5f;
            tvPlanDtlCntn.setText(FormatUtil.checkNullString(infoMapList.get(i).get("plan_ttl")));
            llSub.addView(tvPlanDtlCntn, tvP2);

            llPlan.addView(llSub, p);

            LinearLayout.LayoutParams divP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divP.setMargins((int) Tools.fromDpToPx(40), 0, 0, 0);

            View divView = new View(SNSGroupFunctionActivity.this);
            divView.setBackgroundColor(ContextCompat.getColor(SNSGroupFunctionActivity.this, R.color.line_color_list_divider));

            llPlan.addView(divView, divP);

            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            llSub.setBackgroundResource(backgroundResource);
            typedArray.recycle();
            llSub.setClickable(true);

            llSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "typeStr = " + typeStr);
                    linkTypeActivity(typeStr, infoMap);
                }
            });

            m++;
        }
    }

    public void linkTypeActivity(String typeStr, Map<String, Object> infoMap) {
        Intent intent = null;
        if (CodeConstant.TYPE_WORK.equals(typeStr)) {
            intent = new Intent(SNSGroupFunctionActivity.this, WorkDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.work_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("obj_tar_tp")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("obj_tar_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_MILE.equals(typeStr) ) {
            Toast.makeText(SNSGroupFunctionActivity.this, "마일스톤 일정은 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
        } else if (CodeConstant.TYPE_MEMO.equals(typeStr) ) {
            intent = new Intent(SNSGroupFunctionActivity.this, MemoDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.memo_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("obj_tar_tp")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("obj_tar_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
            intent = new Intent(SNSGroupFunctionActivity.this, ConferenceDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.cfrc_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("obj_tar_tp")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("obj_tar_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_TASK.equals(typeStr) ) {
            intent = new Intent(SNSGroupFunctionActivity.this, TaskDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.task_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("obj_tar_tp")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("obj_tar_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_GROUP.equals(typeStr)) {
            intent = new Intent(SNSGroupFunctionActivity.this, PlanDetailActivity.class);
            intent.putExtra(PlanDetailActivity.PLAN_ID, FormatUtil.checkNullString(infoMap.get("plan_id")));
            intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(FormatUtil.checkNullString(infoMap.get("start_dttm"))));
            startActivity(intent);
        } else if (CodeConstant.TYPE_USER.equals(typeStr)) {
            intent = new Intent(SNSGroupFunctionActivity.this, PlanDetailActivity.class);
            intent.putExtra(PlanDetailActivity.PLAN_ID, FormatUtil.checkNullString(infoMap.get("plan_id")));
            intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(FormatUtil.checkNullString(infoMap.get("start_dttm"))));
            startActivity(intent);
        }
    }
}
