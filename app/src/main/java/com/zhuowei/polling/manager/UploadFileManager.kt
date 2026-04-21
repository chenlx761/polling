package com.zhuowei.polling.manager

import android.content.Context
import com.chenming.httprequest.XLog
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.dialog.UploadFileProgressDialog

object UploadFileManager {

    interface OnUploadAllCallBack {
        fun onAllSuccessful(results: List<UploadFileResult>)
        fun onError(errorMsg: String, failedPaths: List<String>)
    }

    /**
     * 上传文件并显示进度弹窗
     * @param context  上下文
     * @param paths    文件路径列表
     * @param callBack 上传完成回调
     */
    fun uploadFileWithProgress(
        context: Context,
        paths: List<String>,
        callBack: OnUploadAllCallBack
    ) {
        if (paths.isEmpty()) {
            callBack.onAllSuccessful(emptyList())
            return
        }

        val progressDialog = UploadFileProgressDialog(context)
        progressDialog.show()

        uploadFile(paths, object : OnUploadAllCallBack {
            override fun onAllSuccessful(results: List<UploadFileResult>) {
                progressDialog.showSuccess()
                // 延迟关闭弹窗，让用户看到完成状态
                progressDialog.window?.decorView?.postDelayed({
                    progressDialog.dismiss()
                    callBack.onAllSuccessful(results)
                }, 500)
            }

            override fun onError(errorMsg: String, failedPaths: List<String>) {
                val successCount = paths.size - failedPaths.size
                progressDialog.showCompleted(successCount, failedPaths.size)
                // 延迟关闭弹窗，让用户看到完成状态
                progressDialog.window?.decorView?.postDelayed({
                    progressDialog.dismiss()
                    callBack.onError(errorMsg, failedPaths)
                }, 500)
            }
        }, progressDialog)
    }

    fun uploadFile(paths: List<String>, callBack: OnUploadAllCallBack) {
        uploadFile(paths, callBack, null)
    }

    private fun uploadFile(
        paths: List<String>,
        callBack: OnUploadAllCallBack,
        progressDialog: UploadFileProgressDialog?
    ) {
        val results = mutableListOf<UploadFileResult>()
        val failedPaths = mutableListOf<String>()
        uploadFileInternal(paths, 0, results, failedPaths, callBack, progressDialog)
    }

    private fun uploadFileInternal(
        paths: List<String>,
        index: Int,
        results: MutableList<UploadFileResult>,
        failedPaths: MutableList<String>,
        callBack: OnUploadAllCallBack,
        progressDialog: UploadFileProgressDialog?
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

        // 更新进度弹窗
        progressDialog?.updateProgress(index + 1, paths.size)

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
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack, progressDialog)
                    }

                    override fun onDataError(
                        errorMsg: String?,
                        t: BaseBean<UploadFileResult>?
                    ) {
                        XLog.e("uploadFile onDataError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack, progressDialog)
                    }

                    override fun onRequestError(
                        errorMsg: String?,
                        throwable: Throwable?
                    ) {
                        XLog.e("uploadFile onRequestError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(paths, index + 1, results, failedPaths, callBack, progressDialog)
                    }
                })
    }
}
