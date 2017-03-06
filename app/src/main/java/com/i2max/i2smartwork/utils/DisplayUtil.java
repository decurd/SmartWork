package com.i2max.i2smartwork.utils;

import android.content.Context;

/**
 * Created by shlee on 15. 9. 9..
 */
public class DisplayUtil {

        /**
         * px > dp
         */
        public static int px2dip(Context context, float pxValue) {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }

        /**
         * dip, dp > px
         */
        public static int dip2px(Context context, float dipValue) {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dipValue * scale + 0.5f);
        }

        /**
         * px > sp
         */
        public static int px2sp(Context context, float pxValue) {
            float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (pxValue / fontScale + 0.5f);
        }

        /**
         * sp > px
         */
        public static int sp2px(Context context, float spValue) {
            float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * fontScale + 0.5f);
        }
}
