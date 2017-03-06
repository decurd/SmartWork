package com.i2max.i2smartwork.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.i2max.i2smartwork.BuildConfig;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by shlee on 2016. 1. 18..
 */
public class GcmUtil {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GcmUtil";
    private static BroadcastReceiver mRegistrationBroadcastReceiver;
    private static String UserCheck;

    /**
     * Google Play Service를 사용할 수 있는 환경이지를 체크한다.
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService를 실행한다.
     */
    public static void getInstanceIdToken(Activity activity) {
        if (GcmUtil.checkPlayServices(activity)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(activity, RegistrationIntentService.class);
            activity.startService(intent);
        }
    }

    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public static void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(PreferenceUtil.REGISTRATION_READY)) {
                    // 액션이 READY일 경우
                } else if (action.equals(PreferenceUtil.REGISTRATION_GENERATING)) {
                    // 액션이 GENERATING일 경우
                } else if (action.equals(PreferenceUtil.REGISTRATION_COMPLETE)) {
                    // 액션이 COMPLETE일 경우
                    String gcmToken = intent.getStringExtra("token");
                    Log.e(TAG, "GCM TOKEN===========>" + gcmToken);
                    PreferenceUtil.initializeInstance(context.getApplicationContext());
                    PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_GCM_TOKEN, gcmToken);
                    UserCheck = gcmToken;
                }

            }
        };
    }

    public static boolean CheckUser() {

        boolean CheckUserIntro = false;

        if (!"".equals(UserCheck) && null != UserCheck) {
            CheckUserIntro = true;
        }

        return CheckUserIntro;
    }


    public static void registGcm(Context context) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(PreferenceUtil.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(PreferenceUtil.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(PreferenceUtil.REGISTRATION_COMPLETE));
    }

    public static void unregistGcm(Context context) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getToken(){ // GCMToken 얻기
        if(CheckUser()){
            return UserCheck;
        }else{
            return "";
        }
    }


    /**
     * GCM TOKEN 서버저장
     *
     * @param context
     * @param accessToken
     * @param gcmToken
     */
    public static void loadSaveGcmToken(final Context context, String accessToken, String gcmToken) {

        I2ConnectApi.requestJSON2Map(context, I2UrlHelper.GCM.getSaveGcmToken(
                accessToken, BuildConfig.OAUTH_CLIENT_ID, gcmToken, "Y"))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.e(TAG, "I2UrlHelper.GCM.getSaveGcmToken onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.GCM.getSaveGcmToken onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(context, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.GCM.getSaveGcmToken onNext");
                    }
                });
    }
}
