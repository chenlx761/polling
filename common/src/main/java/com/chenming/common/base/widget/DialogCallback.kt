package com.chenming.common.base.widget
import android.app.Activity
import android.app.Dialog
import android.view.Window
import androidx.annotation.NonNull


/**
 * Created by wuqx14 on 2021/1/4.
 */
interface DialogCallback {
    @NonNull
    fun bindDialog(activity: Activity?): Dialog?

    fun setWindowStyle(window: Window?)
}