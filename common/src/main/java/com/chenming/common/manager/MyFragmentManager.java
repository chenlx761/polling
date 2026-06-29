package com.chenming.common.manager;

import android.os.Bundle;

import com.chenming.common.R;
import com.chenming.httprequest.XLog;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


/**
 * Created by wqx on 2018/4/25.
 */

public class MyFragmentManager {


    private int lastPosition = 0;


    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    private Fragment[] mFragments;
    private String[] mTags;

    public MyFragmentManager(FragmentActivity activity, Bundle savedInstanceState, Fragment[] fragments, String[] tags) {
        mFragments = fragments;
        mTags = tags;
        initFragment(activity, savedInstanceState);
    }

    private void initFragment(FragmentActivity activity, Bundle savedInstanceState) {

        fragmentManager = activity.getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        for (int i = 0; i < mFragments.length; i++) {
            transaction.add(R.id.fragment_container, mFragments[i], mTags[i]);
        }
        //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);   //过渡动画
        if (savedInstanceState != null && mFragments != null && mTags != null) {

            //            for (int i = 0; i < mTags.length; i++) {
            //                mFragments[i] = fragmentManager.findFragmentByTag(mTags[i]);
            //            }
            XLog.Companion.d("有缓存savedInstanceState");
            String currentFragment = savedInstanceState.getString("CurrentFragment");
            // boolean one = false, two = false, three = false;
            if (currentFragment != null) {
                for (int i = 0; i < mTags.length; i++) {
                    if (currentFragment.equals(mTags[i])) {
                        lastPosition = i;
                    }
                }
            }
            //隐藏无关的fragment
            for (int i = 0; i < mFragments.length; i++) {
                if (lastPosition != i) {
                    transaction.hide(mFragments[i]);
                }
            }
            transaction.show(mFragments[lastPosition]);
        } else {
            // transaction.replace(R.id.fragment_container, mFragments[0], mTags[0]);
            lastPosition = 0;
            transaction.show(mFragments[lastPosition]);
            //隐藏无关的fragment
            for (int i = 0; i < mFragments.length; i++) {
                if (lastPosition != i) {
                    transaction.hide(mFragments[i]);
                }
            }

        }

        transaction.commitAllowingStateLoss();
    }


    public void switchFragment(FragmentActivity activity, int position) {
        if (position != lastPosition) {
            transaction = activity.getSupportFragmentManager().beginTransaction();
            //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.hide(mFragments[lastPosition]);

            if (fragmentManager.findFragmentByTag(mTags[position]) == null) {
                transaction.add(R.id.fragment_container, mFragments[position], mTags[position]);
            }
            transaction.show(mFragments[position]);
            transaction.commitAllowingStateLoss();
            lastPosition = position;
        }
    }

    public void clearFragment(FragmentActivity activity) {
        if (mFragments != null) {
            lastPosition = 0;
            transaction = activity.getSupportFragmentManager().beginTransaction();
            for (int i = 0; i < mFragments.length; i++) {
                transaction.remove(mFragments[i]);
            }
            transaction.commitAllowingStateLoss();
        }
    }

    public void destroyFragment() {
        for (int i = 0; i < mFragments.length; i++) {
            mFragments[i] = null;
        }
        transaction = null;
        fragmentManager = null;
        mFragments = null;
    }

    public int getCurrentPosition() {
        return lastPosition;
    }

    public String getCurrentFragmentByTag() {
        return mTags[lastPosition];
    }

}
