package com.zhuowei.polling

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.chenming.common.base.BaseActivity
import com.chenming.common.base.empty.EmptyViewModel
import com.chenming.common.beans.DiscountTab
import com.chenming.common.manager.MyFragmentManager
import com.chenming.common.utils.PermissionXUtil
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.zhuowei.polling.databinding.ActivityMainBinding
import com.zhuowei.polling.ui.fragment.MainFragment
import com.zhuowei.polling.ui.fragment.MyFragment


class MainActivity : BaseActivity<EmptyViewModel, ActivityMainBinding>() {

    private var mFragmentManager: MyFragmentManager? = null
    private var mFragments: Array<Fragment>? = null
    private var mTags: Array<String>? = null
    private var mTitles: Array<String>? = null
    private val mTabEntities = ArrayList<CustomTabEntity>()


    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getLayoutId(): Int {


        return R.layout.activity_main
    }

    override fun setListener() {
        mBinding!!.mainTab.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {

                mFragmentManager!!.switchFragment(this@MainActivity, position)
            }

            override fun onTabReselect(position: Int) {
            }
        })


    }

    override fun onSaveInstanceState(outState: Bundle) {
        //使用show和hide控制显示和隐藏界面重叠问题；
        if (mFragmentManager != null)
            outState.putString("CurrentFragment", mFragmentManager!!.getCurrentFragmentByTag());
        super.onSaveInstanceState(outState);


    }


    private fun initTab() {
        mTitles = arrayOf(
            getString(R.string.main_first),
            getString(R.string.main_my),
        )
        val mTicketTab = DiscountTab(mTitles!![0], R.mipmap.ticket_select, R.mipmap.ticket_normal)
        val mMyTab = DiscountTab(mTitles!![1], R.mipmap.my_select, R.mipmap.my_normal)

        mTabEntities.add(mTicketTab)
        mTabEntities.add(mMyTab)
        mBinding!!.mainTab.setTabData(mTabEntities)

    }

    private fun initFragmentAndTag() {

        val mainFragment = MainFragment.newInstance()
        val myFragment = MyFragment.newInstance()

        mFragments = arrayOf(
            mainFragment, myFragment
        )
        mTags = arrayOf("TICKET", "MY")

    }


    override fun setObserveListener() {

    }

    override fun initData() {


    }

    override fun initView(savedInstanceState: Bundle?) {
        initTab()
        initFragmentAndTag()
        mFragmentManager =
            MyFragmentManager(this@MainActivity, savedInstanceState, mFragments, mTags)
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {


        requestPermission()

    }

    override fun setStatusBarStyle() {
        setStatusBarThemeBackground(true)
    }


    private fun requestPermission() {
        PermissionXUtil.checkPermission(
            this,
            getString(R.string.permission_tip),
            object : PermissionXUtil.PermissionListener {
                override fun onGranted() {


                }

                override fun onDenied() {

                }

            },
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }


    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
