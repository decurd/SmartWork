package com.i2max.i2smartwork.utils;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.digits.sdk.android.Digits;
import com.i2max.i2smartwork.constant.AppConstant;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by shlee on 2015. 12. 11..
 */
public class TraceUtil {

    public static void logAnswer(Context context, String title, String mode, String pageId) {
        if (!Fabric.isInitialized()) {
            //init fabric
            TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConstant.TWITTER_KEY, AppConstant.TWITTER_SECRET);
            Fabric.with(context, new TwitterCore(authConfig), new Digits(), new Crashlytics());
        } else {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(title)
                    .putContentType(mode)
                    .putContentId(pageId));
        }
    }
}
