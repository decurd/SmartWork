package com.i2max.i2smartwork.i2api;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.i2max.i2smartwork.BuildConfig;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.constant.NetworkConstant;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

/**
 * Created by berserk1147 on 2015. 7. 12..
 */
public class I2UrlHelper {

    public static Picasso buildPicassoAddTokenHeader(Context context) {
        OkHttpClient picassoClient = new OkHttpClient();
        picassoClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN))
                        .build();
                //DUDA NUNCA PASA POR AQUI;
                Log.i("Peticion", newRequest.toString());
                Log.i("Cabecera", newRequest.headers().toString());
                return chain.proceed(newRequest);
            }
        });

        return new Picasso.Builder(context).downloader(new OkHttpDownloader(picassoClient)).build();
    }

    public static boolean isAirplaneModeOn(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
    }

    public static String getTokenAuthorization() {
        return "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN);
    }


    public static Map<String, String> getTokenHeader() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Authorization", "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN));
        header.put("User-Agent", "andorid mobile");

        return header;
    }

    public static Request getTokenRequest(String strURL) {
        Request request = new Request.Builder()
                .url(strURL)
                .header("Authorization", "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN))
                .addHeader("User-Agent", "andorid mobile")
                .build();

        return request;
    }

    public static Request getTokenRequest(String strURL, RequestBody formBody) {
        Request request = new Request.Builder()
                .url(strURL)
                .header("Authorization", "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN))
                .addHeader("User-Agent", "andorid mobile")
                .post(formBody)
                .build();

        return request;
    }

    public static Request getUploadFileRequest(String strURL, RequestBody formBody) {
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(strURL)
                    .header("Authorization", "Bearer " + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN))
                    .addHeader("User-Agent", "andorid mobile")
                    .addHeader("Content-Length", String.valueOf(formBody.contentLength()))
                    .post(formBody)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return request;
    }

    public static class GCM {
        private static final String SaveTokenToServer = "/sns/saveSnsUserClient.json";
        private static final String SaveTokenToUrl = "http://m.expertbank.kr:16383/?";

        public static Request getSaveGcmToken(String accessToken, String clientId, String gcmToken, String useYn) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SaveTokenToServer);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("access_token", TextUtils.isEmpty(accessToken) ? "" : accessToken)
                    .add("client_id", TextUtils.isEmpty(clientId) ? "" : clientId)
                    .add("push_token", TextUtils.isEmpty(gcmToken) ? "" : gcmToken)
                    .add("use_yn", TextUtils.isEmpty(useYn) ? "" : useYn)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        } //미사용 2016.12.21

        public static Request getSendableGcmToken(String userid, String gcmToken) { // 2016.12.21 토큰 발송용 Request 생성
            StringBuilder builder = new StringBuilder();
            builder.append(SaveTokenToUrl);
            builder.append("login_id=");
            builder.append(userid);
            builder.append("&rid=");
            builder.append(gcmToken);
            Log.d("TEST", builder.toString());

            RequestBody formBody = new FormEncodingBuilder()
                    .add("get", "")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

    }

    public static class Update {
        private static final String VERSION_CHECK = "/mobile/versionCheck.json";
        private static final String UPDATE_DOWNLOAD = "/mobile/download.json";
        private static final String UPDATE_MENU = "/i2cowork/home/listMobileMenu.json";

        public static Request getVersionCheck() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VERSION_CHECK);

            Request request = new Request.Builder()
                    .url(builder.toString())
                    .build();

            return request;
        }

        public static Request getMenu() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_MENU);

            RequestBody formBody = new FormEncodingBuilder()
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    public static class Login {
        private static final String OAUTH_LOGIN_NEW = "/i2cowork/auth/login/i2cLoginAction.do";

        public static Request getOAuthLogin(String loginMode, String loginId, String loginPw) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(OAUTH_LOGIN_NEW);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("login_mode", loginMode) //"APP"
                    .add("username", loginId)
                    .add("password", loginPw)
                    .add("client_id", BuildConfig.OAUTH_CLIENT_ID)
                    .add("client_secret", BuildConfig.OAUTH_CLIENT_SECRET)
                    .add("grant_type", "password")
                    .build();

            Request request = new Request.Builder()
                    .url(builder.toString())
                    .header("User-Agent", "andorid mobile")
                    .post(formBody)
                    .build();

            return request;
        }
    }

    public static class LogInsert {
        private static final String LOG_NEW = "/i2cowork/log/stats/logStatsMobileInsert.json";

        public static Request getLogInfo(String code, String usr_id, String usr_tp_cd, String grp_id, String remark) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LOG_NEW);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("code", code)
                    .add("usr_id", usr_id)
                    .add("usr_tp_cd", usr_tp_cd)
                    .add("grp_id", grp_id)
                    .add("remark", remark)
                    .build();

            Request request = new Request.Builder()
                    .url(builder.toString())
                    .header("User-Agent", "andorid mobile")
                    .post(formBody)
                    .build();

            return request;
        }
    }

    public static class SNS {
        private static final String SNS_PATH = "";
        private static final String SLELECT_SNS_USER_BY_PROFILE = "/i2cowork/sns/user/selectSnsUserByProfile.json";
        private static final String LIST_SNS_POST               = "/i2cowork/sns/post/listSnsPost.json";
        private static final String SELECT_SNS_POST             = "/i2cowork/sns/post/selectSnsPost.json";
        private static final String LIST_SNS_LIKE_BY_USERS      = "/i2cowork/sns/post/listSnsPostByLikeUsers.json";
        private static final String TOGGLE_SNS_LIKE             = "/i2cowork/sns/post/toggleSnsPostByLike.json";
        private static final String VIEW_SNS_POST_REPLY         = "/i2cowork/sns/selectSnsPostReply.json";
//        private static final String VIEW_LINK_WEBSITE = "/sns/viewLinkWebSite.json";
        private static final String SAVE_SNS_POST               = "/i2cowork/sns/post/insertSnsPost.json";
        private static final String DELETE_SNS_POST             = "/i2cowork/sns/post/deleteSnsPost.json";
//        private static final String SAVE_SNS_SURVEY = "/sns/saveSnsSurvey.json";
        private static final String LIST_SNS_SURVEY_VOTE_RESULT = "/i2cowork/sns/post/listSnsPostBySurveyVoteResult.json";
        private static final String SAVE_SNS_SURVEY_VOTE        = "/i2cowork/sns/post/insertSnsPostBySurveyVote.json";
        private static final String LIST_SNS_FOLLOWING           = "/i2cowork/sns/post/listSnsFollowing.json";
        private static final String LIST_SNS_FOLLOWER            = "/i2cowork/sns/post/listSnsFollower.json";
        private static final String LIST_USER_GROUP             = "/i2cowork/sns/group/listSnsGroupByMyGroup.json";
        private static final String LIST_SNS_GROUP              = "/i2cowork/sns/group/listSnsGroup.json";
        private static final String LIST_GROUP_MEMBER           = "/i2cowork/sns/group/listSnsGroupByUser.json";
        private static final String LIST_JOIN_APPLY             = "/i2cowork/sns/group/insertSnsGroupByUser.json";
        private static final String TOGGLE_SNS_FOLLOW           = "/i2cowork/sns/post/toggleSnsFollow.json";
        private static final String VIEW_SNS_GROUP              = "/i2cowork/sns/group/selectSnsGroupByProfile.json";
        private static final String LIST_SNS_USER               = "/i2cowork/sns/user/listSnsUser.json";
        private static final String LIST_SNS_FILE_ATTACH        = "/i2cowork/sns/file/listSnsFileAttach.json";
        private static final String LIST_USER_THUMBNAIL         = "/i2cowork/sns/post/listUserThumbnail.json";
        private static final String UPDATE_SNS_USER_PROFIE      = "/i2cowork/sns/user/updateSnsUser.json";
        private static final String UPDATE_SNS_GROUP            = "/i2cowork/sns/group/updateSnsGroup.json";
        private static final String SAVE_SNS_GROUP              = "/i2cowork/sns/group/insertSnsGroup.json";
        private static final String UPDATE_SNS_GROUP_IMAGE      = "/i2cowork/sns/group/updateSnsGroup.json";
        private static final String JOIN_APPLY = "/sns/joinApply.json";
        private static final String JOIN_APPROVAL               = "/i2cowork/sns/group/insertSnsGroupByUser.json";
        private static final String UPDATE_SNS_GROUP_ADMIN = "/sns/updateSnsGroupAdmin.json";
        private static final String LEAVE_GROUP_MEEBER  = "/sns/leaveGroupMember.json";

//        public static Request getViewSNSUser(String userID) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(NetworkConstant.SERVER_HOST);
//            builder.append(VIEW_SNS_USER);
//
//            RequestBody formBody = new FormEncodingBuilder()
//                    .add("usr_id", userID)
//                    .build();
//
//            return getTokenRequest(builder.toString(), formBody);
//        }

//        public static Request getViewSNSUserByInfo(String userID) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(NetworkConstant.SERVER_HOST);
//            builder.append(VIEW_SNS_USER_BY_INFO);
//
//            RequestBody formBody = new FormEncodingBuilder()
//                    .add("usr_id", userID)
//                    .build();
//
//            return getTokenRequest(builder.toString(), formBody);
//        }

        public static Request getViewSNSUserProfile(String userID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SLELECT_SNS_USER_BY_PROFILE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", userID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        /**
         * SNS리스트 조회
         * API하나를 여러개로 쪼개놓아서 통합처리 함
         * @param tarObjTp
         * @param tarObjId
         * @param postId
         * @param postMode
         * @param page
         * @param serachText
         * @return
         */
        public static Request getListSNSPost(String tarObjTp, String tarObjId,
                                             String postId, String postMode, String page, String serachText) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_POST);

            RequestBody formBody = new FormEncodingBuilder()
//                    .add("tar_usr_id", TextUtils.isEmpty(tarUsrId)?"":tarUsrId) //tar_obj_id 통합
//                    .add("tar_grp_id", TextUtils.isEmpty(tarGrpId)?"":tarGrpId) //tar_obj_id 통합
                    .add("tar_obj_tp_cd", TextUtils.isEmpty(tarObjTp) ? "" : tarObjTp)
                    .add("tar_obj_id", TextUtils.isEmpty(tarObjId) ? "" : tarObjId)
                    .add("post_id", TextUtils.isEmpty(postId)?"":postId)
                    .add("post_mode", TextUtils.isEmpty(postMode)?"":postMode)
                    .add("sort_type", "MOD") //수정기준 MOD, 작성기준 CRT
                    .add("paging_yn", "Y")
                    .add("page", TextUtils.isEmpty(page) ? "" : page)
                    .add("limit", "10")
//                    .add("CALL_MODE", "MYHOME")
//                    .add("post_tp_cd", "FEED")
                    .add("post_search_text", TextUtils.isEmpty(serachText) ? "" : serachText)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * post_id 값으로 sns post단건 검색
         * 상세화면 용
         * @param postId
         * @return
         */
        public static Request getSelectSNSPost(String postId) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SELECT_SNS_POST);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", TextUtils.isEmpty(postId) ? "" : postId)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

//        /**
//         * TODO 서버 미적용
//         * @param postId
//         * @return
//         */
//        public static Request getViewSNSPost(String postId) {
//            return getListSNSPost("", "", postId, "", "", "");
//        }

        /**
         * SNS포스트 오브젝트 정보를 통한
         * @param tarObjTp
         * @param tarObjId
         * @param postMode
         * @param page
         * @param serachText
         * @return
         */
        public static Request getListSNSByObjectId(String tarObjTp, String tarObjId,
                                                   String postMode, String page, String serachText) {
            return getListSNSPost(tarObjTp, tarObjId, "", postMode, page, serachText);
        }

        public static Request saveSnsSurvey(FormEncodingBuilder snsPostFromBuilder, String surveyUsrOpen, List<String> surveyNmList ) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_POST);

            RequestBody formBody;
            snsPostFromBuilder
                    .add("post_tp_cd˙", "POLL")
                    .add("survey_usr_open_yn", surveyUsrOpen);

            for (int i=0; i<surveyNmList.size(); i++) {
                snsPostFromBuilder.add("survey_itm_nm", surveyNmList.get(i));
            }

            formBody = snsPostFromBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getSnsSurveyVoteResult(String postID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_SURVEY_VOTE_RESULT);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request saveSnsSurveyVote(String postID, String surveyItemID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_SURVEY_VOTE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .add("survey_itm_id", surveyItemID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsLikeByUsers(String postID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_LIKE_BY_USERS);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getToggleSnsLike(String postID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(TOGGLE_SNS_LIKE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsPostReply(String postID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_POST_REPLY);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request saveSnsPostReply(String upPostID, String tarObjTp, String tarObjID, String cntn, FormEncodingBuilder userLinkBuilder) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_POST);

            Log.e("saveSnsPostReply", "upPostID = " + upPostID + " /tarObjTp = " + tarObjTp + " /tarObjID = " + tarObjID + " /cntn = " + cntn);

            RequestBody formBody;
            if (userLinkBuilder!=null) {
                formBody = userLinkBuilder
                        .add("dvc_tp_cd", "MOBILE")
                        .add("up_post_id", upPostID)
                        .add("tar_obj_tp_cd", tarObjTp)
                        .add("tar_obj_id", tarObjID)
                        .add("post_tp_cd", "FEED")
                        .add("post_mode", "FOLLOW")
                        .add("ttl", "")
                        .add("cntn", cntn)
                        .build();
            } else {
                formBody = new FormEncodingBuilder()
                        .add("dvc_tp_cd", "MOBILE")
                        .add("up_post_id", upPostID)
                        .add("tar_obj_tp_cd", tarObjTp)
                        .add("tar_obj_id", tarObjID)
                        .add("post_tp_cd", "FEED")
                        .add("post_mode", "FOLLOW")
                        .add("ttl", "")
                        .add("cntn", cntn)
                        .build();
            }

            return getTokenRequest(builder.toString(), formBody);
        }

        public static FormEncodingBuilder getSnsPostFormBuilder(String postTpTd, String tarObjTp, String tarObjId, String tarObjNm, String cntn) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();
            //tar_obj_tp_cd = [USER:사용자, 그룹:GROUP, 회의:CFRC, 메모보고:MEMO, 작업:TASK
            formBuilder
                    .add("dvc_tp_cd", "MOBILE")
//                    .add("tar_usr_id", tarUsrID) //삭제
//                    .add("tar_grp_id", tarGrpID) //삭제
                    .add("tar_obj_tp_cd", tarObjTp)
                    .add("tar_obj_id", tarObjId)
                    .add("tar_obj_nm", tarObjNm)  // 웹에서 있음.
                    .add("post_tp_cd", postTpTd)
                    .add("post_mode", "FOLLOW")
//                    .add("sort_mode", "CRT") // 웹에서는 없음.
                    .add("cntn", cntn)
                    .build();

            return formBuilder;
        }

        public static Request saveSnsPostNoAdd(FormEncodingBuilder snsPostFormBuilder) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_POST);

            RequestBody formBody = snsPostFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request saveSnsFilesPost(FormEncodingBuilder snsPostFormBuilder, Map<String, Object> fileInfo) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_POST);

            RequestBody formBody;

            for (int i=0; i<((List<String>)fileInfo.get("attach_nm_list")).size(); i++) {
                snsPostFormBuilder.add("tar_file_tp_cd", "FEED");
                snsPostFormBuilder.add("file_nm", ((List<String>)fileInfo.get("attach_nm_list")).get(i));
                snsPostFormBuilder.add("file_tp_cd", ((List<String>)fileInfo.get("attach_tp_cd_list")).get(i));
                snsPostFormBuilder.add("phscl_file_nm", ((List<String>)fileInfo.get("phscl_file_nm_list")).get(i));
                snsPostFormBuilder.add("file_ext", ((List<String>) fileInfo.get("file_ext_list")).get(i));
                snsPostFormBuilder.add("file_id", ((List<String>) fileInfo.get("file_id_list")).get(i));
                snsPostFormBuilder.add("file_size", ((List<String>) fileInfo.get("file_size_list")).get(i));
                snsPostFormBuilder.add("file_path", ((List<String>) fileInfo.get("file_path_list")).get(i));
                snsPostFormBuilder.add("attach_mode", "NEW");
            }

            formBody = snsPostFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request saveSnsPostLinks(FormEncodingBuilder snsPostFormBuilder, String linkNm, String userURL) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_POST);

            RequestBody formBody;

//            snsPostFormBuilder.add("post_tp_cd", "LINK");
            snsPostFormBuilder.add("link_ttl", linkNm);
            snsPostFormBuilder.add("link_url", userURL);

            formBody = snsPostFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

//        public static Request getViewLinkWebsite(String userLink) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(NetworkConstant.SERVER_HOST);
//            builder.append(VIEW_LINK_WEBSITE);
//
//            RequestBody formBody = new FormEncodingBuilder()
//                    .add("userLink", userLink)
//                    .build();
//
//            return getTokenRequest(builder.toString(), formBody);
//        }

        public static Request deleteSnsPost(String postID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_POST);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("post_id", postID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static FormEncodingBuilder getLinkUserFormBuilder(List<String> userIDList, List<String> userNmList) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();

            for (int i=0; i<userIDList.size(); i++) {
                formBuilder.add("post_tar_usr_id", userIDList.get(i));
                formBuilder.add("post_tar_usr_check", "[" + userNmList.get(i) + "]");
                formBuilder.add("post_tar_usr_map_idx", String.format("%d", i + 1));
            }

            return formBuilder;
        }


        public static FormEncodingBuilder getLinkUserFormBuilder(FormEncodingBuilder formBuilder,
                                                                 List<String> userIDList, List<String> userNmList) {

            //FormEncodingBuilder formBuilder = new FormEncodingBuilder();

            for (int i=0; i<userIDList.size(); i++) {
                formBuilder.add("post_tar_usr_id", userIDList.get(i));
                formBuilder.add("post_tar_usr_check", "[" + userNmList.get(i) + "]");
                formBuilder.add("post_tar_usr_map_idx", String.format("%d",i+1));
            }

            return formBuilder;
        }

        public static Request getListSNSFollowing(String userID, String page) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_FOLLOWING);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", userID)
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSNSFollower(String userID, String page) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_FOLLOWER);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", userID)
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "30")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListUserGroup(String tarUsrID, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_USER_GROUP);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", tarUsrID)
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "30")
                    .add("searchKeyword", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsGroup(String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_GROUP);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "30")
                    .add("searchKeyword", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListGroupMember(String grpID, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_GROUP_MEMBER);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("searchKeyword", searchKeyword)
                    .add("search_type", "JOIN")
                    .add("grp_id", grpID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        public static Request getListJoinApply(String grpID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_JOIN_APPLY);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_id", grpID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request toggleSnsFollow(String tarUsrID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(TOGGLE_SNS_FOLLOW);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", tarUsrID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsGroup(String grpID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_GROUP);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_grp_id", grpID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsUser(String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_USER);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("searchKeyword", searchKeyword)
                    .add("use_stop_yn", "N")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request joinApply(String grpID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(JOIN_APPLY);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_id", grpID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request joinApproval(String grpID, String usrID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(JOIN_APPROVAL);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_id", grpID)
                    .add("usr_id", usrID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request updateSnsGroupAdmin(String grpID, String usrID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_SNS_GROUP_ADMIN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_id", grpID)
                    .add("tar_usr_id", usrID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request leaveGroupMember(String grpID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LEAVE_GROUP_MEEBER);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_id", grpID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsAttachByUser(String tarUsrID, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_FILE_ATTACH);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("crt_usr_id", tarUsrID)
                    .add("search_mode", "MY")
                    .add("search_text", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsAttachByGroup(String tarGrpID, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_FILE_ATTACH);
            
            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("grp_id", tarGrpID)
                    .add("search_text", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * API 미개발 이번버젼에서 빠짐
         * @param tarUsrID
         * @param page
         * @param searchKeyword
         * @return
         */
        public static Request getListUserThumbnail(String tarUsrID, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_USER_THUMBNAIL);

            //[sns/listUserThumbnail.json], param : [tar_obj_tp=USER&tar_usr_id=2ec9ff78fea87ac270a708aa4e94338ab0816091&post_mode=&sort_type=MOD&page=1&post_search_text=] (I2talkCore.js, line 28)

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("tar_usr_id", tarUsrID)
                    .add("tar_obj_tp_cd", "USER")
                    .add("sort_type", "MOD")
                    .add("post_search_text", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 유저 정보 수정
         * @param usrID
         * @param loginPasswd
         * @param newLoginPasswd
         * @param newLoginPasswd2
         * @param selfIntro
         * @return
         */
        public static Request updateSnsUserProfile(String usrID, String loginPasswd, String newLoginPasswd, String newLoginPasswd2, String selfIntro) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_SNS_USER_PROFIE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("usr_id", usrID)
                    .add("curnt_passwd", loginPasswd)
                    .add("change_passwd", newLoginPasswd)
                    .add("change_passwd_confirm", newLoginPasswd2)
                    .add("self_intro", selfIntro)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 프로필 사진 업데이트
         * @param usrID
         * @param photoImg
         * @param x
         * @param y
         * @param w
         * @param h
         * @return
         */
        public static Request updateProfilePhoto(String usrID, String photoImg, String x, String y, String w, String h) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_SNS_USER_PROFIE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("update_type", "PHOTO")
                    .add("usr_id", usrID)
                    .add("photo_img", photoImg)
                    .add("x", x)
                    .add("y", y)
                    .add("w", w)
                    .add("h", h)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request updateSnsGroupImage(String grpID, String photoImg, String x, String y, String w, String h) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_SNS_GROUP_IMAGE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("update_type", "PHOTO")
                    .add("photo_img", photoImg)
                    .add("grp_id", grpID)
                    .add("x", x)
                    .add("y", y)
                    .add("w", w)
                    .add("h", h)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        public static Request saveSnsGroup(String grpNm, String grpIntro, String openYN, String imgNm, String x, String y, String w, String h) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_GROUP);
            if (w == null) {
                w = "";
            }

            if (h == null) {
                h = "";
            }

            RequestBody formBody = new FormEncodingBuilder()
                    .add("grp_nm", grpNm)
                    .add("grp_tp_cd", "GNR")
                    .add("grp_intro", grpIntro)
                    .add("open_yn", openYN)
                    .add("photo_img", imgNm)
                    .add("x", x)
                    .add("y", y)
                    .add("w", w)
                    .add("h", h)
                    .build();

//            "grp_nm=""그룹이름""
//            grp_tp_cd=""그룹유형코드:GNR""
//            grp_intro=""그룹 정보""
//            open_yn=""공개그룹 여부""
//            photo_img=""그룹 프로필 이미지:fileInfo.phscl_file_nm"""

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request updateSnsGroup(String grpID, String grpNm, String grpIntro, String openYN) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_SNS_GROUP);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("update_type", "INFO")
                    .add("grp_id", grpID)
                    .add("grp_nm", grpNm)
                    .add("grp_intro", grpIntro)
                    .add("open_yn", openYN)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    public static class Plan {
        private static final String LIST_SNS_PLAN   = "/i2cowork/sns/plan/listSnsPlanScr.json";
        private static final String VIEW_SNS_PLAN   = "/i2cowork/sns/plan/selectSnsPlan.json";
        private static final String SAVE_SNS_PLAN   = "/i2cowork/sns/plan/insertSnsPlan.json";
        private static final String DELETE_SNS_PLAN = "/i2cowork/sns/plan/deleteSnsPlan.json";

        public static Request getListSnsPlan(String tarObjTp, String tarOjbID, String termMonth) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_PLAN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("tar_obj_tp_cd", tarObjTp)
                    .add("tar_obj_id", tarOjbID)
                    .add("termMonth", termMonth)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsPlan(String planID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_PLAN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("plan_id", planID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request deleteSnsPlan(String planID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_PLAN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("plan_id", planID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request saveSnsPlan(String planID, String planTtl, String startDttm, String endDttm,
                                          String place, String planType, String planOpenYN, String planDtlCntn, String TarObjTpCd, String objTarID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_PLAN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("plan_ttl", planTtl)
                    .add("start_dttm", startDttm)
                    .add("end_dttm", endDttm)
                    .add("place", place)
                    .add("plan_tp", planType)
                    .add("plan_id", planID)
                    .add("plan_open_yn", planOpenYN)
                    .add("plan_dtl_cntn", planDtlCntn)
                    .add("obj_tar_id", objTarID)
                    .add("tar_obj_tp_cd", TarObjTpCd)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    public static class Push {
        private static final String LIST_SNS_MESSAGE = "/push/listSnsMessage.json";
        private static final String VIEW_UNREAD_MESSAGEBOX_COUNT = "/push/viewUnreadMessageBoxCount.json";
        private static final String LIST_SNS_UNREAD_MESSAGE = "/push/listUnreadMessage.json";
        private static final String LIST_SNS_UNREAD_MESSAGE_COUNT = "/push/listUnreadMessageCount.json";
        private static final String RECEIVE_CONFIRM = "/push/receiveConfirm.json";

        public static Request getListSnsMessage(String page, String reciveConfirm) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_MESSAGE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("biz_tp_cd", "")
                    .add("receive_confirm", reciveConfirm)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewUnreadMessageboxCount() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_UNREAD_MESSAGEBOX_COUNT);

            return getTokenRequest(builder.toString());
        }

        public static Request getListUnreadMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_UNREAD_MESSAGE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("paging_yn", "Y")
                    .add("page", "1")
                    .add("limit", "10")
                    .add("limit", "20")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListUnreadMessageCount() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_UNREAD_MESSAGE_COUNT);

            return getTokenRequest(builder.toString());
        }

        public static Request messageReceiveConfirm(String recvID) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(RECEIVE_CONFIRM);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("recv_id", recvID)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    /**
     * 회의(Conference) API
     */
    public static class Cfrc {
        private static final String LIST_SNS_CONFERENCE             = "/i2cowork/sns/cfrc/listSnsConference.json";
        private static final String VIEW_SNS_CONFERENCE             = "/i2cowork/sns/cfrc/selectSnsConference.json";
        private static final String LIST_SNS_CFRC_FILE              = "/i2cowork/sns/cfrc/listSnsConferenceFile.json";
        private static final String INSERT_SNS_CONFERENCE           = "/i2cowork/sns/cfrc/insertSnsConference.json";
        private static final String UPDATE_SNS_CONFERENCE           = "/i2cowork/sns/cfrc/updateSnsConference.json";
        private static final String DELETE_SNS_CONFERENCE           = "/i2cowork/sns/cfrc/deleteSnsConference.json";
        private static final String LIST_SNS_CONFERENCE_ONLINE_ROOM = "/i2cowork/sns/cfrc/listSnsConferenceOnline.json";
        private static final String LIST_SNS_CONFERENCE_ATTACH      = "/i2cowork/sns/cfrc/listSnsConferenceAttach.json";
        private static final String LIST_SNS_CONFERENCE_OFFLINE     = "/i2cowork/sns/cfrc/listSnsConferencePlan.json";
        private static final String LIST_SNS_CONFERENCE_PLAN        = "/i2cowork/sns/cfrc/listSnsConferencePlan.json";

        /**
         * 회의현황 리스트
         * @param mode
         * @param page
         * @param searchKeyword
         * @return
         */
        public static Request getListSnsConference(String mode, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CONFERENCE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_st", mode)
                    .add("searchKeyword", searchKeyword)
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 회의 상세
         * @param cfrc_id
         * @return
         */
        public static Request getViewSnsConference(String cfrc_id) {
            Log.e("","getViewSnsConference cfrcId = "+ cfrc_id);
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_CONFERENCE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_id", cfrc_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsCfrcFile(String cfrc_id) {
            Log.e("","getListSnsCfrcFile cfrcId = "+ cfrc_id);
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CFRC_FILE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_id", cfrc_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        /**
         * 회의실 예약 현황
         * @param cfrc_dt 조회일자
         * @return
         */
        public static Request getListSnsConferencePlan(String cfrc_dt) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CONFERENCE_PLAN);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_dt", cfrc_dt)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 오프라인 회의실별 예약현황
         * @param cfrc_dt
         * @param cfrc_room_id
         * @return
         */
        public static Request getListSnsConferenceOfflien(String cfrc_dt, String cfrc_room_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CONFERENCE_OFFLINE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_dt", cfrc_dt)
                    .add("cfrc_room_id", cfrc_room_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 회의자료실
         * @param tar_usr_id
         * @param page
         * @param searchKeyword
         * @return
         */
        public static Request getListSnsConferenceAttach(String tar_usr_id, String page, String searchKeyword) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CONFERENCE_ATTACH);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", tar_usr_id)
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .add("searchKeyword", searchKeyword)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 온라인 회의실
         * @param tar_usr_id
         * @return
         */
        public static Request getListConferenceOnlineRoom(String tar_usr_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_CONFERENCE_ONLINE_ROOM);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", tar_usr_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        public static FormEncodingBuilder getCfrcFormBuilder(String cfrcId, String ttl, String cfrcSt,
                                                             String cfrcDt, String startTm, String endTm,
                                                             String roomTp, String roomId, String cfrcTp,
                                                             String cntn, String notiYn, String openYn, String rsltCntn) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();
            Log.e("getCfrcFormBuilder", "saveCfrc CFRC_ID= " + cfrcId);
            formBuilder
                    .add("cfrc_id", cfrcId)
                    .add("cfrc_ttl", ttl)
                    .add("cfrc_st", cfrcSt) //RDY
                    .add("cfrc_dt", cfrcDt)
                    .add("start_tm", startTm)
                    .add("end_tm", endTm)
                    .add("cfrc_room_tp", roomTp) //OFFLINE|ONLINE
                    .add("cfrc_room_id", roomId)
                    .add("cfrc_tp", cfrcTp) //GEN|DOC|MOV|MOVDOC
                    .add("cfrc_cntn", cntn)
                    .add("srchText", "")
                    .add("cfrc_crt_noti_yn", notiYn)
                    .add("plan_open_yn", openYn)
                    .add("cfrc_rslt_cntn", rsltCntn)
                    .add("cfrc_gb", "")
                    .add("rsvt_noti_tm", "")
                    .build();

            return formBuilder;
        }


        /**
         * 회의생성
         * @param cfrcFormBuilder
         * @param memberList
         * @param totalList
         * @return
         */
        public static Request getSaveConference(String cfrcId, FormEncodingBuilder cfrcFormBuilder,
                                               List<Map<String, String>> memberList,
                                               List<List> totalList) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);

            if(TextUtils.isEmpty(cfrcId)) builder.append(INSERT_SNS_CONFERENCE);
            else builder.append(UPDATE_SNS_CONFERENCE);

            RequestBody formBody;

            //member
            if(memberList != null && memberList.size() > 0 ) {
                for (int i = 0; i < memberList.size(); i++) {
                    Map<String, String> member = memberList.get(i);
                    cfrcFormBuilder.add("usr_id", member.get("usr_id"));
                    cfrcFormBuilder.add("cfrc_usr_flag", member.get("cfrc_usr_flag"));
                }
            }

            //file
            if(totalList != null && totalList.size() > 0 ) {
                for (int i = 0; i < totalList.size(); i++) {
                    if(totalList.get(i) == null || (totalList.get(i) != null && totalList.get(i).size() <= 0)) continue;
                    for (int j = 0; j < totalList.get(i).size(); j++) {
                        Map<String, String> file = (Map<String, String>)totalList.get(i).get(j);
                        Log.e("getSaveConference","i = "+i+" /j="+j);
                        if(file == null ) break;
                        else {
                            Log.e("getSaveConference", "i = " + i + " /j=" + j + " / file_nm = " + file.toString());
                            cfrcFormBuilder.add("file_id", file.get("file_id"));
                            cfrcFormBuilder.add("file_nm", file.get("file_nm"));
                            cfrcFormBuilder.add("file_tp_cd", file.get("file_tp_cd")); //PHOTO|FILE
                            cfrcFormBuilder.add("tar_obj_tp_cd", file.get("tar_obj_tp_cd")); //CFRC
                            cfrcFormBuilder.add("tar_file_tp_cd", file.get("tar_file_tp_cd")); // GNR|MOV|DOC|REST
                            cfrcFormBuilder.add("phscl_file_nm", file.get("phscl_file_nm"));
                            cfrcFormBuilder.add("file_ext", file.get("file_ext"));
                            cfrcFormBuilder.add("file_size", file.get("file_size"));
                            cfrcFormBuilder.add(CodeConstant.ATTACH_ST, file.get(CodeConstant.ATTACH_ST)); //"":기존파일, "N" 신규파일, "D" 기존파일삭제 TODO "Y", "N", "D"
                        }

                    }
                }
            }


            formBody = cfrcFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 회의 삭제
         * @param cfrcId
         * @return
         */
        public static Request getDeleteConference(String cfrcId) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_CONFERENCE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("cfrc_id", cfrcId)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

    }

    public static class Task {
        private static final String LIST_SNS_TASK               = "/i2cowork/sns/task/listSnsTask.json";
        private static final String LIST_SNS_TASK_BY_OBJECT_ID  = "/task/listSnsTaskByObjectId.json"; //미사용
        private static final String VIEW_SNS_TASK               = "/i2cowork/sns/task/selectSnsTask.json";
        private static final String LIST_SNS_TASK_FILE          = "/i2cowork/sns/file/listSnsFileForEditPage.json";
        private static final String INSERT_SNS_TASK             = "/i2cowork/sns/task/insertSnsTask.json";
        private static final String UPDATE_SNS_TASK             = "/i2cowork/sns/task/updateSnsTask.json";
        private static final String DELETE_SNS_TASK             = "/i2cowork/sns/task/deleteSnsTask.json";

        public static Request getListSnsTask(String task_st_cd, String optTask, String page) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_TASK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", "")
                    .add("task_st_cd", task_st_cd)
                    .add("optTask", optTask)
                    .add("paging_yn", "Y")
                    .add("limit", "10")
                    .add("page", page)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        //미사용 메모, 회의, 과제관리 상세에서 작업리스트
        public static Request getListSnsTaskByObjectId(String tar_obj_tp, String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_TASK_BY_OBJECT_ID);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_obj_tp_cd", tar_obj_tp)
                    .add("tar_obj_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsTask(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_TASK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("task_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getListSnsTaskFile(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_TASK_FILE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_obj_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static FormEncodingBuilder getTaskFormBuilder(String tarObjTp, String tarObjId, String crtUsrId, String tarObjTtl,
                                                             String taskId, String ttl, String taskSt,
                                                             String startDttm, String endDttm,
                                                             String imptTp, String taskOrd,
                                                             String cntn, String taskStCdRest, String restCntn) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();

            formBuilder
                    .add("task_id", taskId)
                    .add("post_id", "")
                    .add("tar_obj_tp_cd", tarObjTp)
                    .add("tar_obj_id", tarObjId)
                    .add("up_tar_obj_id", tarObjId)
                    .add("crt_usr_id", crtUsrId)
                    .add("tar_obj_ttl", tarObjTtl)
                    .add("ttl", ttl)
                    .add("task_st_cd", taskSt)  //NST|FIN|DLY|RDY
                    .add("start_dttm", startDttm)
                    .add("end_dttm", endDttm)
                    .add("cntn", cntn)
                    .add("srchTextN", "")
                    .add("impt_tp", imptTp) //LOW|MIDD|HIGH ?
                    .add("task_ord", taskOrd) //number

                    .add("task_st_cd_rest", taskStCdRest)
                    .add("rest_cntn", restCntn)
                    .build();

            return formBuilder;
        }


        /**
         * 작업생성
         * @param taskFormBuilder
         * @param memberList
         * @param totalList
         * @return
         */
        public static Request getSaveTask(String taskId, FormEncodingBuilder taskFormBuilder,
                                                List<Map<String, String>> memberList,
                                                List<List> totalList) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            if(TextUtils.isEmpty(taskId)) builder.append(INSERT_SNS_TASK);
            else  builder.append(UPDATE_SNS_TASK);

            RequestBody formBody;

            //member
            if(memberList != null && memberList.size() > 0 ) {
                for (int i = 0; i < memberList.size(); i++) {
                    Map<String, String> member = memberList.get(i);
                    taskFormBuilder.add("usr_id", member.get("usr_id"));
                }
            }

            //file
            if(totalList != null && totalList.size() > 0 ) {
                for (int i = 0; i < totalList.size(); i++) {
                    if(totalList.get(i) == null || totalList.get(i).size() <= 0 ) continue;
                    for (int j = 0; j < totalList.get(i).size(); j++) {
                        Map<String, String> file = (Map<String, String>)totalList.get(i).get(j);
                        Log.e("", "i = " + i + " /j="+j);
                        if(file == null ) break;
                        else {
                            Log.e("","i = "+i+" /j="+j + " / attach_nm = "+ file.get("attach_nm"));
                            taskFormBuilder.add("file_nm", file.get("file_nm"));
                            taskFormBuilder.add("file_id", file.get("file_id"));
                            taskFormBuilder.add("file_tp_cd", file.get("file_tp_cd")); //FILE
                            taskFormBuilder.add("tar_obj_tp_cd", "USER"); // 이부분 NULL값이면 에러가 남
                            // taskFormBuilder.add("tar_obj_tp_cd", file.get("tar_obj_tp_cd")); // 원래는 주석처리
                            taskFormBuilder.add("tar_file_tp_cd", "FEED");
                            //taskFormBuilder.add("tar_file_tp_cd", file.get("tar_file_tp_cd"));  // GNL|REST
                            taskFormBuilder.add("phscl_file_nm", file.get("phscl_file_nm"));
                            Log.e("decurd", "attach_mode: " + file.get(CodeConstant.ATTACH_ST));
                            if (file.get(CodeConstant.ATTACH_ST) == "NEW") {
                                taskFormBuilder.add("file_ext", file.get("file_ext"));
                                taskFormBuilder.add("file_size", file.get("file_size"));
                                taskFormBuilder.add("file_path", file.get("file_path"));
                                taskFormBuilder.add(CodeConstant.ATTACH_ST, "NEW"); //""?"N":기존파일, "Y" 신규파일, "D" 기존파일삭제 ??TODO 회의랑 다른지 확인
                            } else if (file.get(CodeConstant.ATTACH_ST) == "DELETE")  {
                                taskFormBuilder.add("file_ext", "");
                                taskFormBuilder.add("file_size", "");
                                taskFormBuilder.add("file_path", "");
                                taskFormBuilder.add(CodeConstant.ATTACH_ST, "DELETE");
                            } else {
                                taskFormBuilder.add("file_ext", "");
                                taskFormBuilder.add("file_size", "");
                                taskFormBuilder.add("file_path", "");
                                taskFormBuilder.add(CodeConstant.ATTACH_ST, "NONE");
                            }

                        }

                    }
                }
            }

            formBody = taskFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 작업삭제
         * @param taskId
         * @return
         */
        public static Request getDeleteTask(String taskId) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_TASK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("task_id", taskId)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


    }

    /**
     * 메모보고
     */
    public static class Memo {
        private static final String LIST_SNS_MEMO               = "/i2cowork/sns/memo/listSnsMemo.json";
        private static final String LIST_SNS_MEMO_BY_OBJECT_ID = "/appr/listSnsApproveByObjectId.json";  //미사용
        private static final String UPDATE_APPROVE_BY_APPR_ST_CD = "/appr/updateSnsApproveByApprStCd.json";
        private static final String VIEW_SNS_MEMO               = "/i2cowork/sns/memo/selectSnsMemo.json";
        private static final String INSERT_SNS_MEMO             = "/i2cowork/sns/memo/insertSnsMemo.json";
        private static final String UPDATE_SNS_MEMO             = "/i2cowork/sns/memo/updateSnsMemo.json";
        private static final String DELETE_SNS_MEMO             = "/i2cowork/sns/memo/deleteSnsMemo.json";

        public static Request getListSnsMemo(String memo_st_cd, String optMemo, String page) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_MEMO);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", "")
                    .add("appr_st_cd", memo_st_cd)
                    .add("r_taskradio1", "")
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsMemo(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_MEMO);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("memo_appr_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request updateApproveByApprStcd(String tar_obj_id, String appr_st_cd) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPDATE_APPROVE_BY_APPR_ST_CD);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("appr_id", tar_obj_id)
                    .add("appr_st_cd", appr_st_cd)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        //과제관리 상세에서 작업리스트
        public static Request getListSnsMemoByObjectId(String tar_obj_tp, String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_MEMO_BY_OBJECT_ID);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_obj_tp_cd", tar_obj_tp)
                    .add("tar_obj_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static FormEncodingBuilder getMemoFormBuilder(String tarObjTp, String tarObjId, String crtUsrId, String tarObjTtl,
                                                             String taskId, String ttl, String taskSt,
                                                             String startDttm, String endDttm,
                                                             String imptTp, String taskOrd,
                                                             String cntn, String taskStCdRest, String restCntn) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();

            formBuilder
                    .add("appr_id", taskId)
                    .add("post_id", "")
                    .add("tar_obj_tp_cd", tarObjTp)
                    .add("tar_obj_id", tarObjId)
                    .add("crt_usr_id", crtUsrId)
                    .add("tar_obj_ttl", tarObjTtl)
                    .add("ttl", ttl)
                    .add("task_st_cd", taskSt)  //NST|FIN|DLY|RDY
                    .add("start_dttm", startDttm)
                    .add("end_dttm", endDttm)
                    .add("cntn", cntn)
                    .add("srchTextN", "")
                    .add("impt_tp", imptTp) //LOW|MIDD|HIGH ?
                    .add("task_ord", taskOrd) //number

                    .add("task_st_cd_rest", taskStCdRest)
                    .add("rest_cntn", restCntn)
                    .build();

            return formBuilder;
        }


        /**
         * 메모생성
         * @param memoFormBuilder
         * @param memberList
         * @param totalList
         * @return
         */
        public static Request getSaveMemo(String memoId, FormEncodingBuilder memoFormBuilder,
                                          List<Map<String, String>> memberList,
                                          List<List> totalList) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            if(TextUtils.isEmpty(memoId)) builder.append(INSERT_SNS_MEMO);
            else  builder.append(UPDATE_SNS_MEMO);

            RequestBody formBody;

            //member
            if(memberList != null && memberList.size() > 0 ) {
                for (int i = 0; i < memberList.size(); i++) {
                    Map<String, String> member = memberList.get(i);
                    memoFormBuilder.add("task_usr_id", member.get("task_usr_id"));
                    memoFormBuilder.add("fnl_appr_yn", member.get("fnl_appr_yn")); //신규"N"|기존"Y"
                }
            }

            //file
            if(totalList != null && totalList.size() > 0 ) {
                for (int i = 0; i < totalList.size(); i++) {
                    if(totalList.get(i) == null || totalList.get(i).size() <= 0 ) continue;
                    for (int j = 0; j < totalList.get(i).size(); j++) {
                        Map<String, String> file = (Map<String, String>)totalList.get(i).get(j);
                        Log.e("", "i = " + i + " /j="+j);
                        if(file == null ) break;
                        else {
                            Log.e("","i = "+i+" /j="+j + " / attach_nm = "+ file.get("attach_nm"));
                            memoFormBuilder.add("file_nm", file.get("file_nm"));
                            memoFormBuilder.add("file_id", file.get("file_id"));
                            memoFormBuilder.add("file_tp_cd", file.get("file_tp_cd")); //FILE
                            memoFormBuilder.add("tar_tp", file.get("tar_tp")); //TASK
                            memoFormBuilder.add("tar_sub_tp", file.get("tar_sub_tp"));  // GNL|REST
                            memoFormBuilder.add("phscl_file_nm", file.get("phscl_file_nm"));
                            memoFormBuilder.add("attach_yn", file.get("attach_yn")); //""?"N":기존파일, "Y" 신규파일, "D" 기존파일삭제 ??TODO 회의랑 다른지 확인
                            memoFormBuilder.add("file_ext", file.get("file_ext"));
                            memoFormBuilder.add("file_size", file.get("file_size"));
                        }

                    }
                }
            }

            formBody = memoFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 메모 삭제
         * @param tar_obj_id
         * @return
         */
        public static Request getDeleteMemo(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_MEMO);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("memo_appr_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


    }

    public static class Milestone {
        private static final String VIEW_SNS_MILESTONE = "/milestone/viewSnsMilestone.json";
        private static final String LIST_SNS_MILESTONE_BY_OBJECT_ID = "/milestone/listSnsMilestoneByObjectId.json";
        private static final String SAVE_SNS_MILESTONE = "/milestone/saveSnsMilestone.json";
        private static final String DELETE_SNS_MILESTONE = "/milestone/deleteSnsMilestone.json";
        //과제관리 상세에서 작업리스트
        public static Request getListSnsMilestoneByObjectId(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_MILESTONE_BY_OBJECT_ID);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("work_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsMilestone(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_MILESTONE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("miles_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        /**
         * 마일스톤 삭제
         * @param tar_obj_id
         * @return
         */
        public static Request getDeleteMilestone(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_MILESTONE);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("miles_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }


        public static FormEncodingBuilder getMilesFormBuilder(String curObjId, String tarObjId, String crtUsrId, String tarObjTtl,
                                                             String postId, String ttl, String startDt, String endDt,
                                                             String cpltDt, String pgrsSt) {

            FormEncodingBuilder formBuilder = new FormEncodingBuilder();

            formBuilder
                    .add("miles_id", curObjId)
                    .add("post_id", postId)
                    .add("work_id", tarObjId)
                    .add("crt_usr_id", crtUsrId)
                    .add("tar_obj_ttl", tarObjTtl)
                    .add("ttl", ttl)
                    .add("start_dt", startDt)
                    .add("end_dt", endDt)
                    .add("cplt_dt", cpltDt)
                    .add("pgrs_st", pgrsSt)
                    .build();

            return formBuilder;
        }

        public static Request getSaveMilestone(FormEncodingBuilder milesFormBuilder,
                                          List<Map<String, String>> memberList,
                                          List<List> totalList) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(SAVE_SNS_MILESTONE);

            RequestBody formBody;

            //member
            if(memberList != null && memberList.size() > 0 ) {
                for (int i = 0; i < memberList.size(); i++) {
                    Map<String, String> member = memberList.get(i);
                    milesFormBuilder.add("work_usr_id", member.get("usr_id"));
                }
            }

            formBody = milesFormBuilder.build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    /**
     * 이번버젼 미개발
     * 과제관리
     */
    public static class Work {
        private static final String LIST_SNS_WORK = "/work/listSnsWork.json";
        private static final String VIEW_SNS_WORK = "/work/viewSnsWork.json";
        private static final String SAVE_SNS_WORK = "/work/saveSnsWork.json";
        private static final String DELETE_SNS_WORK = "/work/deleteSnsWork.json";

        public static Request getListSnsWork(String st_cd, String searchKeyword, String searchKeywordBefore, String page) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(LIST_SNS_WORK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("tar_usr_id", "")
                    .add("pgrs_st", st_cd)
                    .add("searchKeyword", searchKeyword)
                    .add("searchKeywordBefore", searchKeywordBefore) //이전 과제 검색 키워드
                    .add("paging_yn", "Y")
                    .add("page", page)
                    .add("limit", "10")
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getViewSnsWork(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(VIEW_SNS_WORK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("work_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }

        public static Request getDeleteWork(String tar_obj_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(DELETE_SNS_WORK);

            RequestBody formBody = new FormEncodingBuilder()
                    .add("work_id", tar_obj_id)
                    .build();

            return getTokenRequest(builder.toString(), formBody);
        }
    }

    public static class File {
        private static final String PHOTO_IMAGE         = "/i2cowork/sns/file/image.do";
        private static final String FILE_DOWNLOAD       = "/i2cowork/sns/file/download.do";
        private static final String FILE_DOWNLOAD_SWF   = "/i2cowork/sns/file/i2ViewrFile.do";
        private static final String UPLOAD_FILE         = "/i2cowork/sns/file/upload.json";

        /**
         * 유저등록 이미지 요청(직접경로)
         * @param filePath
         * @return
         */
        public static String getUsrImage(String filePath) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(filePath);

            return builder.toString();
        }

        /**
         * SNS등에 포함된 파일 포토 이미지 요청 URL
         * @param fileId
         * @return
         */
        public static String getPhotoImage(String fileId) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(PHOTO_IMAGE);
            builder.append("?file_id=" + fileId);

            return builder.toString();
        }

        /**
         * 파일 다운로드 요청 URL
         * @param fileId
         * @return
         */
        public static String getDownloadFile(String fileId, String usr_id) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(FILE_DOWNLOAD);
            builder.append("?file_id=" + fileId + "&usr_id=" + usr_id);

            return builder.toString();
        }

        /**
         * SWF 파일 다운로드
         * I2VIEWER 용
         * @param fileId
         * @return
         */
        public static String getDownloadSwfFile(String fileId) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(FILE_DOWNLOAD_SWF);
            builder.append("?file_id=" + fileId);

            return builder.toString();
        }

        /**
         * 파일 업로드 요청
         * @param requestBody
         * @return
         */
        public static Request uploadFileRequest(RequestBody requestBody) {

            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(UPLOAD_FILE);

            return getUploadFileRequest(builder.toString(), requestBody);
        }


    }

    public static class Webview {
        private static final String WEBVIEW_BASE_URL = "/i2cowork/home/i2cWebView.do";

        /**
         * webview 로드 URL
         * @param url
         * @return
         */
        public static String getWebviewUrl(String url) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(WEBVIEW_BASE_URL);
            builder.append("?i2cViewType=mbhyd");
            builder.append("&call_url=" + url);

            return builder.toString();
        }
    }

    /**
     * i2 app 연동  URL
     * i2conference
     * i2livecat
     * i2viewer
     */
    public static class I2App {
        private static final String I2CONFERENCE_PAKAGE_URL = "i2conference://";
        private static final String I2LIVECHAT_PAKAGE_URL = "i2livechat://";
        private static final String I2VIEWER_PAKAGE_URL = "i2viewer://";

        private static final String I2CONFERENCE_APP_DOWNLOAD_URL = "/i2conference/i2Conference.apk";
        private static final String I2LIVECHAT_APP_DOWNLOAD_URL = "/livechat/i2LiveChat.apk";

        public static String getI2conferenceAppDownloadUrl() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(I2CONFERENCE_APP_DOWNLOAD_URL);
            return builder.toString();
        }

        public static String getI2LivechatAppDownloadUrl() {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkConstant.SERVER_HOST);
            builder.append(I2LIVECHAT_APP_DOWNLOAD_URL);
            return builder.toString();
        }

        public static String getI2conferencePakageUrl(String cfrcId) {
            StringBuilder builder = new StringBuilder();
            builder.append(I2CONFERENCE_PAKAGE_URL);
            if (!TextUtils.isEmpty(cfrcId))
                builder.append("?cfrc_id=" + cfrcId);
            builder.append("&user_id=" + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
            builder.append("&access_token=" + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN));
            return builder.toString();
        }

        public static String getI2livechatPakageUrl(String userType, String tarId) {
            StringBuilder builder = new StringBuilder();
            builder.append(I2LIVECHAT_PAKAGE_URL);
            try { //암호화 인코딩값을 UTF8로 변경
                builder.append("?usr_id=" + URLEncoder.encode(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_ENC_USR_ID), "utf-8")); //암호화된 usr_id
                if (!TextUtils.isEmpty(userType)) // CS고객상담
                    builder.append("&type=" + userType);
                if (!TextUtils.isEmpty(tarId)) // CS고객상담
                    builder.append("&tar_id=" + URLEncoder.encode(tarId, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("getI2livechatPakageUrl", "url = "+ builder.toString());
            return builder.toString();
        }

        public static String getI2viewerPakageUrl(String fileId, String FileName) {
            StringBuilder builder = new StringBuilder();
            builder.append(I2VIEWER_PAKAGE_URL);
            builder.append("?access_token=" + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_OAUTH_TOKEN));
            if (!TextUtils.isEmpty(fileId))
                builder.append("&download_url=" + File.getDownloadSwfFile(fileId));
            if (!TextUtils.isEmpty(FileName))
                builder.append("&file_name=" + FileName);
            return builder.toString();
        }

    }

}
