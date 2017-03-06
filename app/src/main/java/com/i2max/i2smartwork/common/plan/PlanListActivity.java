package com.i2max.i2smartwork.common.plan;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.common.memo.MemoDetailActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.i2max.i2smartwork.common.work.WorkDetailActivity;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PlanListActivity extends BaseAppCompatActivity {
    protected static String TAG = PlanListActivity.class.getSimpleName();

    public static final String MONTH_TERM = "month_term";
    public static final String PLAN_ID = "plan_id";
    public static final String PLAN_DATE = "plan_date";

    public static final String KEY_PLAN_DAY = "plan_day";
    public static final String KEY_LIST_MAP = "list_map";

    protected int mMonthTerm, mBeforeTerm, mAfterTerm, mPosFirstScroll;
    protected boolean checkLoading = false, checkFirstScrolled = false;
    protected String mTarObjTp, mTarPlanID, mTarPlanDate;

    protected List<JSONObject> mJsonList;
    protected List<Map<String, Object>> mPlanDataArray;
    protected UltimateRecyclerView mRecyclerView;
    protected PlanAdapter simpleAdapter = null;
    protected LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        Intent intent = getIntent();
        mTarObjTp =  intent.getStringExtra(CodeConstant.TAR_OBJ_TP);
        mMonthTerm = intent.getIntExtra(MONTH_TERM, 0);
        mTarPlanID = intent.getStringExtra(PLAN_ID);
        mTarPlanDate = intent.getStringExtra(PLAN_DATE);

        mBeforeTerm = mMonthTerm;
        mAfterTerm = mMonthTerm;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("일정리스트");

        mRecyclerView = (UltimateRecyclerView) findViewById(R.id.ultimate_recycler_view);

        mJsonList = new ArrayList<>();
        mPlanDataArray = new ArrayList<>();
        simpleAdapter = new PlanAdapter(mPlanDataArray);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(simpleAdapter);

        mRecyclerView.enableLoadmore();
        simpleAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.custom_bottom_progressbar, null));

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (!checkLoading) {
                    checkLoading = true;
                    mAfterTerm++;

                    loadListSnsPlan(mAfterTerm);
                }
            }
        });

        mRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!checkLoading) {
                    checkLoading = true;
                    mBeforeTerm--;

                    loadListSnsPlan(mBeforeTerm);
                }
            }
        });


        loadListSnsPlan(mMonthTerm);
    }

    public void loadListSnsPlan(int term) {

        String monthTerm = String.format("%d", term);
        Log.d(TAG, "monthTerm = " + monthTerm);

        I2ConnectApi.requestJSON(PlanListActivity.this, I2UrlHelper.Plan.getListSnsPlan(mTarObjTp, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID), monthTerm))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onCompleted");
                        checkLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(PlanListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsPlan onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoList != null && statusInfoList.size() > 0) {
                                for (int i=0; i<mJsonList.size(); i++) {
                                    try {
                                        // 중복데이터 제거
                                        String planID = mJsonList.get(i).getString("plan_id");
                                        for (int j=statusInfoList.size()-1; j>=0; j--) {
                                            if (planID.equals(statusInfoList.get(j).getString("plan_id"))) {
                                                statusInfoList.remove(j);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                makePlanListData(statusInfoList);
                                mJsonList.addAll(statusInfoList);
                                simpleAdapter.notifyDataSetChanged();

                                // 최초로딩 시 선택한 날짜에 해당하는 위치로 그리드 스크롤
                                if (!checkFirstScrolled) {

                                    String tarPlanDate = mTarPlanDate.substring(0, 8);
                                    for (int i=0; i<mPlanDataArray.size(); i++) {
                                        String planDay = mPlanDataArray.get(i).get(KEY_PLAN_DAY).toString().substring(0, 8);
                                        if (planDay.equals(tarPlanDate)) {
                                            mPosFirstScroll = i;
                                            mRecyclerView.scrollVerticallyToPosition(mPosFirstScroll);
                                            checkFirstScrolled = true;
                                            break;
                                        }
                                    }
                                }

                            } else {
                                mRecyclerView.reenableLoadmore();
                            }

                        } else {
                            Toast.makeText(PlanListActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void makePlanListData(List<JSONObject> jsonList) {
        for (int i=0; i<jsonList.size(); i++) {

            boolean isExist = false;
            for (int j=0; j<mPlanDataArray.size(); j++) {
                try {
                    Map<String, Object> planData = mPlanDataArray.get(j);
                    String planDay = planData.get(KEY_PLAN_DAY).toString();
                    String startDay = jsonList.get(i).getString("start_dttm").substring(0, 8);

                    if (planDay.equals(startDay)) {
                        List<Map<String, Object>> listMap = (List<Map<String, Object>>)mPlanDataArray.get(j).get(KEY_LIST_MAP);
                        Map<String, Object> subData = new Gson().fromJson(jsonList.get(i).toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
                        listMap.add(subData);
                        planData.put(KEY_LIST_MAP, listMap);
                        isExist = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!isExist) {
                try {
                    Map<String, Object> planData = new HashMap();
                    planData.put(KEY_PLAN_DAY, jsonList.get(i).getString("start_dttm").substring(0, 8));

                    List<Map<String, Object>> listMap = new ArrayList<>();
                    Map<String, Object> subData = new Gson().fromJson(jsonList.get(i).toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
                    listMap.add(subData);
                    planData.put(KEY_LIST_MAP, listMap);

                    mPlanDataArray.add(planData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // 날짜순 정렬
        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                return m1.get(KEY_PLAN_DAY).toString().compareTo(m2.get(KEY_PLAN_DAY).toString());
            }
        };
        Collections.sort(mPlanDataArray, mapComparator);
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

    public class PlanAdapter extends UltimateViewAdapter<PlanAdapter.PlanAdapterViewHolder> {

        private List<Map<String, Object>> mPlanList;

        public PlanAdapter(List<Map<String, Object>> planList) {
            this.mPlanList = planList;
        }

        @Override
        public int getAdapterItemCount() {
            return mPlanList.size();
        }

        @Override
        public long generateHeaderId(int i) {
            return 0;
        }

        @Override
        public PlanAdapterViewHolder getViewHolder(View view) {
            return new PlanAdapterViewHolder(view, false);
        }

        @Override
        public PlanAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_plan, parent, false);
            return new PlanAdapterViewHolder(v, true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return null;
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {}

        public class PlanAdapterViewHolder extends UltimateRecyclerviewViewHolder {

            TextView mTvPlanDay;
            LinearLayout mLlSubPlan;

            public  PlanAdapterViewHolder(View itemView, boolean isItem) {
                super(itemView);

                if (isItem) {
                    mTvPlanDay = (TextView) itemView.findViewById(R.id.tv_plan_day);
                    mLlSubPlan = (LinearLayout) itemView.findViewById(R.id.ll_sub_plan);
                }

            }

            @Override
            public void onItemSelected() {
                itemView.setBackgroundColor(Color.LTGRAY);
            }

            @Override
            public void onItemClear() {
                itemView.setBackgroundColor(0);
            }
        }

        public Map<String, Object> getItem(int position) {
            if (customHeaderView != null)
                position--;
            if (position < mPlanList.size())
                return mPlanList.get(position);
            else return null;
        }

        @Override
        public void onBindViewHolder(final PlanAdapterViewHolder holder, int pos) {
            //Log.d(TAG, "onBindViewHolder size = " + mPlanList.size() + " / pos = " + pos);

            if (pos < getItemCount() && (customHeaderView != null ? pos <= mPlanList.size() : pos < mPlanList.size()) && (customHeaderView == null || pos > 0)) {

                holder.mTvPlanDay.setText(DateCalendarUtil.getKoreanYMDEFromYYYYMMDD(mPlanList.get(pos).get(KEY_PLAN_DAY).toString()) );
                List<Map<String, Object>> listMap = (List<Map<String, Object>>)mPlanList.get(pos).get(KEY_LIST_MAP);
                if (listMap.size() > 1) {
                    // 시간순 정렬
                    Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
                        public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                            return m1.get("stime").toString().compareTo(m2.get("stime").toString());
                        }
                    };
                    Collections.sort(listMap, mapComparator);
                }

                if (listMap.size() > 0) {
                    holder.mLlSubPlan.removeAllViews();

                    for(int i=0; i<listMap.size(); i++) {

                        LinearLayout llSub = new LinearLayout(PlanListActivity.this);
                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llSub.setPadding((int) Tools.fromDpToPx(16), (int) Tools.fromDpToPx(16), (int) Tools.fromDpToPx(16), (int) Tools.fromDpToPx(16));
                        llSub.setOrientation(LinearLayout.HORIZONTAL);

                        ImageView ivType = new ImageView(PlanListActivity.this);
                        final String typeStr = nullCheck(listMap.get(i).get("tar_obj_tp_cd"));
                        final Map<String, Object> infoMap = listMap.get(i);

                        if (CodeConstant.TYPE_WORK.equals(typeStr)) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_mile));
                        } else if (CodeConstant.TYPE_MILE.equals(typeStr) ) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_mile));
                        } else if (CodeConstant.TYPE_MEMO.equals(typeStr)) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_memo));
                        } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_conference));
                        } else if (CodeConstant.TYPE_TASK.equals(typeStr) ) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_task));
                        } else if (CodeConstant.TYPE_GROUP.equals(typeStr) ) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_user));
                        } else if (CodeConstant.TYPE_USER.equals(typeStr) ) {
                            ivType.setImageDrawable(ContextCompat.getDrawable(PlanListActivity.this, R.drawable.ic_type_user));
                        }
                        LinearLayout.LayoutParams ivP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ivP.setMargins(0,0,(int)Tools.fromDpToPx(10),0);
                        llSub.addView(ivType, ivP);

                        ContextThemeWrapper newContext1 = new ContextThemeWrapper(PlanListActivity.this, R.style.TextListMediumPrimary);
                        TextView tvDttm = new TextView(newContext1);
                        LinearLayout.LayoutParams tvP1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tvP1.weight = 0.3f;
                        if (nullCheck(listMap.get(i).get("plan_tp_cd")).equals("ALDD")) {
                            tvDttm.setText("하루종일");
                        } else {
                            tvDttm.setText(nullCheck(listMap.get(i).get("stime")) + " ~ " + nullCheck(listMap.get(i).get("etime")));
                        }
                        llSub.addView(tvDttm, tvP1);

                        ContextThemeWrapper newContext2 = new ContextThemeWrapper(PlanListActivity.this, R.style.TextListMedium);
                        TextView tvPlanDtlCntn = new TextView(newContext2);
                        LinearLayout.LayoutParams tvP2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tvP2.weight = 0.7f;
                        tvPlanDtlCntn.setText(nullCheck(listMap.get(i).get("plan_dtl_cntn")));
                        llSub.addView(tvPlanDtlCntn, tvP2);

                        holder.mLlSubPlan.addView(llSub, p);

                        LinearLayout.LayoutParams divP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        divP.setMargins((int) Tools.fromDpToPx(40), 0, 0, 0);

                        View divView = new View(PlanListActivity.this);
                        divView.setBackgroundColor(ContextCompat.getColor(PlanListActivity.this, R.color.line_color_list_divider));

                        holder.mLlSubPlan.addView(divView, divP);

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
                    }
                }
            }
        }

        public void linkTypeActivity(String typeStr, Map<String, Object> infoMap) {
            Intent intent = null;
            Class claszz = null;
            if (CodeConstant.TYPE_WORK.equals(typeStr)) {
                intent = new Intent(PlanListActivity.this, WorkDetailActivity.class);
                intent.putExtra(CodeConstant.TITLE, getString(R.string.work_detail));
                intent.putExtra(CodeConstant.CUR_OBJ_TP, nullCheck(infoMap.get("tar_obj_tp_cd")));
                intent.putExtra(CodeConstant.CUR_OBJ_ID, nullCheck(infoMap.get("tar_obj_id")));
                intent.putExtra(CodeConstant.CRT_USR_ID, nullCheck(infoMap.get("crt_usr_id")));
            } else if (CodeConstant.TYPE_MILE.equals(typeStr)) {
                Toast.makeText(PlanListActivity.this, "마일스톤 일정은 아직 지원하지 않습니다", Toast.LENGTH_LONG).show();
            } else if (CodeConstant.TYPE_MEMO.equals(typeStr) ) {
                intent = new Intent(PlanListActivity.this, MemoDetailActivity.class);
                intent.putExtra(CodeConstant.TITLE, getString(R.string.memo_detail));
                intent.putExtra(CodeConstant.CUR_OBJ_TP, nullCheck(infoMap.get("tar_obj_tp_cd")));
                intent.putExtra(CodeConstant.CUR_OBJ_ID, nullCheck(infoMap.get("tar_obj_id")));
                intent.putExtra(CodeConstant.CRT_USR_ID, nullCheck(infoMap.get("crt_usr_id")));
            } else if (CodeConstant.TYPE_CFRC.equals(typeStr)) {
                intent = new Intent(PlanListActivity.this, ConferenceDetailActivity.class);
                intent.putExtra(CodeConstant.TITLE, getString(R.string.cfrc_detail));
                intent.putExtra(CodeConstant.CUR_OBJ_TP, nullCheck(infoMap.get("tar_obj_tp_cd")));
                intent.putExtra(CodeConstant.CUR_OBJ_ID, nullCheck(infoMap.get("tar_obj_id")));
                intent.putExtra(CodeConstant.CRT_USR_ID, nullCheck(infoMap.get("crt_usr_id")));
            } else if (CodeConstant.TYPE_TASK.equals(typeStr)) {
                intent = new Intent(PlanListActivity.this, TaskDetailActivity.class);
                intent.putExtra(CodeConstant.TITLE, getString(R.string.task_detail));
                intent.putExtra(CodeConstant.CUR_OBJ_TP, nullCheck(infoMap.get("tar_obj_tp_cd")));
                intent.putExtra(CodeConstant.CUR_OBJ_ID, nullCheck(infoMap.get("tar_obj_id")));
                intent.putExtra(CodeConstant.CRT_USR_ID, nullCheck(infoMap.get("crt_usr_id")));
            } else if (CodeConstant.TYPE_GROUP.equals(typeStr) ) {
                intent = new Intent(PlanListActivity.this, PlanDetailActivity.class);
                intent.putExtra(PlanDetailActivity.PLAN_ID, nullCheck(infoMap.get("plan_id")));
                intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(nullCheck(infoMap.get("start_dttm"))));
                startActivity(intent);
            } else if (CodeConstant.TYPE_USER.equals(typeStr) ) {
                intent = new Intent(PlanListActivity.this, PlanDetailActivity.class);
                intent.putExtra(PlanDetailActivity.PLAN_ID, nullCheck(infoMap.get("plan_id")));
                intent.putExtra(PlanDetailActivity.PLAN_DATE, DateCalendarUtil.getKoreaMDFromYYYYMMDDHHMMSS(nullCheck(infoMap.get("start_dttm"))));

            }
            if(intent != null) startActivity(intent);
        }

        public String nullCheck(Object objStr) {
            if (objStr == null) return "";

            return objStr.toString();
        }

    }
}
