package com.zhuowei.polling.manager

import com.chenming.httprequest.XLog
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.constants.HttpConstants

object UploadFileManager {

    fun uploadFile(paths: List<String>) {
        RetrofitUtil.Builder(HttpConstants.POST_FILE)
            .setFile(paths[0])
            .build()
            .postFile(
                UploadFileResult::class.java,
                object : OnHttpCallBack<BaseBean<UploadFileResult>> {
                    override fun onSuccessful(t: BaseBean<UploadFileResult>?) {
                        XLog.e("onSuccessful")


                    }

                    override fun onDataError(
                        errorMsg: String?,
                        t: BaseBean<UploadFileResult>?
                    ) {
                        XLog.e("onDataError")
                    }

                    override fun onRequestError(
                        errorMsg: String?,
                        throwable: Throwable?
                    ) {
                        XLog.e("onDataError")
                    }
                })
    }
}
