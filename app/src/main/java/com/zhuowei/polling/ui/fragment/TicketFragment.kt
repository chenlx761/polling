package com.zhuowei.polling.ui.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
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
    private var mSearchType: Int = 1 // 0=用户名, 1=地址,2户号
    private var mIsInitializing: Boolean = true

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


        val searchTypes = arrayOf(getString(R.string.user_name_label), getString(R.string.address),getString(R.string.account_number))
        val adapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, searchTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding!!.spSearchType.adapter = adapter
        mBinding!!.spSearchType.setSelection(1) // 默认选中地址
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
                                performSearch()
                            }
                        }

                    })!!,
                    requireActivity(),
                    (i as TicketListBean.RowsDTO).id.toString(),
                    false
                )
            }

        })



        mBinding!!.spSearchType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    mSearchType = position
                    if (!mIsInitializing) {
                        performSearch()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        mBinding!!.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()

                // 隐藏软键盘
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true // 返回 true 表示我们已经处理了该事件
            } else {
                false
            }
        }

        mBinding!!.srlFlash.setOnRefreshListener {
            performSearch()
        }

        mBinding!!.srlFlash.setOnLoadMoreListener {
            performLoadMore()
        }
        mBinding!!.srlFlash.postDelayed({
            mViewModel!!.flashTicketList(mTicketStaus, "", "","")
            mIsInitializing = false
        }, 500)


    }

    private fun performSearch() {
        val keyword = mBinding!!.etSearch.text.toString().trim()
        val account = if (mSearchType == 0) keyword else ""
        val address = if (mSearchType == 1) keyword else ""
        val number = if (mSearchType == 2) keyword else ""
        mViewModel!!.flashTicketList(mTicketStaus, account, address,number)
    }

    private fun performLoadMore() {
        val keyword = mBinding!!.etSearch.text.toString().trim()
        val account = if (mSearchType == 0) keyword else ""
        val address = if (mSearchType == 1) keyword else ""
        val number = if (mSearchType == 2) keyword else ""
        mViewModel!!.loadMoreTicketList(mTicketStaus, account, address,number)
    }


}
