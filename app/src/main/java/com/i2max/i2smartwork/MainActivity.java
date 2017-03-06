package com.i2max.i2smartwork;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.common.conference.ConferenceFileListActivity;
import com.i2max.i2smartwork.common.conference.ConferenceMainFragment;
import com.i2max.i2smartwork.common.conference.ConferenceOnlineActivity;
import com.i2max.i2smartwork.common.conference.ConferenceRoomScheduleActivity;
import com.i2max.i2smartwork.common.conference.ConferenceWriteActivity;
import com.i2max.i2smartwork.common.memo.MemoWriteActivity;
import com.i2max.i2smartwork.common.plan.PlanCreateActivity;
import com.i2max.i2smartwork.common.plan.PlanMainFragment;
import com.i2max.i2smartwork.common.push.PushListActivity;
import com.i2max.i2smartwork.common.sns.SNSDetailProfileActivity;
import com.i2max.i2smartwork.common.sns.SNSMainFragment;
import com.i2max.i2smartwork.common.sns.SNSPersonalConfigActivity;
import com.i2max.i2smartwork.common.sns.SNSPersonalFunctionActivity;
import com.i2max.i2smartwork.common.sns.SNSWriteActivity;
import com.i2max.i2smartwork.common.task.TaskMainFragment;
import com.i2max.i2smartwork.common.task.TaskWriteActivity;
import com.i2max.i2smartwork.common.web.WebviewFragment;
import com.i2max.i2smartwork.common.work.WorkMainFragment;
import com.i2max.i2smartwork.component.BackPressCloseHandler;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.AppConstant;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.gcm.GcmUtil;
import com.i2max.i2smartwork.gcm.RegistrationIntentService;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.IntentUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.i2max.i2smartwork.utils.TraceUtil;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import com.i2max.i2smartwork.nhfire.constant.NhfireCodeConstant;
//import com.i2max.i2smartwork.nhfire.util.NhfireUtil;

/**
 * TODO
 */
public class MainActivity extends BaseAppCompatActivity {
    static String TAG = MainActivity.class.getSimpleName();

    private final Handler mDrawerActionHandler = new Handler();

    private Drawer mDrawer = null;
//    private List<JSONObject> mJsonMenu;
    private List<LinkedTreeMap<String, Object>> mJsonMapMenu;
    private List<Map<String, String>> mListMenu;
    private PreferenceUtil mPref;
    private BackPressCloseHandler backPressCloseHandler;

    private Fragment currentfragment;
    private int mCurrentMenuNo = -1;
    private String mCurrentType = "";
    protected String mSearchStr = "";

    // 프로필 관련
    protected CircleImageView civUsrPhoto;
    protected TextView tvUsrNM, tvPosNm, tvDeptNm, tvSelfIntro, tvPhnNum, tvEmail;

    // 메인 플로팅 추가버튼
    public FloatingActionMenu mFab;
    private FloatingActionButton mFabSnsAdd, mFabPlanAdd, mFabCfrcAdd, mFabTaskAdd, mFabMemoAdd, mFabWorkAdd;
    public int sns_menu_exist_cnt = 0, plan_menu_exist_cnt = 0, task_menu_exist_cnt = 0, cfrc_menu_exist_cnt = 0, memo_menu_exist_cnt = 0, work_menu_exist_cnt = 0;

    // 상단우측 메뉴아이템
    protected MenuItem mMiSearch, mMiFunction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceUtil.initializeInstance(getApplicationContext());
        mPref = PreferenceUtil.getInstance();

        if (getIntent().getBooleanExtra(CodeConstant.LOGOUT_EXIT, false)) {
            mPref.setString(PreferenceUtil.PREF_AUTO_LOGIN, null);
            mPref.setString(PreferenceUtil.PREF_LOGIN_ID, null);
            mPref.setString(PreferenceUtil.PREF_LOGIN_PASSWD, null);
            mPref.setString(PreferenceUtil.PREF_OAUTH_TOKEN, null);
            GcmUtil.unregistGcm(MainActivity.this);
            finish();
        }

        makeDrawerMenu();

        backPressCloseHandler = new BackPressCloseHandler(this);

        //추가 플로팅 버튼 처리
        mFab = (FloatingActionMenu) findViewById(R.id.fab);
        mFab.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(this, R.anim.show_from_bottom));
        mFab.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(this, R.anim.hide_to_bottom));
        mFab.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFab.toggle(true);
            }
        });

        mFabSnsAdd = (FloatingActionButton) findViewById(R.id.fab_sns_add);
        mFabPlanAdd = (FloatingActionButton) findViewById(R.id.fab_plan_add);
        mFabCfrcAdd = (FloatingActionButton) findViewById(R.id.fab_cfrc_add);
        mFabTaskAdd = (FloatingActionButton) findViewById(R.id.fab_task_add);
        mFabMemoAdd = (FloatingActionButton) findViewById(R.id.fab_memo_add);
        mFabWorkAdd = (FloatingActionButton) findViewById(R.id.fab_work_add);
        mFabSnsAdd.setOnClickListener(fabClickListener);
        mFabPlanAdd.setOnClickListener(fabClickListener);
        mFabCfrcAdd.setOnClickListener(fabClickListener);
        mFabTaskAdd.setOnClickListener(fabClickListener);
        mFabMemoAdd.setOnClickListener(fabClickListener);

        //NIA2차 과제관리 기능 미포함
//        mFabWorkAdd.setVisibility(View.GONE);
//        mFabWorkAdd.setOnClickListener(fabClickListener);

//        mFab.setClosedOnTouchOutside(true);


    }

    public void initDrawerView() {
        civUsrPhoto = (CircleImageView)mDrawer.getHeader().findViewById(R.id.civ_usr_photo);
        tvUsrNM = (TextView)mDrawer.getHeader().findViewById(R.id.tv_usr_nm);
        tvPosNm = (TextView)mDrawer.getHeader().findViewById(R.id.tv_pos_nm);
        tvDeptNm = (TextView)mDrawer.getHeader().findViewById(R.id.tv_dept_nm);
        tvSelfIntro = (TextView)mDrawer.getHeader().findViewById(R.id.tv_self_intro);
        tvPhnNum = (TextView)mDrawer.getHeader().findViewById(R.id.tv_phn_num);
        tvEmail = (TextView)mDrawer.getHeader().findViewById(R.id.tv_email);

        Button btnUserSetting = (Button) mDrawer.getHeader().findViewById(R.id.btn_user_setting);
        btnUserSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SNSPersonalConfigActivity.class);
                startActivity(intent);
            }
        });

        tvUsrNM.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
        tvPosNm.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_POS_NM));
        tvDeptNm.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_DEPT_NM));

        String selfIntro = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_SELF_INTRO);
        if (TextUtils.isEmpty(selfIntro) || "null".equals(selfIntro))
            tvSelfIntro.setText(getString(R.string.empty_self_intro));
        else tvSelfIntro.setText(selfIntro);
        tvPhnNum.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_PHONE));
        tvEmail.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_EMAIL));

        Glide.with(civUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_PHOTO)))
                .animate(android.R.anim.fade_in)
                .error(R.drawable.ic_no_usr_photo)
                .centerCrop()
                .into(civUsrPhoto);
        civUsrPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SNSDetailProfileActivity.class);
                intent.putExtra(SNSDetailProfileActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSDetailProfileActivity.USR_NM, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
                startActivity(intent);
            }
        });

        if(getPackageName().equals("com.i2max.i2smartwork.nhfire")) {
            showDefaultMenu("i2cNhhome");
        } else {
            showDefaultMenu(CodeConstant.SNS_FOLLOW);
        }


        //동적메뉴에 따른 버튼 표시
        if(sns_menu_exist_cnt < 1  && mFabSnsAdd != null) mFabSnsAdd.setVisibility(View.GONE);
        if(cfrc_menu_exist_cnt < 1 && mFabCfrcAdd != null) mFabCfrcAdd.setVisibility(View.GONE);
        if(plan_menu_exist_cnt < 1 && mFabPlanAdd != null) mFabPlanAdd.setVisibility(View.GONE);
        if(task_menu_exist_cnt < 1 && mFabTaskAdd != null) mFabTaskAdd.setVisibility(View.GONE);
        if(memo_menu_exist_cnt < 1 && mFabMemoAdd != null) mFabMemoAdd.setVisibility(View.GONE);
        if(work_menu_exist_cnt < 1 && mFabWorkAdd != null) mFabWorkAdd.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * GCM 이동처리
         * check login info if null, move login page,
         */
        if (getIntent().getBooleanExtra(CodeConstant.LAUNCH_GCM_MSG, false)) {
            if(TextUtils.isEmpty(mPref.getString(PreferenceUtil.PREF_OAUTH_TOKEN))) {
                Intent intent = new Intent(MainActivity.this, IntroLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(CodeConstant.LAUNCH_GCM_MSG, true);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(MainActivity.this, PushListActivity.class);
                intent.putExtra(CodeConstant.TITLE, getString(R.string.notification));
                startActivity(intent);
                getIntent().putExtra(CodeConstant.LAUNCH_GCM_MSG, false);
            }
        }

//        loadListMessageCount();

        if (SNSMainFragment.isChangedList) {
            if (currentfragment != null && !(currentfragment instanceof SNSMainFragment) ){
                if(getPackageName().equals("com.i2max.i2smartwork.nhfire")) {
                    showDefaultMenu("i2cNhhome");
                } else {
                    showDefaultMenu(CodeConstant.SNS_FOLLOW);
                    SNSMainFragment.isChangedList = false;
                }

            }
        }
        //기보드 숨김
        hideSoftKeyboard();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void makeDrawerMenu() {
        if(AppConstant.MENU_LOCAL_JSON_ENABLED) {
            //read from local json in assets
            Map<String, Object> status = FileUtil.readMenuFromAsset(MainActivity.this, "menu.json");
            Map<String, Object> statusInfo = (Map<String, Object>)status.get("statusInfo");
            mJsonMapMenu = (List<LinkedTreeMap<String, Object>>)statusInfo.get("list_menu");
            drawerMenu();
        } else { //read from server
            getUpdateMenuFromServer();
        }
    }

    public void getUpdateMenuFromServer() {
        I2ConnectApi.requestJSON2Map(getBaseContext(), I2UrlHelper.Update.getMenu())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Update.getMenu onCompleted");
                        drawerMenu();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Update.getMenu onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(MainActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Update.getMenu onNext");
                        Map<String, Object> statusInfo = (Map<String, Object>) status.get("statusInfo");
                        mJsonMapMenu = (List<LinkedTreeMap<String, Object>>) statusInfo.get("list_menu");

                    }
                });
    }

    public void drawerMenu() {
        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();

        if (mJsonMapMenu != null ) {
            //default setting notification menu 알림메뉴 고정 > TODO 이번 데모에서는 일단 제외 처리
//            drawerItems.add(new PrimaryDrawerItem().withName(getString(R.string.notification)).withIcon(R.drawable.ic_menu_notify)
//                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.colorAccent))
//                    .withIdentifier(0).withSelectable(false).withTag(getString(R.string.notification)).withIdentifier(CodeConstant.NOTI_MAIN));

            for (int i = 0; i < mJsonMapMenu.size(); i++) {
                LinkedTreeMap<String, Object> menuItem = (LinkedTreeMap<String, Object>) mJsonMapMenu.get(i);
                // addDrawerMenu(drawerItems, menuItem); // 리스트 그룹명은 출력에서 제외
                String child_yn = String.valueOf(menuItem.get("child_yn"));

                if (!TextUtils.isEmpty(child_yn) && "Y".equals(child_yn)) {
                    List<LinkedTreeMap<String, Object>> childList = (List<LinkedTreeMap<String, Object>>) menuItem.get("child_list");

                    for (int j = 0; j < childList.size(); j++) {
                        LinkedTreeMap<String, Object> secondMenuItem = (LinkedTreeMap<String, Object>) childList.get(j);
                        addDrawerMenu(drawerItems, secondMenuItem);

                        String second_child_yn = String.valueOf(secondMenuItem.get("child_yn"));
                        if (!TextUtils.isEmpty(second_child_yn) && "Y".equals(second_child_yn)) {
                            List<LinkedTreeMap<String, Object>> secondChildList = (List<LinkedTreeMap<String, Object>>) secondMenuItem.get("child_list");
                            for (int k = 0; k < secondChildList.size(); k++) {
                                LinkedTreeMap<String, Object> thirdMenuItem = (LinkedTreeMap<String, Object>) secondChildList.get(k);
                                addDrawerMenu(drawerItems, thirdMenuItem);
                            }
                        }
                    }
                }
            }

        }

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withHeader(R.layout.nav_header)
                .withDrawerItems(drawerItems)
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName("로그아웃").withSelectedTextColor(Color.WHITE).withSelectedColorRes(R.color.colorPrimary).withIdentifier(CodeConstant.LOGOUT).withSetSelected(true)
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                    }
                })
                .withOnDrawerItemClickListener(onDrawerItemClickListener)
                .build();
        // 로그아웃 버튼 체크색상표시를 위함

        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawer.setStickyFooterSelectionAtPosition(0, false);
            }
        }, 500);

        initDrawerView();

    }

    public void addDrawerMenu(ArrayList<IDrawerItem> drawerItems, LinkedTreeMap<String, Object> menuItem) {
        if(mListMenu == null) mListMenu = new ArrayList<>();

        String menuNm = String.valueOf(menuItem.get("menu_nm"));
        String menuId = String.valueOf(menuItem.get("program_file_nm"));
        String childYn = String.valueOf(menuItem.get("child_yn"));

        //공통메뉴 존재 여부 카운트 수량이 적은것부터 처리
        if(CodeConstant.PLAN_MAIN.equals(menuId)) plan_menu_exist_cnt++;
        else if(CodeConstant.TASK_MAIN.equals(menuId)) task_menu_exist_cnt++;
        else if(CodeConstant.MEMO_MAIN.equals(menuId)) memo_menu_exist_cnt++;
        else if(CodeConstant.WORK_MAIN.equals(menuId)) work_menu_exist_cnt++;
        else if(menuId.indexOf("I2cSnsConference") > -1) cfrc_menu_exist_cnt++;
        else if(menuId.indexOf("I2cSnsHome") > -1) sns_menu_exist_cnt++;

        Map<String, String> menuMap = new HashMap();
        menuMap.put(CodeConstant.MENU_TYPE, menuId);
        menuMap.put(CodeConstant.TITLE, menuNm);
        menuMap.put(CodeConstant.CHILD_YN, childYn);
        menuMap.put(CodeConstant.MENU_URL, String.valueOf(menuItem.get("menu_url")));
        Log.e(TAG, "menuMap add = " + menuMap.toString());
        mListMenu.add(menuMap);

        int menuIndex = mListMenu.size()-1; //리스트 위치값

        if(TextUtils.isEmpty(menuId)) return;

        if(CodeConstant.MENU_DIR.equals(menuId)) { //기능 타이틀 dir
            if("피드".equals(menuNm)) return;

            if(!TextUtils.isEmpty(childYn) && "Y".equals(childYn)) {
                drawerItems.add(new SectionDrawerItem().withName(menuNm).withTag(menuNm).withIdentifier(menuIndex));
            } else {
                drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_cms).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
            }
        } else if(CodeConstant.SNS_FOLLOW.equals(menuId) || CodeConstant.SNS_ME.equals(menuId) ||
            CodeConstant.SNS_OBJECT.equals(menuId) || CodeConstant.SNS_BOOKMARK.equals(menuId) ||
            CodeConstant.SNS_OPEN.equals(menuId)) { //SNS
            drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_sns).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
        } else if(CodeConstant.TASK_MAIN.equals(menuId)) { //TASK
            drawerItems.add(new DividerDrawerItem());
            drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_sns).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
            // R.drawable.ic_menu_task
        } else if(CodeConstant.CFRC_LIST.equals(menuId) || CodeConstant.CFRC_ONLINE.equals(menuId) ||
            CodeConstant.CFRC_FILE.equals(menuId) || CodeConstant.CFRC_SCHEDULE.equals(menuId)) { //CFRC
            drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_conference).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
        } else if(CodeConstant.MEMO_MAIN.equals(menuId)) { //MEMO
            drawerItems.add(new DividerDrawerItem());
            drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_memo).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
        } else if(CodeConstant.WORK_MAIN.equals(menuId)) { //WORK
            drawerItems.add(new DividerDrawerItem());
            drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_work).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
        } else { //각 사이트별 기능
            // 스킵처리
            if("회의신청".equals(menuNm) || "회의실관리".equals(menuNm)) return;

            if("정부지원사업".equals(menuNm)) { // 위쪽 행 경계선 처리  TODO 아이콘 적용시 변경 방법 필요
                drawerItems.add(new DividerDrawerItem());
                drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_sns).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
                // R.drawable.ic_menu_cms
            } else {
                drawerItems.add(new PrimaryDrawerItem().withName(menuNm).withIcon(R.drawable.ic_menu_sns).withSelectable(false).withTag(menuNm).withIdentifier(menuIndex));
            }

        }
    }

    protected Drawer.OnDrawerItemClickListener onDrawerItemClickListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int m, IDrawerItem iDrawerItem) {
            if (iDrawerItem==null) return false;

            final int menuNo = iDrawerItem.getIdentifier();
            switch (menuNo) {
                case CodeConstant.LOGOUT:
                    DialogUtil.showLogoutDialog(MainActivity.this);
                    break;
                default:
                    // 메뉴이동
                    mDrawerActionHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            makePageWithMenu(menuNo);
                        }
                    }, CodeConstant.DRAWER_CLOSE_DELAY_MS);
                    break;
            }

            return false;
        }
    };

    public void showDefaultMenu(String menuType) {
        if(mListMenu == null || TextUtils.isEmpty(menuType)) return;

        for (int i = 0; i < mListMenu.size(); i++) {
            if(menuType.equals(mListMenu.get(i).get(CodeConstant.MENU_TYPE))) {
                makePageWithMenu(i);
                break;
            }
        }
    }

    public void makePageWithMenu(int menuNo) {
        mCurrentMenuNo = menuNo;
        String type = mListMenu.get(menuNo).get(CodeConstant.MENU_TYPE);
        String title = mListMenu.get(menuNo).get(CodeConstant.TITLE);
        String url = mListMenu.get(menuNo).get(CodeConstant.MENU_URL);

        //fabric answer 로그 처리
        TraceUtil.logAnswer(MainActivity.this, title, type, String.valueOf(menuNo));
//        Log.e(TAG, menuNo + "menuMap  = "+ mListMenu.get(menuNo).toString());

//        if(getPackageName().equals("com.i2max.i2smartwork.nhfire")) NhfireUtil.addNhMenu(this, type, title);

        if (menuNo == CodeConstant.NOTI_MAIN) { //첫번째 노티 강제 셋팅 TODO 알람사용시 위 알람 메뉴등록 풀고 NOTI_MAIN 값을 0으로 셋팅
            Intent intent = new Intent(getBaseContext(), PushListActivity.class);
//            Intent intent = new Intent(getBaseContext(), ReportWriteActivity.class);
            intent.putExtra(CodeConstant.TITLE, title);
            startActivity(intent);
        } else if (type.equals(CodeConstant.SNS_FOLLOW)) {
            Fragment fragment = new SNSMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SNSMainFragment.MODE, "FOLLOW");
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.SNS_ME) ) {
            Fragment fragment = new SNSMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SNSMainFragment.MODE, "ME");
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.SNS_OBJECT) ) {
            Fragment fragment = new SNSMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SNSMainFragment.MODE, "OBJECT");
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.SNS_BOOKMARK) ) {
            Fragment fragment = new SNSMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SNSMainFragment.MODE, "BOOKMARK");
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.SNS_OPEN) ) {
            Fragment fragment = new SNSMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SNSMainFragment.MODE, "OPEN");
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.PLAN_MAIN)) {
            Fragment fragment = new PlanMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.CFRC_LIST)) {
            Fragment fragment = new ConferenceMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.CFRC_ONLINE) ) {
            Intent intent = new Intent(getBaseContext(), ConferenceOnlineActivity.class);
            intent.putExtra(CodeConstant.TITLE, title);
            startActivity(intent);
        } else if (type.equals(CodeConstant.CFRC_FILE) ) {
            Intent intent = new Intent(getBaseContext(), ConferenceFileListActivity.class);
            intent.putExtra(CodeConstant.TITLE, title);
            startActivity(intent);
        } else if (type.equals(CodeConstant.CFRC_SCHEDULE) ) {
            Intent intent = new Intent(getBaseContext(), ConferenceRoomScheduleActivity.class);
            intent.putExtra(CodeConstant.TITLE, title);
            intent.putExtra(ConferenceRoomScheduleActivity.READ_ONLY, "Y");
            startActivity(intent);
        } else if (type.equals(CodeConstant.TASK_MAIN)) {
            Fragment fragment = new TaskMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.MEMO_MAIN)) {
            Fragment fragment = new com.i2max.i2smartwork.common.memo.MemoMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.WORK_MAIN)) {
            Fragment fragment = new WorkMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        } else if (type.equals(CodeConstant.I2LIVECHAT)) {
            try {
                Intent intent = IntentUtil.getI2LiveChatIntent(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_TYPE),"");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                DialogUtil.showConfirmDialog(MainActivity.this, "알림", "I2LiveChat앱이 설치되어 있지않습니다.\n다운로드를 진행합니다.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "I2LiveChat앱 설치 APK를 다운로드를 시작합니다", Toast.LENGTH_LONG).show();
                                String downloadURL = I2UrlHelper.I2App.getI2LivechatAppDownloadUrl();
                                //토큰 인증처리
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                Bundle bundle = new Bundle();
                                bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization());
                                intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                Log.d("", "intent:" + intent.toString());
                            }
                        });
            }
        } else if(!TextUtils.isEmpty(url)) { //URL WEBVIEW 가장 마지막에 처리
            Fragment fragment = new WebviewFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CodeConstant.MENU_URL, url);
            bundle.putString(CodeConstant.TITLE, title);
            fragment.setArguments(bundle);
            changeMainFragment(fragment);
        }
//        showHideMenuItem();
    }

    public void changeMainFragment(Fragment fragment){
        mSearchStr = "";

        if (fragment != null) {
            currentfragment = fragment;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        DialogUtil.destoryDialogs();
    }

//    public void showHideMenuItem() {
//        if (mMiSearch==null) return;
//
//        if (mCurrentType.equals(CodeConstant.NOTI) || mCurrentType.equals(CodeConstant.SNS_ALL_LIST) ||
//                mCurrentType.equals(CodeConstant.SNS_FOLLOW_LIST) || mCurrentType.equals(CodeConstant.SNS_ME_LIST) ||
//                mCurrentType.equals(CodeConstant.SNS_BUSINESS_LIST) || mCurrentType.equals(CodeConstant.SNS_BOOKMARK_LIST) ||
//                mCurrentType.equals(CodeConstant.SNS_OPEN_LIST) || mCurrentType.equals(CodeConstant.CONFERENCE_LIST) ||
//                mCurrentType.equals(CodeConstant.CONFERENCE_ONLINE_LIST) || mCurrentType.equals(CodeConstant.CONFERENCE_PLAN_LIST) ||
//                mCurrentType.equals(CodeConstant.TASK_LIST) || mCurrentType.equals(CodeConstant.MEMO_LIST) ||
//                mCurrentType.equals(CodeConstant.WORK_LIST) ) {
//            mMiSearch.setVisible(true);
//        } else if (mCurrentType.equals(CodeConstant.SCHEDULE)) {
//            mMiSearch.setVisible(false);
//        }
//    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        mMiSearch = menu.findItem(R.id.action_search);
//        mMiFunction = menu.findItem(R.id.action_sns_function);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_main_activity, menu);
        mMiSearch = menu.findItem(R.id.action_search);
        mMiFunction = menu.findItem(R.id.action_sns_function);
//        mMiSearch = (MenuItem) findViewById(R.id.action_search);
//        mMiFunction = (MenuItem) findViewById(R.id.action_sns_function);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer();
                return true;
            case R.id.action_search:
                Intent i2Search = new Intent(this, I2SearchActivity.class);
                i2Search.putExtra(I2SearchActivity.START_POS, I2SearchActivity.RIGHT_2);
                i2Search.putExtra(I2SearchActivity.SEARCH_STR, mSearchStr);
                startActivityForResult(i2Search, I2SearchActivity.REQUEST_SEARCH);
                return true;
            case R.id.action_sns_function:
                Intent intent = new Intent(MainActivity.this, SNSPersonalFunctionActivity.class);
                intent.putExtra(SNSPersonalFunctionActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonalFunctionActivity.USR_NM_DEPT_NM,
                        PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM) + " " +
                                PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_POS_NM) );
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else if (mFab.isOpened()) {
            mFab.close(true);
        } else {
            if (mCurrentMenuNo > 0) {
                mCurrentMenuNo = 0;
                Fragment fragment = new SNSMainFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SNSMainFragment.MODE, "FOLLOW");
                bundle.putString(CodeConstant.TITLE, "나의 피드");
                fragment.setArguments(bundle);
                changeMainFragment(fragment);
            } else {
                backPressCloseHandler.onBackPressed();
            }
        }
    }

    //플로팅 버튼 표시/숨김 처리
    public void setVisibleFabButton(boolean bool) {

        if(bool) mFab.showMenuButton(true);
        else mFab.hideMenuButton(true);

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
                            intent = new Intent(MainActivity.this, SNSWriteActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.fab_plan_add:
                            intent = new Intent(MainActivity.this, PlanCreateActivity.class);
                            intent.putExtra(PlanCreateActivity.MODE, PlanCreateActivity.MODE_CREATE);
                            startActivity(intent);
                            break;
                        case R.id.fab_cfrc_add:
                            intent = new Intent(MainActivity.this, ConferenceWriteActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.fab_task_add:
                            intent = new Intent(MainActivity.this, TaskWriteActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.fab_memo_add:
                            intent = new Intent(MainActivity.this, MemoWriteActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.fab_work_add:
//                            intent = new Intent(MainActivity.this, WorkWriteActivity.class);
//                            startActivity(intent);
                            break;
                    }
                }
            }, CodeConstant.DRAWER_CLOSE_DELAY_MS);
            mFab.toggle(true);
        }
    };

    public void doSearchFragment(String searchStr) {
        if (currentfragment != null && (currentfragment instanceof SNSMainFragment) ){
            ((SNSMainFragment)currentfragment).searchSnsResult(searchStr);
        }

        if (currentfragment != null && (currentfragment instanceof ConferenceMainFragment) ){
            ((ConferenceMainFragment)currentfragment).searchResult(searchStr);
        }

        if (currentfragment != null && (currentfragment instanceof WorkMainFragment) ){
            ((WorkMainFragment)currentfragment).searchResult(searchStr);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == I2SearchActivity.REQUEST_SEARCH && resultCode == RESULT_OK) {
            String searchStr = data.getExtras().getString(I2SearchActivity.EXTRA_SEARCH_STR);
            doSearchFragment(searchStr);
            mSearchStr = searchStr;
        }
    }

    protected  void requestNoti() {
        I2ConnectApi.requestJSON(getBaseContext(), I2UrlHelper.Push.getListUnreadMessageCount())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Push.getListUnreadMessageCount onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Push.getListUnreadMessageCount onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(MainActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.Push.getListUnreadMessageCount onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            List<JSONObject> jsonList = I2ResponseParser.getStatusInfoArrayAsList(jsonObject);
                            int m = 0;
                            for (int i = 0; i < jsonList.size(); i++) {
                                try {
                                    m += jsonList.get(i).getInt("cnt");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            StringHolder sh = new StringHolder(String.format("%d", m));
                            mDrawer.updateBadge(0, sh);
                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * GCM
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    /**
     * Google Play Service를 사용할 수 있는 환경이지를 체크한다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService를 실행한다.
     */
    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public void registBroadcastReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();


                if(action.equals(PreferenceUtil.REGISTRATION_READY)){
                    // 액션이 READY일 경우
//                    mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
//                    mInformationTextView.setVisibility(View.GONE);
                } else if(action.equals(PreferenceUtil.REGISTRATION_GENERATING)){
                    // 액션이 GENERATING일 경우
//                    mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
//                    mInformationTextView.setVisibility(View.VISIBLE);
//                    mInformationTextView.setText(getString(R.string.registering_message_generating));
                } else if(action.equals(PreferenceUtil.REGISTRATION_COMPLETE)){
                    // 액션이 COMPLETE일 경우
//                    mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
//                    mRegistrationButton.setText(getString(R.string.registering_message_complete));
//                    mRegistrationButton.setEnabled(false);
                    String token = intent.getStringExtra("token");
//                    mInformationTextView.setText(token);
                }

            }
        };
    }

    public void forceCrash(View view) {
        //TODO 릴리즈시 force to crash 버튼 안보이게 gone 처리
        throw new RuntimeException("This is a crash");
    }
}

