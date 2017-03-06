package com.i2max.i2smartwork.common.memo;

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

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.I2FSPAdapter;
import com.i2max.i2smartwork.constant.CodeConstant;


public class MemoMainFragment extends Fragment {
    static String TAG = MemoMainFragment.class.getSimpleName();

    public static boolean isChangedList = false;

    private AppCompatActivity acActivity;
    private ViewPager mViewPager;
    private I2FSPAdapter mAdapter;
    private TabLayout mTabLayout;
    private int mTabPos;

    private TextView mTvOtpNm;
    private SwitchCompat mScOtp;
    private String mOtpMemo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_object_main, container, false);

        Bundle bundle = this.getArguments();
        String title = "";
        if (bundle != null) {
            title= bundle.getString(CodeConstant.TITLE, getString(R.string.memo));
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
            mAdapter.addFragment(new MemoMainListFragment(), "처리할 결제");
            mAdapter.addFragment(new MemoMainListFragment(), "참 조");
            mAdapter.addFragment(new MemoMainListFragment(), "완 결");

            mViewPager.setAdapter(mAdapter);

            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mTabPos = position;
                    loadListMemo(mTabPos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });

            mViewPager.setCurrentItem(mTabPos);
            loadListMemo(mTabPos);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isChangedList) {
            Log.d(TAG, "isChangedList = true");

            loadListMemo(mTabPos);

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
        mTvOtpNm.setText("처리할 결제");
        mScOtp = (SwitchCompat) MenuItemCompat.getActionView(scItem);
        mScOtp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibleOtp(isChecked);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setVisibleOtp(boolean bool) {
        if (bool) {
            mTvOtpNm.setText("내 결제함");
            //FragmentStatePagerAdapter에서는 mAdapter.notifyDataSetChanged();로 view업데이트가 안됨.
            mTabLayout.getTabAt(0).setText("작성중");
            mTabLayout.getTabAt(1).setText("상 신");
            mTabLayout.getTabAt(2).setText("완 료");
        } else {
            mTvOtpNm.setText("처리할 결제");
            mTabLayout.getTabAt(0).setText("처리할 결제");
            mTabLayout.getTabAt(1).setText("참 조");
            mTabLayout.getTabAt(2).setText("완 결");
        }
        loadListMemo(mTabPos);
    }

    public void loadListMemo(int position) {
        if(mScOtp == null || !mScOtp.isChecked()) mOtpMemo = "optMemo1";
        else mOtpMemo = "optMemo2";

        MemoMainListFragment memoMainListFragment = (MemoMainListFragment) ((I2FSPAdapter) mViewPager.getAdapter()).getItem(position);
        memoMainListFragment.onAttach(getContext());
        memoMainListFragment.setPostMode(position);
        memoMainListFragment.initListPage();
        memoMainListFragment.loadRecyclerView(mOtpMemo);
    }

//    public void searchResult(String searchStr) {
//        loadListMemo(mTabPos, searchStr);
//    }

}
