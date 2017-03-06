package com.i2max.i2smartwork.constant;

import android.os.Environment;

import com.i2max.i2smartwork.utils.FileUtil;

//import FileUtil;

/**
 * Created by shlee on 15. 7. 14..
 */
public class AppConstant {

    /* 빌드 정보 */
    /* 베포일자 */
    public static final String DISTRIBUTION_DATE = "2015.11.15";
    /* 구글 플레이 마켓에서 앱 다운로드*/
    public static final String I2CONNECT_NATIVE_PACKAGE_NAME = "com.i2max.i2smartwork";

    /* 빌드 테스트 옵션 배포시: 모두 true */
    /* security mode */
    public static final boolean SECURITY_MODE_ENABLED = false;
    /* 백신앱 기동 */
    public static final boolean VACCINE_ENABLED = false;
    /*보안옵션 : 보안키패드 암호화 적용 secure_yn=Y */
    public static final boolean MTK_CIPHER_ENABLED = false;
    /*보안옵션 : 보안키패드 암호화 SCURITY KEY 16Byte */
    public static final byte[] MTK_SECRET = "NHPCI_I2TALK1402".getBytes();
    /*보안옵션 : 구간 암호화 적용 여부 */
    public static final boolean KSW_CIPHER_ENABLED = false;
    /* MDM 기동 */
    public static final boolean MDM_ENABLED = false;
    /* 구글 플레이 마켓에서 앱 다운로드*/
    public static final boolean DOWNLOAD_APP_VIA_GOOGLEPLAY = true;
    /* 로드 : 업데이트 다운로드 기동 */
    public static final boolean DOWNLOAD_UPDATE_ENABLED = false;
    /* 업데이트 TYPE */
    public static final String UPDATE_TYPE = "connect";
    /*보안옵션 : VPN 로그인 ID 체크 사용  */
    public static final boolean VPN_CHECK_ID_ENABLED = false;
    //GCM옵션 :  Google Cloud Message 서비스 사용여부
    public static final boolean GCM_ENABLED = true;
    //망분리옵션 : 파일 업로드후 망연계 서버에 정보 전송여부
    public static final boolean SEPARATED_FILE_NETWORK_ENABLE = true;

    //테스트옵션 :  기능메뉴를 서버에서 받을지 아니면 로컬에서 읽을지
    public static final boolean MENU_LOCAL_JSON_ENABLED = true;
    //테스트옵션 :  APK 테스트 여부 : hybrid 컨텐츠 asset에서 로딩 : 릴리즈버젼 다운로드하여 ex_storage에서 hybrid 컨텐츠로드
    public static final boolean ASSET_BUILD_ENABLED = false;

    /****
     * Oauth2.0 i2Connect android phone
     ****/
//    public static final String OAUTH_CLIENT_ID = "DE970DFE51";
//    public static final String OAUTH_CLIENT_SECRET = "AC10E3D004C051143C7A";
    /**
     * i2conference연동 프로토콜 미사용으로 변경 주석처리
     * i2conference Oauth Alice API OAuth 2.0 spec 안내
     * client_id : 55FA268E93(고정값) client_passwd : C54441F95F98CFC7ED8E(고정값) grant_type : client_credentials(고정값) username : user의 login id(변동값) password
     * : user의 login password(변동값)
     */
//    public static final String I2CFRC_OAUTH_CLIENT_ID = "8995A8637E";
//    public static final String I2CFRC_OAUTH_CLIENT_SECRET = "EEDD7EAD0ED316A6786E";

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    public static final String TWITTER_KEY = "v87rxLesjOJHakGa0RjvIv7Lq";
    public static final String TWITTER_SECRET = "9QCLKy2w2uUcWHvv8jVcuNehIAtalQGLHfrk9OP50JXXxeqZeX";

    /****
     * ROOT PATH
     ****/
    public static final String ROOT_PATH = "/.i2root";
    public static final String WV_ASSET_ROOT = "file:///android_asset";
    public static final String WV_EXSTORAGE_ROOT = "file://" + Environment.getExternalStorageDirectory() + ROOT_PATH;
    /****
     * Download PATH
     ****/
    public static final String WEB_CONTENTS_PATH = "/mobile_phone";
    //    public static final String ROOT_DOWNLOAD_ZIP_PATH = FileUtil.getRootPathFromExternalSD(ROOT_PATH + WEB_CONTENTS_PATH);
//    public static final String ROOT_DOWNLOAD_FILE_PATH = FileUtil.getRootPathFromExternalSD(ROOT_PATH + "/download");
    public static final String ROOT_DOWNLOAD_IMAGE_PATH = FileUtil.getRootPathFromExternalSD(ROOT_PATH + "/images");


}
