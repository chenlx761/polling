package com.zhuowei.polling.dialog

import android.content.Context
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseDialog

class MyCommonLoadingDialog(
    context: Context, val onCancelListener: Runnable?,
    val canCancel: Boolean = true
) :
    MyBaseDialog(context, R.style.LoadingDialogStyle) {
    override fun initEvent() {
    }

    override fun setData() {
    }

    override fun initView() {
        window!!.setDimAmount(0f);
    }

    override fun setDialogLayout(): Int {
        return R.layout.dialog_loading
    }

    override fun setCancelable(): Boolean {
        return canCancel
    }

    override fun cancel() {
        super.cancel()
        onCancelListener?.run()
    }

    override fun dismiss() {
        super.dismiss()

    }
}