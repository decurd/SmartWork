package com.i2max.i2smartwork.component;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.Digits;
import com.i2max.i2smartwork.constant.AppConstant;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by shlee on 2015. 10. 30..
 */
public class BaseApplication extends Application {

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConstant.TWITTER_KEY, AppConstant.TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits(), new Crashlytics());

    }

    public static RefWatcher getRefWatcher(Context context) {
        BaseApplication application = (BaseApplication) context.getApplicationContext();
        return application.refWatcher;
    }

}
