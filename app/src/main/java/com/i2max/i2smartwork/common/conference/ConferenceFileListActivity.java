package com.i2max.i2smartwork.common.conference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import com.i2max.i2smartwork.I2SearchActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.EndlessRecyclerOnScrollListener;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConferenceFileListActivity extends BaseAppCompatActivity {
    static String TAG = ConferenceFileListActivity.class.getSimpleName();

    protected String mTarUsrID;

    public boolean checkLoading = false;
    protected int mMode, mListPage, mTotalCnt;
    protected String mStrSearch;

    protected List<JSONObject> mFileList;
    protected FileRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_file_list);

        Intent intent = getIntent();
        mTarUsrID = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID);
        String title = intent.getStringExtra(CodeConstant.TITLE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

        mRV = (RecyclerView) findViewById(R.id.rv_file);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRV.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!checkLoading) {

                    if (mTotalCnt > mFileList.size()) {
                        mListPage++;

                        loadUserFileList(mListPage, mStrSearch);
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mFileList = new ArrayList<>();
        mAdapter = new FileRecyclerViewAdapter(ConferenceFileListActivity.this, mFileList);
        mRV.setAdapter(mAdapter);

        loadUserFileList(mListPage, mStrSearch);

    }

    public void loadUserFileList(int page, String searchStr) {
        mStrSearch = searchStr;
        I2ConnectApi.requestJSON(ConferenceFileListActivity.this, I2UrlHelper.Cfrc.getListSnsConferenceAttach(mTarUsrID, String.format("%d", page), mStrSearch))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading= false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByUser onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByUser onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(ConferenceFileListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByUser onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> listData = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (listData.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfo.getString("list_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mFileList.addAll(listData);
                                mAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == I2SearchActivity.REQUEST_SEARCH && resultCode == RESULT_OK) {
            String searchStr = data.getExtras().getString(I2SearchActivity.EXTRA_SEARCH_STR);
            mListPage = 1;
            mFileList.clear();
            loadUserFileList(mListPage, searchStr);
        }
    }


    public static class FileRecyclerViewAdapter
            extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mCrtUsrID, mPostID, mTarObjTp, mTarObjId;

            public final View mView;
            public final ImageView mIvFile;
            public final TextView mTvCrtUsrNm, mTvCrtDttm, mTvAttachNm;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIvFile = (ImageView) view.findViewById(R.id.iv_file);
                mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
                mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
                mTvAttachNm = (TextView) view.findViewById(R.id.tv_attach_nm);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public JSONObject getValueAt(int position) {
            return mValues.get(position);
        }

        public FileRecyclerViewAdapter(Context context, List<JSONObject> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mContext = context;
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_file, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.get(position);

                holder.mIvFile.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_file_doc));
                holder.mCrtUsrID = jsonObject.getString("crt_usr_id");
//                holder.mPostID = jsonObject.getString("post_id");
                holder.mTarObjTp = jsonObject.getString("tar_obj_tp_cd");
                holder.mTarObjId = jsonObject.getString("tar_obj_id");
                holder.mTvCrtUsrNm.setText(jsonObject.getString("crt_usr_nm"));
                holder.mTvCrtDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMM(jsonObject.getString("crt_dttm")));
                holder.mTvAttachNm.setText(jsonObject.getString("file_nm"));

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ConferenceDetailActivity.class);
                        intent.putExtra(CodeConstant.CUR_OBJ_TP, holder.mTarObjTp);
                        intent.putExtra(CodeConstant.CUR_OBJ_ID, holder.mTarObjId);
                        intent.putExtra(CodeConstant.CRT_USR_ID, holder.mCrtUsrID);

                        mContext.startActivity(intent);
                    }
                });
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
