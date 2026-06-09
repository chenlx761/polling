package com.zhuowei.polling.ui.activitys.ticket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
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
import com.chenming.common.utils.AppManager
import com.chenming.common.utils.ToastUtil
import com.chenming.httprequest.XLog
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.AddPhotoAdapter
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.beans.TicketListBean
import com.zhuowei.polling.beans.UploadFileResult
import com.zhuowei.polling.contract.vm.TicketDetailVm
import com.zhuowei.polling.databinding.ActivityTicketDetailBinding
import com.zhuowei.polling.location.BaiDuLocationManager
import com.zhuowei.polling.location.LocationCallBack
import com.zhuowei.polling.location.LocationResult
import com.zhuowei.polling.manager.UploadFileManager
import com.zhuowei.polling.ui.activitys.image.ImagePreviewActivity
import com.zhuowei.polling.util.FileHelper
import com.zhuowei.polling.utils.GetPhotoUtils.Companion.getPathFromUri
import com.zhuowei.polling.utils.SpManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketDetailActivity : MyBaseActivity<TicketDetailVm, ActivityTicketDetailBinding>() {

    companion object {
        private const val MAX_PHOTO_COUNT = 9
        private const val Ticket_Id = "Ticket_Id"
        private const val Ticket_Status = "Ticket_Status"

        @JvmStatic
        fun newIntent(
            myActivityLauncher: ActivityResultLauncher<Intent>,
            context: Context,
            ticketId: String?,
            ticketStatus: String?
        ) {
            val intent = Intent(context, TicketDetailActivity::class.java)
            intent.putExtra(Ticket_Id, ticketId)
            intent.putExtra(Ticket_Status, ticketStatus)
            myActivityLauncher.launch(intent)
        }
    }

    private var mBean: TicketListBean.RowsDTO? = null
    private val mAllPhotos = ObservableArrayList<String>()
    private val mGovernmentPhotos = ObservableArrayList<String>()
    private var mAddPhotoAdapter: AddPhotoAdapter? = null
    private var mGovernmentPhotoAdapter: AddPhotoAdapter? = null
    private var mCurrentPhotoPath: String? = null
    private var mCurrentPhotoType: PhotoType = PhotoType.SCENE
    private var mLocationResult: LocationResult? = null
    private var mTicketStatus: String? = null
    private var mTicketId: String? = null

    private enum class PhotoType {
        SCENE,
        GOVERNMENT
    }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                mCurrentPhotoPath?.let { path ->
                    addPhotoToCurrentList(path)
                }
            } else {
                mCurrentPhotoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
            mCurrentPhotoPath = null
        }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val path = getPathFromUri(uri)
                    if (path != null) {
                        addPhotoToCurrentList(path)
                    } else {
                        Toast.makeText(this, R.string.photo_select_failed, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    private val pickFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) {
                    }
                    val path = getPathFromUri(uri)
                    if (path != null) {
                        addPhotoToCurrentList(path)
                    } else {
                        Toast.makeText(this, R.string.photo_select_failed, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showPhotoChoiceDialog()
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_ticket_detail

    override fun onDestroy() {
        super.onDestroy()
        BaiDuLocationManager.instance.release()
    }

    override fun setObserveListener() {
        mViewModel.mUpdateFinish.observe(this) {
            for (path in (mAllPhotos + mGovernmentPhotos).filter { it.isNotEmpty() }) {
                val file = File(path)
                if (file.exists()) file.delete()
            }
            setResult(RESULT_OK, Intent())
            finish()
        }

        mViewModel.mDetail.observe(this) {
            mBean = it
            setData2View()
        }
    }

    override fun setListener() {
        mBinding!!.myTitleBar.setLeftLayoutClickListener {
            finish()
        }

        mBinding!!.tvLocation.setOnClickListener {
            getLocation()
        }

        mBinding!!.btnSubmit.setOnClickListener {
            if (mLocationResult == null) {
                ToastUtil.showShortToast(getString(R.string.location_hint))
                return@setOnClickListener
            }
            if (mAllPhotos.filter { it.isNotEmpty() }.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.please_take_photo_hint))
                return@setOnClickListener
            }

            val scenePhotos = mAllPhotos.filter { it.isNotEmpty() }
            val governmentPhotos = mGovernmentPhotos.filter { it.isNotEmpty() }
            val allUploadPhotos = scenePhotos + governmentPhotos

            UploadFileManager.uploadFileWithProgress(
                AppManager.getAppManager().topActivity,
                allUploadPhotos,
                mLocationResult,
                mBean,
                object : UploadFileManager.OnUploadAllCallBack {
                    override fun onAllSuccessful(results: List<UploadFileResult>) {
                        XLog.e("完成咯")
                        mBean?.let { bean ->
                            val sceneSize = scenePhotos.size
                            val sceneResults = results.take(sceneSize)
                            val governmentResults = results.drop(sceneSize)
                            bean.remark = mBinding!!.etRemark.text.toString()
                            bean.images = TextUtils.join(",", sceneResults.map { it.filePath })
                            bean.governmentImages =
                                TextUtils.join(",", governmentResults.map { it.filePath })
                            bean.buildLocation = mLocationResult!!.address
                            bean.buildLocationCoord =
                                "${mLocationResult!!.latitude},${mLocationResult!!.longitude}"
                            mViewModel!!.postTicketDetail(bean)
                        }
                    }

                    override fun onError(
                        errorMsg: String,
                        failedPaths: List<String>
                    ) {
                    }
                }
            )
        }

        mViewModel.getTicketDetail(mTicketId)
    }

    override fun initData() {
        mTicketStatus = intent.getStringExtra(Ticket_Status)
        mTicketId = intent.getStringExtra(Ticket_Id)
    }

    override fun initViewModel(): TicketDetailVm = createViewModel(TicketDetailVm::class.java)

    override fun onResume() {
        super.onResume()
    }

    private fun setData2View() {
        mBean?.let {
            mBinding!!.etAddress.setText(it.userAddress)
            mBinding!!.etArea.setText(it.workOrderCompany)
            mBinding!!.etName.setText(it.userName)
            mBinding!!.etAccount.setText(it.userNo)
            mBinding!!.etRemark.setText(it.remark)
            mAllPhotos.addAll(it.serverPhotosList)
            ensureAddPlaceholder(mAllPhotos)
            mGovernmentPhotos.addAll(it.governmentServerPhotosList)
            ensureAddPlaceholder(mGovernmentPhotos)
            if (TextUtils.isEmpty(it.buildLocation)) {
                mBinding!!.tvLocation.postDelayed({
                    getLocation()
                }, 500)
            } else {
                mLocationResult = LocationResult(
                    it.buildLocationCoord.split(",")[0].toDouble(),
                    it.buildLocationCoord.split(",")[1].toDouble(),
                    it.buildLocation,
                    ""
                )
                mBinding!!.tvLocation.text = it.buildLocationCoord
            }
        }
    }

    override fun setData() {
        mAddPhotoAdapter = AddPhotoAdapter(this, mAllPhotos, MAX_PHOTO_COUNT)
        mGovernmentPhotoAdapter =
            AddPhotoAdapter(this, mGovernmentPhotos, MAX_PHOTO_COUNT, supportFilePlaceholder = true)

        mBinding!!.rvPhoto.apply {
            adapter = mAddPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mBinding!!.rvGovernmentPhoto.apply {
            adapter = mGovernmentPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mAddPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                handlePhotoItemClick(mAllPhotos, position, PhotoType.SCENE)
            }
        })

        mGovernmentPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                handlePhotoItemClick(mGovernmentPhotos, position, PhotoType.GOVERNMENT)
            }
        })

        mAddPhotoAdapter?.setOnDeleteClickListener { position ->
            removePhotoAt(mAllPhotos, position)
        }

        mGovernmentPhotoAdapter?.setOnDeleteClickListener { position ->
            removePhotoAt(mGovernmentPhotos, position)
        }
    }

    private fun handlePhotoItemClick(
        photoList: ObservableArrayList<String>,
        position: Int,
        photoType: PhotoType
    ) {
        val item = photoList.getOrNull(position)
        if (item.isNullOrEmpty()) {
            if (mLocationResult == null) {
                ToastUtil.showShortToast(getString(R.string.location_hint))
                return
            }
            checkPermissionAndShowDialog(photoType, photoList)
            return
        }

        if (!isImageFile(item)){
            if (item.startsWith("http")){
                FileHelper.downloadWithHeaders(this,item, SpManager.getToken(), File(item).name)
            }
            return
        }


        val previewList = photoList.filter { it.isNotEmpty() && isImageFile(it) }
        val previewPosition = previewList.indexOf(item)
        if (previewPosition >= 0) {
            ImagePreviewActivity.newInstance(
                this@TicketDetailActivity,
                previewList,
                previewPosition
            )
        }
    }

    private fun getLocation() {
        showLoading()
        BaiDuLocationManager.instance.requestLocation(this, object : LocationCallBack {
            override fun onLocationSuccess(result: LocationResult) {
                XLog.e("定位成功", result.toString())
                mLocationResult = result
                mBinding.tvLocation.text = "${result.latitude},${result.longitude}"
                dismissDialog()
            }

            override fun onLocationError(errorCode: Int, errorMessage: String) {
                XLog.e("定位失败", "$errorCode $errorMessage")
                ToastUtil.showShortToast(errorMessage)
                dismissDialog()
            }
        })
    }

    private fun addPhotoToCurrentList(path: String) {
        addPhotoToList(
            when (mCurrentPhotoType) {
                PhotoType.SCENE -> mAllPhotos
                PhotoType.GOVERNMENT -> mGovernmentPhotos
            },
            path
        )
    }

    private fun addPhotoToList(photoList: ObservableArrayList<String>, path: String) {
        if (photoList.contains(path)) return
        if (getActualPhotoCount(photoList) >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        if (photoList.isNotEmpty() && photoList.last().isEmpty()) {
            photoList.removeAt(photoList.lastIndex)
        }

        photoList.add(path)
        ensureAddPlaceholder(photoList)
    }

    private fun ensureAddPlaceholder(photoList: ObservableArrayList<String>) {
        if (getActualPhotoCount(photoList) < MAX_PHOTO_COUNT && !photoList.contains("")) {
            photoList.add("")
        }
    }

    private fun removePhotoAt(photoList: ObservableArrayList<String>, position: Int) {
        if (position < 0 || position >= photoList.size) return

        val removedPath = photoList.removeAt(position)
        if (removedPath.isNotEmpty()) {
            try {
                if (removedPath.startsWith("http")) {
                    ensureAddPlaceholder(photoList)
                    return
                }
                val file = File(removedPath)
                val appStorageDir =
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
                if (appStorageDir != null && removedPath.startsWith(appStorageDir) && file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                XLog.e("删除图片文件失败", e.message ?: "")
            }

            ensureAddPlaceholder(photoList)
        }
    }

    private fun getActualPhotoCount(photoList: ObservableArrayList<String>): Int =
        photoList.count { it.isNotEmpty() }

    private fun checkPermissionAndShowDialog(
        photoType: PhotoType,
        photoList: ObservableArrayList<String>
    ) {
        if (getActualPhotoCount(photoList) >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        mCurrentPhotoType = photoType
        if (photoType == PhotoType.GOVERNMENT) {
            showPhotoChoiceDialog()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showPhotoChoiceDialog()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showPhotoChoiceDialog() {
        if (mCurrentPhotoType == PhotoType.GOVERNMENT) {
            val options = arrayOf(
                getString(R.string.take_photo),
                getString(R.string.select_photo),
                "选择文件"
            )
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_photo_title))
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> ensureCameraAndTakePhoto()
                        1 -> pickFromGallery()
                        2 -> pickFile()
                    }
                }
                .show()
        } else {
            takePhoto()
        }
    }

    private fun ensureCameraAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
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

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        pickImageLauncher.launch(intent)
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickFileLauncher.launch(intent)
    }

    private fun isImageFile(path: String): Boolean {
        val extension = path.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }
}
