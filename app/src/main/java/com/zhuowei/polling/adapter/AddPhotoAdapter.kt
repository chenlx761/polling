package com.zhuowei.polling.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableArrayList
import com.chenming.common.utils.ImageLoaderUtil
import com.example.hudundemo.base.NormalAdapter
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.AdapterAddPhotoBinding

class AddPhotoAdapter(
    context: Context,
    datas: ObservableArrayList<String>,
    private val maxCount: Int = 9,
    resId: Int = R.layout.adapter_add_photo
) : NormalAdapter<String, AdapterAddPhotoBinding>(context, resId, datas) {

    private var onDeleteClickListener: ((Int) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (Int) -> Unit) {
        onDeleteClickListener = listener
    }


    override fun onBindOtherViewHolder(
        holder: AdapterAddPhotoBinding, position: Int, adapterPosition: Int
    ) {
        super.onBindOtherViewHolder(holder, position, adapterPosition)

        if (!TextUtils.isEmpty(mDatas[position])) {
            holder.ivPhoto.visibility = View.VISIBLE
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivAdd.visibility = View.GONE


            ImageLoaderUtil.getInstance().loadImg(holder.ivPhoto,mDatas[position])
            holder.ivDelete.setOnClickListener {
                onDeleteClickListener?.invoke(position)
            }

        } else {
            holder.ivPhoto.visibility = View.GONE
            holder.ivDelete.visibility = View.GONE
            holder.ivAdd.visibility = View.VISIBLE


        }
        holder.cvItem.setOnClickListener {
            mOnItemClickListener?.onClick(position, mDatas[position], it)
        }
    }
}