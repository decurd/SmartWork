package com.i2max.i2smartwork.common.push;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.I2FSPAdapter;

public class PushListActivity extends BaseAppCompatActivity {
    static String TAG = PushListActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_list);

        ImageView ivClose = (ImageView)findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        if (mViewPager != null) {
            mAdapter.addFragment(new PushListFragment(), "미확인");
            mAdapter.addFragment(new PushListFragment(), "확 인");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Log.d(TAG, "onPageSelected = " + position);

                    loadListPush(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadListPush(0);
                }
            }, 500);
        }
    }

    public void loadListPush(int pos) {
        PushListFragment snsListFragment = (PushListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
        snsListFragment.setPostMode(pos);
        snsListFragment.loadRecyclerView();
    }

    //플로팅 버튼 표시/숨김 처리
//    public void setVisibleFabButton(boolean bool) {
//        if(bool) mFab.showMenuButton(true);
//        else mFab.hideMenuButton(true);
//    }
}
