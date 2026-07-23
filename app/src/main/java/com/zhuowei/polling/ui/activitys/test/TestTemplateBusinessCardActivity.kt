package com.zhuowei.polling.ui.activitys.test

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.bumptech.glide.Glide
import com.chenming.common.base.empty.EmptyViewModel
import com.google.gson.Gson
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.businesscard.BUSINESS_CARD_SCHEMA_VERSION
import com.zhuowei.polling.businesscard.BusinessCardAssetStore
import com.zhuowei.polling.businesscard.BusinessCardCanvasView
import com.zhuowei.polling.businesscard.BusinessCardContentScale
import com.zhuowei.polling.businesscard.BusinessCardElement
import com.zhuowei.polling.businesscard.BusinessCardElementType
import com.zhuowei.polling.businesscard.BusinessCardImageSourceKind
import com.zhuowei.polling.businesscard.BusinessCardOrientation
import com.zhuowei.polling.businesscard.BusinessCardPreviewDialog
import com.zhuowei.polling.businesscard.BusinessCardState
import com.zhuowei.polling.businesscard.BusinessCardStateCodec
import com.zhuowei.polling.businesscard.BusinessCardStateException
import com.zhuowei.polling.databinding.ActivityTestTemplateBusinessCardBinding
import com.zhuowei.polling.databinding.DialogBusinessCardColorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TestTemplateBusinessCardActivity :
    MyBaseActivity<EmptyViewModel, ActivityTestTemplateBusinessCardBinding>() {

    companion object {
        const val EXTRA_INITIAL_TEMPLATE_JSON = "extra_initial_template_json"
        const val EXTRA_TEMPLATE_JSON = "extra_template_json"
        const val EXTRA_LOCAL_ASSET_PATHS = "extra_local_asset_paths"

        private const val FONT_SIZE_MIN_RATIO = 0.03f
        private const val FONT_SIZE_PROGRESS_SCALE = 1000f
        private const val IMAGE_INSPECTION_SIZE_PX = 1024
        private const val TAG = "BusinessCardEditor"
        private const val STATE_EDITOR_JSON = "state_editor_json"
        private const val STATE_INITIAL_JSON = "state_initial_json"
        private const val STATE_LOAD_ERROR = "state_load_error"
        private const val STATE_VERIFIED_PATHS = "state_verified_paths"
        private const val STATE_NEW_ASSET_PATHS = "state_new_asset_paths"

        fun createIntent(context: Context, initialTemplateJson: String? = null): Intent {
            return Intent(context, TestTemplateBusinessCardActivity::class.java).apply {
                if (initialTemplateJson != null) {
                    putExtra(EXTRA_INITIAL_TEMPLATE_JSON, initialTemplateJson)
                }
            }
        }

        fun getTemplateJson(data: Intent?): String? =
            data?.getStringExtra(EXTRA_TEMPLATE_JSON)

        fun getLocalAssetPaths(data: Intent?): List<String> =
            data?.getStringArrayListExtra(EXTRA_LOCAL_ASSET_PATHS).orEmpty()
    }

    private data class ImportedImage(
        val absolutePath: String,
        val aspectRatio: Float
    )

    private var initialState = BusinessCardState()
    private var initialCanonicalJson = ""
    private var loadError: String? = null
    private var syncingControls = false
    private var restoredEditorJson: String? = null
    private var restoredInitialJson: String? = null
    private var restoredLoadError: String? = null
    private var validatingImages = false
    private var keepPrivateAssetsOnFinish = false
    private var suppressBaseRestoreFinish = false
    private val internalGson = Gson()
    private val assetStore by lazy { BusinessCardAssetStore(this) }
    private val verifiedLocalPaths = mutableSetOf<String>()
    private val newlyCreatedAssetPaths = mutableSetOf<String>()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        importPickedImage(uri) { image ->
            mBinding.businessCardCanvas.addImageElement(
                sourceKind = BusinessCardImageSourceKind.LOCAL_PATH,
                sourceValue = image.absolutePath,
                intrinsicAspectRatio = image.aspectRatio
            )
        }
    }

    private val pickBackgroundImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        importPickedImage(uri) { image ->
            val previousPath = localBackgroundPath()
            mBinding.businessCardCanvas.setCanvasBackgroundImage(
                sourceKind = BusinessCardImageSourceKind.LOCAL_PATH,
                sourceValue = image.absolutePath,
                intrinsicAspectRatio = image.aspectRatio
            )
            previousPath?.let(::deleteNewPrivateAssetIfUnused)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (!syncingControls) {
                mBinding.businessCardCanvas.updateSelectedText(s?.toString().orEmpty())
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_test_template_business_card

    override fun onCreate(savedInstanceState: Bundle?) {
        restoredEditorJson = savedInstanceState?.getString(STATE_EDITOR_JSON)
        restoredInitialJson = savedInstanceState?.getString(STATE_INITIAL_JSON)
        restoredLoadError = savedInstanceState?.getString(STATE_LOAD_ERROR)
        verifiedLocalPaths.addAll(
            savedInstanceState?.getStringArrayList(STATE_VERIFIED_PATHS).orEmpty()
        )
        newlyCreatedAssetPaths.addAll(
            savedInstanceState?.getStringArrayList(STATE_NEW_ASSET_PATHS).orEmpty()
        )
        // Preserve registry/fragment restoration while suppressing BaseActivity's legacy early finish.
        suppressBaseRestoreFinish = savedInstanceState != null
        try {
            super.onCreate(savedInstanceState)
        } finally {
            suppressBaseRestoreFinish = false
        }
    }

    override fun initViewModel(): EmptyViewModel =
        createViewModel(EmptyViewModel::class.java)

    override fun initData() {
        restoredEditorJson?.let { restored ->
            val restoredState = try {
                internalGson.fromJson(restored, BusinessCardState::class.java)
                    ?: BusinessCardState()
            } catch (_: Exception) {
                BusinessCardState()
            }
            val hasUnsupportedImage = restoredState.elements.any { element ->
                element.type == BusinessCardElementType.IMAGE &&
                    element.image?.sourceKind == null
            }
            val restoredBackground = restoredState.canvas.backgroundImage
            val hasUnsupportedBackground = restoredBackground != null &&
                (restoredBackground.sourceKind !in BusinessCardImageSourceKind.values() ||
                    restoredBackground.contentScale != BusinessCardContentScale.CROP)
            initialState = if (restoredState.schemaVersion == BUSINESS_CARD_SCHEMA_VERSION &&
                !hasUnsupportedImage &&
                !hasUnsupportedBackground
            ) {
                restoredState
            } else {
                loadError = "Saved editor state uses an unsupported image source"
                BusinessCardState()
            }
            initialCanonicalJson = restoredInitialJson.orEmpty()
            loadError = loadError ?: restoredLoadError
            return
        }

        initialState = if (!intent.hasExtra(EXTRA_INITIAL_TEMPLATE_JSON)) {
            BusinessCardState()
        } else {
            try {
                BusinessCardStateCodec.decode(
                    intent.getStringExtra(EXTRA_INITIAL_TEMPLATE_JSON).orEmpty()
                )
            } catch (error: BusinessCardStateException) {
                loadError = error.message
                BusinessCardState()
            }
        }
        initialCanonicalJson = BusinessCardStateCodec.encode(initialState)
    }

    override fun setData() {
        mBinding.businessCardCanvas.setListener(object : BusinessCardCanvasView.Listener {
            override fun onSelectionChanged(element: BusinessCardElement?) {
                bindSelectedElement(element)
            }

            override fun onStateChanged(state: BusinessCardState) {
                updateCanvasRatio(state.canvas.orientation)
                syncCanvasBackgroundActions(state)
            }
        })
        mBinding.businessCardCanvas.setState(initialState)
        syncOrientation(initialState.canvas.orientation)
        updateCanvasRatio(initialState.canvas.orientation)
        syncCanvasBackgroundActions(initialState)
        bindSelectedElement(null)

        loadError?.let {
            mBinding.root.post { showInvalidTemplateDialog() }
        }
    }

    override fun setListener() {
        mBinding.myTitleBar.setLeftLayoutClickListener { handleBack() }
        mBinding.btnAddText.setOnClickListener {
            mBinding.businessCardCanvas.addTextElement()
            mBinding.etTextContent.post {
                mBinding.etTextContent.requestFocus()
                mBinding.etTextContent.selectAll()
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showSoftInput(mBinding.etTextContent, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        mBinding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }
        mBinding.btnCanvasImage.setOnClickListener {
            pickBackgroundImageLauncher.launch(arrayOf("image/*"))
        }
        mBinding.btnClearCanvasImage.setOnClickListener {
            clearCanvasBackgroundImage()
        }
        mBinding.btnCanvasColor.setOnClickListener {
            val current = mBinding.businessCardCanvas.getState().canvas.backgroundColor
            showColorDialog(getString(R.string.business_card_canvas_color), current) { color ->
                val previousPath = localBackgroundPath()
                mBinding.businessCardCanvas.setCanvasBackgroundColor(color)
                mBinding.businessCardCanvas.clearCanvasBackgroundImage()
                previousPath?.let(::deleteNewPrivateAssetIfUnused)
            }
        }
        mBinding.btnTextColor.setOnClickListener {
            val current = mBinding.businessCardCanvas.getSelectedElement()?.text?.color ?: return@setOnClickListener
            showColorDialog(getString(R.string.business_card_text_color), current) { color ->
                mBinding.businessCardCanvas.updateSelectedTextColor(color)
            }
        }
        mBinding.btnDeleteElement.setOnClickListener {
            val removedSource = mBinding.businessCardCanvas.getSelectedElement()
                ?.image
                ?.takeIf { it.sourceKind == BusinessCardImageSourceKind.LOCAL_PATH }
                ?.sourceValue
            if (mBinding.businessCardCanvas.deleteSelected() && removedSource != null) {
                deleteNewPrivateAssetIfUnused(removedSource)
            }
        }
        mBinding.btnMoveDown.setOnClickListener {
            mBinding.businessCardCanvas.moveSelectedBackward()
        }
        mBinding.btnMoveUp.setOnClickListener {
            mBinding.businessCardCanvas.moveSelectedForward()
        }
        mBinding.btnPreview.setOnClickListener { previewCard() }
        mBinding.btnComplete.setOnClickListener { completeEditing() }

        mBinding.orientationGroup.setOnCheckedChangeListener { _, checkedId ->
            if (syncingControls) return@setOnCheckedChangeListener
            val orientation = when (checkedId) {
                R.id.radio_portrait -> BusinessCardOrientation.PORTRAIT
                else -> BusinessCardOrientation.LANDSCAPE
            }
            mBinding.businessCardCanvas.setOrientation(orientation)
            updateCanvasRatio(orientation)
        }

        mBinding.etTextContent.addTextChangedListener(textWatcher)
        mBinding.fontSizeSeekBar.setOnSeekBarChangeListener(
            simpleSeekBarListener { progress ->
                if (!syncingControls) {
                    mBinding.businessCardCanvas.updateSelectedFontSize(
                        FONT_SIZE_MIN_RATIO + progress / FONT_SIZE_PROGRESS_SCALE
                    )
                }
            }
        )
        mBinding.imageSizeSeekBar.setOnSeekBarChangeListener(
            simpleSeekBarListener { progress ->
                if (!syncingControls) {
                    val range = mBinding.businessCardCanvas.getSelectedImageWidthRange()
                        ?: return@simpleSeekBarListener
                    val fraction = progress.toFloat() / mBinding.imageSizeSeekBar.max.coerceAtLeast(1)
                    val widthRatio = range.start +
                        fraction * (range.endInclusive - range.start)
                    mBinding.businessCardCanvas.updateSelectedImageWidthRatio(widthRatio)
                }
            }
        )
    }

    private fun bindSelectedElement(element: BusinessCardElement?) {
        syncingControls = true
        try {
            val hasSelection = element != null
            mBinding.elementActionPanel.visibility = if (hasSelection) View.VISIBLE else View.GONE
            mBinding.textPropertyPanel.visibility =
                if (element?.type == BusinessCardElementType.TEXT) View.VISIBLE else View.GONE
            mBinding.imagePropertyPanel.visibility =
                if (element?.type == BusinessCardElementType.IMAGE) View.VISIBLE else View.GONE
            mBinding.selectionHint.text = when (element?.type) {
                BusinessCardElementType.TEXT -> getString(R.string.business_card_selected_text)
                BusinessCardElementType.IMAGE -> getString(R.string.business_card_selected_image)
                null -> getString(R.string.business_card_no_selection)
            }

            element?.text?.let { text ->
                if (mBinding.etTextContent.text?.toString() != text.value) {
                    mBinding.etTextContent.setText(text.value)
                    mBinding.etTextContent.setSelection(text.value.length)
                }
                val progress = ((text.fontSizeRatio - FONT_SIZE_MIN_RATIO) * FONT_SIZE_PROGRESS_SCALE)
                    .roundToInt()
                    .coerceIn(0, mBinding.fontSizeSeekBar.max)
                mBinding.fontSizeSeekBar.progress = progress
                mBinding.tvFontSizeValue.text = getString(
                    R.string.business_card_font_size_value,
                    (text.fontSizeRatio * 100).roundToInt()
                )
                setColorPreview(mBinding.textColorPreview, text.color)
            }

            element?.image?.let {
                val range = mBinding.businessCardCanvas.getSelectedImageWidthRange()
                val span = range?.let { it.endInclusive - it.start } ?: 0f
                val progress = if (range == null || span <= 0f) {
                    0
                } else {
                    (((element.widthRatio - range.start) / span) * mBinding.imageSizeSeekBar.max)
                        .roundToInt()
                        .coerceIn(0, mBinding.imageSizeSeekBar.max)
                }
                mBinding.imageSizeSeekBar.progress = progress
                mBinding.tvImageSizeValue.text = getString(
                    R.string.business_card_image_size_value,
                    (element.widthRatio * 100).roundToInt()
                )
            }

            val lastIndex = mBinding.businessCardCanvas.getState().elements.lastIndex
            mBinding.btnMoveDown.isEnabled = element != null && element.zIndex > 0
            mBinding.btnMoveUp.isEnabled = element != null && element.zIndex < lastIndex
        } finally {
            syncingControls = false
        }
    }

    private fun syncOrientation(orientation: BusinessCardOrientation) {
        syncingControls = true
        try {
            if (orientation == BusinessCardOrientation.PORTRAIT) {
                mBinding.radioPortrait.isChecked = true
            } else {
                mBinding.radioLandscape.isChecked = true
            }
        } finally {
            syncingControls = false
        }
    }

    private fun updateCanvasRatio(orientation: BusinessCardOrientation) {
        val params = mBinding.cardCanvasContainer.layoutParams as? ConstraintLayout.LayoutParams
            ?: return
        val expected = if (orientation == BusinessCardOrientation.PORTRAIT) "3:5" else "5:3"
        if (params.dimensionRatio != expected) {
            params.dimensionRatio = expected
            mBinding.cardCanvasContainer.layoutParams = params
            mBinding.cardCanvasContainer.requestLayout()
        }
    }

    private fun syncCanvasBackgroundActions(state: BusinessCardState) {
        mBinding.btnClearCanvasImage.isEnabled = state.canvas.backgroundImage != null
    }

    private fun previewCard() {
        validateImagesAndRun { json, _ ->
            if (isFinishing || supportFragmentManager.isStateSaved) return@validateImagesAndRun
            mBinding.businessCardCanvas.clearSelection()
            BusinessCardPreviewDialog.newInstance(json)
                .show(supportFragmentManager, BusinessCardPreviewDialog.TAG)
        }
    }

    private fun completeEditing() {
        validateImagesAndRun { json, state ->
            if (isFinishing) return@validateImagesAndRun
            val localAssetPaths = BusinessCardStateCodec.localAssetPaths(state)
            val hasInvalidPath = localAssetPaths.any { path ->
                assetStore.pathForSource(path) != path
            }
            if (hasInvalidPath) {
                Toast.makeText(
                    this,
                    R.string.business_card_local_image_invalid,
                    Toast.LENGTH_SHORT
                ).show()
                return@validateImagesAndRun
            }
            val result = Intent().apply {
                putExtra(EXTRA_TEMPLATE_JSON, json)
                putStringArrayListExtra(
                    EXTRA_LOCAL_ASSET_PATHS,
                    ArrayList(localAssetPaths)
                )
            }
            cleanupPrivateAssetsForCompletion(localAssetPaths.toSet())
            setResult(RESULT_OK, result)
            keepPrivateAssetsOnFinish = true
            finish()
        }
    }

    private fun validatedState(): Pair<String, BusinessCardState>? {
        mBinding.businessCardCanvas.getValidationError(checkLocalImages = false)?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            return null
        }
        return try {
            val json = BusinessCardStateCodec.encode(mBinding.businessCardCanvas.getState())
            json to BusinessCardStateCodec.decode(json)
        } catch (error: BusinessCardStateException) {
            Toast.makeText(
                this,
                getString(R.string.business_card_data_invalid, error.message.orEmpty()),
                Toast.LENGTH_LONG
            ).show()
            null
        }
    }

    private fun validateImagesAndRun(onValid: (String, BusinessCardState) -> Unit) {
        if (validatingImages) return
        val validated = validatedState() ?: return
        val pendingPaths = BusinessCardStateCodec.localAssetPaths(validated.second)
            .filterNot { it in verifiedLocalPaths }
        if (pendingPaths.isEmpty()) {
            onValid(validated.first, validated.second)
            return
        }

        validatingImages = true
        lifecycleScope.launch {
            showLoading(false)
            val invalidPath = try {
                withContext(Dispatchers.IO) {
                    pendingPaths.firstOrNull { path ->
                        val ownedPath = assetStore.pathForSource(path)
                        ownedPath == null || inspectLocalImage(ownedPath) == null
                    }
                }
            } finally {
                dismissDialog()
                validatingImages = false
            }
            if (invalidPath != null) {
                Toast.makeText(
                    this@TestTemplateBusinessCardActivity,
                    R.string.business_card_local_image_invalid,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            verifiedLocalPaths.addAll(pendingPaths)
            lifecycle.whenResumed {
                validatedState()?.let { latest -> onValid(latest.first, latest.second) }
            }
        }
    }

    private fun showColorDialog(title: String, currentColor: String, onSelected: (String) -> Unit) {
        val binding = DialogBusinessCardColorBinding.inflate(layoutInflater)
        binding.tvColorDialogTitle.text = title
        val dialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .create()

        val swatches = listOf(
            binding.swatchWhite to "#FFFFFFFF",
            binding.swatchBlack to "#FF111111",
            binding.swatchGray to "#FF9AA5B1",
            binding.swatchBlue to "#FF246BFD",
            binding.swatchRed to "#FFD64545",
            binding.swatchGreen to "#FF258A5B"
        )

        fun selectColor(value: String) {
            binding.etColorHex.setText(value)
            binding.etColorHex.setSelection(value.length)
            swatches.forEach { (button, color) ->
                updateSwatchSelection(button, color, color == value)
            }
        }

        swatches.forEach { (button, color) ->
            button.setOnClickListener { selectColor(color) }
        }
        selectColor(currentColor)
        binding.btnColorCancel.setOnClickListener { dialog.dismiss() }
        binding.btnColorConfirm.setOnClickListener {
            try {
                val normalized = BusinessCardStateCodec.normalizeColor(
                    binding.etColorHex.text?.toString().orEmpty()
                )
                onSelected(normalized)
                dialog.dismiss()
            } catch (_: BusinessCardStateException) {
                binding.etColorHex.error = getString(R.string.business_card_color_invalid)
            }
        }
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.90f).roundToInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.show()
    }

    private fun updateSwatchSelection(
        button: AppCompatImageButton,
        color: String,
        selected: Boolean
    ) {
        button.isSelected = selected
        val tint = if (!selected) {
            Color.TRANSPARENT
        } else if (color == "#FFFFFFFF") {
            Color.rgb(17, 17, 17)
        } else {
            Color.WHITE
        }
        ImageViewCompat.setImageTintList(button, ColorStateList.valueOf(tint))
    }

    private fun setColorPreview(view: View, color: String) {
        view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    private fun importPickedImage(uri: Uri, applyImage: (ImportedImage) -> Unit) {
        lifecycleScope.launch {
            showLoading(false)
            val image = try {
                withContext(Dispatchers.IO) { importSelectedImage(uri) }
            } finally {
                dismissDialog()
            }
            if (image == null) {
                Toast.makeText(
                    this@TestTemplateBusinessCardActivity,
                    R.string.business_card_image_save_failed,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            newlyCreatedAssetPaths.add(image.absolutePath)
            var appliedToCanvas = false
            try {
                lifecycle.whenResumed {
                    verifiedLocalPaths.add(image.absolutePath)
                    applyImage(image)
                    appliedToCanvas = true
                }
            } finally {
                if (!appliedToCanvas) {
                    newlyCreatedAssetPaths.remove(image.absolutePath)
                    verifiedLocalPaths.remove(image.absolutePath)
                    assetStore.deletePath(image.absolutePath)
                }
            }
        }
    }

    private fun clearCanvasBackgroundImage() {
        val previousPath = localBackgroundPath()
        if (mBinding.businessCardCanvas.clearCanvasBackgroundImage()) {
            previousPath?.let(::deleteNewPrivateAssetIfUnused)
        }
    }

    private fun localBackgroundPath(): String? =
        mBinding.businessCardCanvas.getState().canvas.backgroundImage
            ?.takeIf { it.sourceKind == BusinessCardImageSourceKind.LOCAL_PATH }
            ?.sourceValue

    private fun importSelectedImage(sourceUri: Uri): ImportedImage? {
        val asset = try {
            assetStore.importImage(sourceUri)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to save image from ${sourceUri.authority}", error)
            return null
        }
        val aspectRatio = inspectLocalImage(asset.absolutePath)
        if (aspectRatio == null) {
            assetStore.deletePath(asset.absolutePath)
            return null
        }
        return ImportedImage(
            absolutePath = asset.absolutePath,
            aspectRatio = aspectRatio
        )
    }

    private fun inspectLocalImage(path: String): Float? {
        val future = Glide.with(applicationContext)
            .asBitmap()
            .load(File(path))
            .override(IMAGE_INSPECTION_SIZE_PX, IMAGE_INSPECTION_SIZE_PX)
            .submit()
        return try {
            val bitmap = future.get(15, TimeUnit.SECONDS)
            if (bitmap.width > 0 && bitmap.height > 0) {
                bitmap.width.toFloat() / bitmap.height
            } else {
                null
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to decode a private business card image", error)
            null
        } finally {
            Glide.with(applicationContext).clear(future)
        }
    }

    private fun simpleSeekBarListener(onChanged: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
    }

    private fun showInvalidTemplateDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.business_card_invalid_template_title)
            .setMessage(R.string.business_card_invalid_template_message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .show()
    }

    private fun handleBack() {
        if (!hasUnsavedChanges()) {
            deleteAllNewPrivateAssets()
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.business_card_discard_title)
            .setMessage(R.string.business_card_discard_message)
            .setNegativeButton(R.string.business_card_cancel, null)
            .setPositiveButton(R.string.business_card_discard) { _, _ ->
                deleteAllNewPrivateAssets()
                finish()
            }
            .show()
    }

    private fun hasUnsavedChanges(): Boolean {
        return try {
            BusinessCardStateCodec.encode(mBinding.businessCardCanvas.getState()) != initialCanonicalJson
        } catch (_: BusinessCardStateException) {
            true
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        handleBack()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(
            STATE_EDITOR_JSON,
            internalGson.toJson(mBinding.businessCardCanvas.getState())
        )
        outState.putString(STATE_INITIAL_JSON, initialCanonicalJson)
        outState.putString(STATE_LOAD_ERROR, loadError)
        outState.putStringArrayList(STATE_VERIFIED_PATHS, ArrayList(verifiedLocalPaths))
        outState.putStringArrayList(
            STATE_NEW_ASSET_PATHS,
            ArrayList(newlyCreatedAssetPaths)
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations && !keepPrivateAssetsOnFinish) {
            deleteAllNewPrivateAssets()
        }
        super.onDestroy()
    }

    override fun finish() {
        if (suppressBaseRestoreFinish) return
        super.finish()
    }

    private fun deleteNewPrivateAsset(sourceValue: String) {
        val path = assetStore.pathForSource(sourceValue) ?: return
        if (newlyCreatedAssetPaths.remove(path)) {
            verifiedLocalPaths.remove(path)
            assetStore.deletePath(path)
        }
    }

    private fun deleteNewPrivateAssetIfUnused(sourceValue: String) {
        val state = mBinding.businessCardCanvas.getState()
        val stillUsed = state.canvas.backgroundImage?.sourceValue == sourceValue ||
            state.elements.any { it.image?.sourceValue == sourceValue }
        if (!stillUsed) deleteNewPrivateAsset(sourceValue)
    }

    private fun deleteAllNewPrivateAssets() {
        newlyCreatedAssetPaths.toList().forEach { path ->
            assetStore.deletePath(path)
            newlyCreatedAssetPaths.remove(path)
        }
    }

    private fun cleanupPrivateAssetsForCompletion(currentPaths: Set<String>) {
        (newlyCreatedAssetPaths - currentPaths).forEach(assetStore::deletePath)
        newlyCreatedAssetPaths.retainAll(currentPaths)
    }

}
