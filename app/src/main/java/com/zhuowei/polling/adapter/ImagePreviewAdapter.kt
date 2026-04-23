package com.zhuowei.polling.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chenming.common.utils.ImageLoaderUtil
import com.zhuowei.polling.databinding.AdapterImagePreviewBinding
import java.io.File

class ImagePreviewAdapter(
    private val imagePaths: List<String>
) : RecyclerView.Adapter<ImagePreviewAdapter.ImagePreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewViewHolder {
        val binding = AdapterImagePreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImagePreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagePreviewViewHolder, position: Int) {
        holder.bind(imagePaths[position])
    }

    override fun getItemCount(): Int = imagePaths.size

    inner class ImagePreviewViewHolder(
        private val binding: AdapterImagePreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imagePath: String) {
            val file = File(imagePath)

            if (file.exists()) {
                ImageLoaderUtil.getInstance().loadImg(
                    binding.photoView,
                    imagePath
                )
            } else {
                ImageLoaderUtil.getInstance().loadImg(
                    binding.photoView,
                    imagePath
                )
            }

            binding.photoView.setOnPhotoTapListener { _, _, _ ->
            }
        }
    }
}
