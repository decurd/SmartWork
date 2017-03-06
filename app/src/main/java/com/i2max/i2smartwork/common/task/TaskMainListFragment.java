package com.i2max.i2smartwork.common.task;

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

public class TaskMainListFragment extends Fragment {
    static String TAG = TaskMainListFragment.class.getSimpleName();

    public static final int POST_MODE_HODING = 0;
    public static final int POST_MODE_ING = 1;
    public static final int POST_MODE_DELAY = 2;
    public static final int POST_MODE_FINISH = 3;

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mPostMode, mListPage, mTotalCnt;
    protected String mMode, mOptTask;
    public void setPostMode(int postMode) {
        mPostMode = postMode;
    }
    public void initListPage() {
        mListPage = 1;
        if (mStatusInfo != null && mStatusInfo.size() > 0)
            mStatusInfo.clear();
    }

    private List<LinkedTreeMap<String, Object>> mStatusInfo;
    protected TaskListRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_task_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mStatusInfo = new ArrayList<LinkedTreeMap<String, Object>>();
        mAdapter = new TaskListRecyclerViewAdapter(getActivity(), mStatusInfo);
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
                        loadRecyclerView("");
                    }
                    checkNextLoading = true;
                    Log.e(TAG, "loadMore2 checkNextLoading = " + checkNextLoading);
                }

            }
        });

        mURV.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "onRefresh1 checkRefreshLoading = "+ checkRefreshLoading);
                if (!checkRefreshLoading) {
                    checkRefreshLoading = true;
                    Log.e(TAG, "onRefresh2 checkRefreshLoading = " + checkRefreshLoading);
                    mStatusInfo.clear();
                    mListPage = 1;
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

    public void loadRecyclerView(String optTask) {
//        if(getContext() == null || DialogUtil.isCircularProgressLoding()) return;
//        DialogUtil.showCircularProgressDialog(getContext());

        mMode = "";
        switch (mPostMode) {
            case POST_MODE_HODING :
                mMode = "NST";
                break;
            case POST_MODE_ING :
                mMode = "ING";
                break;
            case POST_MODE_DELAY :
                mMode = "DLY";
                break;
            case POST_MODE_FINISH :
                mMode = "FIN";
                break;
        }

        Log.e(TAG, "mode = "+mMode + "optTask = "+optTask);
        mOptTask = optTask;

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Task.getListSnsTask(mMode, optTask, String.format("%d", mListPage)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
//                        DialogUtil.removeCircularProgressDialog();
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTask onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTask onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTask onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        List<LinkedTreeMap<String, Object>> listData = (List<LinkedTreeMap<String, Object>>) statusInfo.get("list_data");
                        if (listData != null && listData.size() > 0) {
                            mTotalCnt = (int)Float.parseFloat(FormatUtil.getStringValidate(statusInfo.get("list_count")));
                            mStatusInfo.addAll(listData);
                            mAdapter.notifyDataSetChanged();
                        }
                        setEmptyResult(listData.size());
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
