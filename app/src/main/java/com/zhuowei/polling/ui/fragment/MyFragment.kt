package com.zhuowei.polling.ui.fragment

import androidx.databinding.ViewDataBinding
import com.chenming.common.base.BaseFragment
import com.chenming.common.base.empty.EmptyViewModel
import com.zhuowei.polling.R

class MyFragment : BaseFragment<EmptyViewModel, ViewDataBinding>() {

    companion object {
        fun newInstance(): MyFragment {
            return MyFragment()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my
    }

    override fun initData() {
    }

    override fun initViewModel(): EmptyViewModel {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
    }

    override fun setListener() {
    }
}