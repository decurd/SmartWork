package com.i2max.i2smartwork.common.task;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.i2max.i2smartwork.MainActivity;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.I2FSPAdapter;
import com.i2max.i2smartwork.constant.CodeConstant;


public class TaskMainFragment extends Fragment {
    static String TAG = TaskMainFragment.class.getSimpleName();

    public static boolean isChangedList = false;

    private AppCompatActivity acActivity;
    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    private int mTabPos;
    private TextView mTvOtpNm;
    private SwitchCompat mScOtp;
    private String mOtpTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_object_main, container, false);

        Bundle bundle = this.getArguments();
        String title = "";
        if (bundle != null) {
            title= bundle.getString(CodeConstant.TITLE, getString(R.string.task));
        }

        acActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        acActivity.setSupportActionBar(toolbar);

        ((MainActivity) acActivity).setVisibleFabButton(true);

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
            mTabPos = 1;

            mAdapter.addFragment(new TaskMainListFragment(), "대 기");
            mAdapter.addFragment(new TaskMainListFragment(), "진 행");
            mAdapter.addFragment(new TaskMainListFragment(), "지 연");
            mAdapter.addFragment(new TaskMainListFragment(), "완 료");
            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadListTask(mTabPos);
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

        if(isChangedList) {
            Log.d(TAG, "isChangedList = true");

            loadListTask(mTabPos);

            isChangedList = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.actions_switch_detail, menu);
        MenuItem tvItem = menu.findItem(R.id.tv_otp_nm);
        MenuItem scItem = menu.findItem(R.id.sc_otp);
        menu.findItem(R.id.action_sns_function);
        mTvOtpNm = (TextView) MenuItemCompat.getActionView(tvItem);
        mTvOtpNm.setText("내작업");
        mScOtp = (SwitchCompat) MenuItemCompat.getActionView(scItem);
        mScOtp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibleAssigned(isChecked);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setVisibleAssigned(boolean bool) {
        if (bool) {
            mTvOtpNm.setText("내가 할당한 작업");
        } else {
            mTvOtpNm.setText("내작업");
        }
        loadListTask(mTabPos);
    }

    public void loadListTask(int position) {
        if(mScOtp == null || mScOtp.isChecked()) mOtpTask = "assignTask"; //내가 할당한 작업
        else mOtpTask = "myTask"; //내작업

        TaskMainListFragment taskMainListFragment = (TaskMainListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(position);
        taskMainListFragment.onAttach(getContext());
        taskMainListFragment.setPostMode(position);
        taskMainListFragment.initListPage();
        taskMainListFragment.loadRecyclerView(mOtpTask);
    }

}
