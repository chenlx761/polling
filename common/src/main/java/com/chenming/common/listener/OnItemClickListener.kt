package com.chenming.common.listener

import android.view.View

/**
 * Created by qixianWu on 2021/1/3.
 */
interface OnItemClickListener {
    fun onClick(position: Int, i: Any, view: View)
}