package com.zhuowei.polling.dialog

import android.content.Context
import android.widget.EditText
import android.widget.TextView
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseDialog

class TicketUserInfoEditDialog(
    context: Context,
    private val initialData: FormData,
    private val onAreaClick: (TicketUserInfoEditDialog) -> Unit,
    private val onConfirm: (FormData) -> Unit
) : MyBaseDialog(context) {

    data class FormData(
        val areaId: Int?,
        val areaName: String,
        val address: String,
        val userName: String,
        val userNo: String
    )

    private lateinit var etArea: EditText
    private lateinit var etAddress: EditText
    private lateinit var etName: EditText
    private lateinit var etAccount: EditText
    private lateinit var tvCancel: TextView
    private lateinit var tvConfirm: TextView
    private var selectedAreaId: Int? = initialData.areaId

    override fun initEvent() {
        etArea.setOnClickListener { onAreaClick(this) }
        tvCancel.setOnClickListener { dismiss() }
        tvConfirm.setOnClickListener {
            onConfirm(
                FormData(
                    areaId = selectedAreaId,
                    areaName = etArea.text.toString().trim(),
                    address = etAddress.text.toString().trim(),
                    userName = etName.text.toString().trim(),
                    userNo = etAccount.text.toString().trim()
                )
            )
        }
    }

    override fun setData() {
        etArea.setText(initialData.areaName)
        etAddress.setText(initialData.address)
        etName.setText(initialData.userName)
        etAccount.setText(initialData.userNo)
    }

    override fun initView() {
        etArea = findViewById(R.id.etDialogArea)
        etAddress = findViewById(R.id.etDialogAddress)
        etName = findViewById(R.id.etDialogName)
        etAccount = findViewById(R.id.etDialogAccount)
        tvCancel = findViewById(R.id.tvDialogCancel)
        tvConfirm = findViewById(R.id.tvDialogConfirm)
    }

    fun setArea(areaId: Int, areaName: String) {
        selectedAreaId = areaId
        etArea.setText(areaName)
    }

    fun getSelectedAreaId(): Int? = selectedAreaId

    fun setSubmitting(submitting: Boolean) {
        tvConfirm.isEnabled = !submitting
        tvConfirm.alpha = if (submitting) 0.6f else 1f
    }

    override fun setDialogLayout(): Int = R.layout.dialog_ticket_user_info_edit
}
