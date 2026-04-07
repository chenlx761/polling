package com.zhuowei.polling

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import cn.tigersec.android.sdk.utils.ErrorCode
import com.chenming.common.base.BaseActivity
import com.chenming.common.beans.DiscountTab
import com.chenming.common.manager.MyFragmentManager
import com.chenming.common.utils.PermissionXUtil
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.tencent.bugly.crashreport.CrashReport
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.hudun.callback.InitFinishCallBack
import com.zhuowei.hudun.callback.LoginFinishCallBack
import com.zhuowei.hudun.callback.PrepareVpnCallBack
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.ActivityMainBinding
import com.zhuowei.polling.ui.fragment.MyFragment
import com.zhuowei.polling.ui.fragment.TicketFragment
import com.zhuowei.polling.utils.AppCrashHandleCallback


class MainActivity : BaseActivity<MainVm, ActivityMainBinding>() {

    private var mFragmentManager: MyFragmentManager? = null
    private var mFragments: Array<Fragment>? = null
    private var mTags: Array<String>? = null
    private var mTitles: Array<String>? = null
    private val mTabEntities = ArrayList<CustomTabEntity>()

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

//        mBinding!!.tvGetTsid.setOnClickListener {
//            mViewModel.getTsId("sysadmin", "123456")
//        }
//
//
//        mBinding!!.tvInit.setOnClickListener {
//            // 初始化崩溃捕获
//            throw Exception("哈哈哈 测试")
//        }
//
//        mBinding!!.tvGetInfo.setOnClickListener {
//
//            mViewModel.testGetInfo()
//        }
//
//        mBinding!!.tvStartLocation.setOnClickListener {
//
//            BaiDuLocationManager.instance.requestLocation(this, object : LocationCallBack {
//                override fun onLocationSuccess(result: LocationResult) {
//
//                    XLog.e("定位成功", result.toString())
//                }
//
//                override fun onLocationError(errorCode: Int, errorMessage: String) {
//                    XLog.e("定位失败", "$errorCode $errorMessage")
//                }
//
//            })
//        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        //使用show和hide控制显示和隐藏界面重叠问题；
        if (mFragmentManager != null)
            outState.putString("CurrentFragment", mFragmentManager!!.getCurrentFragmentByTag());
        super.onSaveInstanceState(outState);


    }


    private fun initTab() {
        mTitles = arrayOf(getString(R.string.main_ticket), getString(R.string.main_my))
        val mTicketTab = DiscountTab(mTitles!![0], R.mipmap.ticket_select, R.mipmap.ticket_normal)
        val mMyTab = DiscountTab(mTitles!![1], R.mipmap.my_select, R.mipmap.my_normal)

        mTabEntities.add(mTicketTab)
        mTabEntities.add(mMyTab)
        mBinding!!.mainTab.setTabData(mTabEntities)

    }

    private fun initFragmentAndTag() {
        val ticketFragment = TicketFragment.newInstance()
        val myFragment = MyFragment.newInstance()

        mFragments = arrayOf(
            ticketFragment, myFragment
        )
        mTags = arrayOf("TICKET", "MY")

    }


    /**
     * 用于判断VPN服务权限有没有申请成功如果申请成功则调用start接口
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ErrorCode.REQUEST_START_VPN) {
            if (resultCode == RESULT_OK) {
                HuDunManager.instance.start(null)
            } else {

            }
        }
    }


    override fun setObserveListener() {
        mViewModel.mLoginResult.observe(this) {
            if (it != null) {
                HuDunManager.instance.login(it.tsid, object : LoginFinishCallBack {
                    override fun onLoginFinish() {
                        HuDunManager.instance.prepareVpn(
                            this@MainActivity,
                            object : PrepareVpnCallBack {
                                override fun onPrepareIntentNull() {
                                    onActivityResult(ErrorCode.REQUEST_START_VPN, RESULT_OK, null)
                                }

                            })
                    }

                    override fun onLoginError(errorCode: Int, errorMsg: String?) {
                    }

                })
            }
        }
    }

    override fun initData() {


    }

    override fun initView(savedInstanceState: Bundle?) {
        initTab()
        initFragmentAndTag()
        mFragmentManager =
            MyFragmentManager(this@MainActivity, savedInstanceState, mFragments, mTags)
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun setData() {


        requestPermission()
        HuDunManager.instance.initSDK(object : InitFinishCallBack {
            override fun onInitFinish(hudunBaseUrl: String?) {
                HttpConstants.HD_BASE_URL = hudunBaseUrl
                //虎盾初始化不知道为什么会把我的全部异常拦截覆盖了,只能在这里初始化
                initCrash()
            }

            override fun onInitError(type: Int, errorCode: Int, errorMsg: String?) {
            }

        })
    }

    private fun initCrash() {
        val strategy = CrashReport.UserStrategy(this)
        strategy.setAppReportDelay(20000)
        strategy.setCrashHandleCallback(AppCrashHandleCallback())
        CrashReport.initCrashReport(MyApplication.getInstance(), "37dc0727ad", false, strategy)
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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

}
