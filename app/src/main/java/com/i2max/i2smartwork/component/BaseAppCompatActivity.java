package com.i2max.i2smartwork.component;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.constant.AppConstant;
import com.i2max.i2smartwork.utils.PreferenceUtil;


/**
 * TODO
 */
public class BaseAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceUtil.initializeInstance(this);

        if(AppConstant.SECURITY_MODE_ENABLED) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        lolipopSetStatusBarColor();
    }

    @Override
    public void onResume() {
        if(PreferenceUtil.isNull()) {
            PreferenceUtil.initializeInstance(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() { super.onPause(); }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void lolipopSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken() == null) {
            Log.e("BaseAppCompatActivity", "hideSoftKeyboard activate");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }



}

