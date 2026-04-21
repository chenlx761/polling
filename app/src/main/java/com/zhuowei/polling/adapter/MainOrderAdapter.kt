package com.zhuowei.polling.adapter

import android.content.Context
import androidx.databinding.ObservableArrayList
import com.example.hudundemo.base.NormalAdapter
import com.zhuowei.polling.R
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.databinding.AdapterMainBinding

class MainOrderAdapter(
    context: Context,
    datas: ObservableArrayList<TicketListBean.RowsDTO>,
    resId: Int = R.layout.adapter_main
) : NormalAdapter<TicketListBean.RowsDTO, AdapterMainBinding>(context, resId, datas) {


    override fun onBindOtherViewHolder(
        holder: AdapterMainBinding, position: Int, adapterPosition: Int
    ) {
        super.onBindOtherViewHolder(holder, position, adapterPosition)
        holder.tvAddress.text = mDatas[position].userAddress
        holder.tvArea.text = mDatas[position].areaCompany
        holder.tvUserId.text = mDatas[position].userNo
        holder.tvUserName.text = mDatas[position].userName

        holder.cvItem.setOnClickListener {
            mOnItemClickListener?.onClick(position, mDatas[position], it)
        }
    }


}