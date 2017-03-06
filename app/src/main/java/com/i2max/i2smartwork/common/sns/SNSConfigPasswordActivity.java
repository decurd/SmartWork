package com.i2max.i2smartwork.common.sns;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;

import org.json.JSONObject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSConfigPasswordActivity extends BaseAppCompatActivity {
    static String TAG = SNSConfigPasswordActivity.class.getSimpleName();

    protected TextView tvUsrID;
    protected EditText etPasswordCurrent, etPasswordNew, etPasswordNewConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_config_password);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("비밀번호 설정");

        tvUsrID = (TextView)findViewById(R.id.tv_usr_id);
        etPasswordCurrent = (EditText) findViewById(R.id.et_password_current);
        etPasswordNew = (EditText) findViewById(R.id.et_password_new);
        etPasswordNewConfirm = (EditText) findViewById(R.id.et_password_new_confirm);

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfilePassword();
            }
        });

        tvUsrID.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_LOGIN_ID));
    }

    public void updateProfilePassword() {
        I2ConnectApi.requestJSON(SNSConfigPasswordActivity.this, I2UrlHelper.SNS.updateSnsUserProfile(
                PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID),
                etPasswordCurrent.getText().toString(), etPasswordNew.getText().toString(), etPasswordNewConfirm.getText().toString(), ""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigPasswordActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
