package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.I2SearchActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.EndlessRecyclerOnScrollListener;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSGroupMemberActivity extends BaseAppCompatActivity {
    static String TAG = SNSGroupMemberActivity.class.getSimpleName();

    public static final String GRP_ID = "tar_usr_id";
    public static final String MODE = "mode";

    public static final int MODE_MEMBER_LIST = 0;
    public static final int MODE_PERMIT_THROW = 1;

    protected String mTarGrpID;

    public boolean checkLoading = false;
    protected int mListPage, mTotalCnt, mMode;
    protected String mStrSearch;

    protected List<JSONObject> mMemberDataList;
    protected MemberRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_group_member);

        Intent intent = getIntent();
        mTarGrpID = intent.getStringExtra(GRP_ID);
        mMode = intent.getIntExtra(MODE, MODE_MEMBER_LIST);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("그룹멤버");

        mRV = (RecyclerView) findViewById(R.id.rv_group_member);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!checkLoading) {

                    if (mTotalCnt > mMemberDataList.size()) {
                        mListPage++;

                        loadMemberList(mStrSearch);
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mMemberDataList = new ArrayList<>();
        mAdapter = new MemberRecyclerViewAdapter(SNSGroupMemberActivity.this, mMemberDataList, mMode, mTarGrpID);
        mRV.setAdapter(mAdapter);

        loadMemberList("");
    }

    public void loadMemberList(String searchStr) {
        mStrSearch = searchStr;

        I2ConnectApi.requestJSON(SNSGroupMemberActivity.this, I2UrlHelper.SNS.getListGroupMember(mTarGrpID, String.format("%d", mListPage), mStrSearch))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSGroupMemberActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListGroupMember onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            mMemberDataList.addAll(statusInfoList);

                            mAdapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == I2SearchActivity.REQUEST_SEARCH && resultCode == RESULT_OK) {
            String searchStr = data.getExtras().getString(I2SearchActivity.EXTRA_SEARCH_STR);
            mListPage = 1;
            mMemberDataList.clear();
            loadMemberList(searchStr);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_search_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                Intent i2Search = new Intent(this, I2SearchActivity.class);
                i2Search.putExtra(I2SearchActivity.START_POS, I2SearchActivity.RIGHT_1);
                i2Search.putExtra(I2SearchActivity.SEARCH_STR, mStrSearch);
                startActivityForResult(i2Search, I2SearchActivity.REQUEST_SEARCH);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public static class MemberRecyclerViewAdapter
            extends RecyclerView.Adapter<MemberRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;
        protected int mMode;
        protected String mGrpID;

        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mUsrID;

            public final View mView;
            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvUsrNm, mTvDeptNm, mTvSelfIntro;
            public final ImageView mIvPermitThrow;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvUsrNm = (TextView) view.findViewById(R.id.tv_usr_nm);
                mTvDeptNm = (TextView) view.findViewById(R.id.tv_dept_nm);
                mTvSelfIntro = (TextView) view.findViewById(R.id.tv_self_intro);
                mIvPermitThrow = (ImageView) view.findViewById(R.id.iv_permit_throw);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public JSONObject getValueAt(int position) {
            return mValues.get(position);
        }

        public MemberRecyclerViewAdapter(Context context, List<JSONObject> items, int mode, String grpID) {
            mContext = context;
            mValues = items;
            mMode = mode;
            mGrpID = grpID;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_member, parent, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                TypedValue outValue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                view.setBackgroundResource(outValue.resourceId);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.get(position);

                holder.mUsrID = jsonObject.getString("usr_id");
                holder.mTvUsrNm.setText(jsonObject.getString("usr_nm"));
                if (!jsonObject.isNull("dept_nm")) holder.mTvDeptNm.setText(jsonObject.getString("dept_nm"));
                if (!jsonObject.isNull("self_intro")) holder.mTvSelfIntro.setText(jsonObject.getString("self_intro"));

                Glide.with(holder.mCivCrtUsrPhoto.getContext())
                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(jsonObject.getString("usr_photo_url"))))
                        .error(R.drawable.ic_no_usr_photo)
                        .fitCenter()
                        .into(holder.mCivCrtUsrPhoto);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SNSDetailProfileActivity.class);

                        intent.putExtra(SNSDetailProfileActivity.USR_ID, holder.mUsrID);
                        intent.putExtra(SNSDetailProfileActivity.USR_NM, holder.mTvUsrNm.getText().toString());

                        mContext.startActivity(intent);
                    }
                });

                if (mMode == SNSGroupMemberActivity.MODE_PERMIT_THROW && !PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(holder.mUsrID)) {
                    holder.mIvPermitThrow.setVisibility(View.VISIBLE);
                    holder.mIvPermitThrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtil.showConfirmDialog(mContext, "안내", holder.mTvUsrNm.getText().toString() + "님께그룹 관리자 권한을 위임하시겠습니까?",
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.updateSnsGroupAdmin(mGrpID, holder.mUsrID))
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<JSONObject>() {
                                                @Override
                                                public void onCompleted() {
                                                    Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupAdmin onCompleted");
                                                    String msg = "권한위임을 완료하였습니다.";
                                                    DialogUtil.showInformationDialog(mContext, msg, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            ((SNSGroupMemberActivity)mContext).finish();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupAdmin onError");
                                                    //Error dialog 표시
                                                    e.printStackTrace();
                                                    DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                                }

                                                @Override
                                                public void onNext(JSONObject jsonObject) {
                                                    Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupAdmin onNext");
                                                    if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                                                    } else {
                                                        Toast.makeText(mContext, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }

}
