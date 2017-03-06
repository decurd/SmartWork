package com.i2max.i2smartwork.common.sns;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;

public class SNSVersionInfoActivity extends BaseAppCompatActivity {
    protected static String TAG = SNSVersionInfoActivity.class.getSimpleName();

    protected TextView tvVersionCurrent, tvVersionServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_version_info);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("버전정보");

        tvVersionCurrent = (TextView) findViewById(R.id.tv_version_current);
        tvVersionServer = (TextView) findViewById(R.id.tv_version_server);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersionCurrent.setText("v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

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
