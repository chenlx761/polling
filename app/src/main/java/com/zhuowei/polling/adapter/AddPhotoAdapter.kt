package com.zhuowei.polling.adapter

import android.content.Context
import androidx.databinding.ObservableArrayList
import com.example.hudundemo.base.NormalAdapter
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.AdapterAddPhotoBinding

class AddPhotoAdapter(
    context: Context,
    datas: ObservableArrayList<String>,
    resId: Int = R.layout.adapter_add_photo
) : NormalAdapter<String, AdapterAddPhotoBinding>(context, resId, datas) {


    override fun onBindOtherViewHolder(
        holder: AdapterAddPhotoBinding, position: Int, adapterPosition: Int
    ) {
        super.onBindOtherViewHolder(holder, position, adapterPosition)


        holder.cvItem.setOnClickListener {
            mOnItemClickListener?.onClick(position, mDatas[position], it)
        }
    }


}