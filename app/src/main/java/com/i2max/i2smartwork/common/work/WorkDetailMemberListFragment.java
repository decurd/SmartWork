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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WorkDetailMemberListFragment extends Fragment {
    static String TAG = WorkDetailMemberListFragment.class.getSimpleName();

    private List<LinkedTreeMap<String, String>> mDataArray;
    protected WorkDetailMemberRecyclerViewAdapter mAdapter;

    protected RecyclerView mRV;
    protected TextView mTvEmpty;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId, mCurObjTp, mCurObjId, mCurCrtUsrID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        mRV = (RecyclerView)view.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)view.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_cfrc_member_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);
        mRV.addItemDecoration(new SimpleDividerItemDecoration(mRV.getContext()));

        mDataArray = new ArrayList<LinkedTreeMap<String, String>>();
        mAdapter = new WorkDetailMemberRecyclerViewAdapter(getActivity(), mDataArray);
        mRV.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String tarObjTp, String tarObjId, String tarObjTtl, String tarCrtUsrId, String curObjTp, String curObjId, String curCrtUsrId) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrId = tarCrtUsrId;
        mCurObjTp = curObjTp;
        mCurObjId = curObjId;
        mCurCrtUsrID = curCrtUsrId;

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Work.getViewSnsWork(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, String>> datalist = (List<LinkedTreeMap<String, String>>) statusInfo.get("ref_user_list");
                        if (datalist != null && datalist.size() > 0) {
                            mDataArray.clear();
                            mDataArray.addAll(datalist);
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyResult(mDataArray.size());
                    }
                });
    }

    public void setEmptyResult(int totalcnt) {
        if(totalcnt < 1) {
            mRV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mRV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
        }
    }


}
