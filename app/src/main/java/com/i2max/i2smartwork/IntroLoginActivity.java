package com.i2max.i2smartwork;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.common.web.WebviewActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.AppConstant;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.gcm.GcmUtil;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.nineoldandroids.animation.Animator;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

import io.fabric.sdk.android.Fabric;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

//import com.i2max.i2smartwork.nhfire.api.NhfireUrlHelper;

/**
 * 농협손해 농협재해 현장조사 시스템 로그인 화면
 */
public class IntroLoginActivity extends BaseAppCompatActivity {
    static String TAG = IntroLoginActivity.class.getSimpleName();

    protected EditText etLoginID, etLoginPassword;
    protected CheckBox cbAutoLogin;
    protected Button btnLogin;
    protected TextView personinfo;
    protected TextView signup;

    private PreferenceUtil mPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init fabric
        TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConstant.TWITTER_KEY, AppConstant.TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits(), new Crashlytics());

        setContentView(R.layout.activity_intro);


        PreferenceUtil.initializeInstance(getApplicationContext());

        personinfo = (TextView) findViewById(R.id.personinfo);

        personinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroLoginActivity.this, PersonInfoActivity.class);
                startActivity(intent);
            }
        });

        signup = (TextView) findViewById(R.id.signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(CodeConstant.TAR_SELECT_URL, "http://m.expertbank.kr/i2cowork/auth/join/joinMembershipPolicyTerms.do");
                // bundle.putString(CodeConstant.TAR_SELECT_URL, "http://192.168.10.25/webview.html");
                bundle.putString(CodeConstant.TITLE, "뒤로");

                Intent intent = new Intent(IntroLoginActivity.this, WebviewActivity.class);
                //Intent intent = new Intent(IntroLoginActivity.this, WebviewFragment.class);
                intent.putExtras(bundle);
                startActivity(intent);
                Log.d(TAG, "onClick: startActivity");
            }
        });

        mPref = PreferenceUtil.getInstance();
        //GCM
        GcmUtil.getInstanceIdToken(IntroLoginActivity.this);
        GcmUtil.registBroadcastReceiver();

        etLoginID = (EditText) findViewById(R.id.et_login_id);
        etLoginPassword = (EditText) findViewById(R.id.et_login_password);
        etLoginPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getId() == etLoginPassword.getId() && actionId == EditorInfo.IME_ACTION_DONE) {
                    loginProcess();
                }

                return false;
            }
        });

        cbAutoLogin = (CheckBox) findViewById(R.id.cb_auto_login);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProcess();
            }
        });


        insertLog("01", "", "", "", "앱 클릭");   // 앱아이콘이 클릭되면 실행하여 실행로그 저장

        //SMS인증버튼
//        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
//        digitsButton.setAuthTheme(android.R.style.Theme_Material);
//        digitsButton.setCallback(authCallback);


        ImageView ivLogo = (ImageView) findViewById(R.id.iv_intro_logo);

        String coopType = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_COOP_TYPE);
        String userId = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_LOGIN_ID);

        if (coopType != null) {
            switch (coopType)
            {
                case "KP":
                    ivLogo.setImageResource(R.drawable.img_logo_kp);
                    break;
                case "DS":
                    ivLogo.setImageResource(R.drawable.img_logo_ds);
                    break;
                case "KI":
                    ivLogo.setImageResource(R.drawable.img_logo_ki);
                    break;
                case "LG":
                    ivLogo.setImageResource(R.drawable.img_logo_lg);
                    break;
                case "EB":
                    ivLogo.setImageResource(R.drawable.img_logo_eb);
                    break;
                case "IP":
                    ivLogo.setImageResource(R.drawable.img_logo_ip);
                    break;
                case "PG":
                    ivLogo.setImageResource(R.drawable.img_logo_pg);
                    break;
                default:
                    ivLogo.setImageResource(R.drawable.img_logo);
                    break;
            }

        }


        //자동로그인 설정값 불러오기
        final Boolean boolAutoLogin = Boolean.valueOf(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_AUTO_LOGIN));
        cbAutoLogin.setChecked(boolAutoLogin);

        if (!getPackageName().equals("com.i2max.i2smartwork")) {
            final TextView tvIntroTitle = (TextView) findViewById(R.id.tv_intro_title);
            TextView tvFooterTitle = (TextView) findViewById(R.id.tv_footer_title);
            /**
             * 로그인 인트로 로고/타이틀 사이트별 별도처리 gradle BuildConfig
             */
            tvIntroTitle.setText(getString(R.string.intro_title));
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvFooterTitle.setText("Version : " + versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            YoYo.with(Techniques.BounceInLeft).duration(0).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    tvIntroTitle.setVisibility(View.VISIBLE);

                    YoYo.with(Techniques.BounceInLeft).duration(0).withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            checkAutoLogin(boolAutoLogin);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    }).playOn(tvIntroTitle);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            }).playOn(ivLogo);
        } else {
            checkAutoLogin(boolAutoLogin);
        }

    }



    @Override
    public void onResume() {
        super.onResume();
        GcmUtil.registGcm(this.getApplicationContext());
    }

    private void checkAutoLogin(boolean autoLogin) {
        if (autoLogin) {
            mPref = PreferenceUtil.getInstance();
            String loginID = mPref.getString(PreferenceUtil.PREF_LOGIN_ID);
            String loginPW = mPref.getString(PreferenceUtil.PREF_LOGIN_PASSWD);

            etLoginID.setText(loginID);
            etLoginPassword.setText(loginPW);

            loginProcess();
        } else {
            showLoginLayout();
        }
    }

    protected void showLoginLayout() {
        findViewById(R.id.group_login).setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceInUp).duration(1000).playOn(findViewById(R.id.group_login));
    }

    private void insertLog(String code, String usr_id, String usr_tp_cd, String grp_id, String remark) {
        I2ConnectApi.requestString(getBaseContext(), I2UrlHelper.LogInsert.getLogInfo(code, usr_id, usr_tp_cd, grp_id, remark))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "insertLog onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "insertLog onError");
                        DialogUtil.removeCircularProgressDialog();

                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d(TAG, "insertLog onNext");
                        Log.e("decurd", result);
                    }
                });
    }

    protected void loginProcess() {
        final String loginId = etLoginID.getText().toString().trim();
        final String loginPw = etLoginPassword.getText().toString().trim();
        if (TextUtils.isEmpty(loginId) || TextUtils.isEmpty(loginPw)) {
            Log.d(TAG, "no input login info");
            DialogUtil.showErrorDialog(IntroLoginActivity.this, "로그인 정보를 입력해주시기 바랍니다.");
            return;
        }

        DialogUtil.showCircularProgressDialog(IntroLoginActivity.this);

        I2ConnectApi.sendGCMToken(getBaseContext(), I2UrlHelper.GCM.getSendableGcmToken(loginId, GcmUtil.getToken())) // 유저 id와 토큰 전송
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "requestSendToken onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "requestLogin onError");
                        DialogUtil.removeCircularProgressDialog();
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(IntroLoginActivity.this, e);
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d(TAG, "requestLogin onNext");
                        Log.d(TAG, "Result = " + result);
                    }
                });

        I2ConnectApi.requestJSON2Map(getBaseContext(), I2UrlHelper.Login.getOAuthLogin("APP", loginId, loginPw))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "requestLogin onCompleted");
                        DialogUtil.removeCircularProgressDialog();

                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra(CodeConstant.LAUNCH_GCM_MSG, getIntent().getBooleanExtra(CodeConstant.LAUNCH_GCM_MSG, false));
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "requestLogin onError");
                        DialogUtil.removeCircularProgressDialog();

                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(IntroLoginActivity.this, e);

                        showLoginLayout();
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "requestLogin onNext");
                        //save login info
                        mPref = PreferenceUtil.getInstance();
                        if (cbAutoLogin.isChecked()) {
                            mPref.setString(PreferenceUtil.PREF_AUTO_LOGIN, "true");
                            mPref.setString(PreferenceUtil.PREF_LOGIN_ID, loginId);
                            mPref.setString(PreferenceUtil.PREF_LOGIN_PASSWD, loginPw);
                        } else {
                            mPref.setString(PreferenceUtil.PREF_AUTO_LOGIN, "false");
                            mPref.setString(PreferenceUtil.PREF_LOGIN_ID, "");
                            mPref.setString(PreferenceUtil.PREF_LOGIN_PASSWD, "");
                        }

                        LinkedTreeMap<String, String> statusInfo = (LinkedTreeMap<String, String>) status.get("statusInfo");
                        mPref.setString(PreferenceUtil.PREF_USR_JSON_INFO, statusInfo.toString());
                        mPref.setString(PreferenceUtil.PREF_OAUTH_TOKEN, statusInfo.get("access_token"));
                        mPref.setString(PreferenceUtil.PREF_USR_ID, statusInfo.get("usr_id"));
                        mPref.setString(PreferenceUtil.PREF_USR_NM, statusInfo.get("usr_nm"));
                        mPref.setString(PreferenceUtil.PREF_POS_NM, statusInfo.get("pos_nm"));
                        mPref.setString(PreferenceUtil.PREF_DEPT_NM, statusInfo.get("dept_nm"));
                        mPref.setString(PreferenceUtil.PREF_USR_PHONE, statusInfo.get("phn_num"));
                        mPref.setString(PreferenceUtil.PREF_USR_EMAIL, statusInfo.get("email"));
                        mPref.setString(PreferenceUtil.PREF_SELF_INTRO, statusInfo.get("self_intro"));
                        mPref.setString(PreferenceUtil.PREF_USR_PHOTO, statusInfo.get("usr_photo_url"));
                        mPref.setString(PreferenceUtil.PREF_USR_TP_CD, statusInfo.get("usr_tp_cd"));
                        mPref.setString(PreferenceUtil.PREF_USR_TYPE, FormatUtil.getStringValidate(statusInfo.get("type"))); //CS 고객상담에서 I2LIVECHAT연동 처리하는 값
                        mPref.setString(PreferenceUtil.PREF_ENC_USR_ID, FormatUtil.getStringValidate(statusInfo.get("enc_user_id"))); //CS 고객상담에서 I2LIVECHAT연동 처리하는 값
                        //TODO 서버 API수정 요청
                        mPref.setString(PreferenceUtil.PREF_COOP_TYPE, statusInfo.get("coop_type"));

                        inputDataBox();
                        logUser();
                        Log.d(TAG, "oauthToken = " + statusInfo.get("access_token"));
                    }
                });

        insertLog("02", mPref.getString(PreferenceUtil.PREF_USR_ID), mPref.getString(PreferenceUtil.PREF_USR_TP_CD), "", "앱 접속");
        // Log.e("decurd", "usr_id = " + mPref.getString(PreferenceUtil.PREF_USR_ID) + "  ,  usr_tp_cd = " + mPref.getString(PreferenceUtil.PREF_USR_TP_CD) + "aaa");
    }

    private void inputDataBox() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logUser() {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
        Crashlytics.setUserName(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
        Crashlytics.setUserEmail(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_EMAIL));

    }

    private AuthCallback authCallback = new AuthCallback() {
        @Override
        public void success(DigitsSession digitsSession, String s) {
            Log.d(TAG, "Digit string: " + s.toString());
            Log.d(TAG, "Digit session: " + digitsSession.getAuthToken().toString());
            Toast.makeText(IntroLoginActivity.this, "digit string: " + s.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void failure(DigitsException e) {
            Log.d(TAG, "Oops Digits issue" + e.getMessage());
            Toast.makeText(IntroLoginActivity.this, "digit string: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    };



}
