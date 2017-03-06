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

package com.i2max.i2smartwork.common.conference;

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

public class ConferenceMainListFragment extends Fragment {
    static String TAG = ConferenceMainListFragment.class.getSimpleName();

    public static final int CFRC_MODE_HODING = 0;
    public static final int CFRC_MODE_ALL = 1;
    public static final int CFRC_MODE_FINISH = 2;

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mPostMode, mListPage, mTotalCnt;
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
    protected ConferenceListRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_cfrc_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mStatusInfo = new ArrayList<LinkedTreeMap<String, Object>>();
        mAdapter = new ConferenceListRecyclerViewAdapter(getActivity(), mStatusInfo);
        mURV.setAdapter(mAdapter);

//        mURV.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (Math.abs(dy) > CodeConstant.SCROLL_OFFSET) {
//                    if (dy > 0) {
//                        ((MainActivity) getActivity()).setVisibleFabButton(true);
//                    } else {
//                        ((MainActivity) getActivity()).setVisibleFabButton(false);
//                    }
//                }
//            }
//        });

        mURV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Log.e(TAG, "loadMore1 checkNextLoading = "+ checkNextLoading);
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
                    mStatusInfo.clear();
                    mListPage = 1;
                    checkRefreshLoading = true;
                    loadRecyclerView("");
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

    @Override
    public void onResume() {
        super.onResume();
    }

    public void loadRecyclerView(String searchStr) {

        String mode = "";
        switch (mPostMode) {
            case CFRC_MODE_HODING :
                mode = "RDY";
                break;
            case CFRC_MODE_ALL :
                mode = "";
                break;
            case CFRC_MODE_FINISH :
                mode = "FIN";
                break;
        }

        mSearchStr = searchStr;
        Log.d(TAG, "mListPage = " + mListPage + " / mSearchStr =" + mSearchStr);

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Cfrc.getListSnsConference(mode, String.format("%d", mListPage), mSearchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                        Log.e(TAG, "I2UrlHelper.Conference.getListSnsConference onCompleted / next " + checkNextLoading + " / refresh = " + checkRefreshLoading);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getListSnsConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getListSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, Object>> listCfrc = (List<LinkedTreeMap<String, Object>>) statusInfo.get("list_data");
                        if (listCfrc != null && listCfrc.size() > 0) {
                            mTotalCnt = (int) Float.parseFloat(FormatUtil.getStringValidate(statusInfo.get("list_count")));
                            mStatusInfo.addAll(listCfrc);
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyResult(mStatusInfo.size());

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
            mTvEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }


    public void searchResult(String searchStr) {
        mStatusInfo.clear();
        mListPage = 1;

        loadRecyclerView(searchStr);
    }
}
