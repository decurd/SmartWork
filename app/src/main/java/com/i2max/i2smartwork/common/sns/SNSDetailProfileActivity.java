package com.i2max.i2smartwork.common.sns;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Button;
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

public class SNSDetailProfileActivity extends BaseAppCompatActivity {
    static String TAG = SNSDetailProfileActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String USR_NM = "tar_usr_nm";

    public boolean checkLoading = false;
    protected int mListPage, mTotalCnt;
    protected String mSearchStr = "";

    protected List<JSONObject> mSnsDataArray;
    protected SNSUltimateRVAdapter mAdapter;
    protected UltimateRecyclerView mUltiRV;

    protected String mUsrID, mUsrName, mUsrPos, mUsrPhone, mUsrEmail, mFollowYN;
    protected CircleImageView mCivCrtUsrPhoto;
    protected TextView tvUsrNM, tvPosNm, tvDeptNm, tvWritePostCnt, tvRecvPostCnt, tvRecvLikeCnt;
    protected RelativeLayout rlFriend, rlFollow;
    protected ImageView ivFriend, ivFollow;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_detail_profile);

        Intent intent = getIntent();
        mUsrName = intent.getStringExtra(USR_NM);
        mUsrID = intent.getStringExtra(USR_ID);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mUsrName);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mUltiRV = (UltimateRecyclerView)findViewById(R.id.rv_sns_other);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUltiRV.setLayoutManager(layoutManager);
        mUltiRV.addItemDecoration(new SimpleDividerItemDecoration(this));
        mUltiRV.enableLoadmore();

        mSnsDataArray = new ArrayList<>();
        mAdapter = new SNSUltimateRVAdapter(SNSDetailProfileActivity.this, mSnsDataArray);
        mUltiRV.setAdapter(mAdapter);
        mUltiRV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (!checkLoading) {
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


        mCivCrtUsrPhoto = (CircleImageView)findViewById(R.id.civ_usr_photo);
        tvUsrNM = (TextView)findViewById(R.id.tv_usr_nm);
        tvPosNm = (TextView)findViewById(R.id.tv_pos_nm);
        tvDeptNm = (TextView)findViewById(R.id.tv_dept_nm);
        tvWritePostCnt = (TextView)findViewById(R.id.tv_write_post_cnt);
        tvRecvPostCnt = (TextView)findViewById(R.id.tv_recv_post_cnt);
        tvRecvLikeCnt = (TextView)findViewById(R.id.tv_recv_like_cnt);

        Button btnPhone = (Button) findViewById(R.id.btn_phone);
        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL); //ACTION_Call 다이렉트 전화
                callIntent.setData(Uri.parse("tel:" + mUsrPhone));
                startActivity(callIntent);
            }
        });
        Button btnEmail = (Button) findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + mUsrEmail)); // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "i2connect 모바일에서 발송");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });

        rlFollow = (RelativeLayout) findViewById(R.id.rl_follow);
        rlFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSnsFollow();
            }
        });
        ivFollow = (ImageView) findViewById(R.id.iv_follow);

        FloatingActionButton fabSnsWrite = (FloatingActionButton) findViewById(R.id.fab_sns_write);
        fabSnsWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent snsIntent = new Intent(SNSDetailProfileActivity.this, SNSWriteActivity.class);
                snsIntent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_USER);
                snsIntent.putExtra(CodeConstant.CUR_OBJ_ID, mUsrID);
                snsIntent.putExtra(SNSWriteActivity.TARGET_NM, mUsrName);
                startActivity(snsIntent);
            }
        });

        I2ConnectApi.requestJSON(SNSDetailProfileActivity.this, I2UrlHelper.SNS.getViewSNSUserProfile(mUsrID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                tvUsrNM.setText(statusInfo.getString("usr_nm"));
                                mUsrPos = statusInfo.getString("pos_nm");
                                if (statusInfo.getString("pos_nm") == "null") {
                                    tvPosNm.setText("");
                                    tvPosNm.setVisibility(View.INVISIBLE);
                                } else {
                                    tvPosNm.setText(statusInfo.getString("pos_nm"));
                                }

                                if (statusInfo.getString("dept_nm") == "null") {
                                    tvDeptNm.setText("");
                                    tvDeptNm.setVisibility(View.INVISIBLE);
                                } else {
                                    tvDeptNm.setText(statusInfo.getString("dept_nm"));
                                }
                                mUsrPhone = statusInfo.getString("phn_num");
                                mUsrEmail = statusInfo.getString("email");

                                tvWritePostCnt.setText(statusInfo.getString("write_post_cnt"));
                                tvRecvPostCnt.setText(statusInfo.getString("recv_post_cnt"));
                                tvRecvLikeCnt.setText(statusInfo.getString("recv_like_cnt"));

                                mFollowYN = statusInfo.getString("follow_yn");
                                setOnOffFollow(mFollowYN);

                                Glide.with(mCivCrtUsrPhoto.getContext())
                                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(statusInfo.getString("usr_photo_url"))))
                                        .error(R.drawable.ic_no_usr_photo)
                                        .fitCenter()
                                        .into(mCivCrtUsrPhoto);
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

        mListPage = 1;
        mSnsDataArray.clear();
        loadRecyclerView(mSearchStr);
    }

    public void loadRecyclerView(String searchStr) {
        I2ConnectApi.requestJSON(SNSDetailProfileActivity.this, I2UrlHelper.SNS.getListSNSByObjectId("USER", mUsrID, "FOLLOW", String.format("%d",mListPage), searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        //DialogUtil.hideCircularProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError = " + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailProfileActivity.this, e);
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
                            Toast.makeText(SNSDetailProfileActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setOnOffFollow(String yn) {
        if (yn.equals("Y")) {
            rlFollow.setBackgroundColor(getResources().getColor(R.color.bg_profile_on));
            ivFollow.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_follow_on));
        } else {
            rlFollow.setBackgroundColor(getResources().getColor(R.color.bg_profile_off));
            ivFollow.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_follow_off));
        }
    }

    public void toggleSnsFollow() {
        I2ConnectApi.requestJSON(SNSDetailProfileActivity.this, I2UrlHelper.SNS.toggleSnsFollow(mUsrID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onCompleted");
                        String msg = "";
                        if (mFollowYN.equals("Y")) {
                            mFollowYN = "N";
                            msg = "팔로잉을 해제 하였습니다.";
                        } else {
                            mFollowYN = "Y";
                            msg = "팔로잉 하였습니다.";
                        }

                        setOnOffFollow(mFollowYN);

                        DialogUtil.showInformationDialog(SNSDetailProfileActivity.this, msg);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onError");
                        //Error dialog 표시//Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.toggleSnsFollow onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

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
            case R.id.action_sns_function:
                Intent intent = new Intent(SNSDetailProfileActivity.this, SNSPersonalFunctionActivity.class);
                intent.putExtra(SNSPersonalFunctionActivity.USR_ID, mUsrID);
                intent.putExtra(SNSPersonalFunctionActivity.USR_NM_DEPT_NM, mUsrName + " " + mUsrPos );
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
