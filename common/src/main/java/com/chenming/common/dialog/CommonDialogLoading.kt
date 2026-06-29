package com.chenming.common.dialog

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.blankj.utilcode.util.BarUtils
import com.chenming.common.R
import com.chenming.common.base.widget.BaseDialogFragment
import com.chenming.common.base.widget.DialogLayoutCallback


/**
 * Created by wuqx14 on 2021/1/4.
 */
class CommonDialogLoading : BaseDialogFragment() {

    fun init(
        context: Context?,
        onCancelListener: Runnable?,
        canCancel: Boolean = true
    ): CommonDialogLoading? {
        super.init(context!!, object : DialogLayoutCallback{
            override fun bindTheme(): Int {
                return R.style.CommonLoadingDialogStyle
            }

            override fun bindLayout(): Int {
                return R.layout.common_dialog_loading
            }

            override fun initView(dialog: BaseDialogFragment?, contentView: View?) {
                isCancelable = canCancel
            }

            override fun setWindowStyle(window: Window?) {
                window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                BarUtils.setStatusBarColor(window, Color.TRANSPARENT)
            }

            override fun onCancel(dialog: BaseDialogFragment?) {
                onCancelListener?.run()
            }

            override fun onDismiss(dialog: BaseDialogFragment?) {}
        })
        return this
    }
}