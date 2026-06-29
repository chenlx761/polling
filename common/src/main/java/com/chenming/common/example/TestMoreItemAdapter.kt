package com.chenming.common.example

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import com.chenming.common.BR
import com.chenming.common.R
import com.chenming.common.adapter.AutoNotifyMultipleAdapter
import com.chenming.common.adapter.BindingViewHolder
import com.chenming.common.databinding.AdapterTestBinding
import com.chenming.common.databinding.AdapterTestBindingImpl

/**
 * 例子adapter
 */
@Deprecated("例子来的,没有用")
class TestMoreItemAdapter(context: Context, datas: ObservableArrayList<TestBean>) :
    AutoNotifyMultipleAdapter<BindingViewHolder<ViewDataBinding>, TestBean>(context, datas) {
    override fun onCreateOtherViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<ViewDataBinding> {
        if (viewType == 0) {
            val inflate = DataBindingUtil.inflate<AdapterTestBinding>(
                LayoutInflater.from(mContext),
                R.layout.adapter_test, parent, false
            )
            return BindingOneViewHolder(inflate)
        }
        val inflate = DataBindingUtil.inflate<AdapterTestBinding>(
            LayoutInflater.from(mContext),
            R.layout.adapter_test, parent, false
        )
        return BindingTwoViewHolder(inflate)
    }

    override fun getOtherItemViewType(position: Int): Int {
        if (position == 0)
            return 0
        return 1
    }

    override fun onBindOtherViewHolder(holder: BindingViewHolder<ViewDataBinding>, position: Int) {
        if (holder is BindingOneViewHolder<*>) {
            val binding = holder.binding as AdapterTestBinding
            binding.setVariable(BR.item, mDatas[position])
        }
    }


    inner class BindingOneViewHolder<T : ViewDataBinding>(binding: T) :
        BindingViewHolder<ViewDataBinding>(
            binding
        )

    inner class BindingTwoViewHolder<T : ViewDataBinding>(binding: T) :
        BindingViewHolder<ViewDataBinding>(
            binding
        )
}