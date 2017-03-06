package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSThumbnailListActivity extends BaseAppCompatActivity {
    static String TAG = SNSThumbnailListActivity.class.getSimpleName();

    public static final String USR_ID = "tar_usr_id";
    public static final String GRP_ID = "tar_grp_id";
    public static final String MODE = "mode";

    public static final int MODE_USR = 0;
    public static final int MODE_GRP = 1;

    protected String mTarUsrID, mTarGrpID;

    public boolean checkLoading = false;
    protected int mMode, mListPage, mTotalCnt;
    protected String mStrSearch;

    protected List<JSONObject> mThumbnailList;
    protected ThumbnailRecyclerViewAdapter mAdapter;
    protected RecyclerView mRV;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnail_list);

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
        getSupportActionBar().setTitle("사진목록");

        mRV = (RecyclerView) findViewById(R.id.rv_thumbnail);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        mRV.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!checkLoading) {

                    if (mTotalCnt > mThumbnailList.size()) {
                        mListPage++;

                        loadThumbnailList(mListPage, mStrSearch);
                    }

                    checkLoading = true;
                }
            }
        });

        mTotalCnt = 0;
        mListPage = 1;
        mStrSearch = "";
        mThumbnailList = new ArrayList<>();
        mAdapter = new ThumbnailRecyclerViewAdapter(SNSThumbnailListActivity.this, mThumbnailList);
        mRV.setAdapter(mAdapter);

        loadThumbnailList(mListPage, mStrSearch);
    }

    public void loadThumbnailList(int page, String searchStr) {

        I2ConnectApi.requestJSON(SNSThumbnailListActivity.this, I2UrlHelper.SNS.getListUserThumbnail(mTarUsrID, String.format("%d", page), searchStr))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        checkLoading= false;
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserThumbnail onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserThumbnail onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSThumbnailListActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserThumbnail onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            List<JSONObject> statusInfoList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);

                            if (statusInfoList.size() > 0) {
                                try {
                                    mTotalCnt = Integer.parseInt(statusInfoList.get(0).getString("total_cnt"));
                                    for (int i=0; i<statusInfoList.size(); i++) {
                                        if (!statusInfoList.get(i).isNull("list_thumb")) {
                                            List<JSONObject> listThumbnail = I2ResponseParser.getListFromJSONArray(statusInfoList.get(i).getJSONArray("list_thumb"));
                                            mThumbnailList.addAll(listThumbnail);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

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
            mStrSearch = searchStr;
            mListPage = 1;
            mThumbnailList.clear();
            loadThumbnailList(mListPage, mStrSearch);
        }
    }

    public static class ThumbnailRecyclerViewAdapter
            extends RecyclerView.Adapter<ThumbnailRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<JSONObject> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mCrtUsrID, mPostID, mTarObjTp;

            public final View mView;
            public final ImageView mIvThumbnail;
            public final TextView mTvAttachNm;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIvThumbnail = (ImageView) view.findViewById(R.id.img_thumbnail);
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

        public ThumbnailRecyclerViewAdapter(Context context, List<JSONObject> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mContext = context;
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item_gallery, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.get(position);

                holder.mCrtUsrID = jsonObject.getString("crt_usr_id");
                holder.mPostID = jsonObject.getString("post_id");
                holder.mTarObjTp = jsonObject.getString("tar_obj_tp_cd");
                holder.mTvAttachNm.setText(jsonObject.getString("attach_nm"));

                Glide.with(holder.mIvThumbnail.getContext())
                        .load(I2UrlHelper.File.getPhotoImage(FormatUtil.getStringValidate(jsonObject.getString("file_id"))))
                        .error(R.drawable.ic_action_close)
                        .fitCenter()
                        .into(holder.mIvThumbnail);

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
