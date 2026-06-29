package com.chenming.common.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.chenming.common.R;
import com.chenming.common.manager.MyFragmentManager;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class MainTabView extends LinearLayout {
    private Context mContext;
    private CommonTabLayout mMainTab;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTags = new ArrayList<>();
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private MyFragmentManager mFragmentManager = null;
    private FragmentActivity mFragmentActivity;

    public MainTabView(Context context) {
        super(context);
        initialize(context, null);
    }


    public MainTabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MainTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }


    public void onSaveInstance(Bundle savedInstanceState) {
        savedInstanceState.putString("CurrentFragment", mFragmentManager.getCurrentFragmentByTag());
    }

    public void init(FragmentActivity activity,
                     Bundle savedInstanceState,
                     List<Fragment> fragments,
                     List<String> tags,
                     List<CustomTabEntity> tabEntities) {
        mFragmentActivity = activity;
        mFragments.clear();
        mTags.clear();
        mTabEntities.clear();

        mFragments.addAll(fragments);
        mTags.addAll(tags);
        mTabEntities.addAll(tabEntities);

        mFragmentManager = new MyFragmentManager(activity,
                savedInstanceState,
                mFragments.toArray(new Fragment[0]),
                mTags.toArray(new String[0]));

        mMainTab.setTabData(mTabEntities);

        if (mFragmentManager.getCurrentPosition() != 0) {
            mMainTab.setCurrentTab(mFragmentManager.getCurrentPosition());
        }
    }

    private void initialize(Context context, AttributeSet attrs) {
        mContext = context;
        initView(attrs);
        setListener();
    }

    private void setListener() {
        mMainTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mFragmentManager.switchFragment(mFragmentActivity, position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    private void initView(AttributeSet attrs) {
        LayoutInflater.from(mContext).inflate(R.layout.view_main_tab, this);
        mMainTab = findViewById(R.id.main_tab);
    }
}
