package com.chenming.common.dialog

import android.content.Context
import com.chenming.common.R
import com.chenming.common.base.widget.BaseDialog

class CommonLoadingDialog(
    context: Context, val onCancelListener: Runnable?,
    val canCancel: Boolean = true
) :
    BaseDialog(context, R.style.CommonLoadingDialogStyle) {
    override fun initEvent() {
    }

    override fun setData() {
    }

    override fun initView() {
        window!!.setDimAmount(0f);
    }

    override fun setDialogLayout(): Int {
        return R.layout.common_dialog_loading
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