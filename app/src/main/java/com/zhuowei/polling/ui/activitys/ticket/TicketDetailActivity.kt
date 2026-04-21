package com.zhuowei.polling.ui.activitys.ticket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chenming.common.listener.OnItemClickListener
import com.chenming.httprequest.XLog
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.AddPhotoAdapter
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.contract.vm.TicketDetailVm
import com.zhuowei.polling.databinding.ActivityTicketDetailBinding
import com.zhuowei.polling.location.BaiDuLocationManager
import com.zhuowei.polling.location.LocationCallBack
import com.zhuowei.polling.location.LocationResult
import com.zhuowei.polling.manager.UploadFileManager
import com.zhuowei.polling.utils.GetPhotoUtils.Companion.getPathFromUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketDetailActivity : MyBaseActivity<TicketDetailVm, ActivityTicketDetailBinding>() {

    companion object {
        private const val MAX_PHOTO_COUNT = 9

        fun newInstance(context: Context) {
            val intent = Intent(context, TicketDetailActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val mAllPhotos = ObservableArrayList<String>()
    private var mAddPhotoAdapter: AddPhotoAdapter? = null
    private var mCurrentPhotoPath: String? = null

    // ========== ActivityResultLaunchers ==========

    /** 拍照：使用 TakePicture 契约，直接传入 Uri，返回是否成功 */
    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                mCurrentPhotoPath?.let { path ->
                    addPhotoToList(path)
                }
            } else {
                // 拍照取消或失败，清理临时文件
                mCurrentPhotoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
            mCurrentPhotoPath = null
        }

    /** 从相册选取图片 */
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val path = getPathFromUri(uri)
                    if (path != null) {
                        addPhotoToList(path)
                    } else {
                        Toast.makeText(this, R.string.photo_select_failed, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    /** 请求相机权限 */
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showPhotoChoiceDialog()
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    // ========== 生命周期方法 ==========

    override fun getLayoutId(): Int = R.layout.activity_ticket_detail

    override fun setListener() {
        mBinding!!.myTitleBar.setLeftLayoutClickListener {
            finish()
        }

        mBinding!!.tvLocation.setOnClickListener {
            getLocation()
        }

        mBinding!!.btnSubmit.setOnClickListener {
            UploadFileManager.uploadFile(mAllPhotos.filter { it.isNotEmpty() })
        }
    }

    override fun initData() {}

    override fun initViewModel(): TicketDetailVm = createViewModel(TicketDetailVm::class.java)

    override fun onResume() {
        super.onResume()
    }

    override fun setData() {
        // 初始添加一个占位项（"添加照片"按钮）
        mAllPhotos.add("")
        mAddPhotoAdapter = AddPhotoAdapter(this, mAllPhotos, MAX_PHOTO_COUNT)

        mBinding!!.rvPhoto.apply {
            adapter = mAddPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mAddPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                if (mAllPhotos.getOrNull(position).isNullOrEmpty()) {
                    // 点击的是"添加照片"按钮
                    checkPermissionAndShowDialog()
                }
            }
        })

        mAddPhotoAdapter?.setOnDeleteClickListener { position ->
            removePhotoAt(position)
        }
    }

    // ========== 定位 ==========

    private fun getLocation() {
        showLoading()
        BaiDuLocationManager.instance.requestLocation(this, object : LocationCallBack {
            override fun onLocationSuccess(result: LocationResult) {
                XLog.e("定位成功", result.toString())
                mBinding.tvLocation.text = result.address
                dismissDialog()
            }

            override fun onLocationError(errorCode: Int, errorMessage: String) {
                XLog.e("定位失败", "$errorCode $errorMessage")
                dismissDialog()
            }
        })
    }

    // ========== 图片选择核心逻辑 ==========

    /**
     * 将图片路径添加到列表中。
     * 先移除末尾的占位空字符串，再插入实际路径，
     * 若仍未达上限则在末尾补回占位空字符串。
     */
    private fun addPhotoToList(path: String) {
        if (mAllPhotos.contains(path)) return
        if (getActualPhotoCount() >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        // 移除末尾的占位空字符串
        if (mAllPhotos.isNotEmpty() && mAllPhotos.last().isEmpty()) {
            mAllPhotos.removeAt(mAllPhotos.lastIndex)
        }

        // 添加实际图片路径
        mAllPhotos.add(path)

        // 未达上限时，补回占位空字符串以显示"添加照片"按钮
        if (getActualPhotoCount() < MAX_PHOTO_COUNT) {
            mAllPhotos.add("")
        }
    }

    /**
     * 删除指定位置的图片。
     * 若删除的是实际图片，则移除后确保占位空字符串存在；
     * 若删除的是占位空字符串则忽略。
     */
    private fun removePhotoAt(position: Int) {
        if (position < 0 || position >= mAllPhotos.size) return

        val removedPath = mAllPhotos.removeAt(position)

        // 只处理实际图片的删除（非占位空字符串）
        if (removedPath.isNotEmpty()) {
            // 仅删除本应用外部存储目录下的临时文件，不删除相册原文件
            try {
                val file = File(removedPath)
                val appStorageDir =
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
                if (appStorageDir != null && removedPath.startsWith(appStorageDir) && file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                XLog.e("删除图片文件失败", e.message ?: "")
            }

            // 确保占位空字符串存在，以显示"添加照片"按钮
            if (!mAllPhotos.contains("")) {
                mAllPhotos.add("")
            }
        }
    }

    /**
     * 获取实际图片数量（排除占位空字符串）
     */
    private fun getActualPhotoCount(): Int = mAllPhotos.count { it.isNotEmpty() }

    // ========== 权限与弹窗 ==========

    private fun checkPermissionAndShowDialog() {
        if (getActualPhotoCount() >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showPhotoChoiceDialog()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showPhotoChoiceDialog() {
        val options = arrayOf(getString(R.string.take_photo), getString(R.string.select_photo))
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_photo_title)).setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                }
            }.show()
    }

    // ========== 拍照 ==========

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
        mCurrentPhotoPath = photoFile.absolutePath
        takePictureLauncher.launch(photoUri)
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("PNG_${timeStamp}_", ".png", storageDir).apply {
                mCurrentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            XLog.e("创建图片文件失败", e.message ?: "")
            null
        }
    }

    // ========== 从相册选取 ==========

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        pickImageLauncher.launch(intent)
    }
}
