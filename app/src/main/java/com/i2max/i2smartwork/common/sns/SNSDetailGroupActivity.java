package com.i2max.i2smartwork.common.sns;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSDetailGroupActivity extends BaseAppCompatActivity {
    static String TAG = SNSDetailGroupActivity.class.getSimpleName();

    public static final String GRP_ID = "tar_grp_id";
    public static final String GRP_NM = "tar_grp_nm";

    public boolean checkLoading = false;
    protected int mListPage, mTotalCnt;
    protected String mSearchStr = "";

    protected List<JSONObject> mSnsDataArray;
    protected SNSUltimateRVAdapter mAdapter;
    protected UltimateRecyclerView mUltiRV;

    protected String mGrpID, mGrpNm, mJoinStatus;
    protected CircleImageView civGrpPhoto;
    protected TextView tvGrpNM, tvGrpIntro;
    protected RelativeLayout rlJoin;
    protected ImageView ivJoin;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_detail_group);

        Intent intent = getIntent();
        mGrpNm = intent.getStringExtra(GRP_NM);
        mGrpID = intent.getStringExtra(GRP_ID);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mGrpNm);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mUltiRV = (UltimateRecyclerView)findViewById(R.id.rv_sns_other);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUltiRV.setLayoutManager(layoutManager);
        mUltiRV.addItemDecoration(new SimpleDividerItemDecoration(this));
        mUltiRV.enableLoadmore();

        mSnsDataArray = new ArrayList<>();
        mAdapter = new SNSUltimateRVAdapter(SNSDetailGroupActivity.this, mSnsDataArray);
        mUltiRV.setAdapter(mAdapter);
        mUltiRV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (!checkLoading && (mTotalCnt > mSnsDataArray.size())) {
                    if (mTotalCnt > mSnsDataArray.size()) {
                        mListPage++;

                        loadRecyclerView(mSearchStr);
                    }
                    checkLoading = true;
                }

            }
        });

        mUltiRV.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!checkLoading) {
                    mSnsDataArray.clear();
                    mListPage = 1;
                    loadRecyclerView("");

                    checkLoading = true;
                }
            }
        });

        civGrpPhoto = (CircleImageView)findViewById(R.id.civ_grp_photo);
        tvGrpNM = (TextView)findViewById(R.id.tv_grp_nm);
        tvGrpIntro = (TextView)findViewById(R.id.tv_grp_intro);

        FloatingActionButton fabSnsWrite = (FloatingActionButton) findViewById(R.id.fab_sns_write);
        fabSnsWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent snsIntent = new Intent(SNSDetailGroupActivity.this, SNSWriteActivity.class);
                snsIntent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_GROUP);
                snsIntent.putExtra(CodeConstant.CUR_OBJ_ID, mGrpID);
                snsIntent.putExtra(SNSWriteActivity.TARGET_NM, mGrpNm);
                startActivity(snsIntent);
            }
        });

        rlJoin = (RelativeLayout) findViewById(R.id.rl_join);
        ivJoin = (ImageView) findViewById(R.id.iv_join);

        I2ConnectApi.requestJSON(SNSDetailGroupActivity.this, I2UrlHelper.SNS.getViewSnsGroup(mGrpID))
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
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailGroupActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                tvGrpNM.setText(statusInfo.getString("grp_nm"));
                                if (!statusInfo.isNull("grp_intro"))
                                    tvGrpIntro.setText(statusInfo.getString("grp_intro"));

                                mJoinStatus = statusInfo.getString("join_status");
                                setOnOffJoin(mJoinStatus);

                                Glide.with(civGrpPhoto.getContext())
                                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(statusInfo.getString("grp_photo_url"))))
                                        .error(R.drawable.ic_no_grp_photo)
                                        .fitCenter()
                                        .into(civGrpPhoto);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                onError(e);
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        rlJoin .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //kwc 그룹탈퇴를 누르는 이벤트 조건입니다. 수정사항 처리하겠습니다.
                if (mJoinStatus.equals("IN")) {
//                    DialogUtil.showConfirmDialog(SNSDetailGroupActivity.this, "안내", "그룹탈퇴를 하시겠습니까?", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            I2ConnectApi.requestJSON(SNSDetailGroupActivity.this, I2UrlHelper.SNS.leaveGroupMember(mGrpID))
//                                    .subscribeOn(Schedulers.newThread())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(new Subscriber<JSONObject>() {
//                                        @Override
//                                        public void onCompleted() {
//                                            Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onCompleted");
//                                            String msg = "그룹을 탈퇴하였습니다.";
//                                            DialogUtil.showInformationDialog(SNSDetailGroupActivity.this, msg, new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    finish();
//                                                }
//                                            });
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                            Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onError");
//                                            //Error dialog 표시
//                                            e.printStackTrace();
//                                            DialogUtil.showErrorDialogWithValidateSession(SNSDetailGroupActivity.this, e);
//                                        }
//
//                                        @Override
//                                        public void onNext(JSONObject jsonObject) {
//                                            Log.d(TAG, "I2UrlHelper.SNS.leaveGroupMember onNext");
//                                            if (I2ResponseParser.checkReponseStatus(jsonObject)) {
//
//                                            } else {
//                                                Toast.makeText(SNSDetailGroupActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
//                        }
//                    });
                } else {
                    I2ConnectApi.requestJSON(SNSDetailGroupActivity.this, I2UrlHelper.SNS.joinApply(mGrpID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JSONObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "I2UrlHelper.SNS.joinApply onCompleted");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "I2UrlHelper.SNS.joinApply onError");
                                    //Error dialog 표시
                                    e.printStackTrace();
                                    DialogUtil.showErrorDialogWithValidateSession(SNSDetailGroupActivity.this, e);
                                }

                                @Override
                                public void onNext(JSONObject jsonObject) {
                                    Log.d(TAG, "I2UrlHelper.SNS.joinApply onNext");
                                    if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                        try {
                                            DialogUtil.showInformationDialog(SNSDetailGroupActivity.this, statusInfo.getString("join_msg"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        Toast.makeText(SNSDetailGroupActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mListPage = 1;
        loadRecyclerView("");
    }

    public void loadRecyclerView(String searchStr) {

        I2ConnectApi.requestJSON(SNSDetailGroupActivity.this, I2UrlHelper.SNS.getListSNSByObjectId("GROUP", mGrpID, "", String.format("%d", mListPage), searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        //DialogUtil.hideCircularProgressDialog();
                        checkLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError = " + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailGroupActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {

                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> listPost = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_post");

                            if (listPost.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfo.getString("list_post_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mSnsDataArray.addAll(listPost);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(SNSDetailGroupActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void setOnOffJoin(String status) {
        if (status.equals("IN")) {
            rlJoin.setBackgroundColor(getResources().getColor(R.color.bg_profile_off));
            ivJoin.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_join_off));
        } else {
            rlJoin.setBackgroundColor(getResources().getColor(R.color.bg_profile_on));
            ivJoin.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_join_on));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sns_function:
                Intent intent = new Intent(SNSDetailGroupActivity.this, SNSGroupFunctionActivity.class);
                intent.putExtra(SNSGroupFunctionActivity.GRP_ID, mGrpID);
                intent.putExtra(SNSGroupFunctionActivity.GRP_NM, mGrpNm);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_detail_activity, menu);
        return true;
    }
}
