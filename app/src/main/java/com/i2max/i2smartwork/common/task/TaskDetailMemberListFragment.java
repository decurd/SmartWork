package com.i2max.i2smartwork.common.task;

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

public class TaskDetailMemberListFragment extends Fragment {
    static String TAG = TaskDetailMemberListFragment.class.getSimpleName();

    private List<LinkedTreeMap<String, String>> mMemberList;
    protected TaskDetailMemberRecyclerViewAdapter mAdapter;

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
//        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (Math.abs(dy) > CodeConstant.SCROLL_OFFSET) {
//                    if (dy > 0) {
//                        ((TaskDetailActivity) getActivity()).setVisibleFabButton(true);
//                    } else {
//                        ((TaskDetailActivity) getActivity()).setVisibleFabButton(false);
//                    }
//                }
//            }
//
//            @Override
//            public void onLoadMore(int current_page) {
//            }
//
//        });

        mMemberList = new ArrayList<LinkedTreeMap<String, String>>();
        mAdapter = new TaskDetailMemberRecyclerViewAdapter(getActivity(), mMemberList);
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

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Task.getViewSnsTask(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");

                        if (statusInfo != null) {
                            mMemberList.clear();
                            mMemberList.addAll((List<LinkedTreeMap<String, String>>) statusInfo.get("ref_user_list"));
                            Log.e(TAG, mMemberList.toString());
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyResult(mMemberList.size());
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
