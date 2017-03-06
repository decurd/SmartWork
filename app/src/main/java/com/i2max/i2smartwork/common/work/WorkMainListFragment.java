/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i2max.i2smartwork.common.work;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WorkMainListFragment extends Fragment {
    static String TAG = WorkMainListFragment.class.getSimpleName();

    public static final int MODE_ALL     = 0;
    public static final int MODE_HOLD    = 1;
    public static final int MODE_ING     = 2;
    public static final int MODE_FINISH  = 3;
    public static final int MODE_DELAY   = 4;

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mPostMode, mListPage, mTotalCnt;
    protected String mMode;
    protected String mSearchStr = "";

    public void setPostMode(int postMode) {
        mPostMode = postMode;
    }

    public void initListPage() {
        mListPage = 1;
        if (mStatusInfo != null && mStatusInfo.size() > 0)
            mStatusInfo.clear();
    }

    private List<LinkedTreeMap<String, Object>> mStatusInfo;
    protected WorkListRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView) v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_work_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mStatusInfo = new ArrayList<LinkedTreeMap<String, Object>>();
        mAdapter = new WorkListRecyclerViewAdapter(getActivity(), mStatusInfo);
        mURV.setAdapter(mAdapter);

        mURV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Log.e(TAG, "loadMore1 checkNextLoading = " + checkNextLoading);
                if (!checkNextLoading) {
                    if (mTotalCnt > mStatusInfo.size()) {
                        mListPage++;
                        loadRecyclerView(mSearchStr);
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
                    mStatusInfo.clear();
                    mListPage = 1;
                    loadRecyclerView(mSearchStr);
                }
            }
        });

        mListPage = 1;

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String searchStr) {
//        if(getContext() == null || DialogUtil.isCircularProgressLoding()) return;
//        DialogUtil.showCircularProgressDialog(getContext());

        mMode = "";
        switch (mPostMode) {
            case MODE_ALL:
                mMode = "ALL";
                break;
            case MODE_HOLD:
                mMode = "NST";
                break;
            case MODE_ING:
                mMode = "ING";
                break;
            case MODE_FINISH:
                mMode = "FIN";
                break;
            case MODE_DELAY:
                mMode = "DLY";
                break;
        }
        Log.e(TAG, "mode = " + mMode);

        mSearchStr = searchStr;

        //work/listSnsWork.json?tar_usr_id=&page=1&pgrs_st=DLY&searchKeyword=&searchKeywordBefore=
        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Work.getListSnsWork(mMode, mSearchStr, mSearchStr, String.format("%d", mListPage)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
//                        DialogUtil.removeCircularProgressDialog();
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                        Log.d(TAG, "getListSnsWork onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "getListSnsWork onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "getListSnsWork onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, Object>> listData = (List<LinkedTreeMap<String, Object>>) statusInfo.get("list_data");
                        if (listData != null && listData.size() > 0) {
                            mTotalCnt = (int)Float.parseFloat(FormatUtil.getStringValidate(statusInfo.get("list_count")));
                            mStatusInfo.addAll(listData);
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyResult(mStatusInfo.size());
                    }
                });
    }

    public void setEmptyResult(int totalcnt) {
        if (totalcnt < 1) {
            mURV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mURV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);

        }
    }

}
