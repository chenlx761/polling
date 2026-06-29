package com.chenming.common.listener

import android.view.View

/**
 * Created by qixianWu on 2021/1/3.
 */
interface OnItemLongClickListener {
    fun onClick(position: Int, i: Any, view: View):Boolean
}