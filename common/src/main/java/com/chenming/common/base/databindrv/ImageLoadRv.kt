package com.chenming.common.base.databindrv

import android.R
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.BindingAdapter
import com.chenming.common.utils.ImageLoaderUtil


class ImageLoadRv {
    companion object {
        @BindingAdapter("imgRes", requireAll = false)
        @JvmStatic
        fun loadRes(imageView: ImageView, res: Int?) {
            if (res == null)
                return
            imageView.setImageResource(res)
        }

        @BindingAdapter("imgBit", requireAll = false)
        @JvmStatic
        fun loadBit(imageView: ImageView, res: Int?) {
            if (res == null)
                return
            var bitmap = BitmapFactory.decodeResource(imageView.resources, res)
            if (bitmap == null) {
                //BitmapFactory.decodeResource感觉没有适配xml格式
                val bitmapDrawable = imageView.resources.getDrawable(res) as VectorDrawable
                bitmap = bitmapDrawable.toBitmap()
            }
            imageView.setImageBitmap(bitmap)
        }


        @BindingAdapter(
            "radiusImgUrl",
            "errorRes",
            "placeholderRes",
            "radius",
            requireAll = false
        )
        @JvmStatic
        fun loadRadiusUrl(
            imageView: ImageView,
            imgUrl: String?,
            errorRes: Int?,
            placeholderRes: Int?,
            radius: Int = 0
        ) {
            if (imgUrl == null)
                return

            if (errorRes != null && placeholderRes != null) {
                ImageLoaderUtil.getInstance()
                    .loadRoundImg(imageView, imgUrl, placeholderRes, errorRes, radius)
            } else {
                ImageLoaderUtil.getInstance().loadRoundImg(imageView, imgUrl, radius)
            }
        }


        @BindingAdapter(
            "imgUrl",
            "errorRes",
            "placeholderRes",
            requireAll = false
        )
        @JvmStatic
        fun loadImgUrl(
            imageView: ImageView,
            imgUrl: String?,
            errorRes: Int?,
            placeholderRes: Int?
        ) {
            if (imgUrl == null)
                return

            if (errorRes != null && placeholderRes != null) {
                ImageLoaderUtil.getInstance()
                    .loadCenterInsideImg(imageView, imgUrl, placeholderRes, errorRes)
            } else {
                ImageLoaderUtil.getInstance().loadFitCenterImg(imageView, imgUrl)
            }
        }
    }
}