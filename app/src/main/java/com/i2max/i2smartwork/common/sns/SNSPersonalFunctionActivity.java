package com.i2max.i2smartwork.common.sns;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSPersonalFunctionActivity extends BaseAppCompatActivity {
    protected static String TAG = SNSPersonalFunctionActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String USR_NM_DEPT_NM = "tar_usr_nm_dept_nm";

    protected final int MAX_CNT = 5;
    protected final int TYPE_FOLLOWING = 0;
    protected final int TYPE_FOLLOWER = 1;
    protected final int TYPE_GROUP = 2;

    protected String mTarUsrID, mTarUsrNmDeptNm;
    protected TextView tvPlanTtl;
    protected LinearLayout llFollowing, llFollower, llGroup, llPlan;
    protected JSONArray mFollowingArray, mFollowerArray, mGroupArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_personal_function);

        Intent intent = getIntent();
        mTarUsrID = intent.getStringExtra(USR_ID);
        mTarUsrNmDeptNm = intent.getStringExtra(USR_NM_DEPT_NM);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mTarUsrNmDeptNm);

        llFollowing = (LinearLayout)findViewById(R.id.ll_following);
        llFollower = (LinearLayout)findViewById(R.id.ll_follower);
        llGroup = (LinearLayout)findViewById(R.id.ll_group);
        llPlan = (LinearLayout)findViewById(R.id.ll_plan);
        tvPlanTtl = (TextView) findViewById(R.id.tv_plan_ttl);
//        tvPlanTtl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
//                String now = sdf.format(new Date());
//                Intent planIntent = new Intent(SNSPersonalFunctionActivity.this, PlanListActivity.class);
//                planIntent.putExtra(CodeConstant.TAR_OBJ_TP, CodeConstant.TYPE_USER);
//                planIntent.putExtra(PlanListActivity.MONTH_TERM, 0);
//                planIntent.putExtra(PlanListActivity.PLAN_ID, "");
//                planIntent.putExtra(PlanListActivity.PLAN_DATE, now.substring(0,12));
//                startActivity(planIntent);
//            }
//        });

        Button btnFollowingMore = (Button) findViewById(R.id.btn_following_more);
        btnFollowingMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSPersonalFunctionActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, mTarUsrID);
                startActivity(intent);
            }
        });
        Button btnFollowerMore = (Button) findViewById(R.id.btn_follower_more);
        btnFollowerMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSPersonalFunctionActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, mTarUsrID);
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_FOLLOWER);
                startActivity(intent);
            }
        });

        Button btnGroupMore = (Button) findViewById(R.id.btn_group_more);
        btnGroupMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSPersonalFunctionActivity.this, SNSGroupSearchActivity.class);
                intent.putExtra(SNSGroupSearchActivity.USR_ID, mTarUsrID);
                startActivity(intent);
            }
        });

        RelativeLayout rlFile = (RelativeLayout)findViewById(R.id.rl_file);
        rlFile.setOnClickListener(rlClickListener);

        RelativeLayout rlPicture = (RelativeLayout)findViewById(R.id.rl_picture);
        rlPicture.setOnClickListener(rlClickListener);

        //TODO mobile v1.1.0 데모용 기능막음 향후삭제
        //rlFile.setVisibility(View.GONE);
        //rlPicture.setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "main onResume");

        loadFollowingList();
        loadFollowerList();
        loadGroupList();
        //TODO SERVER PLAN 미작업
        loadListSnsPlan();
    }

    public void loadFollowingList() {
        I2ConnectApi.requestJSON(SNSPersonalFunctionActivity.this, I2UrlHelper.SNS.getListSNSFollowing(mTarUsrID, "1"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonalFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            mFollowingArray = I2ResponseParser.getJsonArray(statusInfo, "list_data");
                            if (mFollowingArray != null)
                                addFollowingList(mFollowingArray);
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadFollowerList() {
        I2ConnectApi.requestJSON(SNSPersonalFunctionActivity.this, I2UrlHelper.SNS.getListSNSFollower(mTarUsrID, "1"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonalFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            mFollowerArray = I2ResponseParser.getJsonArray(statusInfo, "list_data");
                            if (mFollowerArray != null)
                                addFollowerList(mFollowerArray);
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadGroupList() {
        I2ConnectApi.requestJSON(SNSPersonalFunctionActivity.this, I2UrlHelper.SNS.getListUserGroup(mTarUsrID, "1", ""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonalFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            mGroupArray = I2ResponseParser.getJsonArray(statusInfo, "list_data");
                            if (mGroupArray != null)
                                addGroupList(mGroupArray);

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void addFollowingList(JSONArray array) {
        llFollowing.removeAllViews();

        for (int i=0; i<array.length(); i++) {
            if (i >= MAX_CNT)
                break;

            try {
                JSONObject jsonObject = array.getJSONObject(i);
                CircleImageView civFriend = new CircleImageView(this);
                addCivToLayout(civFriend, llFollowing, jsonObject.getString("usr_photo_url"), TYPE_FOLLOWING, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void addFollowerList(JSONArray array) {
        llFollower.removeAllViews();

        for (int i=0; i<array.length(); i++) {
            if (i >= MAX_CNT)
                break;

            try {
                JSONObject jsonObject = array.getJSONObject(i);
                CircleImageView civFriend = new CircleImageView(this);
                addCivToLayout(civFriend, llFollower, jsonObject.getString("usr_photo_url"), TYPE_FOLLOWER, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addGroupList(JSONArray array) {
        llGroup.removeAllViews();

        for (int i=0; i<array.length(); i++) {
            if (i >= MAX_CNT)
                break;

            try {
                JSONObject jsonObject = array.getJSONObject(i);
                CircleImageView civGroup = new CircleImageView(this);
                addCivToLayout(civGroup, llGroup, jsonObject.getString("grp_photo_url"), TYPE_GROUP, i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void addCivToLayout(CircleImageView civ, LinearLayout ll, String urlStr, final int type, Integer pos) {
        int borderWH = (int)Tools.fromDpToPx(34);
        int civWH = (int)Tools.fromDpToPx(32);
        int rightMargin = (int)Tools.fromDpToPx(10);
        int noImg = R.drawable.ic_no_usr_photo;
        if(TYPE_GROUP == type) {
            noImg = R.drawable.ic_no_grp_photo;
        }

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
                Intent intent = null;

                switch (type) {
                    case TYPE_FOLLOWING:
                        intent = new Intent(SNSPersonalFunctionActivity.this, SNSDetailProfileActivity.class);
                        try {
                            intent.putExtra(SNSDetailProfileActivity.USR_ID, mFollowingArray.getJSONObject((Integer) v.getTag()).getString("tar_usr_id"));
                            intent.putExtra(SNSDetailProfileActivity.USR_NM, mFollowingArray.getJSONObject((Integer) v.getTag()).getString("usr_nm"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case TYPE_FOLLOWER:
                        intent = new Intent(SNSPersonalFunctionActivity.this, SNSDetailProfileActivity.class);
                        try {
                            intent.putExtra(SNSDetailProfileActivity.USR_ID, mFollowerArray.getJSONObject((Integer) v.getTag()).getString("fllw_usr_id"));
                            intent.putExtra(SNSDetailProfileActivity.USR_NM, mFollowerArray.getJSONObject((Integer) v.getTag()).getString("usr_nm"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case TYPE_GROUP:
                        intent = new Intent(SNSPersonalFunctionActivity.this, SNSDetailGroupActivity.class);
                        try {
                            intent.putExtra(SNSDetailGroupActivity.GRP_ID, mGroupArray.getJSONObject((Integer) v.getTag()).getString("grp_id"));
                            intent.putExtra(SNSDetailGroupActivity.GRP_NM, mGroupArray.getJSONObject((Integer) v.getTag()).getString("grp_nm"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                startActivity(intent);
            }
        });

        Glide.with(civ.getContext())
                .load(I2UrlHelper.File.getUsrImage(urlStr))
                .error(noImg)
                .fitCenter()
                .into(civ);
    }

    public void loadListSnsPlan() {

        I2ConnectApi.requestJSON(SNSPersonalFunctionActivity.this, I2UrlHelper.Plan.getListSnsPlan(CodeConstant.TYPE_USER, mTarUsrID, "0"))
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
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonalFunctionActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onNext");
                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                        List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");
                        if (I2ResponseParser.checkReponseStatus(jsonObject) && statusInfoList != null) {
                            if(statusInfoList.size() <= 0) {
                                Toast.makeText(SNSPersonalFunctionActivity.this, getString(R.string.no_plan_data_available), Toast.LENGTH_SHORT).show();
                            }
                            makePlanRow(statusInfoList);
                        } else {
                            Toast.makeText(SNSPersonalFunctionActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void makePlanRow(List<JSONObject> jsonList) {

        List<Map<String, Object>> infoMapList = new ArrayList<>();

        for(int i=0; i<jsonList.size(); i++) {
            Map<String, Object> infoMap = new Gson().fromJson(jsonList.get(i).toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
            infoMapList.add(infoMap);
        }

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

            LinearLayout llSub = new LinearLayout(SNSPersonalFunctionActivity.this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llSub.setPadding((int) Tools.fromDpToPx(50), (int) Tools.fromDpToPx(10), (int) Tools.fromDpToPx(10), (int) Tools.fromDpToPx(10));
            llSub.setOrientation(LinearLayout.HORIZONTAL);

            ImageView ivType = new ImageView(SNSPersonalFunctionActivity.this);
            final String typeStr = FormatUtil.checkNullString(infoMapList.get(i).get("tar_obj_tp_cd"));
            final Map<String, Object> infoMap = infoMapList.get(i);

            if (CodeConstant.TYPE_WORK.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_mile));
            } else if (CodeConstant.TYPE_MILE.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_mile));
            } else if (CodeConstant.TYPE_MEMO.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_memo));
            } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_conference));
            } else if (CodeConstant.TYPE_TASK.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_task));
            } else if (CodeConstant.TYPE_GROUP.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_user));
            } else if (CodeConstant.TYPE_USER.equals(typeStr) ) {
                ivType.setImageDrawable(ContextCompat.getDrawable(SNSPersonalFunctionActivity.this, R.drawable.ic_type_user));
            }
            LinearLayout.LayoutParams ivP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ivP.setMargins(0,0,(int)Tools.fromDpToPx(10),0);
            llSub.addView(ivType, ivP);

            ContextThemeWrapper newContext1 = new ContextThemeWrapper(SNSPersonalFunctionActivity.this, R.style.TextListMediumDarkGray);
            TextView tvDttm = new TextView(newContext1);
            LinearLayout.LayoutParams tvP1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvP1.weight = 0.5f;
            String kYMDE = DateCalendarUtil.getKoreanShortYMDEFromYYYYMMDD(FormatUtil.checkNullString(infoMapList.get(i).get("start_dttm")).substring(0, 8));
            String dttm = kYMDE + " " + FormatUtil.checkNullString(infoMapList.get(i).get("stime"));
            tvDttm.setText(dttm);
            llSub.addView(tvDttm, tvP1);

            ContextThemeWrapper newContext2 = new ContextThemeWrapper(SNSPersonalFunctionActivity.this, R.style.TextListMedium);
            TextView tvPlanDtlCntn = new TextView(newContext2);
            LinearLayout.LayoutParams tvP2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvP2.weight = 0.5f;
            tvPlanDtlCntn.setText(FormatUtil.checkNullString(infoMapList.get(i).get("plan_ttl")));
            llSub.addView(tvPlanDtlCntn, tvP2);

            llPlan.addView(llSub, p);

            LinearLayout.LayoutParams divP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divP.setMargins((int) Tools.fromDpToPx(40), 0, 0, 0);

            View divView = new View(SNSPersonalFunctionActivity.this);
            divView.setBackgroundColor(ContextCompat.getColor(SNSPersonalFunctionActivity.this, R.color.line_color_list_divider));

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
        Intent intent;
        if (CodeConstant.TYPE_WORK.equals(typeStr)) {
            intent = new Intent(SNSPersonalFunctionActivity.this, WorkDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.work_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("tar_obj_tp_cd")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("tar_obj_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_MILE.equals(typeStr) ) {
            Toast.makeText(SNSPersonalFunctionActivity.this, "마일스톤 일정은 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
        } else if (CodeConstant.TYPE_MEMO.equals(typeStr) ) {
            intent = new Intent(SNSPersonalFunctionActivity.this, MemoDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.memo_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("tar_obj_tp_cd")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("tar_obj_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
            intent = new Intent(SNSPersonalFunctionActivity.this, ConferenceDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.cfrc_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("tar_obj_tp_cd")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("tar_obj_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_TASK.equals(typeStr) ) {
            intent = new Intent(SNSPersonalFunctionActivity.this, TaskDetailActivity.class);
            intent.putExtra(CodeConstant.TITLE, getString(R.string.task_detail));
            intent.putExtra(CodeConstant.CUR_OBJ_TP, FormatUtil.checkNullString(infoMap.get("tar_obj_tp_cd")));
            intent.putExtra(CodeConstant.CUR_OBJ_ID, FormatUtil.checkNullString(infoMap.get("tar_obj_id")));
            intent.putExtra(CodeConstant.CRT_USR_ID, FormatUtil.checkNullString(infoMap.get("crt_usr_id")));
            startActivity(intent);
        } else if (CodeConstant.TYPE_GROUP.equals(typeStr)) {
            intent = new Intent(SNSPersonalFunctionActivity.this, PlanDetailActivity.class);
            intent.putExtra(PlanDetailActivity.PLAN_ID, FormatUtil.checkNullString(infoMap.get("plan_id")));
            intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(FormatUtil.checkNullString(infoMap.get("start_dttm"))));
            startActivity(intent);
        } else if (CodeConstant.TYPE_USER.equals(typeStr) ) {
            intent = new Intent(SNSPersonalFunctionActivity.this, PlanDetailActivity.class);
            intent.putExtra(PlanDetailActivity.PLAN_ID, FormatUtil.checkNullString(infoMap.get("plan_id")));
            intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(FormatUtil.checkNullString(infoMap.get("start_dttm"))));
            startActivity(intent);
        }
    }

    public View.OnClickListener rlClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.rl_file :
                    Intent intent1 = new Intent(SNSPersonalFunctionActivity.this, SNSFileListActivity.class);
                    intent1.putExtra(SNSFileListActivity.MODE, SNSFileListActivity.MODE_USR);
                    intent1.putExtra(SNSFileListActivity.USR_ID, mTarUsrID);
                    startActivity(intent1);
                    break;
                case R.id.rl_picture :
                    Intent intent2 = new Intent(SNSPersonalFunctionActivity.this, SNSThumbnailListActivity.class);
                    intent2.putExtra(SNSThumbnailListActivity.USR_ID, mTarUsrID);
                    startActivity(intent2);
                    break;

            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_search_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                Intent intent = new Intent(this, SNSSearchActivity.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
