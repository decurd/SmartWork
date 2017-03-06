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
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConferenceDetailMemberListFragment extends Fragment {
    static String TAG = ConferenceDetailMemberListFragment.class.getSimpleName();

    public boolean checkRefreshLoading = false;

    private List<LinkedTreeMap<String, String>> mDataArray;
    protected ConferenceMemberRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView)view.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)view.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_cfrc_member_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
//        mURV.enableLoadmore();

        mDataArray = new ArrayList<>();
        mAdapter = new ConferenceMemberRecyclerViewAdapter(getActivity(), mDataArray);
        mURV.setAdapter(mAdapter);

        mURV.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "onRefresh1 checkRefreshLoading = " + checkRefreshLoading);
                if (!checkRefreshLoading) {
                    checkRefreshLoading = true;
                    Log.e(TAG, "onRefresh2 checkRefreshLoading = " + checkRefreshLoading);
                    mDataArray.clear();
                    loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId);
                }
            }
        });

//        mURV.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (Math.abs(dy) > CodeConstant.SCROLL_OFFSET) {
//                    if (dy > 0) {
//                        ((ConferenceDetailActivity) getActivity()).setVisibleFabButton(true);
//                    } else {
//                        ((ConferenceDetailActivity) getActivity()).setVisibleFabButton(false);
//                    }
//                }
//            }
//        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String tarObjTp, String tarObjId, String tarObjTtl, String tarCrtUsrId) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrId = tarCrtUsrId;
        Log.e(TAG, "mTarObjTp = " + mTarObjTp + "mTarObjId = " + mTarObjId + "mTarObjTtl = " + mTarObjTtl + "mTarCrtUsrId = " + mTarCrtUsrId);


        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Cfrc.getViewSnsConference(mTarObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, String>> userList = (List<LinkedTreeMap<String, String>>) statusInfo.get("ref_user_list");
                        if (userList.size() > 0) {
                            mDataArray.clear();
                            mDataArray.addAll(userList);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            setEmptyResult(mDataArray.size());
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
