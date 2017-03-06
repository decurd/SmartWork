package com.i2max.i2smartwork.common.web;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.I2WebChromeClient;
import com.i2max.i2smartwork.component.I2WebViewClient;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2UrlHelper;

/**
 * Created by shlee on 15. 9. 22..
 */
public class WebviewActivity extends BaseAppCompatActivity {
    static String TAG = WebviewActivity.class.getSimpleName();
    // Injection token as specified in HTML source
    // private static final String INJECTION_TOKEN = "**injection**";

    private AppCompatActivity acActivity;
    private ValueCallback<Uri> mUploadMessage;
    protected WebView mWv;
    protected String mTarObjTp = "", mTarObjId = "", mTarObjTtl = "", mTarCrtUsrId = "", mCrtUsrId = "";
    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_webview);

        Bundle extra = getIntent().getExtras();
        String title = "";
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, "");
            mTarObjTp = extra.getString(CodeConstant.TAR_OBJ_TP, "");
            mTarObjId = extra.getString(CodeConstant.TAR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mUrl = extra.getString(CodeConstant.TAR_SELECT_URL, "");
            mTarCrtUsrId = extra.getString(CodeConstant.TAR_CRT_USR_ID, "");
            mCrtUsrId = extra.getString(CodeConstant.CRT_USR_ID, "");

            Log.e(TAG, "mTarObjTp = " + mTarObjTp + " /mTarObjId = " + mTarObjId + " /mTarObjTtl = " + mTarObjTtl + " /mTarCrtUsrId= " + mTarCrtUsrId);
            Log.e(TAG, " /CRT_USR_ID= " + mCrtUsrId);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mWv = (WebView) findViewById(R.id.wv_body);

        /*webviewSetting();
        String param = mUrl+""+mTarObjId;
        mWv.loadUrl(param);*/
    }

    @Override
    public void onResume() {
        super.onResume();

        webviewSetting();

        String param = mUrl+""+mTarObjId;
        Log.e(TAG, "wb url = " + I2UrlHelper.Webview.getWebviewUrl(param));
        Log.e(TAG, "wb header = " + I2UrlHelper.getTokenHeader().toString());
        Log.e(TAG, "wb param = " + param);
        mWv.loadUrl(param);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.e("decurd", "onKeyDown: true");

        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWv.canGoBack()) {
            mWv.goBack();
        }

        return super.onKeyDown(keyCode, event);
    }*/

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("JavascriptInterface")
    protected void webviewSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // 팝업(window.open) 허용. setJavaScriptCanOpenWindowsAutomatically만 설정하는 경우 Main WebView에 Url 이 로딩되므로 setSupportMultipleWindows 설정 후 onCreateWindow를 Override 해야 함.
        mWv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWv.getSettings().setSupportMultipleWindows(true);

//        mWv.setWebChromeClient(new WebChromeClient());

        mWv.setBackgroundColor(0xFFEfEfEf); //로딩시 하얀 하면 안뛰우기

        // UserAgent 모바일 브라우져와 구분처리
//        mWv.getSettings().setUserAgentString("i2-Android");
        // WebView client
//        _webViewClient = new I2WebViewClient(this);
//        _webViewClient.setOnLinkedClickListener(_innerOnLinkedClickListener);
//        _webViewClient.setOnLoadCompleteListener(_innerOnLoadCompleteListener);
//        mWv.setWebViewClient(_webViewClient);
        mWv.setWebViewClient(new I2WebViewClient(WebviewActivity.this));
//        mWv.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return false;
//            }
//        });
        // **중요 아래 크롬클라이언트 없으면 javascript call 되지 않음
        mWv.setWebChromeClient(new I2WebChromeClient(WebviewActivity.this, mUploadMessage));
        mWv.getSettings().setJavaScriptEnabled(true);

        // Android <--> Javascript 통신 인터페이스
        mWv.addJavascriptInterface(new AndroidBridge(), "android");
        mWv.addJavascriptInterface(new WebAppInterface(this), "AndroidConnect");


        // javascript가 window.open()을 사용할 수 있도록 설정
        // mWv.getSettings().setJavaScriptCanOpenWindowsAutomtically(true);

        // 플러그인을 사용할 수 있도록 설정
        // mWv.getSettings().setPluginsEnabled(true);
        mWv.getSettings().setPluginState(WebSettings.PluginState.ON);
        // 화면 맞추기
        // wide viewport를 사용하도록 설정
        mWv.getSettings().setUseWideViewPort(true);
        mWv.getSettings().setLoadWithOverviewMode(true);
        // mWv.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        // mWv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        // 여러개의 윈도우를 사용할 수 있도록 설정
        // mWv.getSettings().setSupportMultipleWindows(true);
        // 확대,축소 기능을 사용할 수 있도록 설정
        mWv.getSettings().setSupportZoom(false);
        // 안드로이드에서 제공하는 줌 아이콘을 사용할 수 있도록 설정
        mWv.getSettings().setBuiltInZoomControls(false);
        // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정
        mWv.getSettings().setLoadsImagesAutomatically(true);
        mWv.setHorizontalScrollBarEnabled(false);

        // cache 미사용
        mWv.getSettings().setAppCacheMaxSize(0);
        mWv.getSettings().setAppCacheEnabled(false);
        // html5 application cache 사용
        // mWv.getSettings().setAppCacheMaxSize(NumberConstant.MEMORY_WEBVIEW_CACHE);
        // mWv.getSettings().setAppCacheEnabled(true);
        // mWv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // String appCachePath = getBaseApplication().getCacheDir().getAbsolutePath();
        // mWv.getSettings().setAppCachePath(appCachePath);
        // mWv.getSettings().setAppCachePath(NetworkConstant.WEBVIEW_CACHE_PATH‌​);
        // asset 파일 접근 허용
        mWv.getSettings().setAllowFileAccess(true);
        mWv.getSettings().setAllowFileAccessFromFileURLs(true);
        // js 파일 접근 허용
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWv.getSettings().setAllowUniversalAccessFromFileURLs(true);
//        }

        // setDomStorageEnabled 주석풀면 Android <-> JS 통신이 안됨
         mWv.getSettings().setDomStorageEnabled(true);

        mWv.setFocusable(true);
        mWv.setFocusableInTouchMode(true);
        // mWv.setOnTouchListener(new View.OnTouchListener() {
        // @Override
        // public boolean onTouch(View v, MotionEvent event) {
        // int kAction = event.getAction();
        // switch (kAction) {
        // case MotionEvent.ACTION_DOWN:
        // // Log.e("onLinkedClick", "x = " + event.getX() + ", y=" +
        // // event.getY());
        // // mWv.loadUrl("javascript:alert('tab once')");
        // break;
        // case MotionEvent.ACTION_UP:
        // if (!v.hasFocus()) {
        // v.requestFocus();
        //
        // }
        // break;
        // }
        // return false;
        // }
        // });
        // PictureListener를 설정합니다.

//        mWv.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                WebResourceResponse response = super.shouldInterceptRequest(view, url);
//                if(url != null && url.contains(INJECTION_TOKEN)) {
//                    String assetPath = url.substring(url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
//                    try {
//                        response = new WebResourceResponse(
//                                "application/javascript",
//                                "UTF8",
//                                getContext().getAssets().open(assetPath)
//                        );
//                    } catch (IOException e) {
//                        e.printStackTrace(); // Failed to load asset file
//                    }
//                }
//                return response;
//            }
//        });

    }

    // html javascript call방벙 : ex) window.android.nativeRequest();
    protected class AndroidBridge {
        @JavascriptInterface
        public String getNativePath() {
            Log.e("AndroidBridge", "call nativePath : ");

            return "";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /* // 잠시 막아놈 풀어야 함
    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();
    }
*/

    @Override
    public void onBackPressed() {

        if (mWv.canGoBack()) {
            mWv.goBack();
        } else {
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.e("decurd", "onActivityResult: true");
        if(requestCode == I2WebChromeClient.FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

}
