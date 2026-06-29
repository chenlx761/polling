package com.chenming.common.adapter;

import android.os.Parcelable;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by Admin on 2018/6/19.
 */

public class MyViewPageAdapter extends FragmentStatePagerAdapter {
    private String[] mTitles;
    private List<Fragment> mFragments;

    public MyViewPageAdapter(FragmentManager fm, String[] mTitles, List<Fragment> fragments) {
        super(fm);
        this.mTitles = mTitles;
        this.mFragments = fragments;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void setTitles(String[] titles) {
        mTitles = titles;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {

    }
}
