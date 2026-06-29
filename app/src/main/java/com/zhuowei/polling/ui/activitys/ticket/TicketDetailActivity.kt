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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.chenming.common.listener.OnItemClickListener
import com.chenming.common.utils.AppManager
import com.chenming.common.utils.ToastUtil
import com.chenming.httprequest.XLog
import com.contrarywind.interfaces.IPickerViewData
import com.zhuowei.polling.R
import com.zhuowei.polling.adapter.AddFileAdapter
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
        private const val ROOT_AREA_ID = 1
        private const val AREA_LEVEL_COUNT = 3
        private const val Ticket_Id = "Ticket_Id"
        private const val Ticket_Status = "Ticket_Status"
        private const val Ticket_Create_Mode = "Ticket_Create_Mode"

        @JvmStatic
        fun newIntent(
            myActivityLauncher: ActivityResultLauncher<Intent>,
            context: Context,
            ticketId: String?,
            ticketStatus: String?,
            isCreateMode: Boolean = false
        ) {
            val intent = Intent(context, TicketDetailActivity::class.java)
            intent.putExtra(Ticket_Id, ticketId)
            intent.putExtra(Ticket_Status, ticketStatus)
            intent.putExtra(Ticket_Create_Mode, isCreateMode)
            myActivityLauncher.launch(intent)
        }
    }

    private var mBean: TicketListBean.RowsDTO? = null
    private val mScenePhotos = ObservableArrayList<UploadFileResult>()
    private val mGovernmentPhotos = ObservableArrayList<UploadFileResult>()
    private var mAddPhotoAdapter: AddFileAdapter? = null
    private var mGovernmentPhotoAdapter: AddFileAdapter? = null
    private var mCurrentPhotoPath: String? = null
    private var mCurrentPhotoType: PhotoType = PhotoType.SCENE
    private var mLocationResult: LocationResult? = null
    private var mTicketStatus: String? = null
    private var mTicketId: String? = null
    private var mIsCreateMode: Boolean = false

    private enum class PhotoType {
        SCENE,
        GOVERNMENT
    }

    private data class AreaPickerItem(
        val option: TicketDetailVm.AreaPickerOption
    ) : IPickerViewData {
        override fun getPickerViewText(): String = option.selection.name
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
            try {
                val paths = mGovernmentPhotos.filter { !TextUtils.isEmpty(it.filePath) }.map {
                    it.filePath
                }
                val paths2 = mScenePhotos.filter { !TextUtils.isEmpty(it.filePath) }.map {
                    it.filePath
                }
                for (path in (paths2 + paths).filter { it.isNotEmpty() }) {
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            } catch (e: Exception) {

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

        mBinding!!.etArea.setOnClickListener {
            if (mIsCreateMode) {
                startAreaSelection()
            }
        }

        mBinding!!.tvLocation.setOnClickListener {
            getLocation()
        }

        mBinding!!.btnSubmit.setOnClickListener {
            val submitBean = buildSubmitBean() ?: return@setOnClickListener
            if (mLocationResult == null) {
                ToastUtil.showShortToast(getString(R.string.location_hint))
                return@setOnClickListener
            }

            val scenePhotos =
                mScenePhotos.filter { !TextUtils.isEmpty(it.filePath) }.map { it.filePath }

            if (scenePhotos.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.please_take_photo_hint))
                return@setOnClickListener
            }


            val governmentPhotos =
                mGovernmentPhotos.filter { !TextUtils.isEmpty(it.filePath) }.map { it.filePath }
            val allUploadPhotos = scenePhotos + governmentPhotos

            UploadFileManager.uploadFileWithProgress(
                AppManager.getAppManager().topActivity,
                allUploadPhotos,
                mLocationResult,
                mBean,
                object : UploadFileManager.OnUploadAllCallBack {
                    override fun onAllSuccessful(results: List<UploadFileResult>) {
                        XLog.e("完成咯")
                        val sceneSize = scenePhotos.size
                        val sceneResults = results.take(sceneSize)
                        val governmentResults = results.drop(sceneSize)
                        submitBean.remark = mBinding!!.etRemark.text.toString().trim()
                        submitBean.images = TextUtils.join(",", sceneResults.map { it.filePath })
                        submitBean.governmentImages = governmentResults
                        submitBean.buildLocation = mLocationResult!!.address
                        submitBean.buildLocationCoord =
                            "${mLocationResult!!.latitude},${mLocationResult!!.longitude}"
                        mViewModel!!.postTicketDetail(submitBean, mIsCreateMode)
                    }

                    override fun onError(
                        errorMsg: String,
                        failedPaths: List<String>
                    ) {
                    }
                }
            )
        }

        if (mIsCreateMode) {
            initCreateModeData()
        } else {
            mViewModel.getTicketDetail(mTicketId)
        }
    }

    override fun initData() {
        mTicketStatus = intent.getStringExtra(Ticket_Status)
        mTicketId = intent.getStringExtra(Ticket_Id)
        mIsCreateMode = intent.getBooleanExtra(Ticket_Create_Mode, false) || mTicketId.isNullOrEmpty()
    }

    override fun initViewModel(): TicketDetailVm = createViewModel(TicketDetailVm::class.java)

    override fun onResume() {
        super.onResume()
    }

    private fun setData2View() {
        mBean?.let {
            mScenePhotos.clear()
            mGovernmentPhotos.clear()
            mBinding!!.etAddress.setText(it.userAddress)
            mBinding!!.etArea.setText(it.workOrderCompany)
            mBinding!!.etName.setText(it.userName)
            mBinding!!.etAccount.setText(it.userNo)
            mBinding!!.etRemark.setText(it.remark)
            mScenePhotos.addAll(it.serverPhotosList)
            ensureAddPlaceholder(mScenePhotos)
            mGovernmentPhotos.addAll(it.governmentImages)
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
                mBinding!!.tvLocation.text = it.buildLocation
            }
        }
    }

    override fun setData() {
        mAddPhotoAdapter = AddFileAdapter(this, mScenePhotos, MAX_PHOTO_COUNT)
        mGovernmentPhotoAdapter =
            AddFileAdapter(this, mGovernmentPhotos, MAX_PHOTO_COUNT, supportFilePlaceholder = true)

        mBinding!!.rvPhoto.apply {
            adapter = mAddPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mBinding!!.rvGovernmentPhoto.apply {
            adapter = mGovernmentPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mAddPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                handlePhotoItemClick(mScenePhotos, position, PhotoType.SCENE)
            }
        })

        mGovernmentPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                handlePhotoItemClick(mGovernmentPhotos, position, PhotoType.GOVERNMENT)
            }
        })

        mAddPhotoAdapter?.setOnDeleteClickListener { position ->
            removePhotoAt(mScenePhotos, position)
        }

        mGovernmentPhotoAdapter?.setOnDeleteClickListener { position ->
            removePhotoAt(mGovernmentPhotos, position)
        }

        updateInputMode()
    }


    private fun handlePhotoItemClick(
        photoList: ObservableArrayList<UploadFileResult>,
        position: Int,
        photoType: PhotoType
    ) {
        val item = photoList.getOrNull(position) ?: return
        val filePath = item.filePath.orEmpty()
        if (filePath.isEmpty()) {
            if (mLocationResult == null) {
                ToastUtil.showShortToast(getString(R.string.location_hint))
                return
            }
            checkPermissionAndShowDialog(photoType, photoList)
            return
        }

        if (!isImageFile(filePath)) {
            if (filePath.startsWith("http")) {
                FileHelper.downloadWithHeaders(
                    this,
                    filePath,
                    SpManager.getToken(),
                    item.fileName?.takeIf { it.isNotEmpty() } ?: File(filePath).name
                )
            }
            return
        }

        val previewList = photoList.mapNotNull { photo ->
            photo.filePath?.takeIf { it.isNotEmpty() && isImageFile(it) }
        }
        val previewPosition = previewList.indexOf(filePath)
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
                mBinding.tvLocation.text = result.address.ifEmpty {
                    "${result.latitude},${result.longitude}"
                }
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
        when (mCurrentPhotoType) {
            PhotoType.SCENE -> addPhotoToList(mScenePhotos, path)
            PhotoType.GOVERNMENT -> addPhotoToList(mGovernmentPhotos, path)
        }
    }


    private fun addPhotoToList(
        photoList: ObservableArrayList<UploadFileResult>,
        path: String
    ) {
        if (photoList.any { it.filePath == path }) return
        if (getPhotoCount(photoList) >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        if (photoList.isNotEmpty() && photoList.last().filePath.isNullOrEmpty()) {
            photoList.removeAt(photoList.lastIndex)
        }

        photoList.add(UploadFileResult().apply {
            filePath = path
            fileName = File(path).name
        })
        ensureAddPlaceholder(photoList)
    }


    private fun ensureAddPlaceholder(photoList: ObservableArrayList<UploadFileResult>) {
        if (getPhotoCount(photoList) < MAX_PHOTO_COUNT && photoList.none { it.filePath.isNullOrEmpty() }) {
            photoList.add(UploadFileResult())
        }
    }


    private fun removePhotoAt(
        photoList: ObservableArrayList<UploadFileResult>,
        position: Int
    ) {
        if (position < 0 || position >= photoList.size) return

        val removedItem = photoList.removeAt(position)
        val removedPath = removedItem.filePath.orEmpty()
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
        }

        ensureAddPlaceholder(photoList)
    }


    private fun getPhotoCount(photoList: ObservableArrayList<UploadFileResult>): Int =
        photoList.count { !it.filePath.isNullOrEmpty() }


    private fun checkPermissionAndShowDialog(
        photoType: PhotoType,
        photoList: ObservableArrayList<UploadFileResult>
    ) {
        if (getPhotoCount(photoList) >= MAX_PHOTO_COUNT) {
            Toast.makeText(this, R.string.photo_count_limit, Toast.LENGTH_SHORT).show()
            return
        }

        mCurrentPhotoType = photoType
        showPhotoChoiceDialog()
    }

    private fun showPhotoChoiceDialog() {
        if (mCurrentPhotoType == PhotoType.GOVERNMENT) {
            val options = arrayOf(
                getString(R.string.take_photo),
                getString(R.string.select_photo),
                "选择文件"
            )
            showBottomOptionsPicker(
                title = getString(R.string.select_photo_title),
                options = options.toList()
            ) { which ->
                when (which) {
                    0 -> ensureCameraAndTakePhoto()
                    1 -> pickFromGallery()
                    2 -> pickFile()
                }
            }
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
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickFileLauncher.launch(intent)
    }

    private fun isImageFile(path: String): Boolean {
        val extension = path.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
    }

    private fun initCreateModeData() {
        mBean = TicketListBean.RowsDTO().apply {
            surveyStatus = mTicketStatus ?: "0"
        }
        mScenePhotos.clear()
        mGovernmentPhotos.clear()
        ensureAddPlaceholder(mScenePhotos)
        ensureAddPlaceholder(mGovernmentPhotos)
        mBinding!!.etAddress.setText("")
        mBinding!!.etArea.setText("")
        mBinding!!.etName.setText("")
        mBinding!!.etAccount.setText("")
        mBinding!!.etRemark.setText("")
        mBinding!!.tvLocation.text = getString(R.string.location_click_hint)
    }

    private fun updateInputMode() {
        updateAreaInputState()
        updateEditTextState(mBinding!!.etAddress, mIsCreateMode, R.string.address_input_hint)
        updateEditTextState(mBinding!!.etName, mIsCreateMode, R.string.user_name_input_hint)
        updateEditTextState(mBinding!!.etAccount, mIsCreateMode, R.string.account_input_hint)
        mBinding!!.btnSubmit.text =
            getString(if (mIsCreateMode) R.string.create_and_submit else R.string.submit)
    }

    private fun updateAreaInputState() {
        val editText = mBinding!!.etArea
        if (mIsCreateMode) {
            val horizontalPadding =
                resources.getDimensionPixelSize(R.dimen.ticket_input_padding_horizontal)
            val verticalPadding =
                resources.getDimensionPixelSize(R.dimen.ticket_input_padding_vertical)
            editText.isEnabled = true
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
            editText.isCursorVisible = false
            editText.isLongClickable = false
            editText.hint = getString(R.string.area_input_hint)
            editText.setBackgroundResource(R.drawable.bg_f5f9ff_radius_5)
            editText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                android.R.drawable.arrow_down_float,
                0
            )
            editText.compoundDrawablePadding = horizontalPadding
        } else {
            updateEditTextState(editText, false, R.string.area_input_hint)
            editText.isCursorVisible = false
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun updateEditTextState(
        editText: android.widget.EditText,
        editable: Boolean,
        hintResId: Int
    ) {
        editText.isEnabled = editable
        editText.isFocusable = editable
        editText.isFocusableInTouchMode = editable
        editText.isLongClickable = editable
        editText.hint = if (editable) getString(hintResId) else ""
        editText.setBackgroundResource(
            if (editable) R.drawable.bg_f5f9ff_radius_5 else android.R.color.transparent
        )
        val horizontalPadding = if (editable) resources.getDimensionPixelSize(R.dimen.ticket_input_padding_horizontal) else 0
        val verticalPadding = if (editable) resources.getDimensionPixelSize(R.dimen.ticket_input_padding_vertical) else 0
        editText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private fun startAreaSelection() {
        mViewModel.getAreaPickerData(mBinding!!.etArea.text?.toString()) { pickerData ->
            if (pickerData.level1Items.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.area_empty_hint))
                return@getAreaPickerData
            }
            showAreaPicker(pickerData)
        }
    }

    private fun showAreaPicker(pickerData: TicketDetailVm.AreaPickerDisplayData) {
        val level1Items = pickerData.level1Items.map(::AreaPickerItem)
        val level2Items = pickerData.level2Items.map { level2List ->
            level2List.map(::AreaPickerItem)
        }
        val level3Items = pickerData.level3Items.map { level3Group ->
            level3Group.map { level3List ->
                level3List.map(::AreaPickerItem)
            }
        }
        val pickerView: OptionsPickerView<Any> = OptionsPickerBuilder(this) { option1, option2, option3, _ ->
            val areaText = mViewModel.buildSelectedAreaText(pickerData, option1, option2, option3)
                ?: return@OptionsPickerBuilder
            mBinding!!.etArea.setText(areaText)
        }
            .setTitleText(getString(R.string.area_input_hint))
            .setSubmitColor(ContextCompat.getColor(this, R.color.primary))
            .setCancelColor(ContextCompat.getColor(this, R.color.primary))
            .setTextColorCenter(ContextCompat.getColor(this, R.color.gray_700))
            .setTextColorOut(ContextCompat.getColor(this, R.color.gray_600))
            .setContentTextSize(18)
            .isRestoreItem(true)
            .isCenterLabel(false)
            .build()
        pickerView.setSelectOptions(
            pickerData.selectedLevel1,
            pickerData.selectedLevel2,
            pickerData.selectedLevel3
        )
        pickerView.setPicker(
            level1Items,
            level2Items,
            level3Items
        )
        pickerView.show()
    }

    private fun showBottomOptionsPicker(
        title: String,
        options: List<String>,
        onSelected: (Int) -> Unit
    ) {
        if (options.isEmpty()) return
        val pickerItems = options.mapIndexed { index, name ->
            SimplePickerItem(index, name)
        }
        val pickerView: OptionsPickerView<Any> = OptionsPickerBuilder(this) { option1, _, _, _ ->
            onSelected(pickerItems.getOrNull(option1)?.value ?: return@OptionsPickerBuilder)
        }
            .setTitleText(title)
            .setSubmitColor(ContextCompat.getColor(this, R.color.primary))
            .setCancelColor(ContextCompat.getColor(this, R.color.primary))
            .setTextColorCenter(ContextCompat.getColor(this, R.color.gray_700))
            .setTextColorOut(ContextCompat.getColor(this, R.color.gray_600))
            .setContentTextSize(18)
            .isRestoreItem(true)
            .isCenterLabel(false)
            .build()
        pickerView.setPicker(pickerItems)
        pickerView.show()
    }

    private data class SimplePickerItem(
        val value: Int,
        val label: String
    ) : IPickerViewData {
        override fun getPickerViewText(): String = label
    }





    private fun buildSubmitBean(): TicketListBean.RowsDTO? {
        val area = mBinding!!.etArea.text.toString().trim()
        val address = mBinding!!.etAddress.text.toString().trim()
        val userName = mBinding!!.etName.text.toString().trim()
        val userNo = mBinding!!.etAccount.text.toString().trim()

        if (mIsCreateMode) {
            if (area.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.area_required_hint))
                return null
            }
            if (address.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.address_required_hint))
                return null
            }
            if (userName.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.user_name_required_hint))
                return null
            }
            if (userNo.isEmpty()) {
                ToastUtil.showShortToast(getString(R.string.account_required_hint))
                return null
            }
        }

        return (mBean ?: TicketListBean.RowsDTO().apply {
            surveyStatus = mTicketStatus ?: "0"
        }).apply {
            this.workOrderCompany = area
            this.areaCompany = area
            this.userAddress = address
            this.address = address
            this.userName = userName
            this.userNo = userNo
        }
    }
}
