package com.i2max.i2smartwork.common.sns;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.component.I2FSPAdapter;


public class SNSSearchActivity extends BaseAppCompatActivity {
    static String TAG = SNSSearchActivity.class.getSimpleName();

    public static final int LIST_SNS_USER = 0;
    public static final int LIST_SNS_GROUP = 1;

    public static boolean isChangedList = false;

    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;

    private int mTabPos;
    public String mSearchStr;
    public EditText EtSearch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_search);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("유저, 그룹 검색");



        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        EtSearch = (EditText) findViewById(R.id.et_search);
        EtSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mSearchStr = EtSearch.getText().toString();
                loadList(mTabPos);
                return false;
            }
        });

        mSearchStr = "";
        mTabPos = LIST_SNS_USER;
        if (mViewPager != null) {
            mAdapter.addFragment(new SNSSearchUserFragment(), "유 저");
            mAdapter.addFragment(new SNSSearchGroupFragment(), "그 룹");

            mViewPager.setAdapter(mAdapter);
            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadList(mTabPos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });

            mViewPager.setCurrentItem(mTabPos); //call onPageSelected;
        }
    }

    public void loadList(int pos) {
        Log.e(TAG, "3 mSearchStr= " + mSearchStr);

        switch (pos) {
            case LIST_SNS_USER:
                SNSSearchUserFragment searchUserFragment = (SNSSearchUserFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                searchUserFragment.clear();
                searchUserFragment.loadRecyclerView(mSearchStr);
                break;
            case LIST_SNS_GROUP:
                SNSSearchGroupFragment searchGroupFragment = (SNSSearchGroupFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(pos);
                searchGroupFragment.clear();
                searchGroupFragment.loadRecyclerView(mSearchStr);
                break;
        }

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
