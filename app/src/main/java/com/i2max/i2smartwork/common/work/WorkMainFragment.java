package com.i2max.i2smartwork.common.work;

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


public class WorkMainFragment extends Fragment {
    static String TAG = WorkMainFragment.class.getSimpleName();

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
        String title = "";
        if (bundle != null) {
            title= bundle.getString(CodeConstant.TITLE, getString(R.string.work));
        }

        acActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        acActivity.setSupportActionBar(toolbar);

        // if this is set true,
        // Activity.onCreateOptionsMenu will call Fragment.onCreateOptionsMenu
        // Activity.onOptionsItemSelected will call Fragment.onOptionsItemSelected
        setHasOptionsMenu(true);

        final ActionBar ab = acActivity.getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mViewPager = (ViewPager) v.findViewById(R.id.viewpager);

        mAdapter = new I2FSPAdapter(acActivity.getSupportFragmentManager());
        mTabLayout = (TabLayout) v.findViewById(R.id.tabs);

        if (mViewPager != null) {
            mTabPos = 0;
            //디폴트 처리할 결제
            mAdapter.addFragment(new WorkMainListFragment(), "전체");
            mAdapter.addFragment(new WorkMainListFragment(), "대기");
            mAdapter.addFragment(new WorkMainListFragment(), "진행");
            mAdapter.addFragment(new WorkMainListFragment(), "완료");
            mAdapter.addFragment(new WorkMainListFragment(), "지연");

            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadList(mTabPos, "");
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });

            mViewPager.setCurrentItem(mTabPos);
            loadList(mTabPos, "");
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isChangedList) {
            Log.d(TAG, "isChangedList = true");

            loadList(mTabPos, "");

            isChangedList = false;
        }
    }

    public void loadList(int position, String searchStr) {
        WorkMainListFragment workMainListFragment = (WorkMainListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(position);
        workMainListFragment.onAttach(getContext());
        workMainListFragment.setPostMode(position);
        workMainListFragment.initListPage();
        workMainListFragment.loadRecyclerView(searchStr);
    }

    public void searchResult(String searchStr) {
        loadList(mTabPos, searchStr);
    }
}
