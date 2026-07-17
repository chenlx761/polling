package com.zhuowei.polling.businesscard

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.zhuowei.polling.R
import com.zhuowei.polling.databinding.DialogBusinessCardPreviewBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class BusinessCardPreviewDialog : DialogFragment() {

    companion object {
        const val TAG = "BusinessCardPreviewDialog"
        private const val ARG_TEMPLATE_JSON = "arg_template_json"
        private const val LANDSCAPE_EXPORT_WIDTH = 1500
        private const val LANDSCAPE_EXPORT_HEIGHT = 900
        private const val PORTRAIT_EXPORT_WIDTH = 900
        private const val PORTRAIT_EXPORT_HEIGHT = 1500
        private const val EXPORT_CORNER_RADIUS_RATIO = 4f / 300f

        fun newInstance(templateJson: String): BusinessCardPreviewDialog {
            return BusinessCardPreviewDialog().apply {
                arguments = Bundle().apply { putString(ARG_TEMPLATE_JSON, templateJson) }
            }
        }
    }

    private var binding: DialogBusinessCardPreviewBinding? = null
    private var previewOrientation = BusinessCardOrientation.LANDSCAPE
    private var saving = false
    private var saveJob: Job? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            savePreviewImage()
        } else {
            context?.applicationContext?.let { appContext ->
                Toast.makeText(
                    appContext,
                    R.string.business_card_preview_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val state = BusinessCardStateCodec.decode(
            requireArguments().getString(ARG_TEMPLATE_JSON).orEmpty()
        )
        previewOrientation = state.canvas.orientation
        val dialogBinding = DialogBusinessCardPreviewBinding.inflate(layoutInflater)
        binding = dialogBinding
        dialogBinding.previewCanvas.setEditingEnabled(false)
        dialogBinding.previewCanvas.setState(state)
        updateCanvasRatio(dialogBinding, state.canvas.orientation)
        dialogBinding.btnClosePreview.setOnClickListener { dismiss() }
        dialogBinding.btnSavePreview.setOnClickListener { requestSavePreview() }

        return Dialog(requireContext(), R.style.ImagePreviewTheme).apply {
            setContentView(dialogBinding.root)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        saveJob?.cancel()
        saveJob = null
        saving = false
        binding = null
        super.onDestroyView()
    }

    private fun requestSavePreview() {
        if (saving) return
        val context = context ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        savePreviewImage()
    }

    private fun savePreviewImage() {
        if (saving) return
        val canvasView = binding?.previewCanvas ?: return
        val appContext = canvasView.context.applicationContext
        val (targetWidth, targetHeight) = when (previewOrientation) {
            BusinessCardOrientation.LANDSCAPE -> LANDSCAPE_EXPORT_WIDTH to LANDSCAPE_EXPORT_HEIGHT
            BusinessCardOrientation.PORTRAIT -> PORTRAIT_EXPORT_WIDTH to PORTRAIT_EXPORT_HEIGHT
        }

        setSavingState(true)
        saveJob = lifecycleScope.launch {
            var exportedBitmap: Bitmap? = null
            try {
                val cornerRadius = min(targetWidth, targetHeight) * EXPORT_CORNER_RADIUS_RATIO
                val bitmap = canvasView.exportBitmap(targetWidth, targetHeight, cornerRadius)
                exportedBitmap = bitmap
                withContext(Dispatchers.IO) {
                    BusinessCardGallerySaver.savePng(appContext, bitmap)
                }
                if (isAdded) {
                    Toast.makeText(
                        appContext,
                        R.string.business_card_preview_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(TAG, "Failed to export the business card preview", error)
                if (isAdded) {
                    Toast.makeText(
                        appContext,
                        R.string.business_card_preview_save_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                exportedBitmap?.recycle()
                setSavingState(false)
                saveJob = null
            }
        }
    }

    private fun setSavingState(isSaving: Boolean) {
        saving = isSaving
        binding?.btnSavePreview?.apply {
            isEnabled = !isSaving
            visibility = if (isSaving) View.INVISIBLE else View.VISIBLE
        }
        binding?.savePreviewProgress?.visibility = if (isSaving) View.VISIBLE else View.GONE
    }

    private fun updateCanvasRatio(
        binding: DialogBusinessCardPreviewBinding,
        orientation: BusinessCardOrientation
    ) {
        val params = binding.previewCanvasContainer.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = if (orientation == BusinessCardOrientation.PORTRAIT) "3:5" else "5:3"
        binding.previewCanvasContainer.layoutParams = params
    }

}
