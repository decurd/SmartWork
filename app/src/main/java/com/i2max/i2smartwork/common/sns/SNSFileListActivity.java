package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.Intent;
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
import com.i2max.i2smartwork.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSFileListActivity extends BaseAppCompatActivity {
    static String TAG = SNSFileListActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String GRP_ID = "tar_grp_id";
    public static final String MODE = "mode";

    public static final int MODE_USR = 0;
    public static final int MODE_GRP = 1;

    private static String mTarUsrID;
    private String mTarGrpID;

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
        mMode = intent.getIntExtra(MODE, MODE_USR);
        if (mMode == MODE_USR) {
            mTarUsrID = intent.getStringExtra(USR_ID);
        } else {
            mTarGrpID = intent.getStringExtra(GRP_ID);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("파일목록");

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

                        if (mMode == MODE_USR) {
                            loadUserFileList(mListPage, mStrSearch);
                        } else {
                            loadGroupFileList(mListPage, mStrSearch);
                        }
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mFileList = new ArrayList<>();
        mAdapter = new FileRecyclerViewAdapter(SNSFileListActivity.this, mFileList);
        mRV.setAdapter(mAdapter);

        if (mMode == MODE_USR) {
            loadUserFileList(mListPage, mStrSearch);
        } else {
            loadGroupFileList(mListPage, mStrSearch);
        }

    }

    public void loadUserFileList(int page, String searchStr) {
        Log.e(TAG, "mTarUsrID = "+ mTarUsrID);
        mStrSearch = searchStr;
        I2ConnectApi.requestJSON(SNSFileListActivity.this, I2UrlHelper.SNS.getListSnsAttachByUser(mTarUsrID, String.format("%d", page), mStrSearch))
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
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSFileListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByUser onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfo.getString("list_total_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mFileList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getBaseContext(), getString(R.string.no_file_data_available), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loadGroupFileList(int page, String searchStr) {
        mStrSearch = searchStr;
        Log.e(TAG, "mTarGrpID = "+ mTarGrpID);
        I2ConnectApi.requestJSON(SNSFileListActivity.this, I2UrlHelper.SNS.getListSnsAttachByGroup(mTarGrpID, String.format("%d", page), mStrSearch))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading= false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSFileListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListSnsAttachByGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfo.getString("list_total_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mFileList.addAll(statusInfoList);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getBaseContext(), getString(R.string.no_file_data_available), Toast.LENGTH_SHORT).show();
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
            if (mMode == MODE_USR) {
                loadUserFileList(mListPage, searchStr);
            } else {
                loadGroupFileList(mListPage, searchStr);
            }
        }
    }


    public static class FileRecyclerViewAdapter
            extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mCrtUsrID, mPostID, mTarObjTp;

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

                //확장자에 따른 아이콘 변경처리
                holder.mIvFile.setImageResource(R.drawable.ic_file_doc);
                FileUtil.setFileExtIcon(holder.mIvFile, jsonObject.getString("file_nm"));

                // holder.mCrtUsrID = jsonObject.getString("crt_usr_id");
                holder.mCrtUsrID = mTarUsrID;
                // holder.mPostID = jsonObject.getString("post_id");
                holder.mPostID = jsonObject.getString("tar_obj_id");    // 실제 tar_obj_id가 이전에 정의된 post_id인지 알 수 없다. Json 상에서는 post_id를 찾을 수 없다
                holder.mTarObjTp = jsonObject.getString("tar_obj_tp_cd");
                holder.mTvCrtUsrNm.setText(jsonObject.getString("crt_usr_nm"));
                holder.mTvAttachNm.setText(jsonObject.getString("file_nm"));
                holder.mTvCrtDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMM(jsonObject.getString("crt_dttm")));


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SNSDetailPostActivity.class);
                        intent.putExtra(CodeConstant.POST_ID, holder.mPostID);
                        intent.putExtra(CodeConstant.TAR_OBJ_TP, holder.mTarObjTp);
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
