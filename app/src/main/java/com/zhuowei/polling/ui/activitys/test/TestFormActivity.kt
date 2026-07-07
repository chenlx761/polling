package com.zhuowei.polling.ui.activitys.test

import android.Manifest
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputType
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chenming.common.base.empty.EmptyViewModel
import com.chenming.common.utils.ToastUtil
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.databinding.ActivityTestFormBinding
import com.zhuowei.polling.ui.activitys.image.ImagePreviewActivity
import com.zhuowei.polling.utils.GetPhotoUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TestFormActivity : MyBaseActivity<EmptyViewModel, ActivityTestFormBinding>() {

    private val formItems = mutableListOf<FormItem>()
    private lateinit var formAdapter: DynamicFormAdapter
    private var pendingPickItemId: String? = null
    private var currentPhotoPath: String? = null

    private val pickAttachmentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handlePickedAttachments(result.data?.data, result.data?.clipData)
            } else {
                pendingPickItemId = null
            }
        }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val targetItemId = pendingPickItemId
            val photoPath = currentPhotoPath
            if (success && targetItemId != null && photoPath != null) {
                val item = formItems.firstOrNull { it.id == targetItemId }
                val itemPosition = formItems.indexOf(item)
                if (item != null && itemPosition != -1 && item.selectedAttachments.size < item.maxCount) {
                    val file = File(photoPath)
                    if (file.exists()) {
                        item.selectedAttachments.add(
                            FormAttachment(
                                name = file.name,
                                path = file.absolutePath
                            )
                        )
                        formAdapter.notifyItemChanged(itemPosition)
                    }
                }
            } else if (!success && !photoPath.isNullOrBlank()) {
                File(photoPath).takeIf { it.exists() }?.delete()
            }
            currentPhotoPath = null
            pendingPickItemId = null
        }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhoto()
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, TestFormActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_test_form
    }

    override fun setListener() {
        mBinding.myTitleBar.setLeftLayoutClickListener {
            finish()
        }
    }

    override fun initData() {
        formItems.clear()
        formItems.addAll(mockFormItems())
    }

    override fun initViewModel(): EmptyViewModel? {
        return createViewModel(EmptyViewModel::class.java)
    }

    override fun setData() {
        formAdapter = DynamicFormAdapter(
            items = formItems,
            onAction = ::handleFormAction
        )
        mBinding.rvFormList.apply {
            layoutManager = LinearLayoutManager(this@TestFormActivity)
            adapter = formAdapter
        }
    }

    private fun mockFormItems(): List<FormItem> {
        return listOf(
            FormItem(
                id = "inspect_name",
                key = FormItem.KEY_INPUT,
                label = "巡检人",
                hint = "请输入巡检人姓名",
                value = "张三",
                inputType = InputType.TYPE_CLASS_TEXT,
                required = true
            ),
            FormItem(
                id = "inspect_type",
                key = FormItem.KEY_SELECT,
                label = "巡检类型",
                options = listOf("日常巡检", "专项巡检", "整改复查"),
                value = "日常巡检",
                required = true
            ),
            FormItem(
                id = "inspect_date",
                key = FormItem.KEY_DATE,
                label = "巡检日期",
                value = "2026-07-07",
                required = true
            ),
            FormItem(
                id = "need_rectify",
                key = FormItem.KEY_SWITCH,
                label = "是否立即整改",
                description = "开启后表示当前问题需要现场立刻处理",
                checked = true
            ),
            FormItem(
                id = "remark",
                key = FormItem.KEY_TEXTAREA,
                label = "巡检说明",
                hint = "请输入现场情况说明",
                value = "发现一处消防器材摆放不规范，已通知责任人处理。"
            ),
            FormItem(
                id = "rectify_images",
                key = FormItem.KEY_IMAGE_PICKER,
                label = "整改图片",
                required = true,
                maxCount = 3
            ),
            FormItem(
                id = "attachments",
                key = FormItem.KEY_FILE_PICKER,
                label = "附件材料",
                maxCount = 2
            ),
            FormItem(
                id = "submit",
                key = FormItem.KEY_SUBMIT,
                buttonText = "提交表单"
            )
        )
    }

    private fun handleFormAction(action: FormAdapterAction) {
        when (action.type) {
            FormActionType.ITEM_CLICK -> {
                when (action.item.key) {
                    FormItem.KEY_SELECT -> showSelectDialog(action.item, action.itemPosition)
                    FormItem.KEY_DATE -> showDateDialog(action.item, action.itemPosition)
                    FormItem.KEY_IMAGE_PICKER -> showImageSourceDialog(action.item)
                    FormItem.KEY_FILE_PICKER -> openFilePicker(action.item)
                    FormItem.KEY_SUBMIT -> submitForm()
                }
            }

            FormActionType.ATTACHMENT_PREVIEW -> previewImage(action)
            FormActionType.ATTACHMENT_DELETE -> deleteAttachment(action)
        }
    }

    private fun showSelectDialog(item: FormItem, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(item.label)
            .setItems(item.options.toTypedArray()) { _, which ->
                item.value = item.options[which]
                formAdapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun showDateDialog(item: FormItem, position: Int) {
        val calendar = Calendar.getInstance()
        val dateParts = item.value.split("-")
        if (dateParts.size == 3) {
            calendar.set(
                dateParts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (dateParts[1].toIntOrNull() ?: 1) - 1,
                dateParts[2].toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                item.value = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
                formAdapter.notifyItemChanged(position)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showImageSourceDialog(item: FormItem) {
        if (item.selectedAttachments.size >= item.maxCount) {
            Toast.makeText(this, "${item.label}最多选择${item.maxCount}个", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(item.label)
            .setItems(arrayOf("拍照", "选择图片")) { _, which ->
                when (which) {
                    0 -> ensureCameraAndTakePhoto(item)
                    1 -> openImagePicker(item)
                }
            }
            .show()
    }

    private fun openImagePicker(item: FormItem) {
        pendingPickItemId = item.id
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickAttachmentLauncher.launch(intent)
    }

    private fun openFilePicker(item: FormItem) {
        if (item.selectedAttachments.size >= item.maxCount) {
            Toast.makeText(this, "${item.label}最多选择${item.maxCount}个", Toast.LENGTH_SHORT).show()
            return
        }
        pendingPickItemId = item.id
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickAttachmentLauncher.launch(intent)
    }

    private fun ensureCameraAndTakePhoto(item: FormItem) {
        pendingPickItemId = item.id
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_SHORT).show()
            return
        }
        val photoFile = createImageFile() ?: run {
            Toast.makeText(this, R.string.create_image_file_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        currentPhotoPath = photoFile.absolutePath
        takePictureLauncher.launch(photoUri)
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("FORM_${timeStamp}_", ".jpg", storageDir)
        } catch (_: Exception) {
            null
        }
    }

    private fun handlePickedAttachments(singleUri: Uri?, clipData: ClipData?) {
        val targetItemId = pendingPickItemId ?: return
        val item = formItems.firstOrNull { it.id == targetItemId } ?: return
        val itemPosition = formItems.indexOf(item)
        if (itemPosition == -1) return

        val uriList = mutableListOf<Uri>()
        if (clipData != null) {
            for (index in 0 until clipData.itemCount) {
                clipData.getItemAt(index)?.uri?.let(uriList::add)
            }
        } else if (singleUri != null) {
            uriList.add(singleUri)
        }
        if (uriList.isEmpty()) return

        val remainCount = item.maxCount - item.selectedAttachments.size
        uriList.take(remainCount).forEach { uri ->
            val attachment = createAttachmentFromUri(uri) ?: return@forEach
            val exists = item.selectedAttachments.any { it.path == attachment.path }
            if (!exists) {
                item.selectedAttachments.add(attachment)
            }
        }
        formAdapter.notifyItemChanged(itemPosition)
        pendingPickItemId = null
    }

    private fun previewImage(action: FormAdapterAction) {
        if (action.item.key != FormItem.KEY_IMAGE_PICKER) return
        if (action.attachmentPosition !in action.item.selectedAttachments.indices) return
        val imagePaths = action.item.selectedAttachments.map { it.path }
        ImagePreviewActivity.newInstance(this, imagePaths, action.attachmentPosition)
    }

    private fun deleteAttachment(action: FormAdapterAction) {
        if (action.attachmentPosition !in action.item.selectedAttachments.indices) return
        val removed = action.item.selectedAttachments.removeAt(action.attachmentPosition)
        File(removed.path).takeIf { it.exists() }?.delete()
        formAdapter.notifyItemChanged(action.itemPosition)
    }

    private fun createAttachmentFromUri(uri: Uri): FormAttachment? {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        val displayName = queryDisplayName(uri)
        val path = GetPhotoUtils.getPathFromUri(uri)
        val file = path?.let { File(it) }?.takeIf { it.exists() } ?: run {
            ToastUtil.showShortToast("选择失败，请重试")
            return null
        }
        return FormAttachment(
            name = displayName ?: file.name,
            path = file.absolutePath
        )
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

    private fun submitForm() {
        val submitText = buildString {
            formItems.forEach { item ->
                when (item.key) {
                    FormItem.KEY_INPUT, FormItem.KEY_TEXTAREA -> {
                        appendLine("${item.label}：${item.value.ifBlank { "未填写" }}")
                    }

                    FormItem.KEY_SELECT, FormItem.KEY_DATE -> {
                        appendLine("${item.label}：${item.value.ifBlank { "未选择" }}")
                    }

                    FormItem.KEY_SWITCH -> {
                        appendLine("${item.label}：${if (item.checked) "是" else "否"}")
                    }

                    FormItem.KEY_IMAGE_PICKER, FormItem.KEY_FILE_PICKER -> {
                        appendLine(
                            "${item.label}：${
                                if (item.selectedAttachments.isEmpty()) "未选择"
                                else item.selectedAttachments.joinToString("，") { attachment -> attachment.name }
                            }"
                        )
                    }

                    FormItem.KEY_SUBMIT -> Unit
                }
            }
        }.trim()
        Toast.makeText(this, submitText, Toast.LENGTH_LONG).show()
    }
}
