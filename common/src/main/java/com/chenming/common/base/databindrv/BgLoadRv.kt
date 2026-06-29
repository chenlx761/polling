package com.chenming.common.base.databindrv

import android.view.View
import androidx.databinding.BindingAdapter


class BgLoadRv {
    companion object {
        @BindingAdapter("bgRes", requireAll = false)
        @JvmStatic
        fun loadRes(view: View, res: Int?) {
            if (res == null)
                return
            view.setBackgroundResource(res)
        }

        @BindingAdapter("bgColor", requireAll = false)
        @JvmStatic
        fun loadBit(view: View, color: Int?) {
            if (color == null)
                return

            view.setBackgroundColor(view.resources.getColor(color))
        }

    }
}