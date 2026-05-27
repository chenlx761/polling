package com.zhuowei.polling.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.chenming.common.utils.TimeUtil
import com.chenming.httprequest.XLog
import com.chenming.httprequest.http.RetrofitUtil
import com.chenming.httprequest.http.bean.BaseBean
import com.chenming.httprequest.http.listener.OnHttpCallBack
import com.zhuowei.polling.MyApplication
import com.zhuowei.polling.R
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.dialog.UploadFileProgressDialog
import com.zhuowei.polling.location.LocationResult
import com.zhuowei.polling.utils.ImageWatermarkUtils
import com.zhuowei.polling.utils.SpManager
import java.io.File
import java.io.FileOutputStream

object UploadFileManager {
    private var mLocationResult: LocationResult? = null
    private var mBean: TicketListBean.RowsDTO? = null

    interface OnUploadAllCallBack {
        fun onAllSuccessful(results: List<UploadFileResult>)
        fun onError(errorMsg: String, failedPaths: List<String>)
    }

    /** 图片压缩配置 */
    object CompressConfig {
        /** 最大宽度（像素），超过则等比缩放 */
        const val MAX_WIDTH = 1080

        /** 最大高度（像素），超过则等比缩放 */
        const val MAX_HEIGHT = 1920

        /** 压缩质量 0-100，数值越小文件越小、画质越低 */
        const val QUALITY = 80

        /** 压缩后输出格式 */
        val FORMAT: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG

        /** 压缩后文件后缀 */
        const val SUFFIX = ".jpg"

        /** 支持的图片扩展名（小写） */
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp")
    }

    /**
     * 判断文件是否为图片（根据扩展名）
     */
    private fun isImageFile(path: String): Boolean {
        val extension = path.substringAfterLast('.', "").lowercase()
        return extension in CompressConfig.IMAGE_EXTENSIONS
    }

    /**
     * 压缩图片文件，返回压缩后的文件路径。
     * 如果不是图片或压缩失败，返回原始路径。
     */
    private fun compressImage(path: String): String {
        if (!isImageFile(path)) return path

        try {
            val originalFile = File(path)
            if (!originalFile.exists()) return path

            // 第一步：只解码尺寸，判断是否需要缩放
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight

            // 如果无法获取尺寸，返回原始路径
            if (srcWidth <= 0 || srcHeight <= 0) return path

            // 计算采样率（inSampleSize 必须是 2 的幂）
            var inSampleSize = 1
            while (srcWidth / inSampleSize > CompressConfig.MAX_WIDTH * 2 ||
                srcHeight / inSampleSize > CompressConfig.MAX_HEIGHT * 2
            ) {
                inSampleSize *= 2
            }

            // 第二步：用采样率解码得到缩小后的 Bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val bitmap = BitmapFactory.decodeFile(path, decodeOptions) ?: return path

            // 第三步：如果解码后的尺寸仍然超过最大限制，再做精确缩放
            var finalBitmap = bitmap
            if (bitmap.width > CompressConfig.MAX_WIDTH || bitmap.height > CompressConfig.MAX_HEIGHT) {
                val scale = minOf(
                    CompressConfig.MAX_WIDTH.toFloat() / bitmap.width,
                    CompressConfig.MAX_HEIGHT.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                finalBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                if (finalBitmap !== bitmap) {
                    bitmap.recycle()
                }
            }

            // 第四步：写入临时文件
            val tempDir = File(originalFile.parent, "compress_temp")
            if (!tempDir.exists()) tempDir.mkdirs()
            val tempFile = File(
                tempDir,
                "compressed_${System.currentTimeMillis()}_${originalFile.nameWithoutExtension}${CompressConfig.SUFFIX}"
            )

            FileOutputStream(tempFile).use { fos ->
                finalBitmap.compress(CompressConfig.FORMAT, CompressConfig.QUALITY, fos)
                fos.flush()
            }

            finalBitmap.recycle()

            XLog.e("compressImage: ${originalFile.name} original=${originalFile.length() / 1024}KB, compressed=${tempFile.length() / 1024}KB")

            // 如果压缩后反而更大，则使用原文件
            if (tempFile.length() >= originalFile.length()) {
                tempFile.delete()
                return path
            }

            return tempFile.absolutePath
        } catch (e: Exception) {
            XLog.e("compressImage error: $path, ${e.message}")
            return path
        }
    }

    /**
     * 清理压缩产生的临时文件
     */
    private fun cleanCompressTempFiles(paths: List<String>) {
        paths.forEach { path ->
            if (path.contains("compress_temp")) {
                try {
                    if (!path.startsWith("http"))
                        File(path).delete()
                } catch (e: Exception) {
                    XLog.e("cleanCompressTempFiles error: ${e.message}")
                }
            }
        }
    }

    /**
     * 上传文件并显示进度弹窗
     * @param context 上下文
     * @param paths 文件路径列表
     * @param callBack 上传完成回调
     */
    fun uploadFileWithProgress(
        context: Context,
        paths: List<String>,
        locationResult: LocationResult?,
        bean: TicketListBean.RowsDTO?,
        callBack: OnUploadAllCallBack
    ) {
        if (paths.isEmpty()) {
            callBack.onAllSuccessful(emptyList())
            return
        }
        this.mLocationResult = locationResult
        this.mBean = bean
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
        val compressedPaths = mutableListOf<String>()
        uploadFileInternal(
            paths,
            0,
            results,
            failedPaths,
            callBack,
            progressDialog,
            compressedPaths
        )
    }

    private fun getWaterMark(): String {
        var currentTime = TimeUtil.getCurrentTime()
        if (mLocationResult != null) {
            currentTime =
                MyApplication.getInstance()
                    .getString(R.string.take_photo_time) + ":" +  currentTime + "\n" +
                        MyApplication.getInstance()
                            .getString(R.string.location_address) + ":" +   mLocationResult!!.address + "\n" +
                        MyApplication.getInstance()
                            .getString(R.string.address) + ":" + mBean!!.userAddress + "\n" +
                        MyApplication.getInstance()
                            .getString(R.string.user_name_label) + ":" + mBean!!.userName + "\n" +
                        MyApplication.getInstance()
                            .getString(R.string.take_account) + ":" + SpManager.getUserName()

        }
        return currentTime;
    }

    private fun uploadFileInternal(
        paths: List<String>,
        index: Int,
        results: MutableList<UploadFileResult>,
        failedPaths: MutableList<String>,
        callBack: OnUploadAllCallBack,
        progressDialog: UploadFileProgressDialog?,
        compressedPaths: MutableList<String>
    ) {
        if (index >= paths.size) {
            // 所有文件上传完毕，清理临时压缩文件
            cleanCompressTempFiles(compressedPaths)

            if (failedPaths.isEmpty()) {
                callBack.onAllSuccessful(results)
            } else {
                callBack.onError("部分文件上传失败", failedPaths)
            }
            return
        }

        // 更新进度弹窗
        progressDialog?.updateProgress(index + 1, paths.size)

        if (paths[index].startsWith("http")) {
            val uploadFileResult = UploadFileResult()
            uploadFileResult.filePath = paths[index].replace(HttpConstants.BASE_URL, "")
            uploadFileResult.url = paths[index]
            results.add(uploadFileResult)
            // 上传下一个
            uploadFileInternal(
                paths,
                index + 1,
                results,
                failedPaths,
                callBack,
                progressDialog,
                compressedPaths
            )
            return
        }

        val watermarkToCache = ImageWatermarkUtils.watermarkToCache(
            MyApplication.getInstance(), paths[index], getWaterMark(),
        )
        var mPath = "";
        if (!TextUtils.isEmpty(watermarkToCache)) {
            mPath = watermarkToCache!!
        } else {

            mPath = paths[index]
        }
        // 压缩图片
        val uploadPath = compressImage(mPath)
        if (uploadPath != paths[index]) {
            compressedPaths.add(uploadPath)
        }

        RetrofitUtil.Builder(HttpConstants.POST_FILE)
            .setFile(uploadPath)
            .build()
            .postFile(
                UploadFileResult::class.java,
                object : OnHttpCallBack<BaseBean<UploadFileResult>> {
                    override fun onSuccessful(t: BaseBean<UploadFileResult>?) {
                        XLog.e("uploadFile onSuccessful:${t!!.data.url}")
                        t?.data?.let { results.add(it) }
                        // 上传下一个
                        uploadFileInternal(
                            paths,
                            index + 1,
                            results,
                            failedPaths,
                            callBack,
                            progressDialog,
                            compressedPaths
                        )
                    }

                    override fun onDataError(
                        errorMsg: String?,
                        t: BaseBean<UploadFileResult>?
                    ) {
                        XLog.e("uploadFile onDataError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(
                            paths,
                            index + 1,
                            results,
                            failedPaths,
                            callBack,
                            progressDialog,
                            compressedPaths
                        )
                    }

                    override fun onRequestError(
                        errorMsg: String?,
                        throwable: Throwable?
                    ) {
                        XLog.e("uploadFile onRequestError: ${paths[index]}, errorMsg: $errorMsg")
                        failedPaths.add(paths[index])
                        // 继续上传下一个
                        uploadFileInternal(
                            paths,
                            index + 1,
                            results,
                            failedPaths,
                            callBack,
                            progressDialog,
                            compressedPaths
                        )
                    }
                })
    }
}
