package com.zhuowei.polling.dialog

import android.content.Context
import android.widget.TextView
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseDialog

class LogoutConfirmDialog(
    context: Context,
    private val onConfirm: () -> Unit
) : MyBaseDialog(context) {

    private lateinit var tvCancel: TextView
    private lateinit var tvConfirm: TextView

    override fun initEvent() {
        tvCancel.setOnClickListener {
            dismiss()
        }
        tvConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }

    override fun setData() {
    }

    override fun initView() {
        tvCancel = findViewById(R.id.tvCancel)
        tvConfirm = findViewById(R.id.tvConfirm)
    }

    override fun setDialogLayout(): Int {
        return R.layout.dialog_logout_confirm
    }
}
