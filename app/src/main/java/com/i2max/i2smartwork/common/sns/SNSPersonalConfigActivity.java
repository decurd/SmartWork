package com.i2max.i2smartwork.common.sns;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.i2max.i2smartwork.MainActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.utils.DialogUtil;

public class SNSPersonalConfigActivity extends BaseAppCompatActivity {
    protected static String TAG = SNSPersonalConfigActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_personal_config);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("설정");


        RelativeLayout rlProfile = (RelativeLayout)findViewById(R.id.rl_profile);
        rlProfile.setOnClickListener(rlClickListener);

        RelativeLayout rlPassword = (RelativeLayout)findViewById(R.id.rl_password);
        rlPassword.setOnClickListener(rlClickListener);

//        RelativeLayout rlPush = (RelativeLayout)findViewById(R.id.rl_push);
//        rlPush.setOnClickListener(rlClickListener);

        RelativeLayout rlVersionInfo = (RelativeLayout)findViewById(R.id.rl_version_info);
        rlVersionInfo.setOnClickListener(rlClickListener);

        Button btnLogout = (Button)findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showLogoutDialog(SNSPersonalConfigActivity.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public View.OnClickListener rlClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.rl_profile :
                    Intent intent1 = new Intent(SNSPersonalConfigActivity.this, SNSConfigProfileActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.rl_password :
                    Intent intent2 = new Intent(SNSPersonalConfigActivity.this, SNSConfigPasswordActivity.class);
                    startActivity(intent2);
                    break;
//                case R.id.rl_push :
//                    Log.d(TAG, "rl_plan Onclick");
//                    break;
                case R.id.rl_version_info :
                    Intent intent4 = new Intent(SNSPersonalConfigActivity.this, SNSVersionInfoActivity.class);
                    startActivity(intent4);
                    break;

            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
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
}
