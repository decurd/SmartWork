package com.i2max.i2smartwork.i2api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okio.Buffer;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by JHTart on 15. 6. 15..
 */
public class I2ConnectApi {
    static String TAG = I2ConnectApi.class.getSimpleName();

    public static Observable<String> sendGCMToken(final Context context, final Request request) { // GCM토큰 발송 Observable 생성
        Observable<String> observable;
        try {
            observable = Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {

                    try {
                        Log.e(TAG, "requestURL = " + request.urlString());
                        Log.e(TAG, "requestHeader = " + request.headers().toString());
                        Log.e(TAG, "requestBody = " + bodyToString(request));

                        OkHttpClient client = new OkHttpClient();

                        //TODO 암호화

                        Response responses = client.newCall(request).execute();

                        String jsonString = responses.body().string();
                        Log.e(TAG, "responses = " + jsonString.toString());
                        //TODO 복호화

                        subscriber.onNext(jsonString);

                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    }

                    subscriber.onCompleted();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            observable = null;
            DialogUtil.showErrorDialog(context, context.getString(R.string.unable_access_network_error));
        }
        return observable;
    }

    public static Observable<String> requestString(final Context context, final Request request) {
        Observable<String> observable;
        try {
            observable = Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {

                    try {
                        Log.e(TAG, "requestURL = " + request.urlString());
                        Log.e(TAG, "requestHeader = " + request.headers().toString());
//                        Log.d(TAG, "requestBody = " + bodyToString(request));

                        OkHttpClient client = new OkHttpClient();

                        Response responses = client.newCall(request).execute();

                        String result = responses.body().string();
                        Log.e(TAG, "responses = " + result.toString());

                        subscriber.onNext(result);

                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    }

                    subscriber.onCompleted();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            observable = null;
            DialogUtil.showErrorDialog(context, context.getString(R.string.unable_access_network_error));
        }
        return observable;
    }

    public static Observable<Map<String, Object>> requestJSON2Map(final Context context, final Request request) {
        Observable<Map<String, Object>> observable;
        try {
            observable = Observable.create(new Observable.OnSubscribe<Map<String, Object>>() {
                @Override
                public void call(Subscriber<? super Map<String, Object>> subscriber) {

                    try {
                        Log.e(TAG, "requestURL = " + request.urlString());
                        Log.e(TAG, "requestHeader = " + request.headers().toString());
                        Log.e(TAG, "requestBody = " + bodyToString(request));

                        OkHttpClient client = new OkHttpClient();

                        //TODO 암호화

                        Response responses = client.newCall(request).execute();

                        String jsonString = responses.body().string();
                        Log.e(TAG, "responses = " + jsonString.toString());
                        //TODO 복호화

                        Gson gson = new Gson();
                        Map<String, Object> status = new HashMap<String, Object>();
                        status = (Map<String, Object>) gson.fromJson(jsonString, status.getClass());

                        if (isError(status)) {
                            if (status.containsKey("error")) { //OAUTH 2.0 ERRORstatus.get("error_description").toString()
                                subscriber.onError(new IOException(status.get("error_description").toString()));
                            } else {
                                subscriber.onError(
                                        new IOException(status.get("statusMessage") != null ? status.get("statusMessage").toString() : "로그인 정보가 맞지 않습니다\n다시 입력해주시기 바랍니다"));
                            }
                        }

//                    Log.d(TAG, "statusInfoMap = " + status.toString());

                        subscriber.onNext(status);

                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    }

                    subscriber.onCompleted();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            observable = null;
            DialogUtil.showErrorDialog(context, context.getString(R.string.unable_access_network_error));
        }
        return observable;
    }

    public static Observable<JSONObject> requestJSON(final Context context, final Request request) {
        Observable<JSONObject> observable;
        try {
            observable = Observable.create(new Observable.OnSubscribe<JSONObject>() {
                @Override
                public void call(Subscriber<? super JSONObject> subscriber) {

                    try {
                        Log.e(TAG, "requestURL = " + request.urlString());
                        Log.e(TAG, "requestHeader = " + request.headers().toString());
                        Log.e(TAG, "requestBody lll= " + bodyToString(request));
                        //tar_obj_tp_cd=USER&tar_obj_id=65vpv5p9zb8x8&post_id=&post_mode=FOLLOW&sort_type=MOD&paging_yn=Y&page=1&limit=10&post_search_text=
                        //requestBody lll= grp_id=5ep6b4f5qu3kk
                        OkHttpClient client = new OkHttpClient();

                        Response responses = client.newCall(request).execute();

                        String jsonString = responses.body().string();
                        Log.e(TAG, "responses = " + jsonString.toString());
                        JSONObject status = new JSONObject(jsonString);
                        if (isError(status)) {
                            if (status.has("error")) { //OAUTH 2.0 ERROR
                                subscriber.onError(new IOException(status.has("error_description") ? status.get("error_description").toString() : "에러가 발생하였씁니다"));
                            } else {
                                subscriber.onError(new IOException(status.has("statusMessage") ? status.get("statusMessage").toString() : "에러가 발생하였씁니다"));
                            }
                        }
//                    Log.d(TAG, "responses jsonData = " + status.toString());
                        subscriber.onNext(status);

                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    }

                    subscriber.onCompleted();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            observable = null;
            DialogUtil.showErrorDialog(context, context.getString(R.string.unable_access_network_error));
        }
        return observable;
    }

    public static Observable<JSONObject> uploadFile(Context context, final String fileName, final String mimeType, final File file) {
        Observable<JSONObject> observable;
        try {
            observable = Observable.create(new Observable.OnSubscribe<JSONObject>() {
                @Override
                public void call(Subscriber<? super JSONObject> subscriber) {
                    MediaType MEDIA_TYPE = MediaType.parse(mimeType + "; charset=utf-8");

                    OkHttpClient client = new OkHttpClient();

                    try {
                        RequestBody requestBody;
                        final String BOUNDARY = String.valueOf(System.currentTimeMillis());

                        MultipartBuilder mb = new MultipartBuilder(BOUNDARY)
                                .type(MultipartBuilder.FORM)
                                .addFormDataPart("file", fileName, RequestBody.create(MEDIA_TYPE, file));

                        requestBody = mb.build();

                        Request request = I2UrlHelper.File.uploadFileRequest(requestBody);

                        Log.d(TAG, "requestURL = " + request.urlString());
                        Log.d(TAG, "requestBody = " + bodyToString(request));

                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);

                        String jsonString = response.body().string();
                        JSONObject status = new JSONObject(jsonString);
                        if (isError(status)) {
                            if (status.has("error")) { //OAUTH 2.0 ERROR
                                subscriber.onError(new IOException(status.getString("error_description")));
                            } else {
                                subscriber.onError(new IOException(status.getString("statusMessage")));
                            }
                        }

                        Log.d(TAG, "jsonData = " + status.toString());

                        subscriber.onNext(status);

                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        subscriber.onError(new IOException(e.getMessage()));
                    }

                    subscriber.onCompleted();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            observable = null;
            DialogUtil.showErrorDialog(context, context.getString(R.string.unable_access_network_error));
        }
        return observable;
    }


    public static boolean isError(JSONObject resultJSON) {
        boolean result = false;

        if (TextUtils.isEmpty(resultJSON.toString())) {
            result = true;
        } else if (resultJSON.has("error")) { //OAUTH 2.0 ERROR
            result = true;
        } else if (resultJSON.has("statusCode")) { //I2CONNECT ERROR
            int statusCode = -1;
            try {
                statusCode = resultJSON.getInt("statusCode");
            } catch (JSONException e) {
                e.printStackTrace();
                return true;
            }
            result = statusCode < 0; //0일떄 정상 양수면 알림(에러??), 음수면 시스템 알림  구분이 애매해서 음수 에러/ 양수 정상 처리로 변경
        } else {
            result = false;
        }

        return result;
    }

    public static boolean isError(Map<String, Object> resultJSON) {
        boolean result = false;
        int statusCode = -1;

        if (resultJSON.containsKey("error")) { //OAUTH 2.0 ERROR
            result = true;
        } else if (resultJSON.get("statusCode") != null) { //I2CONNECT ERROR
            statusCode = Integer.parseInt(resultJSON.get("statusCode").toString());
            result = statusCode < 0; //0일떄 정상 양수면 알림(에러??), 음수면 시스템 알림  구분이 애매해서 음수 에러/ 양수 정상 처리로 변경
        } else {
            result = false;
        }

        return result;
    }

    private static String bodyToString(final Request request) {

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }


}
