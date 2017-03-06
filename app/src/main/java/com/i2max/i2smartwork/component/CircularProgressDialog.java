package com.i2max.i2smartwork.component;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.i2max.i2smartwork.R;


/**
 * Created by berserk1147 on 15. 8. 2..
 */
public class CircularProgressDialog extends Dialog {

    public CircularProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_circular_progress);
    }
}