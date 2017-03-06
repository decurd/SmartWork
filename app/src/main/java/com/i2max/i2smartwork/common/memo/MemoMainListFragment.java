package com.i2max.i2smartwork.common.memo;

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

public class MemoMainListFragment extends Fragment {
    static String TAG = MemoMainListFragment.class.getSimpleName();

    public static final int MODE_TODO = 0;
    public static final int MODE_REFERENCE = 1;
    public static final int MODE_FINISH = 2;

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mPostMode, mListPage, mTotalCnt;
    protected String mMode, mOpt;
    public void setPostMode(int postMode) {
        mPostMode = postMode;
    }
    public void initListPage() {
        mListPage = 1;
        if (mStatusInfo != null && mStatusInfo.size() > 0)
            mStatusInfo.clear();
    }

    private List<LinkedTreeMap<String, Object>> mStatusInfo;
    protected MemoListRecyclerViewAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_memo_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mStatusInfo = new ArrayList<LinkedTreeMap<String, Object>>();
        mAdapter = new MemoListRecyclerViewAdapter(getActivity(), mStatusInfo);
        mURV.setAdapter(mAdapter);

        mURV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Log.e(TAG, "loadMore1 checkNextLoading = "+ checkNextLoading);
                if (!checkNextLoading) {
                    if (mTotalCnt > mStatusInfo.size()) {
                        mListPage++;
                        loadRecyclerView(mOpt);
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
                    loadRecyclerView(mOpt);
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

    public void loadRecyclerView(String opt) {
//        if(getContext() == null || DialogUtil.isCircularProgressLoding()) return;
//        DialogUtil.showCircularProgressDialog(getContext());


        mOpt = opt;
        mMode = "";
        switch (mPostMode) {
            case MODE_TODO :
                if("optMemo1".equals(mOpt)) mMode = "PRCS"; //내 결제함
                else mMode = "TEMP"; //처리할 결제
                break;
            case MODE_REFERENCE :
                if("optMemo1".equals(mOpt)) mMode = "REF";
                else mMode = "RPT";
                break;
            case MODE_FINISH :
                if("optMemo1".equals(mOpt)) mMode = "CPLT";
                else mMode = "FIN";
                break;
        }
        Log.e(TAG, "mode = "+mMode + " / optTask = "+opt); //optMemo1(PRCS|REF|CPLT)|    optMemo2(TEMP|RPT|FIN)


        //appr/listSnsApprove.json  tar_usr_id=&page=1&appr_st_cd=TEMP&optMemo=optMemo2
        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Memo.getListSnsMemo(mMode, opt, String.format("%d", mListPage)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
//                        DialogUtil.removeCircularProgressDialog();
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                        Log.d(TAG, "I2UrlHelper.Memo.getListSnsMemo onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.getListSnsMemo onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.getListSnsMemo onNext");
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
        if(totalcnt < 1) {
            mURV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mURV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);

        }
    }

}
