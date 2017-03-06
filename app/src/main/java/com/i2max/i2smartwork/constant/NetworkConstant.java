package com.i2max.i2smartwork.constant;

import com.i2max.i2smartwork.BuildConfig;

/**
 * Created by shlee on 15. 7. 14..
 */
public class NetworkConstant {
    /** Release 서버 정보 */
    public static final String SERVER_HOST = BuildConfig.SERVER_HOST;
//        "http://cs.i2smartwork.co.kr"; // 고객상담 expert bank & U+ 서버
//        "http://hw.i2smartwork.co.kr"; // 게임물 재택
//        "http://sb.i2smartwork.co.kr"; // 한우리티엔 서버(중소기업)
//        "http://sc.i2smartwork.co.kr"; // 코베아 서버
//        "http://sw.i2smartwork.co.kr"; // 한국장애인복지관협회 서버
    /** DEV 서버 정보 */
//    "http://i2maxi2.iptime.org:8090"; // 허차장님PC 테스트
//    "http://192.168.0.32:8080"; // 이균차장님 테스트 32
//    "http://192.168.0.232:80"; // 강남구차장님 테스트 232

    // HTTP TIME OUT SETTING
    public static final int HTTP_REQUEST_TIME_OUT = 10000;  // 10 sec
    // bufferstream 버퍼 사이즈
    public static final int BUFFER_SIZE = 1024 * 8;
    // upload boundary 값
    public static final String UPLOAD_BOUNDARY = BuildConfig.OAUTH_CLIENT_SECRET;
}
