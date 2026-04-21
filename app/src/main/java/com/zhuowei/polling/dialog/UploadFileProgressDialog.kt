package com.zhuowei.polling.dialog

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseDialog

class UploadFileProgressDialog(context: Context) : MyBaseDialog(context) {

    private lateinit var tvTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

    override fun initEvent() {
    }

    override fun setData() {
    }

    override fun initView() {
        tvTitle = findViewById(R.id.tvTitle)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
    }

    override fun setDialogLayout(): Int {
        return R.layout.dialog_upload_file_progress
    }

    override fun setCancelable(): Boolean {
        return false
    }

    /**
     * 更新上传进度
     * @param current 当前正在上传的文件索引（从1开始）
     * @param total   总文件数
     */
    fun updateProgress(current: Int, total: Int) {
        if (!isShowing) return
        val progress = (current * 100) / total
        tvTitle.text = context.getString(R.string.upload_file_progress_title, current, total)
        progressBar.progress = progress
        tvProgress.text = context.getString(R.string.percent_format, progress)
    }

    /**
     * 显示全部上传成功
     */
    fun showSuccess() {
        if (!isShowing) return
        tvTitle.text = context.getString(R.string.upload_file_success)
        progressBar.progress = 100
        tvProgress.text = context.getString(R.string.percent_format, 100)
    }

    /**
     * 显示上传完成（有失败）
     * @param successCount 成功数量
     * @param failCount    失败数量
     */
    fun showCompleted(successCount: Int, failCount: Int) {
        if (!isShowing) return
        tvTitle.text = context.getString(R.string.upload_file_completed, successCount, failCount)
        progressBar.progress = 100
        tvProgress.text = context.getString(R.string.percent_format, 100)
    }
}
