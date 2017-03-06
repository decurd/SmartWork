package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.I2SearchActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.EndlessRecyclerOnScrollListener;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
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

public class SNSPersonSearchActivity extends BaseAppCompatActivity {
    static String TAG = SNSPersonSearchActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String MODE = "mode";

    public static final int REQUEST_FRIEND_SEARCH = 1004;

//    public static final int MODE_NORMAL = 0;
//    public static final int MODE_LINK_ADD = 1;
//    public static final int MODE_FOLLOWER = 2;
//    public static final int MODE_CFRC_MEMBER_ADD = 3;
//    public static final int MODE_TASK_MEMBER_ADD = 4;
//    public static final int MODE_MEMO_MEMBER_ADD = 5;
//    public static final int MODE_WORK_MEMBER_ADD = 6;

    protected int mMode = CodeConstant.MODE_SEARCH_ALL;

    protected String mTarUsrID;

    public boolean checkLoading = false;
    protected int mListPage, mTotalCnt;
    protected String mStrSearch;

    protected List<JSONObject> mFriendDataList;
    protected SNSUserRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;
    protected TextView mTvNoData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_friend_search);

        Intent intent = getIntent();
        mTarUsrID = intent.getStringExtra(USR_ID);
        mMode = intent.getIntExtra(MODE, CodeConstant.MODE_SEARCH_ALL);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setActionbarTitle(false);

        mTvNoData = (TextView) findViewById(R.id.tv_no_data);

        mRV = (RecyclerView) findViewById(R.id.rv_friend);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!checkLoading) {

                    if (mTotalCnt > mFriendDataList.size()) {
                        mListPage++;

                        if (mStrSearch.trim().length() > 0) { //전체검색
                            loadSearchFriendList(mListPage, mStrSearch);
                        } else { //팔로워/팔로잉 요청
                            loadFollowList();
                        }
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mFriendDataList = new ArrayList<>();
        mAdapter = new SNSUserRecyclerViewAdapter(SNSPersonSearchActivity.this, mFriendDataList, mMode);
        mRV.setAdapter(mAdapter);

        loadFollowList();
    }

    public void setActionbarTitle(boolean isSearch) {
        if(isSearch) {
            getSupportActionBar().setTitle("전체 사용자 검색");
        } else {
            switch (mMode) {
                case CodeConstant.MODE_FOLLOWER:
                    getSupportActionBar().setTitle("팔로워 목록");
                    break;
                case CodeConstant.MODE_CFRC_MEMBER_ADD:
                    getSupportActionBar().setTitle("참가자 등록");
                    break;
                case CodeConstant.MODE_TASK_MEMBER_ADD:
                    getSupportActionBar().setTitle("담당자 등록");
                    break;
                case CodeConstant.MODE_MEMO_MEMBER_ADD:
                    getSupportActionBar().setTitle("담당자 등록");
                    break;
                default:
                    getSupportActionBar().setTitle("팔로잉 목록");
                    break;
            }
        }
    }

    public void loadFollowList() {
        if (mMode == CodeConstant.MODE_FOLLOWER) { //팔로워
            loadFollowerList(mListPage);
        } else {  //디폴트 팔로잉
            loadFollowingList(mListPage);
        }
    }

    public void loadFollowingList(int page) {

        I2ConnectApi.requestJSON(SNSPersonSearchActivity.this, I2UrlHelper.SNS.getListSNSFollowing(mTarUsrID, String.format("%d", page)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonSearchActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollowing onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");
                            if (statusInfoList != null && statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = statusInfo.getInt("list_total_cnt");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mFriendDataList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }

                            if (mFriendDataList.size() == 0) {
                                mTvNoData.setVisibility(View.VISIBLE);
                            } else {
                                mTvNoData.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadFollowerList(int page) {

        I2ConnectApi.requestJSON(SNSPersonSearchActivity.this, I2UrlHelper.SNS.getListSNSFollower(mTarUsrID, String.format("%d", page)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonSearchActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSNSFollower onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoList != null && statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = statusInfo.getInt("list_total_cnt");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mFriendDataList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }

                            if (mFriendDataList.size() == 0) {
                                mTvNoData.setVisibility(View.VISIBLE);
                            } else {
                                mTvNoData.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadSearchFriendList(int page, String searchStr) {
        I2ConnectApi.requestJSON(SNSPersonSearchActivity.this, I2UrlHelper.SNS.getListSnsUser(String.format("%d", page), searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsUser onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsUser onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSPersonSearchActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsUser onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoList != null && statusInfoList.size() > 0) {
                                try {
                                    //TODO
                                    mTotalCnt = statusInfo.getInt("list_total_count");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                mFriendDataList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }

                            if (mFriendDataList.size() == 0) {
                                mTvNoData.setVisibility(View.VISIBLE);
                            } else {
                                mTvNoData.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(SNSPersonSearchActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setResultFinish(String usrID, String usrNM, String usrImg) {
        Log.e(TAG, "usrNM = "+ usrNM);
        Intent data = getIntent();
        data.putExtra(CodeConstant.USR_ID, usrID);
        data.putExtra(CodeConstant.USR_NM, usrNM);
        data.putExtra(CodeConstant.USR_IMG, usrImg);
        setResult(RESULT_OK, data);
        finish();
    }

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
                Intent i2Search = new Intent(this, I2SearchActivity.class);
                i2Search.putExtra(I2SearchActivity.START_POS, I2SearchActivity.RIGHT_1);
                i2Search.putExtra(I2SearchActivity.SEARCH_STR, mStrSearch);
                startActivityForResult(i2Search, I2SearchActivity.REQUEST_SEARCH);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == I2SearchActivity.REQUEST_SEARCH && resultCode == RESULT_OK) {
            String searchStr = data.getExtras().getString(I2SearchActivity.EXTRA_SEARCH_STR);
            mStrSearch = searchStr;
            mListPage = 1;
            mFriendDataList.clear();

            if (mMode == CodeConstant.MODE_FOLLOWER) {
                mMode = CodeConstant.MODE_SEARCH_ALL;
                mAdapter.setMode(mMode);
            }

            if (mStrSearch.trim().length() > 0) { //검색어가 있으면, 전체 검색
                setActionbarTitle(true);
                loadSearchFriendList(mListPage, mStrSearch);
            } else { //없으면 팔로워 리스트 요청
                setActionbarTitle(false);
                loadFollowList(); //팔로워/팔로잉 요청
            }
        }
    }

}
