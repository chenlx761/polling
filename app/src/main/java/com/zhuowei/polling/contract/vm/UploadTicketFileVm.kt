package com.zhuowei.polling.contract.vm

import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.UploadTicketFileContract
import com.zhuowei.polling.contract.model.UploadTicketFileModel
import java.io.File

class UploadTicketFileVm : BaseViewModel<UploadTicketFileContract.IUploadTicketFileModel>(),
    UploadTicketFileContract.IUploadTicketFileVm {
    override fun getModel(): UploadTicketFileContract.IUploadTicketFileModel? {
        return UploadTicketFileModel()

    }

    override fun uploadFile(file: File?) {
        mModel.uploadFile(
            file,
            object : BaseCallBack<BaseBean<in Any>>(HttpConstants.UPLOAD_TICKET_FILE_URL) {
                override fun onSuccessful(t: BaseBean<in Any>?) {

                }

            })
    }
}