package com.zhuowei.polling.ui.fragment

import com.chenming.common.base.BaseFragment
import com.chenming.httprequest.XLog
import com.zhuowei.polling.R
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.FragmentTicketBinding
import com.zhuowei.polling.location.BaiDuLocationManager
import com.zhuowei.polling.location.LocationCallBack
import com.zhuowei.polling.location.LocationResult

class TicketFragment : BaseFragment<MainVm, FragmentTicketBinding>() {

    companion object {
        fun newInstance(): TicketFragment {
            return TicketFragment()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ticket
    }

    override fun initData() {
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun setData() {
    }

    override fun setListener() {





        mBinding!!.tvGetTsid.setOnClickListener {
            mViewModel!!.getTsId("sysadmin", "123456")
        }


        mBinding!!.tvInit.setOnClickListener {
            // 初始化崩溃捕获
            throw Exception("哈哈哈 测试")
        }

        mBinding!!.tvGetInfo.setOnClickListener {

            mViewModel!!.testGetInfo()
        }

        mBinding!!.tvStartLocation.setOnClickListener {

            BaiDuLocationManager.instance.requestLocation(requireActivity(), object : LocationCallBack {
                override fun onLocationSuccess(result: LocationResult) {

                    XLog.e("定位成功", result.toString())
                }

                override fun onLocationError(errorCode: Int, errorMessage: String) {
                    XLog.e("定位失败", "$errorCode $errorMessage")
                }

            })
        }

    }
}