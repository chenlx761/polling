package com.chenming.common.base.widget
import android.view.View
import android.view.Window


/**
 * Created by wuqx14 on 2021/1/4.
 */
interface DialogLayoutCallback {

    fun bindTheme(): Int

    fun bindLayout(): Int

    fun initView(dialog: BaseDialogFragment?, contentView: View?)

    fun setWindowStyle(window: Window?)

    fun onCancel(dialog: BaseDialogFragment?)

    fun onDismiss(dialog: BaseDialogFragment?)
}