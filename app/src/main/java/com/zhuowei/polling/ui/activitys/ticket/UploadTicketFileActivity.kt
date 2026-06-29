package com.zhuowei.polling.ui.activitys.ticket

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.chenming.common.utils.ToastUtil
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.contract.vm.UploadTicketFileVm
import com.zhuowei.polling.databinding.ActivityUploadTicketFileBinding
import com.zhuowei.polling.utils.GetPhotoUtils
import java.io.File

class UploadTicketFileActivity : MyBaseActivity<UploadTicketFileVm, ActivityUploadTicketFileBinding>() {
    private var selectedFile: File? = null


    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, UploadTicketFileActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val pickFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleSelectedFile(uri)
                }
            }
        }

    override fun getLayoutId(): Int {
        return R.layout.activity_upload_ticket_file
    }

    override fun setListener() {
        mBinding.clSelectFile.setOnClickListener {
            pickXlsxFile()
        }
        mBinding.tvChooseFile.setOnClickListener {
            pickXlsxFile()
        }
        mBinding.btnSubmit.setOnClickListener {
            submitUpload()
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): UploadTicketFileVm {
        return createViewModel(UploadTicketFileVm::class.java)
    }

    override fun setData() {
        updateSelectedFile(null, null)

        mViewModel.mUploadSuccess.observe(this) {
            dismissDialog()
            mBinding.btnSubmit.isEnabled = true
            ToastUtil.showShortToast(getString(R.string.upload_ticket_file_success))
            setResult(RESULT_OK)
            finish()
        }

        mViewModel.mUploadError.observe(this) { errorMsg ->
            dismissDialog()
            updateSubmitButtonState()
            ToastUtil.showShortToast(errorMsg ?: getString(R.string.upload_ticket_file_failed))
        }
    }

    private fun pickXlsxFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickFileLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        val displayName = queryDisplayName(uri)
        if (!isXlsxFile(displayName)) {
            ToastUtil.showShortToast(getString(R.string.upload_ticket_file_invalid_type))
            updateSelectedFile(null, null)
            return
        }
        val path = GetPhotoUtils.getPathFromUri(uri)
        val file = path?.let { File(it) }?.takeIf { it.exists() }
        if (file == null || !isXlsxFile(file.name)) {
            ToastUtil.showShortToast(getString(R.string.upload_ticket_file_select_failed))
            updateSelectedFile(null, null)
            return
        }
        updateSelectedFile(file, displayName ?: file.name)
    }

    private fun submitUpload() {
        val file = selectedFile
        if (file == null || !file.exists()) {
            ToastUtil.showShortToast(getString(R.string.upload_ticket_file_empty_hint))
            updateSelectedFile(null, null)
            return
        }
        mBinding.btnSubmit.isEnabled = false
        showLoading(false)
        mViewModel.uploadFile(file)
    }

    private fun updateSelectedFile(file: File?, fileName: String?) {
        selectedFile = file
        val hasFile = file != null
        mBinding.tvFileName.text = fileName ?: getString(R.string.upload_ticket_file_no_selection)
        mBinding.tvFilePath.text = if (hasFile) {
            file?.absolutePath
        } else {
            getString(R.string.upload_ticket_file_select_tip)
        }
        mBinding.tvFilePath.visibility = View.VISIBLE
        mBinding.tvSelectedTag.visibility = if (hasFile) View.VISIBLE else View.GONE
        mBinding.tvChooseFile.text = getString(
            if (hasFile) R.string.upload_ticket_file_reselect else R.string.upload_ticket_file_choose
        )
        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        val enabled = selectedFile != null
        mBinding.btnSubmit.isEnabled = enabled
        mBinding.btnSubmit.alpha = if (enabled) 1f else 0.5f
    }

    private fun isXlsxFile(fileName: String?): Boolean {
        return fileName?.lowercase()?.endsWith(".xlsx") == true
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(columnIndex)
            } else {
                null
            }
        }
    }
}
