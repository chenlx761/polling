package com.zhuowei.polling.manager

import com.chenming.httprequest.XLog
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.constants.HttpConstants

object UploadFileManager {

    interface OnUploadAllCallBack {
        fun onAllSuccessful(results: List<UploadFileResult>)
        fun onError(errorMsg: String, failedPaths: List<String>)
    }

    fun uploadFile(paths: List<String>, callBack: OnUploadAllCallBack) {
        val results = mutableListOf<UploadFileResult>()
        val failedPaths = mutableListOf<String>()
        uploadFileInternal(paths, 0, results, failedPaths, callBack)
    }

    private fun uploadFileInternal(
        paths: List<String>,
        index: Int,
        results: MutableList<UploadFileResult>,
        failedPaths: MutableList<String>,
        callBack: OnUploadAllCallBack
    ) {
        if (index >= paths.size) {
            // 所有文件上传完毕
            if (failedPaths.isEmpty()) {
                callBack.onAllSuccessful(results)
            } else {
                callBack.onError("部分文件上传失败", failedPaths)
            }
            return
        }

        RetrofitUtil.Builder(HttpConstants.POST_FILE)
            .setFile(paths[index])
            .build()
            .postFile(
                UploadFileResult::class.java,
                object : OnHttpCallBack<BaseBean<UploadFileResult>> {
                    override fun onSuccessful(t: BaseBean<UploadFileResult>?) {
                        XLog.e("uploadFile onSuccessful:${t!!.data.url}")
                        t?.data?.let { results.add(it) }
                        // 上传下一个
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack)
                    }

                    override fun onDataError(
                        errorMsg: String?,
                        t: BaseBean<UploadFileResult>?
                    ) {
                        XLog.e("uploadFile onDataError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack)
                    }

                    override fun onRequestError(
                        errorMsg: String?,
                        throwable: Throwable?
                    ) {
                        XLog.e("uploadFile onRequestError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack)
                    }
                })
    }
}
