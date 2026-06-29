package com.chenming.common.listener

import android.view.View

/**
 * Created by qixianWu on 2021/1/3.
 */
interface OnItemChangeListener {
    fun onClick(position: Int, i: Any, view: View)
}