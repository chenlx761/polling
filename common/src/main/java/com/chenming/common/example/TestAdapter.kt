package com.chenming.common.example

import android.content.Context
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import com.chenming.common.R
import com.chenming.common.adapter.AutoNotifyAdapter
import com.chenming.common.adapter.BindingViewHolder
import com.chenming.common.BR
import com.chenming.common.databinding.AdapterTestBinding
import com.chenming.common.listener.OnItemClickListener
import com.chenming.httprequest.XLog

/**
 * 例子adapter
 */
@Deprecated("例子来的,没有用")
class TestAdapter(context: Context, resId: Int, datas: ObservableArrayList<TestBean>) :
    AutoNotifyAdapter<BindingViewHolder<ViewDataBinding>, TestBean, AdapterTestBinding>
        (context, resId, datas) {

    constructor(context: Context, datas: ObservableArrayList<TestBean>) :
            this(context, R.layout.adapter_test, datas) {

    }

    override fun onBindOtherViewHolder(
        holder: AdapterTestBinding,
        position: Int,
        adapterPosition: Int
    ) {
        holder.setVariable(BR.item, mDatas[position])
        holder.setVariable(BR.position, position)
        mOnItemClickListener?.let {
            holder.setVariable(BR.listener, it)
        }

    }
}