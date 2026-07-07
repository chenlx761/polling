package com.zhuowei.polling.ui.activitys.test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.utils.ImageLoaderUtil
import com.zhuowei.polling.databinding.ItemFormAttachmentFileBinding
import com.zhuowei.polling.databinding.ItemFormAttachmentImageBinding

class FormAttachmentAdapter(
    private val itemKey: String,
    private val onPreviewClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val attachments = mutableListOf<FormAttachment>()

    fun submitList(data: List<FormAttachment>) {
        attachments.clear()
        attachments.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = attachments.size

    override fun getItemViewType(position: Int): Int {
        return if (itemKey == FormItem.KEY_IMAGE_PICKER) VIEW_TYPE_IMAGE else VIEW_TYPE_FILE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_IMAGE) {
            ImageViewHolder(ItemFormAttachmentImageBinding.inflate(inflater, parent, false))
        } else {
            FileViewHolder(ItemFormAttachmentFileBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = attachments[position]
        if (holder is ImageViewHolder) {
            holder.bind(item)
        } else if (holder is FileViewHolder) {
            holder.bind(item)
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemFormAttachmentImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormAttachment) {
            ImageLoaderUtil.getInstance().loadImg(binding.ivImage, item.path)
            binding.ivImage.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onPreviewClick(currentPosition)
                }
            }
            binding.ivDelete.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(currentPosition)
                }
            }
        }
    }

    inner class FileViewHolder(
        private val binding: ItemFormAttachmentFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormAttachment) {
            binding.tvName.text = item.name
            binding.tvDelete.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(currentPosition)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_FILE = 2
    }
}
