package com.zhuowei.polling.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.chenming.common.base.BaseFragment
import com.chenming.common.listener.OnActivityResultListener
import com.chenming.common.listener.OnItemClickListener
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.MainOrderAdapter
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.contract.vm.MainVm
import com.zhuowei.polling.databinding.FragmentTicketBinding
import com.zhuowei.polling.ui.activitys.ticket.TicketDetailActivity

class TicketFragment : BaseFragment<MainVm, FragmentTicketBinding>() {

    private var mMainOrderAdapter: MainOrderAdapter? = null
    private var mTicketStaus: String = "0"

    companion object {
        val Ticket_Staus_Key = "ticketStaus"
        fun newInstance(ticketStaus: String): TicketFragment {
            //写入参数
            val bundle = Bundle()
            bundle.putString(Ticket_Staus_Key, ticketStaus)
            return TicketFragment().apply {
                arguments = bundle
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ticket
    }

    override fun initData() {
        //获取参数
        mTicketStaus = arguments?.getString(Ticket_Staus_Key)!!
    }

    override fun initViewModel(): MainVm {
        return createViewModel(MainVm::class.java)
    }

    override fun setData() {
        mMainOrderAdapter = MainOrderAdapter(requireActivity(), mViewModel!!.mTicketDatas)

        mBinding!!.rvTicket.apply {
            adapter = mMainOrderAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    override fun setListener() {

        mViewModel!!.mStopFlash.observe(this) {
            mBinding!!.srlFlash.finishRefresh()
        }

        mViewModel!!.mStopLoadMore.observe(this) {
            mBinding!!.srlFlash.finishLoadMore()
        }

        mMainOrderAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {


                TicketDetailActivity.newIntent(
                    getActivityLauncher(object : OnActivityResultListener {
                        override fun onActivityResult(result: ActivityResult?) {
                            if (result != null && result.resultCode == Activity.RESULT_OK) {
                                mViewModel!!.flashTicketList(mTicketStaus, "", "")
                            }
                        }

                    })!!,
                    requireActivity(), i as TicketListBean.RowsDTO
                )
            }

        })


        mBinding!!.srlFlash.setOnRefreshListener {
            mViewModel!!.flashTicketList(mTicketStaus, "", "")
        }

        mBinding!!.srlFlash.setOnLoadMoreListener {
            mViewModel!!.loadMoreTicketList(mTicketStaus, "", "")
        }
        mBinding!!.srlFlash.postDelayed({
            //vpn没有启动完 居然就让我进来这个界面了!!!
            mViewModel!!.flashTicketList(mTicketStaus, "", "")
        }, 500)


    }


}