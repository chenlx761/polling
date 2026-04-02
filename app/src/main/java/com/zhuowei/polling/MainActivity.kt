package com.zhuowei.polling

import android.content.Context
import android.content.Intent
import cn.tigersec.android.sdk.ITVAPI
import cn.tigersec.android.sdk.utils.ActivationType
import cn.tigersec.android.sdk.utils.ErrorCode
import com.chenming.common.base.BaseActivity
import com.chenming.httprequest.XLog
import com.zhuowei.hudun.HuDunApplication
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.hudun.callback.InitFinishCallBack
import com.zhuowei.hudun.callback.LoginFinishCallBack
import com.zhuowei.hudun.callback.PrepareVpnCallBack
import com.zhuowei.hudun.callback.StartCallBack
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.ActivityMainBinding


class MainActivity : BaseActivity<MainVm, ActivityMainBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun setListener() {

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
                        HuDunManager.instance.prepareVpn(this@MainActivity,
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
