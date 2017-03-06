package com.i2max.i2smartwork.common.memo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.sns.SNSWriteActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.I2FSPAdapter;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by shlee on 15. 9. 3..
 */
public class MemoDetailActivity extends BaseAppCompatActivity {
    static String TAG = MemoDetailActivity.class.getSimpleName();

    public static boolean isChangedList = false;

    public static final int LIST_MEMO_MEMBER = 0;
    public static final int VIEW_MEMO_DETAIL = 1;
//    public static final int LIST_MEMO_TASK = 2;
    public static final int LIST_MEMO_FEED = 2;
    public static final String APPR_ST_CD = "appr_st_cd";
    public static final String TAR_USR_ID = "tar_usr_id";

    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    protected Button mBtEdit, mBtDel, mBtReport, mBtAppr, mBtDeny;

    private int mTabPos;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID, mApprStCd, mTarUsrId;

    // 메인 플로팅 추가버튼
    private FloatingActionButton mFabSnsAdd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.memo_detail);
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.memo_detail));
            mTarObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, ""); //parent object
            mTarObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, VIEW_MEMO_DETAIL);
            mCurObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, ""); //MEMO
            mCurObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mCurCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mApprStCd = extra.getString(APPR_ST_CD, "");
            mTarUsrId = extra.getString(TAR_USR_ID, ""); //결재자
        } else {
            mTarObjTp = ""; //parent object
            mTarObjId = "";
            mTarObjTtl = "";
            mTarCrtUsrID = "";
            mTabPos = VIEW_MEMO_DETAIL;
            mCurObjTp = ""; //MEMO
            mCurObjId = "";
            mCurCrtUsrID = "";
            mApprStCd = "";
            mTarUsrId = "";
        }
        Log.e(TAG, "mTarObjTp= " + mTarObjTp + ", mTarObjId=" + mTarObjId + ", mTarObjTtl= " + mTarObjTtl + ", mTarCrtUsrID= " + mTarCrtUsrID);
        Log.e(TAG, "mCurObjTp= " + mCurObjTp + ", mCurObjId=" + mCurObjId + ", mCurCrtUsrID= " + mCurCrtUsrID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.memo_detail);

        //삭제, 편집 버튼 표시처리 (작성자이면 표시)
        if ("TEMP".equals(mApprStCd)  && PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
            mBtDel = (Button) findViewById(R.id.btn_del);
            mBtDel.setVisibility(View.VISIBLE);
            mBtDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
                        delMemo();
                    } else {
                        Toast.makeText(MemoDetailActivity.this, "삭제권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            mBtEdit = (Button) findViewById(R.id.btn_edit);
            mBtEdit.setVisibility(View.VISIBLE);
            mBtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
                        Intent intent = new Intent(MemoDetailActivity.this, MemoWriteActivity.class);
                        intent.putExtra(CodeConstant.TITLE, "메모 편집");
                        intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                        intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
                        intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                        intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                        intent.putExtra(CodeConstant.CUR_OBJ_TP, mCurObjTp);
                        intent.putExtra(CodeConstant.CUR_OBJ_ID, mCurObjId);
                        intent.putExtra(CodeConstant.CRT_USR_ID, mCurCrtUsrID);
                        intent.putExtra(CodeConstant.TAB_POS, VIEW_MEMO_DETAIL);
                        startActivityForResult(intent, CodeConstant.REQUEST_EDIT);
                    } else {
                        Toast.makeText(MemoDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //상신, 승인, 반려 버튼 표시처리 (작성자이면 표시)
        if("TEMP".equals(mApprStCd) && PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
            mBtReport = (Button) findViewById(R.id.btn_report); //상신
            mBtReport.setVisibility(View.VISIBLE);
            mBtReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadUpdateApprove("RPT");
                }
            });
        } else if("RPT".equals(mApprStCd)  && PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mTarUsrId)) {
            mBtAppr = (Button) findViewById(R.id.btn_appr); //승인
            mBtAppr.setVisibility(View.VISIBLE);
            mBtAppr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadUpdateApprove("APPR");
                }
            });
            mBtDeny = (Button) findViewById(R.id.btn_deny); //반려
            mBtDeny.setVisibility(View.VISIBLE);
            mBtDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadUpdateApprove("RJCT");
                }
            });
        }

        //추가 플로팅 버튼 처리
        mFabSnsAdd = (FloatingActionButton) findViewById(R.id.fab_sns_write);
        mFabSnsAdd.setVisibility(View.VISIBLE);
        mFabSnsAdd.setOnClickListener(fabClickListener);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        if (mViewPager != null) {
            mTabPos = VIEW_MEMO_DETAIL;

            mAdapter.addFragment(new MemoDetailMemberListFragment(), "담당자");
            mAdapter.addFragment(new MemoDetailViewFragment(), "상 세");
//            mAdapter.addFragment(new MemoDetailTaskListFragment(), "작 업");
            mAdapter.addFragment(new MemoDetailSNSListFragment(), "피 드");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;

                    loadListMemo(mTabPos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });
            mViewPager.setCurrentItem(mTabPos);
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (isChangedList) {
            Log.d(TAG, "isChangedList = true");

            mViewPager.setCurrentItem(mTabPos);

            isChangedList = false;
        }
    }

    public void loadListMemo(int pos) {
        switch (pos) {
            case LIST_MEMO_MEMBER:
                MemoDetailMemberListFragment memoDetailMemberListFragment = (MemoDetailMemberListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                memoDetailMemberListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case VIEW_MEMO_DETAIL:
                MemoDetailViewFragment memoDetailListFragment = (MemoDetailViewFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                memoDetailListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
//            case LIST_MEMO_TASK:
//                MemoDetailTaskListFragment memoDetailTaskListFragment = (MemoDetailTaskListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
//                memoDetailTaskListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
//                break;
            case LIST_MEMO_FEED:
                MemoDetailSNSListFragment MemoDetailSNSListFragment = (MemoDetailSNSListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                MemoDetailSNSListFragment.initListPage();
                MemoDetailSNSListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID, "");
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MemoDetailActivity.isChangedList = true;

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MemoDetailActivity.isChangedList = true;

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();
    }

    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.fab_sns_write:
                    intent = new Intent(MemoDetailActivity.this, SNSWriteActivity.class);
                    intent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_OBJECT);
                    intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
                    intent.putExtra(CodeConstant.CUR_OBJ_ID, mTarObjId);
                    intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                    intent.putExtra(SNSWriteActivity.TARGET_NM, mTarObjTtl);
                    intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                    intent.putExtra(CodeConstant.TAB_POS, LIST_MEMO_FEED);
                    startActivityForResult(intent, CodeConstant.REQUEST_SNS_CREAT);
                    break;
            }
        }
    };

    public void delMemo() {
        DialogUtil.showConfirmDialog(
                MemoDetailActivity.this, "작업삭제", "정말 삭제하시겠습니까",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadDelMemo();
                    }
                });
    }

    public void loadDelMemo() {
        I2ConnectApi.requestJSON2Map(MemoDetailActivity.this, I2UrlHelper.Memo.getDeleteMemo(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Memo.getDeleteMemo onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.getDeleteMemo onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(MemoDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.getDeleteMemo onNext");
                        int statusCode = (int) Float.parseFloat((String) status.get("statusCode"));
                        if (statusCode >= 0) {
                            //성공
                            MemoMainFragment.isChangedList = true;
                            Toast.makeText(MemoDetailActivity.this, "삭제되었습니다", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            onError(new Exception("삭제에 실패하였습니다"));
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {
            if(requestCode == CodeConstant.REQUEST_EDIT) {
                Log.e(TAG, "return cfrc REQUEST_EDIT");
                if(mTabPos == VIEW_MEMO_DETAIL) loadListMemo(VIEW_MEMO_DETAIL);
                else  mViewPager.setCurrentItem(VIEW_MEMO_DETAIL);
            } else if(requestCode == CodeConstant.REQUEST_SNS_CREAT) {
                Log.e(TAG, "return REQUEST_SNS_CREAT");
                if(mTabPos == LIST_MEMO_FEED) loadListMemo(LIST_MEMO_FEED);
                else  mViewPager.setCurrentItem(LIST_MEMO_FEED);
            }
        }
    }

    //상신, 승인, 반려
    public void loadUpdateApprove(String apprStcd) {
        I2ConnectApi.requestJSON2Map(MemoDetailActivity.this, I2UrlHelper.Memo.updateApproveByApprStcd(mCurObjId, apprStcd))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Memo.updateApproveByApprStcd onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.updateApproveByApprStcd onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(MemoDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.updateApproveByApprStcd onNext");
                        int statusCode = (int) Float.parseFloat((String) status.get("statusCode"));
                        if (statusCode >= 0) {
                            Map<String, String> statusInfo = (Map<String, String>)status.get("statusInfo");
                            String resultApprStcd = statusInfo.get("appr_st_cd");
                            //성공
                            String msg = "";
                            if("RPT".equals(resultApprStcd)) {
                                msg = "상신";
                            } else if("APPR".equals(resultApprStcd)) {
                                msg = "승인";
                            } else if("RJCT".equals(resultApprStcd)) {
                                msg = "반려";
                            }
                            MemoMainFragment.isChangedList = true;
                            Toast.makeText(MemoDetailActivity.this, msg+" 처리를 하였습니다", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            onError(new Exception("요청한 작업을 실패하였습니다"));
                        }
                    }
                });
    }

}
