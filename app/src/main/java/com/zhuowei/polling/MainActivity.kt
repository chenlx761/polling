package com.zhuowei.polling

import android.content.Context
import cn.tigersec.android.sdk.ITVAPI
import cn.tigersec.android.sdk.utils.ActivationType
import cn.tigersec.android.sdk.utils.ErrorCode
import com.chenming.common.base.BaseActivity
import com.chenming.httprequest.XLog
import com.zhuowei.hudun.HuDunApplication
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.hudun.callback.InitFinishCallBack
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.ActivityMainBinding


class MainActivity : BaseActivity<MainVm, ActivityMainBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun setListener() {

    }

    override fun initData() {

        mBinding!!.tvGetTsid.setOnClickListener {
            mViewModel.getTsId("sysadmin", "123456")
        }

        mBinding!!.tvGetInfo.setOnClickListener {

        }

        mBinding!!.tvStart.setOnClickListener {


        }
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun setData() {
        HuDunManager.instance.initSDK(object : InitFinishCallBack {
            override fun onInitFinish(hudunBaseUrl: String?) {
                HttpConstants.HD_BASE_URL = hudunBaseUrl
            }

            override fun onInitError(type: Int, errorCode: Int, errorMsg: String?) {
            }

        })
    }
}
