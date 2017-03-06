package com.i2max.i2smartwork.common.conference;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.SimpleDividerItemDecoration;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.common.sns.SNSMainFragment;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


    public class ConferenceOnlineActivity extends BaseAppCompatActivity {
        static String TAG = SNSMainFragment.class.getSimpleName();

        public static boolean isChangedList = false;
        private AppCompatActivity acActivity;

        public boolean checkLoading = false;

        protected List<LinkedTreeMap<String, String>> mCfrcOnlineDataArray;
        protected ConferenceOnlineListRecyclerViewAdapter mAdapter;

        protected RecyclerView mRV;
        protected TextView mTvEmpty;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_conference_online_main);

            Bundle bundle = getIntent().getExtras();
            String title = "";
            if (bundle != null) {
                title= bundle.getString(CodeConstant.TITLE, getString(R.string.connect_community));
            }

            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            final ActionBar ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(title);

            mRV = (RecyclerView) findViewById(R.id.recyclerview);
            mTvEmpty = (TextView) findViewById(R.id.empty_view);

            LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRV.setLayoutManager(layoutManager);
            mRV.addItemDecoration(new SimpleDividerItemDecoration(mRV.getContext()));

            mCfrcOnlineDataArray = new ArrayList<>();
            mAdapter = new ConferenceOnlineListRecyclerViewAdapter(this, mCfrcOnlineDataArray);
            mRV.setAdapter(mAdapter);

            //TODO getI2CfrcOAuthToken API 에러 리턴 > 서버 수정 요망 시연 주석처리
            //etI2CfrcAccessToken();
            loadRecyclerView();
        }

        public void loadRecyclerView() {

            I2ConnectApi.requestJSON2Map(ConferenceOnlineActivity.this, I2UrlHelper.Cfrc.getListConferenceOnlineRoom(
                    PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID)))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Map<String, Object>>() {
                        @Override
                        public void onCompleted() {
                            checkLoading = false;
//                        DialogUtil.hideCircularProgressDialog();
                        }

                        @Override
                        public void onError(Throwable e) {
                            DialogUtil.showErrorDialog(mRV.getContext(), e.getMessage());
                            Log.d(TAG, "onError = " + e.getMessage());
                            e.printStackTrace();
                            //Error dialog 표시
                            DialogUtil.showErrorDialogWithValidateSession(ConferenceOnlineActivity.this, e);
                        }

                        @Override
                        public void onNext(Map<String, Object> status) {
                            Log.d(TAG, "I2UrlHelper.Conference.getListSnsConference onNext");
                            LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                            List<LinkedTreeMap<String, String>> listData = (List<LinkedTreeMap<String, String>>) statusInfo.get("list_data");
                            for (int i = 0; i < listData.size() ; i++) { //플래그가 NO이면 방 미표시
                                if("no".equals(FormatUtil.getStringValidate(listData.get(i).get("cfrc_usr_flag")))) {
                                    listData.remove(i);
                                    i--;
                                }
                            }

                            if (listData != null && listData.size() > 0) {
                                mCfrcOnlineDataArray.addAll(listData);
                                mAdapter.notifyDataSetChanged();
                            }
                            setEmptyResult(mCfrcOnlineDataArray.size());

                        }
                    });
        }

        @Override
        public void onResume() {
            super.onResume();

            if(isChangedList) {
                Log.d(TAG, "isChangedList = true");

                loadRecyclerView();

                isChangedList = false;
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    return true;
            }
            return super.onOptionsItemSelected(item);
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
