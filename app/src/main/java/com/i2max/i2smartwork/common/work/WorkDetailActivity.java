package com.i2max.i2smartwork.common.work;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.memo.MemoMainFragment;
import com.i2max.i2smartwork.common.sns.SNSWriteActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
import com.i2max.i2smartwork.common.task.TaskWriteActivity;
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
public class WorkDetailActivity extends BaseAppCompatActivity {
    static String TAG = WorkDetailActivity.class.getSimpleName();

    public static boolean isChangedList = false;

    public static final int LIST_WORK_MEMBER    = 0;
    public static final int VIEW_WORK_DETAIL    = 1;
    public static final int LIST_WORK_TASK      = 2;
    public static final int LIST_WORK_MILESTONE = 3;
    public static final int LIST_WORK_MEMO      = 4;
    public static final int LIST_WORK_FEED      = 5;


    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    protected Button mBtEdit, mBtDel;

    private int mTabPos;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID;

    // 메인 플로팅 추가버튼
    public FloatingActionMenu mFab;
    private com.github.clans.fab.FloatingActionButton mFabSnsAdd, mFabTaskAdd, mFabMilestoneAdd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.work_detail);
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.work_detail));
            mTarObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, ""); //parent object
            mTarObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, VIEW_WORK_DETAIL);
            mCurObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, "");
            mCurObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mCurCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
        } else {
            mTarObjTp = ""; //parent object
            mTarObjId = "";
            mTarObjTtl = "";
            mTarCrtUsrID = "";
            mTabPos = VIEW_WORK_DETAIL;
            mCurObjTp = "";
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
        ab.setTitle(R.string.work_detail);

        //삭제, 편집 버튼 표시처리 (작성자이면 표시)
        if (PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID)) {
            mBtDel = (Button) findViewById(R.id.btn_del);
            mBtDel.setVisibility(View.VISIBLE);
            mBtDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
                        delete();;
                    } else {
                        Toast.makeText(WorkDetailActivity.this, "삭제권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            mBtEdit = (Button) findViewById(R.id.btn_edit);
            mBtEdit.setVisibility(View.VISIBLE);
            mBtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCurCrtUsrID) ) {
//                        Intent intent = new Intent(WorkDetailActivity.this, WorkWriteActivity.class);
//                        intent.putExtra(CodeConstant.TITLE, "과제 편집");
//                        intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
//                        intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
//                        intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
//                        intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
//                        intent.putExtra(CodeConstant.CUR_OBJ_TP, mCurObjTp);
//                        intent.putExtra(CodeConstant.CUR_OBJ_ID, mCurObjId);
//                        intent.putExtra(CodeConstant.CRT_USR_ID, mCurCrtUsrID);
//                        intent.putExtra(CodeConstant.TAB_POS, VIEW_WORK_DETAIL);
//                        startActivityForResult(intent, CodeConstant.REQUEST_EDIT);
                    } else {
                        Toast.makeText(WorkDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }


        //추가 플로팅 버튼 처리
        //추가 플로팅 버튼 처리
        mFab = (FloatingActionMenu) findViewById(R.id.fab);
        mFab.setVisibility(View.VISIBLE);
        mFab.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(this, R.anim.show_from_bottom));
        mFab.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(this, R.anim.hide_to_bottom));
        mFab.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFab.toggle(true);
            }
        });

        mFabSnsAdd = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_sns_add);
        mFabTaskAdd = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_task_add);
        mFabMilestoneAdd = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_milestone_add);
        mFabSnsAdd.setVisibility(View.VISIBLE);
        mFabTaskAdd.setVisibility(View.VISIBLE);
        mFabMilestoneAdd.setVisibility(View.VISIBLE);
        mFabSnsAdd.setOnClickListener(fabClickListener);
        mFabTaskAdd.setOnClickListener(fabClickListener);
        mFabMilestoneAdd.setOnClickListener(fabClickListener);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        if (mViewPager != null) {
            mAdapter.addFragment(new WorkDetailMemberListFragment(), "멤버");
            mAdapter.addFragment(new WorkDetailViewFragment(), "상세");
            mAdapter.addFragment(new WorkDetailTaskListFragment(), "작업");
            mAdapter.addFragment(new WorkDetailMilestoneListFragment(), "마일스톤");
            mAdapter.addFragment(new WorkDetailMemoListFragment(), "메모");
            mAdapter.addFragment(new WorkDetailSNSListFragment(), "피드");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;

                    loadList(mTabPos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });
            mViewPager.setCurrentItem(mTabPos);
            loadList(mTabPos);
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

    public void loadList(int pos) {
        switch (pos) {
            case LIST_WORK_MEMBER:
                WorkDetailMemberListFragment workDetailMemberListFragment = (WorkDetailMemberListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailMemberListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case VIEW_WORK_DETAIL:
                WorkDetailViewFragment workDetailListFragment = (WorkDetailViewFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case LIST_WORK_TASK:
                WorkDetailTaskListFragment workDetailTaskListFragment = (WorkDetailTaskListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailTaskListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case LIST_WORK_MILESTONE:
                WorkDetailMilestoneListFragment workDetailMilestoneListFragment = (WorkDetailMilestoneListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailMilestoneListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case LIST_WORK_MEMO:
                WorkDetailMemoListFragment workDetailMemoListFragment = (WorkDetailMemoListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailMemoListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID);
                break;
            case LIST_WORK_FEED:
                WorkDetailSNSListFragment workDetailSNSListFragment = (WorkDetailSNSListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                workDetailSNSListFragment.initListPage();
                workDetailSNSListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID, "");
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ConferenceDetailActivity.isChangedList = true;

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ConferenceDetailActivity.isChangedList = true;

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
                    Intent intent = null;
                    switch (v.getId()) {
                        case R.id.fab_sns_add:
                            intent = new Intent(WorkDetailActivity.this, SNSWriteActivity.class);
                            intent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_OBJECT);
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, mTarObjId);
                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                            intent.putExtra(SNSWriteActivity.TARGET_NM, mTarObjTtl);
                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                            intent.putExtra(CodeConstant.TAB_POS, LIST_WORK_FEED);
                            startActivityForResult(intent, CodeConstant.REQUEST_SNS_CREAT);
                            break;
                        case R.id.fab_task_add:
                            intent = new Intent(WorkDetailActivity.this, TaskWriteActivity.class);
                            intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                            intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                            intent.putExtra(CodeConstant.TAB_POS, LIST_WORK_TASK);
                            startActivityForResult(intent, CodeConstant.REQUEST_TASK_CREAT);
                            break;
                        case R.id.fab_milestone_add:
                            //todo
//                            intent = new Intent(WorkDetailActivity.this, MileStoneWriteActivity.class);
//                            intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
//                            intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
//                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
//                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
//                            intent.putExtra(CodeConstant.TAB_POS, LIST_WORK_TASK);
//                            startActivityForResult(intent, CodeConstant.REQUEST_MILESTONE_CREAT);
                            break;
                    }
                }
            }, CodeConstant.DRAWER_CLOSE_DELAY_MS);
            mFab.toggle(true);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            loadList(requestCode);
        }
    }

    public void delete() {
        DialogUtil.showConfirmDialog(
                WorkDetailActivity.this, "과제삭제", "정말 삭제하시겠습니까",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadDel();
                    }
                });
    }

    public void loadDel() {
        I2ConnectApi.requestJSON2Map(WorkDetailActivity.this, I2UrlHelper.Work.getDeleteWork(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "getDeleteWork onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "getDeleteWork onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(WorkDetailActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "getDeleteWork onNext");
                        int statusCode = (int) Float.parseFloat((String) status.get("statusCode"));
                        if (statusCode >= 0) {
                            //성공
                            MemoMainFragment.isChangedList = true;
                            Toast.makeText(WorkDetailActivity.this, "삭제되었습니다", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            onError(new Exception("삭제에 실패하였습니다"));
                        }
                    }
                });
    }

}
