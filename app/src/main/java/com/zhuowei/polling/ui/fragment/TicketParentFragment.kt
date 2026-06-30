package com.zhuowei.polling.ui.fragment

import com.chenming.common.base.BaseFragment
import com.chenming.common.base.empty.EmptyViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.TicketPagerAdapter
import com.zhuowei.polling.databinding.FragmentTicketParentBinding
import com.zhuowei.polling.ui.activitys.login.LoginActivity

class TicketParentFragment : BaseFragment<EmptyViewModel, FragmentTicketParentBinding>() {
    private val tabTitles = ArrayList<String>()


    companion object {
        fun newInstance(): TicketParentFragment {
            return TicketParentFragment()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ticket_parent
    }

    override fun initData() {

        tabTitles.add(getString(R.string.not_work))
        tabTitles.add(getString(R.string.finish_work))
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
    }



    override fun setListener() {
        val adapter = TicketPagerAdapter(this)
        mBinding!!.viewPager.adapter = adapter

        TabLayoutMediator(mBinding!!.tabLayout, mBinding!!.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}
