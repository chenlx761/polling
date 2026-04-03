package com.example.hudundemo.base

import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import com.chenming.common.adapter.AutoNotifyAdapter
import com.chenming.common.adapter.BindingViewHolder
import com.zhuowei.polling.BR

abstract class NormalAdapter<K, BIND : ViewDataBinding>(
    context: Context, resId: Int, datas: ObservableArrayList<K>
) : AutoNotifyAdapter<BindingViewHolder<ViewDataBinding>, K, BIND>(context, resId, datas) {


    override fun onBindOtherViewHolder(
        holder: BIND, position: Int, adapterPosition: Int
    ) {
        holder.setVariable(BR.item, mDatas[position])
        holder.setVariable(BR.position, position)
        mOnItemClickListener?.let {
            holder.setVariable(BR.listener, it)
        }
    }
}