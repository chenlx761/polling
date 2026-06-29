package com.zhuowei.polling.contract.vm

import androidx.lifecycle.MutableLiveData
import com.chenming.common.base.BaseViewModel
import com.chenming.httprequest.http.bean.BaseBean
import com.zhuowei.polling.MyApplication
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.R
import com.zhuowei.polling.contract.UploadTicketFileContract
import com.zhuowei.polling.contract.model.UploadTicketFileModel
import java.io.File

class UploadTicketFileVm : BaseViewModel<UploadTicketFileContract.IUploadTicketFileModel>(),
    UploadTicketFileContract.IUploadTicketFileVm {
    val mUploadSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val mUploadError: MutableLiveData<String> = MutableLiveData()

    override fun getModel(): UploadTicketFileContract.IUploadTicketFileModel? {
        return UploadTicketFileModel()

    }

    override fun uploadFile(file: File?) {
        if (file == null || !file.exists()) {
            mUploadError.postValue(
                MyApplication.getInstance().getString(R.string.upload_ticket_file_empty_hint)
            )
            return
        }
        mModel.uploadFile(
            file,
            object : BaseCallBack<BaseBean<in Any>>(HttpConstants.UPLOAD_TICKET_FILE_URL) {
                override fun onSuccessful(t: BaseBean<in Any>?) {
                    mUploadSuccess.postValue(true)
                }

                override fun onDataError(errorMsg: String?, t: BaseBean<in Any>?) {
                    super.onDataError(errorMsg, t)
                    mUploadError.postValue(
                        errorMsg ?: MyApplication.getInstance()
                            .getString(R.string.upload_ticket_file_failed)
                    )
                }

                override fun onRequestError(errorMsg: String?, throwable: Throwable?) {
                    super.onRequestError(errorMsg, throwable)
                    mUploadError.postValue(
                        errorMsg ?: throwable?.message ?: MyApplication.getInstance()
                            .getString(R.string.upload_ticket_file_failed)
                    )
                }

            })
    }
}
