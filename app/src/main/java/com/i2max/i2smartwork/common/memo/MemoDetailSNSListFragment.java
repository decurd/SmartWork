
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
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.common.sns.SNSUltimateRVAdapter;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MemoDetailSNSListFragment extends Fragment {
    static String TAG = MemoDetailSNSListFragment.class.getSimpleName();

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mListPage, mTotalCnt;
    protected String mSearchStr = "";
    protected List<JSONObject> mDataArray;
    protected SNSUltimateRVAdapter mAdapter;

    protected UltimateRecyclerView mURV;
    protected TextView mTvEmpty;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId, mCurObjTp, mCurObjId, mCurCrtUsrID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ultimate_recyclerview, container, false);

        mURV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvEmpty = (TextView)v.findViewById(R.id.empty_view);
        mTvEmpty.setText(getString(R.string.no_sns_data_available));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mURV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mURV.setLayoutManager(layoutManager);
        mURV.addItemDecoration(new SimpleDividerItemDecoration(mURV.getContext()));
        mURV.enableLoadmore();

        mDataArray = new ArrayList<>();
        mAdapter = new SNSUltimateRVAdapter(getActivity(), mDataArray);
        mURV.setAdapter(mAdapter);

        mURV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                Log.e(TAG, "loadMore1 checkNextLoading = " + checkNextLoading);
                if (!checkNextLoading) {
                    if (mTotalCnt > mDataArray.size()) {
                        mListPage++;
                        loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId, mCurObjTp, mCurObjId, mCurCrtUsrID, mSearchStr);
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
                    loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId, mCurObjTp, mCurObjId, mCurCrtUsrID, mSearchStr);
                }
            }
        });

        mListPage = 1;

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mURV.refreshDrawableState();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String tarObjTp, String tarObjId, String tarObjTtl, String tarCrtUsrId, String curObjTp, String curObjId, String curCrtUsrId, String searchStr) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrId = tarCrtUsrId;
        mCurObjTp = curObjTp;
        mCurObjId = curObjId;
        mCurCrtUsrID = curCrtUsrId;
        mSearchStr = searchStr;

        Log.d(TAG, "mSearchStr = " + mListPage + " / " + mSearchStr);

        I2ConnectApi.requestJSON(getActivity(), I2UrlHelper.SNS.getListSNSByObjectId(mCurObjTp, mCurObjId, "", String.format("%d", mListPage), ""))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JSONObject>() {
                            @Override
                            public void onCompleted() {
                                checkNextLoading = false;
                                checkRefreshLoading = false;
//                        DialogUtil.hideCircularProgressDialog();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError = " + e.getMessage());
                                DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                            }

                            @Override
                            public void onNext(JSONObject jsonObject) {
                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                    List<JSONObject> listPost = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_post");

                                    setEmptyResult(listPost.size());
                                    if (listPost.size() > 0) {
                                        try {
                                            mTotalCnt = Integer.parseInt(statusInfo.getString("list_post_count"));
                                            mDataArray.addAll(listPost);
                                            mAdapter.notifyDataSetChanged();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    Toast.makeText(mURV.getContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

    public void setEmptyResult(int dataSize) {
        if(dataSize < 1) {
            mURV.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mURV.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);

        }
    }

    public void initListPage() {
        mListPage = 1;
        if (mDataArray != null && mDataArray.size() > 0)
            mDataArray.clear();
    }

}
