package com.zhuowei.polling.ui.activitys.ticket

import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.contract.vm.UploadTicketFileVm
import com.zhuowei.polling.databinding.ActivityUploadTicketFileBinding

class UploadTicketFileActivity : MyBaseActivity<UploadTicketFileVm, ActivityUploadTicketFileBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_upload_ticket_file
    }

    override fun setListener() {
    }

    override fun initData() {
    }

    override fun initViewModel(): UploadTicketFileVm {
        return createViewModel(UploadTicketFileVm::class.java)
    }

    override fun setData() {
    }
}