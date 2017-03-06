package com.i2max.i2smartwork.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

public class PreferenceUtil {
	// SharedPreferences String
	public static final String PREF_NAME = (new ApplicationInfo()).packageName + ".preferences";

	//GCM
	public static final String REGISTRATION_READY = "registrationReady";
	public static final String REGISTRATION_GENERATING = "registrationGenerating";
	public static final String REGISTRATION_COMPLETE = "registrationComplete";

	//UPDATE VERSION
	public final static String PREF_UPDATE_VERSION = "update_version";
	public final static String PREF_UPDATE_URL = "update_url";

	//LOGIN
	public final static String PREF_AUTO_LOGIN = "auto_login";
	public final static String PREF_LOGIN_ID = "loginId";
	public final static String PREF_LOGIN_PASSWD = "loginPw";
	public final static String PREF_OAUTH_TOKEN = "oauth_access_token";

	public final static String PREF_COOP_TYPE ="coop";

	public final static String PREF_PROFILE = "profile";

	public final static String PREF_OAUTH_TOKEN_I2CONFERENCE = "i2conf_oauth_access_token";

	public final static String PREF_USR_JSON_INFO = "pref_usr_json_info";
	public final static String PREF_USR_ID = "pref_usr_id";
	public final static String PREF_USR_NM = "pref_usr_nm";
	public final static String PREF_USR_PHOTO = "pref_usr_photo";
	public final static String PREF_USR_TP_CD = "pref_usr_tp_cd";
	public final static String PREF_USR_EMAIL = "pref_usr_email";
	public final static String PREF_USR_TEL = "pref_usr_tel";
	public final static String PREF_USR_PHONE = "pref_usr_phone";
	public final static String PREF_SELF_INTRO = "pref_self_intro";
	public final static String PREF_USR_TYPE = "pref_usr_type"; //SC 고객상담앱에서만 처리 하는 값
	public final static String PREF_ENC_USR_ID = "enc_user_id"; //SC 고객상담앱에서만 처리 하는 값


	public final static String PREF_USR_FAX = "pref_usr_fax";
	public final static String PREF_DEPT_NM = "pref_dept_nm";
	public final static String PREF_DIV_SEQ = "pref_div_seq";
	public final static String PREF_PHOTO_IMG_MIN_PATH = "pref_photo_min_path";
	public final static String PREF_GRP_IMG_S = "pref_grp_img_s";

	public final static String PREF_GCM_CHANGED = "pref_gcm_changed";
	public final static String PREF_GCM_TOKEN = "gcm_device_token";
	public final static String PREF_GCM_REGISTED = "pref_gcm_registed";
	public final static String PREF_GROUP_INFO_JSON = "pref_group_info";
	public final static String PREF_USR_LOGOUT = "pref_usr_logout";

	public final static String PREF_NOTI_CHAT = "pref_noti_chat";
	public final static String PREF_NOTI_TYPE = "pref_noti_type";
	public final static String PREF_POS_NM = "pref_pos_nm";
	public final static String PREF_POTION_CODE = "pref_potion_code";
	public final static String PREF_NOW_DATE = "pref_now_date";
	public final static String PREF_PROFILE_IMAGE_YN = "pref_profile_image_yn";
	public final static String PREF_ALARM_SETTING_JSON = "pref_alarm_setting_json";
	public final static String PREF_ALARM_SETTING_UPDATE = "pref_alarm_setting_update";
	public final static String PREF_CHANGE_PW_DATE = "pref_change_pw_date";
	public final static String PREF_LAST_UPDATE_DATE = "pref_last_update_date";
	public final static String PREF_CURRENT_VERSION = "pref_current_version";
	public final static String PREF_DOWNLOAD_ID = "pref_download_id";
	public static final String ACTION_MESSAGE = "kr.co.i2max.action.pushservice.message";
	public static final String ACTION_RESTART_SERVICE = "kr.co.i2max.action.pushservice.restart";
	
	private static PreferenceUtil sInstance;
    private final SharedPreferences mPref;
 
    private PreferenceUtil(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


	public static void initializeInstance(Context context) {
		if(sInstance == null) {
			synchronized(PreferenceUtil.class) {
				if(sInstance == null) {
					sInstance = new PreferenceUtil(context);
				}
			}
		}
	}
 
    public static synchronized PreferenceUtil getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferenceUtil.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

	public static synchronized boolean isNull() {
		boolean result = false;
		if (sInstance == null) result = true;

		return result;
	}
	
	public void setString(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}
	
	public String getString(String key) {
		return mPref.getString(key, "");
	}
	
	public void remove(String key) {
		mPref.edit().remove(key).commit();
	}
	
	public void clear() {
		mPref.edit().clear().commit();
	}
}
