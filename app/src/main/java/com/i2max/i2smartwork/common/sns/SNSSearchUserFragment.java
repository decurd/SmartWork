package com.i2max.i2smartwork.common.sns;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
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


public class SNSSearchUserFragment extends Fragment {
    static String TAG = SNSSearchUserFragment.class.getSimpleName();

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;

    protected List<JSONObject> mDataArray;
    protected SNSUserRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    protected int mListPage, mTotalCnt;
    private String mSearchStr, mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView)view.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)view.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_data_searched));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mURV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Log.e(TAG, "loadMore1 checkNextLoading = " + checkNextLoading);
                if (!checkNextLoading) {
                    if (mTotalCnt > mDataArray.size()) {
                        mListPage++;
                        loadSearchFriendList(mListPage, mSearchStr);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                    }
                    checkNextLoading = true;
                    Log.e(TAG, "loadMore2 checkNextLoading = " + checkNextLoading);
                }
            }
        });

        mURV.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "onRefresh1 checkRefreshLoading = " + checkRefreshLoading);
                if (!checkRefreshLoading) {
                    checkRefreshLoading = true;
                    Log.e(TAG, "onRefresh2 checkRefreshLoading = " + checkRefreshLoading);
                    mDataArray.clear();
                    mListPage = 1;
                    mSearchStr = "";
                    loadSearchFriendList(mListPage, mSearchStr);
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mSearchStr = "";
        mDataArray = new ArrayList<>();
        mAdapter = new SNSUserRecyclerViewAdapter(getActivity(), mDataArray, CodeConstant.MODE_SEARCH_ALL);
        mURV.setAdapter(mAdapter);

        loadSearchFriendList(mListPage, mSearchStr);

        return view;
    }

    public void clear() {
        if(mDataArray == null) return;
        mDataArray.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void loadRecyclerView(String searchStr) {
        mListPage = 1;
        mSearchStr = searchStr;
        Log.e(TAG, "mSearchStr = " + mSearchStr);

        loadSearchFriendList(mListPage, mSearchStr);

    }

    public void loadSearchFriendList(int page, String searchStr) {
        I2ConnectApi.requestJSON(getActivity(), I2UrlHelper.SNS.getListSnsUser(String.format("%d", page), searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsUser onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsUser onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
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

                                mDataArray.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            }

                            setEmptyResult(mDataArray.size());
                        } else {
                            Toast.makeText(getActivity(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setEmptyResult(int totalcnt) {
        if(totalcnt < 1) {
            mURV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mURV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
        }
    }


}
