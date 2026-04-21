package com.zhuowei.polling.ui.activitys.ticket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.zhuowei.polling.utils.GetPhotoUtils.Companion.getPathFromUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketDetailActivity : MyBaseActivity<TicketDetailVm, ActivityTicketDetailBinding>() {
    private val mAllPhotos = ObservableArrayList<String>()
    private var mAddPhotoAdapter: AddPhotoAdapter? = null
    private var mCurrentPhotoPath: String? = null

    private val maxPhotoCount = 9


    private fun flashAdapter(path: String) {
        mAllPhotos.removeAt(mAllPhotos.size - 1)
        mAllPhotos.add(path)
        if (mAllPhotos.size < maxPhotoCount) {
            mAllPhotos.add("")
        }
    }

    private val takePictureLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mCurrentPhotoPath?.let { path ->
                if (!mAllPhotos.contains(path)) {
                    flashAdapter(path)
                }
            }
        }
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = getPathFromUri(uri)
                if (path != null && !mAllPhotos.contains(path)) {
                    flashAdapter(path)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showPhotoChoiceDialog()
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, TicketDetailActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_ticket_detail
    }

    override fun setListener() {
        mBinding!!.myTitleBar.setLeftLayoutClickListener {
            finish()
        }

        mBinding!!.tvLocation.setOnClickListener {
            getLocation()
        }
    }

    override fun initData() {
    }

    override fun initViewModel(): TicketDetailVm {
        return createViewModel(TicketDetailVm::class.java)
    }

    override fun onResume() {
        super.onResume()

    }

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

    override fun setData() {
        mAllPhotos.add("")
        mAddPhotoAdapter = AddPhotoAdapter(this, mAllPhotos, maxPhotoCount)
        mBinding!!.rvPhoto.apply {
            adapter = mAddPhotoAdapter
            layoutManager = GridLayoutManager(this@TicketDetailActivity, 3)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mAddPhotoAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onClick(position: Int, i: Any, view: View) {
                if (position == mAllPhotos.size - 1) {
                    checkPermissionAndShowDialog()
                }
            }

        })

        mAddPhotoAdapter?.setOnDeleteClickListener { position ->
            if (position < mAllPhotos.size) {
                // 获取被移除的图片路径
                val removedPath = mAllPhotos.removeAt(position)

                // 如果移除的不是占位的空字符串，则删除本地物理文件
                if (removedPath.isNotEmpty()) {
                    val file = File(removedPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                // 没有空值时，添加一个""的值，保证"添加照片"按钮始终显示
                if (!mAllPhotos.contains("")) {
                    mAllPhotos.add("")
                }
            }
        }

    }

    private fun checkPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showPhotoChoiceDialog()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            photoFile?.let {
                val photoUri = FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("PNG_${timeStamp}_", ".png", storageDir).apply {
                mCurrentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }


}