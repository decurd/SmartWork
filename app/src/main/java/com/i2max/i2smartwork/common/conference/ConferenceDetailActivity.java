package com.i2max.i2smartwork.common.conference;

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
import com.i2max.i2smartwork.i2api.I2ResponseParser;
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
public class ConferenceDetailActivity extends BaseAppCompatActivity {
    static String TAG = ConferenceDetailActivity.class.getSimpleName();

    public static final int LIST_CFRC_MEMBER = 0;
    public static final int VIEW_CFRC_DETAIL = 1;
//    public static final int LIST_CFRC_TASK = 2;
    public static final int LIST_CFRC_FEED = 2;

    public static boolean isChangedList = false;

    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    protected Button mBtEdit, mBtDel;

    // 메인 플로팅 추가버튼
    private FloatingActionButton mFabSnsAdd;

    private int mTabPos;
    public String mTarObjTp, mTarObjId, mTarObjTtl,  mTarCrtUsrID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.cfrc_detail);
        if(extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.cfrc_detail));
            mTarObjTp = extra.getString(CodeConstant.CUR_OBJ_TP, "");
            mTarObjId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrID = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, VIEW_CFRC_DETAIL);
        } else {
            mTarObjTp = "";
            mTarObjId = "";
            mTarObjTtl = "";
            mTarCrtUsrID = "";
            mTabPos = VIEW_CFRC_DETAIL;
        }
        Log.e(TAG, "1 mTarObjTp= " + mTarObjTp + ", mTarObjId= " + mTarObjId + ", mTarObjTtl= "+mTarObjTtl+ ", mTarCrtUsrID= " + mTarCrtUsrID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        //삭제, 편집 버튼 표시처리 (작성자이면 표시)
        //삭제, 편집 버튼 표시처리 (작성자이면 표시)
        if (PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mTarCrtUsrID)) {
            mBtDel = (Button) findViewById(R.id.btn_del);
            mBtDel.setVisibility(View.VISIBLE);
            mBtDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mTarCrtUsrID) ) {
                        delCfrc();
                    } else {
                        Toast.makeText(ConferenceDetailActivity.this, "삭제권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            mBtEdit = (Button) findViewById(R.id.btn_edit);
            mBtEdit.setVisibility(View.VISIBLE);
            mBtEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mTarCrtUsrID) ) {
                        Intent intent = new Intent(ConferenceDetailActivity.this, ConferenceWriteActivity.class);
                        intent.putExtra(CodeConstant.TITLE, "회의 편집");
                        intent.putExtra(CodeConstant.CFRC_ID, mTarObjId);
                        intent.putExtra(CodeConstant.TAB_POS, VIEW_CFRC_DETAIL);
                        startActivityForResult(intent, CodeConstant.REQUEST_EDIT);
                    } else {
                        Toast.makeText(ConferenceDetailActivity.this, "편집권한이 없습니다.", Toast.LENGTH_LONG).show();
                    }

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
            mAdapter.addFragment(new ConferenceDetailMemberListFragment(), "참석자");
            mAdapter.addFragment(new ConferenceDetailViewFragment(), "상 세");
//            mAdapter.addFragment(new ConferenceDetailTaskListFragment(), "작 업");
            mAdapter.addFragment(new ConferenceDetailSNSListFragment(), "피 드");

            mViewPager.setAdapter(mAdapter);
            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadListCfrc(mTabPos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });

            mViewPager.setCurrentItem(mTabPos); //call onPageSelected;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if(isChangedList) {
            Log.d(TAG, "isChangedList = true");
//            mFab.showMenuButton(true);
            mViewPager.setCurrentItem(mTabPos);// call loadListCfrc(mTabPos);

            isChangedList = false;
        }
    }

    public void loadListCfrc(int pos) {
        Log.e(TAG, "3 mTarObjTp= " + mTarObjTp + ", mTarObjId= " + mTarObjId + ", mTarObjTtl= "+mTarObjTtl+ ", mTarCrtUsrID= " + mTarCrtUsrID);

        switch (pos) {
            case LIST_CFRC_MEMBER:
                ConferenceDetailMemberListFragment conferenceDetailMemberListFragment = (ConferenceDetailMemberListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                conferenceDetailMemberListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID);
                break;
            case VIEW_CFRC_DETAIL:
                ConferenceDetailViewFragment cfrcDetailListFragment = (ConferenceDetailViewFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                cfrcDetailListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID);
                break;
//            case LIST_CFRC_TASK:
//                ConferenceDetailTaskListFragment conferenceDetailTaskListFragment = (ConferenceDetailTaskListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
//                conferenceDetailTaskListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID);
//                break;
            case LIST_CFRC_FEED:
                ConferenceDetailSNSListFragment conferenceDetailSNSListFragment = (ConferenceDetailSNSListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                conferenceDetailSNSListFragment.initListPage();
                conferenceDetailSNSListFragment.loadRecyclerView(mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, "");
                break;
        }
        Log.e(TAG, "4 mTarObjTp= " + mTarObjTp + ", mTarObjId= " + mTarObjId + ", mTarObjTtl= "+mTarObjTtl+ ", mTarCrtUsrID= " + mTarCrtUsrID);

    }

    public void delCfrc() {
        DialogUtil.showConfirmDialog(
                ConferenceDetailActivity.this, "회의삭제", "정말 삭제하시겠습니까",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadDelCfrc();
                    }
                });
    }

    public void loadDelCfrc() {
        I2ConnectApi.requestJSON2Map(ConferenceDetailActivity.this, I2UrlHelper.Cfrc.getDeleteConference(mTarObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getDeleteConference onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getDeleteConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(ConferenceDetailActivity.this, e);

                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getDeleteConference onNext");
                        if (I2ResponseParser.checkReponseStatus(status)) {
                            //성공
                            ConferenceMainFragment.isChangedList = true;
                            Toast.makeText(ConferenceDetailActivity.this, "삭제되었습니다", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            onError(new Exception("삭제에 실패하였습니다"));
                        }
                    }
                });
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


    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            Intent intent = null;
            switch (v.getId()) {
                case R.id.fab_sns_write:
                    intent = new Intent(ConferenceDetailActivity.this, SNSWriteActivity.class);
                    intent.putExtra(SNSWriteActivity.MODE, SNSWriteActivity.MODE_TARGET_OBJECT);
                    intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
                    intent.putExtra(CodeConstant.CUR_OBJ_ID, mTarObjId);
                    intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                    intent.putExtra(SNSWriteActivity.TARGET_NM, mTarObjTtl);
                    intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrID);
                    intent.putExtra(CodeConstant.TAB_POS, LIST_CFRC_FEED);
                    startActivityForResult(intent, CodeConstant.REQUEST_SNS_CREAT);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {
            if(requestCode == CodeConstant.REQUEST_EDIT) {
                Log.e(TAG, "return cfrc REQUEST_EDIT");
                if(mTabPos == VIEW_CFRC_DETAIL) loadListCfrc(VIEW_CFRC_DETAIL);
                else  mViewPager.setCurrentItem(VIEW_CFRC_DETAIL);
            } else if(requestCode == CodeConstant.REQUEST_SNS_CREAT) {
                Log.e(TAG, "return REQUEST_SNS_CREAT");
                if(mTabPos == LIST_CFRC_FEED) loadListCfrc(LIST_CFRC_FEED);
                else  mViewPager.setCurrentItem(LIST_CFRC_FEED);
            }
        }
    }

}

