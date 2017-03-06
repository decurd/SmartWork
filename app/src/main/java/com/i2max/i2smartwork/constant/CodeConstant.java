package com.i2max.i2smartwork.constant;

/**
 * Created by shlee on 15. 8. 28..
 */
public class CodeConstant {

    //MainActivity GCM 메세지로 기동
    public static final String LAUNCH_GCM_MSG = "launch_gcm_msg";
    //MainActivity 로그아웃으로 메인액티비티 들어옴
    public static final String LOGOUT_EXIT = "EXIT";

    //MENU KEYCODE
    public static final String MENU_NO = "menu_no";
    public static final String MENU_TYPE = "program_file_nm";
    public static final String CHILD_YN = "child_yn";
    public static final String MENU_URL = "menu_url";

    //MENU NO(UNIQ) 공통값만 정의 소통, 일정, 작업, 회의, 메모, 과제등
    public static final int LOGOUT          = 9999999;
    public static final int NOTI_MAIN       = - 9999; //0;
    public static final String MENU_DIR       = "dir";
//    public static final String SNS_MAIN        = "dir";
//    public static final String SNS_FEED        = "dir";
    public static final String SNS_FOLLOW      = "I2cSnsHomeByFollow";
    public static final String SNS_ME          = "I2cSnsHomeByMe";
    public static final String SNS_OBJECT      = "I2cSnsHomeByObject";
    public static final String SNS_BOOKMARK    = "I2cSnsHomeByBookmark";
    public static final String SNS_OPEN        = "I2cSnsHomeByOpen";
    public static final String TASK_MAIN       = "I2cSnsTaskList";
    public static final String PLAN_MAIN       = "I2cSnsPlanList";
//    public static final String CFRC_MAIN       = "dir";
    public static final String CFRC_LIST       = "I2cSnsConferenceList";
    public static final String CFRC_ONLINE     = "I2cSnsConferenceOnlineList";
    public static final String CFRC_FILE       = "I2cSnsConferenceAttachList";
    public static final String CFRC_SCHEDULE   = "I2cSnsConferencePlanList";
    public static final String MEMO_MAIN       = "I2cSnsMemoList";
    public static final String WORK_MAIN       = "I2cSnsWorkList";
    public static final String I2LIVECHAT      = "I2Livechat";

    //MENU TYPE (JSON)
//    public static final String NOTI = "noti";
//    public static final String SNS = "sns";
//    public static final String SNS_ALL_LIST = "sns_all";            //sns submenu
//    public static final String SNS_FOLLOW_LIST = "sns_follow";      //sns submenu
//    public static final String SNS_ME_LIST = "sns_me";              //sns submenu
//    public static final String SNS_BUSINESS_LIST = "sns_business";  //sns submenu
//    public static final String SNS_BOOKMARK_LIST = "sns_bookmark";  //sns submenu
//    public static final String SNS_OPEN_LIST = "sns_open";          //sns submenu
//    public static final String SCHEDULE = "schedule";
//    public static final String CONFERENCE = "cfrc";
//    public static final String CONFERENCE_LIST = "cfrc_list";           //cfrc submenu
//    public static final String CONFERENCE_ONLINE_LIST = "cfrc_online";  //cfrc submenu
//    public static final String CONFERENCE_FILE_LIST = "cfrc_file";      //cfrc submenu
//    public static final String CONFERENCE_PLAN_LIST = "cfrc_plan";      //cfrc submenu
//    public static final String TASK_LIST = "task"; //작업
//    public static final String MEMO_LIST = "memo"; //메모보고
//    public static final String WORK_LIST = "work"; //과제관리

    public static final int ERROR_TYPE_EXCEPTION = -1;// 서버에서 보내는 사용자 에러.validation check
    // status code
    public static final String TAG_STATUS_CODE = "statusCode";
    public static final String TAG_STATUS_MESSAGE = "statusMessage";
    public static final String TAG_STATUS_INFO = "statusInfo";
    public static final String TAG_STATUS_DATE = "updateDate";
    public static final String TAG_STATUS_SUCESS = "0";
    public static final String TAG_STATUS_COMPLETE = "1";
    public static final String TAG_STATUS_ERROR = "-1";

    // intent defaul params TODO정리 필요 UP개념  tar개념 관계개념 정리 필요 규칙이 안느껴서 복잡 UP > TAR
    public static final String TITLE = "title";
    public static final String REFRESH = "refresh"; //리프레쉬 여부
    public static final String TAB_POS = "tab_pos"; //탭 위치
    public static final String TAR_OBJ_TP = "tar_obj_tp_cd";
    public static final String TAR_OBJ_ID = "tar_obj_id";
    public static final String TAR_OBJ_TTL = "tar_obj_ttl";
    public static final String TAR_SELECT_URL = "tar_select_url"; //webview 기능 sns연동 처리
    public static final String TAR_CRT_USR_ID = "tar_crt_usr_id";
    public static final String CUR_OBJ_TP = "cur_obj_tp";
    public static final String CUR_OBJ_ID = "cur_obj_id";
    public static final String CRT_USR_ID = "crt_usr_id";
    public static final String POST_ID = "post_id";
    public static final String USER_ID = "usr_id";
    public static final String GROUP_ID = "grp_id";
    public static final String CFRC_ID = "cfrc_id";
    public static final String TASK_ID = "task_id";
    public static final String MEMO_ID = "memo_id";
    public static final String WORK_ID = "work_id";
    public static final String MILE_ID = "mile_id";

    //member add
    public static final String USR_ID = "usr_id";
    public static final String USR_NM = "usr_nm";
    public static final String USR_IMG = "usr_img";

    //REQUEST
    public static final String REQUEST_CODE       = "requestCode";
    public static final int REQUEST_EDIT            = 1001;
    public static final int REQUEST_SNS_CREAT       = 1002;
    public static final int REQUEST_TASK_CREAT      = 1003;
    public static final int REQUEST_MILESTONE_CREAT = 1004;


    //TAR_OBJECT_ID
    public static final String TYPE_POST = "POST";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_GROUP = "GROUP";
    public static final String TYPE_TASK = "TASK";
    public static final String TYPE_CFRC = "CFRC";
    public static final String TYPE_MEMO = "MEMO";
    public static final String TYPE_WORK = "WORK"; //미지원
    public static final String TYPE_MILE = "MILE"; //미지원
    public static final String TYPE_WEB = "WEB"; //사이트별 기능

    //FAB ADD BUTTON CONFIGURATION
    public static final long DRAWER_CLOSE_DELAY_MS = 300;
    public static final int SCROLL_OFFSET = 4;

    //파일 업로드 구분값
    public static final String ATTACH_ST = "attach_mode";
    public static final String ATTACH_NEW = "NEW"; // 신규 업로드파일 TODO "Y"로 수정
    public static final String ATTACHED = ""; //서버파일 TODO "N"로 수정
    public static final String DEL_ATTACHED = "DELETE";

    //MODE_SEARCH_USER
    public static final int MODE_SEARCH_ALL = 0;
    public static final int MODE_SEARCH_PERSON = 10;
    public static final int MODE_SEARCH_GROUP = 11;
    public static final int MODE_LINK_ADD = 1;
    public static final int MODE_FOLLOWING = 2;
    public static final int MODE_FOLLOWER = 3;
    public static final int MODE_CFRC_MEMBER_ADD = 4;
    public static final int MODE_TASK_MEMBER_ADD = 5;
    public static final int MODE_MEMO_MEMBER_ADD = 6;
    public static final int MODE_WORK_MEMBER_ADD = 7;
    //MODE_SEARCH_GROUP
    public static final int MODE_USER_GROUP = 0;
    public static final int MODE_ALL_GROUP = 1;
}
