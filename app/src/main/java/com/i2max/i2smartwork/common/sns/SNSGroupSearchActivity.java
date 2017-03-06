package com.i2max.i2smartwork.common.sns;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.i2max.i2smartwork.I2SearchActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSGroupSearchActivity extends BaseAppCompatActivity {
    static String TAG = SNSGroupSearchActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";

    protected String mTarUsrID;

    public boolean checkLoading = false;
    protected int mMode, mListPage, mTotalCnt;
    protected String mStrSearch;

    protected List<JSONObject> mGroupDataList;
    protected SNSGroupRecyclerViewAdapter mAdapter;
    protected UltimateRecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_group_search);

        Intent intent = getIntent();
        mTarUsrID = intent.getStringExtra(USR_ID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("가입그룹목록");
        mMode = CodeConstant.MODE_USER_GROUP;

        mRecyclerView = (UltimateRecyclerView) findViewById(R.id.rv_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.enableLoadmore();

        mRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (!checkLoading && (mTotalCnt > mGroupDataList.size())) {
                    mListPage++;
                    loadListGroup();

                    checkLoading = true;
                }
            }
        });

        FloatingActionButton fabGroupCreate = (FloatingActionButton) findViewById(R.id.fab_group_create);
        fabGroupCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent snsIntent = new Intent(SNSGroupSearchActivity.this, SNSGroupCreateActivity.class);
                startActivityForResult(snsIntent,RESULT_OK);
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mGroupDataList = new ArrayList<>();
        mAdapter = new SNSGroupRecyclerViewAdapter(SNSGroupSearchActivity.this, mGroupDataList, mMode);
        mRecyclerView.setAdapter(mAdapter);

        //TODO 재귀호출하여 무한루프 돔 : 확인 요망
        //loadListUserGroup(String.format("%d", mListPage), mStrSearch);
    }

    public void loadListGroup() {
        if (mMode == CodeConstant.MODE_USER_GROUP) {
            loadListUserGroup(String.format("%d", mListPage), mStrSearch);
        } else if (mMode == CodeConstant.MODE_ALL_GROUP) {
            loadListSnsGroup(String.format("%d", mListPage), mStrSearch);
        }
    }

    public void loadListUserGroup(String listPage, String searchStr) {

        I2ConnectApi.requestJSON(SNSGroupSearchActivity.this, I2UrlHelper.SNS.getListUserGroup(mTarUsrID, listPage, searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupSearchActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");
                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = statusInfo.getInt("list_count");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mGroupDataList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }

//                            if (mGroupDataList.size() == 0) {
//                                mTvNoData.setVisibility(View.VISIBLE);
//                            } else {
//                                mTvNoData.setVisibility(View.GONE);
//                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadListSnsGroup(String listPage, String searchStr) {

        I2ConnectApi.requestJSON(SNSGroupSearchActivity.this, I2UrlHelper.SNS.getListSnsGroup(listPage, searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupSearchActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");
                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = statusInfo.getInt("list_count");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mGroupDataList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_group_search_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_change_group:

                if (mMode == CodeConstant.MODE_USER_GROUP) {
                    mMode = CodeConstant.MODE_ALL_GROUP;
                    getSupportActionBar().setTitle("전체그룹목록");
                    item.setTitle("가입그룹");

                } else if (mMode == CodeConstant.MODE_ALL_GROUP) {
                    mMode = CodeConstant.MODE_USER_GROUP;
                    getSupportActionBar().setTitle("가입그룹목록");
                    item.setTitle("전체그룹");
                }

                mTotalCnt = 0;
                mListPage = 1;
                mStrSearch = "";
                mGroupDataList.clear();
                loadListGroup();
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
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mStrSearch = "";
        mListPage = 1;
        mGroupDataList.clear();
        loadListGroup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == I2SearchActivity.REQUEST_SEARCH && resultCode == RESULT_OK) {
            String searchStr = data.getExtras().getString(I2SearchActivity.EXTRA_SEARCH_STR);
            mStrSearch = searchStr;
            mListPage = 1;
            mGroupDataList.clear();
            loadListGroup();
        }
    }

}
