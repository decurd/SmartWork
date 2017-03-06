
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
import com.i2max.i2smartwork.common.task.TaskListRecyclerViewAdapter;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConferenceDetailTaskListFragment extends Fragment {
    static String TAG = ConferenceDetailTaskListFragment.class.getSimpleName();

    public boolean checkRefreshLoading = false;
    private List<LinkedTreeMap<String, Object>> mDataArray;
    protected TaskListRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView) v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_task_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
//        mURV.enableLoadmore();

        mDataArray = new ArrayList<LinkedTreeMap<String, Object>>();
        mAdapter = new TaskListRecyclerViewAdapter(getActivity(), mDataArray);
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

        return v;
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

        mAdapter.setTarObjInfo(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId);

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Task.getListSnsTaskByObjectId(mTarObjTp, mTarObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
//                        DialogUtil.hideCircularProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, Object>> taskList = (List<LinkedTreeMap<String, Object>>) statusInfo.get("taskList");
                        if (taskList.size() > 0) {
                            mDataArray.clear();
                            mDataArray.addAll(taskList);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            setEmptyResult(mDataArray.size());
                        }
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
