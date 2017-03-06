package com.i2max.i2smartwork.common.task;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
public class TaskDetailActivity extends BaseAppCompatActivity {
    static String TAG = TaskDetailActivity.class.getSimpleName();

    public static boolean isChangedList = false;

    public static final int LIST_TASK_MEMBER = 0;
    public static final int VIEW_TASK_DETAIL = 1;
    public static final int LIST_TASK_FEED = 2;

    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    protected Button mBtEdit, mBtDel;

    private int mTabPos;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID, mTask_Id;

    // 메인 플로팅 추가버튼
    public FloatingActionButton mFab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.task_detail);
        if(extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.cfrc_detail));
            mTarObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, ""); //parent object
            mTarObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, VIEW_TASK_DETAIL);
            mCurObjTp =  extra.getString(CodeConstant.CUR_OBJ_TP, ""); //TASK
            mCurObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mCurCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTask_Id = extra.getString(CodeConstant.CUR_OBJ_ID, "");
        } else {
            mTarObjTp = ""; //parent object
            mTarObjId = "";
            mTarObjTtl = "";
            mTarCrtUsrID = "";
            mTabPos = VIEW_TASK_DETAIL;
            mCurObjTp =  ""; //TASK
            mCurObjId = "";
            mCurCrtUsrID = "";
        }
        Log.e(TAG, "mTarObjTp= " + mTarObjTp + ", mTarObjId=" + mTarObjId + ", mTarObjTtl= " + mTarObjTtl + ", mTarCrtUsrID= " + mTarCrtUsrID);
        Log.e(TAG, "mCurObjTp= " + mCurObjTp + ", mCurObjId=" + mCurObjId + ", mCurCrtUsrID= " + mCurCrtUsrID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.task_detail);

        //삭제, 편집 버튼 표시처리 (작성자이면 표시)

        if (PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
            mBtDel = (Button) findViewById(R.id.btn_del);
            mBtDel.setVisibility(View.VISIBLE);
            mBtDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
                        delTask();
                    } else {
                        Toast.makeText(TaskDetailActivity.this, "삭제권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            mBtEdit = (Button) findViewById(R.id.btn_edit);
            mBtEdit.setVisibility(View.VISIBLE);
            mBtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Log.d(TAG,"mBtEdit.onClick()");
                    Log.d(TAG,"PREF_USR_ID="+ PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                    Log.d(TAG,"mCurCrtUsrID="+ mCurCrtUsrID);

                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
                        Intent intent = new Intent(TaskDetailActivity.this, TaskWriteActivity.class);
                        intent.putExtra(CodeConstant.TITLE, "작업 편집");
                        intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                        intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
                        intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                        intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                        intent.putExtra(CodeConstant.CUR_OBJ_TP, mCurObjTp);
                        intent.putExtra(CodeConstant.CUR_OBJ_ID, mCurObjId);
                        intent.putExtra(CodeConstant.CRT_USR_ID, mCurCrtUsrID);
                        intent.putExtra(CodeConstant.TAB_POS, VIEW_TASK_DETAIL);
                        intent.putExtra(CodeConstant.TASK_ID, mTask_Id);

                        startActivityForResult(intent, CodeConstant.REQUEST_EDIT);
                    } else {
                        Toast.makeText(TaskDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }


        //추가 플로팅 버튼 처리
        mFab = (FloatingActionButton) findViewById(R.id.fab_sns_write);
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(fabClickListener);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

//        mBtDel = (Button) findViewById(R.id.btn_del);
//        mBtDel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
//                    delTask();
//                } else {
//                    Toast.makeText(TaskDetailActivity.this, "삭제권한이 없습니다.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//        mBtEdit = (Button) findViewById(R.id.btn_edit);
//        mBtEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(TaskDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
//                //데모기능 제한
////                if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
////                    Intent intent = new Intent(TaskDetailActivity.this, TaskWriteActivity.class);
////                    intent.putExtra(CodeConstant.TITLE, "작업 편집");
////                    intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
////                    intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
////                    intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
////                    intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
////                    intent.putExtra(CodeConstant.CUR_OBJ_TP, mCurObjTp);
////                    intent.putExtra(CodeConstant.CUR_OBJ_ID, mCurObjId);
////                    intent.putExtra(CodeConstant.CRT_USR_ID, mCurCrtUsrID);
////                    intent.putExtra(CodeConstant.TAB_POS, VIEW_TASK_DETAIL);
////                    intent.putExtra(CodeConstant.REQUEST_CODE, CodeConstant.REQUEST_EDIT);
////                    startActivityForResult(intent, CodeConstant.REQUEST_EDIT);
////                } else {
////                    Toast.makeText(TaskDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
////                }
//
//            }
//        });

        if (mViewPager != null) {
            mTabPos = VIEW_TASK_DETAIL;

            mAdapter.addFragment(new TaskDetailMemberListFragment(), "담당자");
            mAdapter.addFragment(new TaskDetailViewFragment(), "상 세");
            mAdapter.addFragment(new TaskDetailSNSListFragment(), "피 드");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;

                    loadListTask(mTabPos);
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

        if(isChangedList) {
            Log.d(TAG, "isChangedList = true");

            mViewPager.setCurrentItem(mTabPos);

            isChangedList = false;
        }
    }

    public void loadListTask(int pos) {
        switch (pos) {
            case LIST_TASK_MEMBER:
                TaskDetailMemberListFragment taskDetailMemberListFragment = (TaskDetailMemberListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                taskDetailMemberListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case VIEW_TASK_DETAIL:
                TaskDetailViewFragment taskDetailListFragment = (TaskDetailViewFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                taskDetailListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID); //mdu9trngbd9ew, mdu9trngbd9ew
                break;
            case LIST_TASK_FEED:
                TaskDetailSNSListFragment taskDetailSNSListFragment = (TaskDetailSNSListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                taskDetailSNSListFragment.initListPage();
                taskDetailSNSListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID, "");
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                TaskDetailActivity.isChangedList = true;

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TaskDetailActivity.isChangedList = true;

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();
    }

    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switch (v.getId()) {
                        case R.id.fab_sns_write:
                            Intent intent = new Intent(TaskDetailActivity.this, SNSWriteActivity.class);
                            intent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_OBJECT);
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, mTarObjId);
                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                            intent.putExtra(SNSWriteActivity.TARGET_NM, mTarObjTtl);
                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                            intent.putExtra(CodeConstant.TAB_POS, LIST_TASK_FEED);
                            startActivityForResult(intent, CodeConstant.REQUEST_SNS_CREAT);
                            break;
                    }
                }
            }, CodeConstant.DRAWER_CLOSE_DELAY_MS);
        }
    };

    public void delTask() {
        DialogUtil.showConfirmDialog(
                TaskDetailActivity.this, "작업삭제", "정말 삭제하시겠습니까",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadDelTask();
                    }
                });
    }

    public void loadDelTask() {
        I2ConnectApi.requestJSON2Map(TaskDetailActivity.this, I2UrlHelper.Task.getDeleteTask(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Task.getDeleteTask onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getDeleteTask onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(TaskDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getDeleteTask onNext");
                        int statusCode = (int) Float.parseFloat((String) status.get("statusCode"));
                        if (statusCode >= 0) {
                            //성공
                            TaskMainFragment.isChangedList = true;
                            Toast.makeText(TaskDetailActivity.this, "삭제되었습니다", Toast.LENGTH_LONG).show();
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
                if(mTabPos == VIEW_TASK_DETAIL) loadListTask(VIEW_TASK_DETAIL);
                else  mViewPager.setCurrentItem(VIEW_TASK_DETAIL);
            } else if(requestCode == CodeConstant.REQUEST_SNS_CREAT) {
                Log.e(TAG, "return REQUEST_SNS_CREAT");
                if(mTabPos == LIST_TASK_FEED) loadListTask(LIST_TASK_FEED);
                else  mViewPager.setCurrentItem(LIST_TASK_FEED);
            }
        }
    }


}
