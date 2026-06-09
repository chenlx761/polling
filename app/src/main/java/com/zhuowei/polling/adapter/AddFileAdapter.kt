package com.zhuowei.polling.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.databinding.ObservableArrayList
import com.chenming.common.utils.ImageLoaderUtil
import com.example.hudundemo.base.NormalAdapter
import com.zhuowei.polling.R
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.databinding.AdapterAddPhotoBinding

class AddFileAdapter(
    context: Context,
    datas: ObservableArrayList<UploadFileResult>,
    private val maxCount: Int = 9,
    resId: Int = R.layout.adapter_add_photo,
    private val supportFilePlaceholder: Boolean = false
) : NormalAdapter<UploadFileResult, AdapterAddPhotoBinding>(context, resId, datas) {

    private var onDeleteClickListener: ((Int) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (Int) -> Unit) {
        onDeleteClickListener = listener
    }

    override fun onBindOtherViewHolder(
        holder: AdapterAddPhotoBinding, position: Int, adapterPosition: Int
    ) {
        super.onBindOtherViewHolder(holder, position, adapterPosition)

        val item = mDatas[position]
        if (!TextUtils.isEmpty(item.filePath)) {
            holder.ivPhoto.visibility = View.VISIBLE
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivAdd.visibility = View.GONE

            if (supportFilePlaceholder && !isImageFile(item.filePath)) {
                holder.ivPhoto.setImageResource(R.mipmap.file_normal)
                holder.ivPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
                holder.tvFileName.visibility = View.VISIBLE
                holder.tvFileName.text =item.fileName
            } else {
                holder.ivPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                holder.tvFileName.visibility = View.GONE
                holder.tvFileName.text = ""
                ImageLoaderUtil.getInstance().loadImg(holder.ivPhoto, item.filePath)
            }

            holder.ivDelete.setOnClickListener {
                onDeleteClickListener?.invoke(position)
            }
        } else {
            holder.ivPhoto.visibility = View.GONE
            holder.ivDelete.visibility = View.GONE
            holder.ivAdd.visibility = View.VISIBLE
            holder.tvFileName.visibility = View.GONE
            holder.tvFileName.text = ""
        }

        holder.cvItem.setOnClickListener {
            mOnItemClickListener?.onClick(position, item, it)
        }
    }

    private fun isImageFile(path: String): Boolean {
        if (path.startsWith("http", true)) {
            val lowerPath = path.lowercase()
            return IMAGE_EXTENSIONS.any { lowerPath.contains(".$it") }
        }
        val extension = path.substringAfterLast('.', "").lowercase()
        return extension in IMAGE_EXTENSIONS
    }

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }
}
