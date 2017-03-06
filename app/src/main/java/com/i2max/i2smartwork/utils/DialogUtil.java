package com.i2max.i2smartwork.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;

import com.i2max.i2smartwork.IntroLoginActivity;
import com.i2max.i2smartwork.MainActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.CircularProgressDialog;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.gcm.GcmUtil;

/**
 * 알림창을 띄워주기 모아놓은 유틸
 * @author berserk1147
 *
 */
public class DialogUtil {
    private static Dialog dialog;
    private static ProgressDialog progressDialog;
    private static CircularProgressDialog circularProgressDialog;

    public static Dialog showErrorDialog(Context c, String message) {
        return showDialog(c, "오류", message);
    }

    public static Dialog showErrorDialog(Context c, String message, DialogInterface.OnClickListener listener) {
        return showDialog(c, "오류", message, listener);
    }

    public static Dialog showInformationDialog(Context c, String message) {
        return showDialog(c, "알림", message);
    }

    public static Dialog showInformationDialog(Context c, String message, DialogInterface.OnClickListener listener) {
        return showDialog(c, "알림", message, listener);
    }

    public static Dialog showDialog(Context c, String title, String message) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();

        if (((Activity)c).isFinishing())
            return null;

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(c, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(c);
        }

        dialog = builder.setTitle(title).setMessage(message)
                .setNeutralButton("확인", null).show();
        return dialog;
    }

    public static Dialog showDialog(Context c, String title, String message, DialogInterface.OnClickListener listener) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(c, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(c);
            }

        dialog = builder.setTitle(title).setMessage(message)
                .setNeutralButton("확인", listener).show();
        return dialog;
    }

    public static Dialog showConfirmDialog(Context c, String title, String message, DialogInterface.OnClickListener listener) {
        return showConfirmDialog(c, title, message, listener, null);
    }

    public static Dialog showConfirmDialog(Context c, String title, String message, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(c, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(c);
        }

        dialog = builder.setTitle(title).setMessage(message)
                .setPositiveButton("확인", positive).setNegativeButton("취소", negative).show();
        return dialog;
    }

    public static void closeDilog()
    {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

    }

    public static void showProgressDialog(Context c) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        progressDialog = new ProgressDialog(c);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }


    }

    public static boolean isCircularProgressLoding() {
        if(circularProgressDialog != null) return true;
        return circularProgressDialog.isShowing();
    }

    public static void showCircularProgressDialog(Context c) {

        if(circularProgressDialog==null) {
            circularProgressDialog = new CircularProgressDialog(c);
            circularProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            circularProgressDialog.setCancelable(true);
        }

        try {
            circularProgressDialog.show();
        }catch (Exception e) {
            Log.e("showCircularProgressDialog",e.toString());
        }

    }

    public static void hideCircularProgressDialog() {
        if(circularProgressDialog!=null)
            circularProgressDialog.dismiss();
    }

    public static void removeCircularProgressDialog() {
        if(circularProgressDialog!=null) {
            circularProgressDialog.dismiss();
            circularProgressDialog = null;
        }
    }

    public static void destoryDialogs() {
        if( dialog != null ) {
            dialog.dismiss();
            dialog = null;
        }

        if( progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if( circularProgressDialog != null ) {
            circularProgressDialog.dismiss();
            circularProgressDialog = null;
        }
    }

    /**
     * 세션만료 재로그인처리
     * @param c
     * @param e
     */
    public static void showErrorDialogWithValidateSession(Context c, Throwable e) {
        if (e != null && e.getMessage().contains("invalid_grant")) {
            DialogUtil.showReLoginDialog(c, c.getString(R.string.expired_login_session_title), c.getString(R.string.expired_login_session_msg));
        } else {
            DialogUtil.showErrorDialog(c, e.getMessage());
        }
    }

    /**
     * 타 모바일기기 로그인으로 인한 로그인 정보 만료시 재로그인 처리
     * @param c
     * @param title
     * @param message
     * @return
     */
    public static Dialog showReLoginDialog(final Context c, String title, String message) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //로그인 정보 초기화
//                        PreferenceUtil.initializeInstance(c);
//                        PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_AUTO_LOGIN, null);
//                        PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_LOGIN_ID, null);
//                        PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_LOGIN_PASSWD, null);

                Intent intent = new Intent(c, IntroLoginActivity.class);
                c.startActivity(intent);
                ((Activity)c).finish();
            }
        };

        return showConfirmDialog(c, title, message, listener, null);
    }

    /**
     * 로그아웃 처리
     * @param c
     * @return
     */
    public static Dialog showLogoutDialog(final Context c) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtil.initializeInstance(c);
                if(c instanceof MainActivity) {
                    //로그인 정보 초기화
                    PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_AUTO_LOGIN, null);
                    PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_LOGIN_ID, null);
                    PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_LOGIN_PASSWD, null);
                    PreferenceUtil.getInstance().setString(PreferenceUtil.PREF_OAUTH_TOKEN, null);
                    GcmUtil.unregistGcm(c);
                    ((Activity) c).finish();
                } else {
                    GcmUtil.loadSaveGcmToken(c, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN), "");
                    Intent intent = new Intent(c, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(CodeConstant.LOGOUT_EXIT, true);
                    c.startActivity(intent);
                    ((Activity)c).finish();
                }
            }
        };

        return showConfirmDialog(c, "로그아웃", "정말 로그아웃 하시겠습니까?", listener, null);
    }
}
