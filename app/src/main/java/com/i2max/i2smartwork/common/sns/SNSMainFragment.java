package com.i2max.i2smartwork.common.sns;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.MainActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SNSMainFragment extends Fragment {
    static String TAG = SNSMainFragment.class.getSimpleName();

    public static final String MODE = "mode";

    public static boolean isChangedList = false;
    private AppCompatActivity acActivity;

    public boolean checkNextLoading = false;
    public boolean checkRefreshLoading = false;
    protected int mListPage, mTotalCnt;
    protected String mSearchStr = "";
    protected String mPostMode = "";

    protected List<JSONObject> mSnsDataArray;
    protected SNSUltimateRVAdapter mAdapter;

    protected UltimateRecyclerView mUltiRV;
    protected TextView mTvNoData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sns_main, container, false);

        Bundle bundle = this.getArguments();
        String title = "";
        if (bundle != null) {
            mPostMode = bundle.getString(MODE, "FOLLOW");
            title= bundle.getString(CodeConstant.TITLE, getString(R.string.connect_community));
        }

        acActivity = (AppCompatActivity)getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        acActivity.setSupportActionBar(toolbar);

        ((MainActivity) acActivity).setVisibleFabButton(true);

        final ActionBar ab = acActivity.getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mUltiRV = (UltimateRecyclerView) v.findViewById(R.id.recyclerview);
        mTvNoData = (TextView)v.findViewById(R.id.tv_no_data);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mUltiRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mUltiRV.setLayoutManager(layoutManager);
        mUltiRV.addItemDecoration(new SimpleDividerItemDecoration(mUltiRV.getContext()));
        mUltiRV.enableLoadmore();

        mSnsDataArray = new ArrayList<>();
        mAdapter = new SNSUltimateRVAdapter(getActivity(), mSnsDataArray);
        mUltiRV.setAdapter(mAdapter);
//        mAdapter.setCustomLoadMoreView(LayoutInflater.from(getActivity()).inflate(R.layout.custom_bottom_progressbar, null));

        mUltiRV.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (!checkNextLoading) {
                    if (mTotalCnt > mSnsDataArray.size()) {
                        mListPage++;

                        loadRecyclerView(mSearchStr);
                    }
                    checkNextLoading = true;
                }

            }
        });

        mUltiRV.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!checkRefreshLoading) {
                    mSnsDataArray.clear();
                    mListPage = 1;
                    checkRefreshLoading = true;
                    loadRecyclerView("");
                }
            }
        });


        mListPage = 1;
        loadRecyclerView("");

        return v;
    }

    public void loadRecyclerView(String searchStr) {

        mSearchStr = searchStr;

        //Log.d(TAG, "mSearchStr = " + mListPage + " / " + mSearchStr + " / " + mPostMode);

        I2ConnectApi.requestJSON(getActivity(),
                I2UrlHelper.SNS.getListSNSByObjectId("USER", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID), mPostMode, String.format("%d", mListPage), mSearchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkNextLoading = false;
                        checkRefreshLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError = " + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            if (statusInfo != null) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfo.getString("list_post_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                List<JSONObject> list_post = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_post");
                                mSnsDataArray.addAll(list_post);
                                mAdapter.notifyDataSetChanged();
                            }

                            if (mSnsDataArray.size() == 0) {
                                mTvNoData.setVisibility(View.VISIBLE);
                            } else {
                                mTvNoData.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(getActivity(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "isChangedList = " + isChangedList);
        if(isChangedList) {
            mSnsDataArray.clear();
            mListPage = 1;
            loadRecyclerView("");

            isChangedList = false;
        }
    }

    public void searchSnsResult(String searchStr) {
        mSnsDataArray.clear();
        mListPage = 1;

        loadRecyclerView(searchStr);
    }
}
