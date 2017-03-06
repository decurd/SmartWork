package com.i2max.i2smartwork.common.conference;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.I2FSPAdapter;
import com.i2max.i2smartwork.constant.CodeConstant;


public class ConferenceMainFragment extends Fragment {
    static String TAG = ConferenceMainFragment.class.getSimpleName();

    public static boolean isChangedList = false;

    private AppCompatActivity acActivity;
    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    private int mTabPos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_object_main, container, false);

        Bundle bundle = this.getArguments();
        String title= bundle.getString(CodeConstant.TITLE, getString(R.string.connect_community));

        acActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        acActivity.setSupportActionBar(toolbar);

        final ActionBar ab = acActivity.getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mViewPager = (ViewPager) v.findViewById(R.id.viewpager);
        mAdapter = new I2FSPAdapter(acActivity.getSupportFragmentManager());
        mTabLayout = (TabLayout) v.findViewById(R.id.tabs);

        if (mViewPager != null) {
            mTabPos = 1;

            mAdapter.addFragment(new ConferenceMainListFragment(), "대 기");
            mAdapter.addFragment(new ConferenceMainListFragment(), "전 체");
            mAdapter.addFragment(new ConferenceMainListFragment(), "종 료");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadListCfrc(mTabPos, "");
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });

            mViewPager.setCurrentItem(mTabPos);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "isChangedList = "+isChangedList);
        if(isChangedList) {
            loadListCfrc(mTabPos, "");

            isChangedList = false;
        }
    }

    public void loadListCfrc(int position, String searchStr) {
        ConferenceMainListFragment conferenceMainListFragment = (ConferenceMainListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(position);
        conferenceMainListFragment.onAttach(getActivity());
        conferenceMainListFragment.setPostMode(position);
        conferenceMainListFragment.initListPage();
        conferenceMainListFragment.loadRecyclerView(searchStr);
    }

    public void searchResult(String searchStr) {
        loadListCfrc(mTabPos, searchStr);
    }

}
