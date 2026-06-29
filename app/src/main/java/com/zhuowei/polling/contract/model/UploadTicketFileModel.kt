package com.zhuowei.polling.contract.model

import com.chenming.common.base.BaseModel
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.contract.UploadTicketFileContract
import java.io.File

class UploadTicketFileModel : BaseModel(), UploadTicketFileContract.IUploadTicketFileModel {
    override fun uploadFile(
        file: File?,
        callBack: OnHttpCallBack<BaseBean<in Any>?>?
    ) {
        addDisposable(
            RetrofitUtil.Builder(HttpConstants.UPLOAD_TICKET_FILE_URL)
                .addPara("file", file)
                .build().postFile(Any::class.java, callBack)
        )
    }
}