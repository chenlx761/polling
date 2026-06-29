package com.chenming.common.base.databindrv

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter


class TextColorRv {
    companion object {
        @BindingAdapter("myTextColor", requireAll = false)
        @JvmStatic
        fun loadRes(view: TextView, res: Int?) {
            if (res == null)
                return
            view.setTextColor(view.context.resources.getColor(res))
        }


    }
}