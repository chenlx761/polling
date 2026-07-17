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
import com.chenming.common.base.empty.EmptyViewModel
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.zhuowei.polling.R
import com.zhuowei.polling.base.MyBaseActivity
import com.zhuowei.polling.businesscard.BusinessCardAssetStore
import com.zhuowei.polling.businesscard.BusinessCardCanvasView
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
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TestTemplateBusinessCardActivity :
    MyBaseActivity<EmptyViewModel, ActivityTestTemplateBusinessCardBinding>() {

    companion object {
        const val EXTRA_INITIAL_TEMPLATE_JSON = "extra_initial_template_json"
        const val EXTRA_TEMPLATE_JSON = "extra_template_json"
        const val EXTRA_LOCAL_ASSET_URIS = "extra_local_asset_uris"
        const val EXTRA_LOCAL_ASSET_PATHS = "extra_local_asset_paths"

        private const val FONT_SIZE_MIN_RATIO = 0.03f
        private const val FONT_SIZE_PROGRESS_SCALE = 1000f
        private const val IMAGE_INSPECTION_SIZE_PX = 1024
        private const val TAG = "BusinessCardEditor"
        private const val STATE_EDITOR_JSON = "state_editor_json"
        private const val STATE_INITIAL_JSON = "state_initial_json"
        private const val STATE_LOAD_ERROR = "state_load_error"
        private const val STATE_VERIFIED_URIS = "state_verified_uris"
        private const val STATE_NEW_URIS = "state_new_uris"
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
        val sourceValue: String,
        val absolutePath: String,
        val aspectRatio: Float
    )

    private data class CompletionPayload(
        val json: String,
        val localAssetUris: List<String>,
        val localAssetPaths: List<String>
    )

    private var initialState = BusinessCardState()
    private var initialCanonicalJson = ""
    private var loadError: String? = null
    private var syncingControls = false
    private var restoredEditorJson: String? = null
    private var restoredInitialJson: String? = null
    private var restoredLoadError: String? = null
    private var validatingImages = false
    private var completing = false
    private var keepUriPermissionsOnFinish = false
    private var suppressBaseRestoreFinish = false
    private val internalGson = Gson()
    private val assetStore by lazy { BusinessCardAssetStore(this) }
    private val verifiedLocalUris = mutableSetOf<String>()
    private val newlyPersistedUris = mutableSetOf<String>()
    private val newlyCreatedAssetPaths = mutableSetOf<String>()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val acquiredPermission = persistReadPermissionForImport(uri)
        lifecycleScope.launch {
            showLoading(false)
            var createdPath: String? = null
            var importedImage: ImportedImage? = null
            try {
                importedImage = withContext(Dispatchers.IO) {
                    importSelectedImage(uri).also { imported ->
                        createdPath = imported?.absolutePath
                    }
                }
            } finally {
                if (importedImage == null) createdPath?.let(assetStore::deletePath)
                if (acquiredPermission) releasePersistedUri(uri.toString())
                dismissDialog()
            }
            val image = importedImage
            if (image == null) {
                Toast.makeText(
                    this@TestTemplateBusinessCardActivity,
                    R.string.business_card_image_save_failed,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            newlyCreatedAssetPaths.add(image.absolutePath)
            var addedToCanvas = false
            try {
                lifecycle.whenResumed {
                    verifiedLocalUris.add(image.sourceValue)
                    mBinding.businessCardCanvas.addImageElement(
                        sourceKind = BusinessCardImageSourceKind.LOCAL_URI,
                        sourceValue = image.sourceValue,
                        intrinsicAspectRatio = image.aspectRatio
                    )
                    addedToCanvas = true
                }
            } finally {
                if (!addedToCanvas) {
                    newlyCreatedAssetPaths.remove(image.absolutePath)
                    assetStore.deletePath(image.absolutePath)
                }
            }
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
        verifiedLocalUris.addAll(
            savedInstanceState?.getStringArrayList(STATE_VERIFIED_URIS).orEmpty()
        )
        newlyPersistedUris.addAll(
            savedInstanceState?.getStringArrayList(STATE_NEW_URIS).orEmpty()
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
            initialState = try {
                internalGson.fromJson(restored, BusinessCardState::class.java)
                    ?: BusinessCardState()
            } catch (_: Exception) {
                BusinessCardState()
            }
            initialCanonicalJson = restoredInitialJson.orEmpty()
            loadError = restoredLoadError
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
            }
        })
        mBinding.businessCardCanvas.setState(initialState)
        syncOrientation(initialState.canvas.orientation)
        updateCanvasRatio(initialState.canvas.orientation)
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
        mBinding.btnCanvasColor.setOnClickListener {
            val current = mBinding.businessCardCanvas.getState().canvas.backgroundColor
            showColorDialog(getString(R.string.business_card_canvas_color), current) { color ->
                mBinding.businessCardCanvas.setCanvasBackgroundColor(color)
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
                ?.takeIf { it.sourceKind == BusinessCardImageSourceKind.LOCAL_URI }
                ?.sourceValue
            if (mBinding.businessCardCanvas.deleteSelected() && removedSource != null) {
                val stillUsed = mBinding.businessCardCanvas.getState().elements
                    .any { it.image?.sourceValue == removedSource }
                if (!stillUsed) {
                    releasePersistedUri(removedSource)
                    deleteNewPrivateAsset(removedSource)
                }
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

    private fun previewCard() {
        validateImagesAndRun { json, _ ->
            if (isFinishing || supportFragmentManager.isStateSaved) return@validateImagesAndRun
            mBinding.businessCardCanvas.clearSelection()
            BusinessCardPreviewDialog.newInstance(json)
                .show(supportFragmentManager, BusinessCardPreviewDialog.TAG)
        }
    }

    private fun completeEditing() {
        if (completing) return
        validateImagesAndRun { _, state ->
            if (isFinishing || completing) return@validateImagesAndRun
            completing = true
            lifecycleScope.launch {
                showLoading(false)
                val importedPaths = mutableSetOf<String>()
                var payload: CompletionPayload? = null
                try {
                    payload = withContext(Dispatchers.IO) {
                        prepareCompletionPayload(state, importedPaths)
                    }
                } finally {
                    if (payload == null) importedPaths.forEach(assetStore::deletePath)
                    dismissDialog()
                    completing = false
                }

                val completion = payload
                if (completion == null) {
                    Toast.makeText(
                        this@TestTemplateBusinessCardActivity,
                        R.string.business_card_image_save_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                newlyCreatedAssetPaths.addAll(importedPaths)
                var resultDelivered = false
                try {
                    lifecycle.whenResumed {
                        val result = Intent().apply {
                            putExtra(EXTRA_TEMPLATE_JSON, completion.json)
                            putStringArrayListExtra(
                                EXTRA_LOCAL_ASSET_URIS,
                                ArrayList(completion.localAssetUris)
                            )
                            putStringArrayListExtra(
                                EXTRA_LOCAL_ASSET_PATHS,
                                ArrayList(completion.localAssetPaths)
                            )
                        }
                        cleanupPrivateAssetsForCompletion(
                            completion.localAssetPaths.toSet()
                        )
                        setResult(RESULT_OK, result)
                        keepUriPermissionsOnFinish = true
                        resultDelivered = true
                        finish()
                    }
                } finally {
                    if (!resultDelivered) {
                        importedPaths.forEach(assetStore::deletePath)
                        newlyCreatedAssetPaths.removeAll(importedPaths)
                    }
                }
            }
        }
    }

    private fun validatedState(): Pair<String, BusinessCardState>? {
        mBinding.businessCardCanvas.getValidationError(checkLocalImages = false)?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            return null
        }
        return try {
            val state = mBinding.businessCardCanvas.getState()
            BusinessCardStateCodec.encode(state) to state
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
        val pendingUris = BusinessCardStateCodec.localAssetUris(validated.second)
            .filterNot { it in verifiedLocalUris }
        if (pendingUris.isEmpty()) {
            onValid(validated.first, validated.second)
            return
        }

        validatingImages = true
        lifecycleScope.launch {
            showLoading(false)
            val invalidUri = try {
                withContext(Dispatchers.IO) {
                    pendingUris.firstOrNull { inspectLocalImage(Uri.parse(it)) == null }
                }
            } finally {
                dismissDialog()
                validatingImages = false
            }
            if (invalidUri != null) {
                Toast.makeText(
                    this@TestTemplateBusinessCardActivity,
                    R.string.business_card_local_image_invalid,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            verifiedLocalUris.addAll(pendingUris)
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

    private fun importSelectedImage(sourceUri: Uri): ImportedImage? {
        val asset = try {
            assetStore.importImage(sourceUri)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to save image from ${sourceUri.authority}", error)
            return null
        }
        val aspectRatio = inspectLocalImage(asset.contentUri)
        if (aspectRatio == null) {
            assetStore.deletePath(asset.absolutePath)
            return null
        }
        return ImportedImage(
            sourceValue = asset.contentUri.toString(),
            absolutePath = asset.absolutePath,
            aspectRatio = aspectRatio
        )
    }

    private fun prepareCompletionPayload(
        state: BusinessCardState,
        importedPaths: MutableSet<String>
    ): CompletionPayload? {
        val resultState = try {
            BusinessCardStateCodec.decode(BusinessCardStateCodec.encode(state))
        } catch (error: BusinessCardStateException) {
            Log.w(TAG, "Failed to prepare the business card result", error)
            return null
        }

        val replacements = mutableMapOf<String, ImportedImage>()
        BusinessCardStateCodec.localAssetUris(resultState).forEach { sourceValue ->
            if (assetStore.pathForSource(sourceValue) != null) return@forEach
            val imported = importSelectedImage(Uri.parse(sourceValue)) ?: return null
            replacements[sourceValue] = imported
            importedPaths.add(imported.absolutePath)
        }

        if (replacements.isNotEmpty()) {
            resultState.elements.forEach { element ->
                val image = element.image ?: return@forEach
                if (image.sourceKind != BusinessCardImageSourceKind.LOCAL_URI) return@forEach
                val replacement = replacements[image.sourceValue] ?: return@forEach
                image.sourceValue = replacement.sourceValue
                image.intrinsicAspectRatio = replacement.aspectRatio
            }
        }

        return try {
            val json = BusinessCardStateCodec.encode(resultState)
            val localAssetUris = BusinessCardStateCodec.localAssetUris(resultState)
            val localAssetPaths = localAssetUris.map { sourceValue ->
                assetStore.pathForSource(sourceValue) ?: return null
            }
            CompletionPayload(json, localAssetUris, localAssetPaths)
        } catch (error: BusinessCardStateException) {
            Log.w(TAG, "Failed to encode the business card result", error)
            null
        }
    }

    private fun persistReadPermissionForImport(uri: Uri): Boolean {
        if (hasPersistedReadPermission(uri)) return false
        return try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            newlyPersistedUris.add(uri.toString())
            true
        } catch (error: Exception) {
            // The temporary OpenDocument grant is sufficient for the immediate private copy.
            Log.w(TAG, "Provider does not support persistent access: ${uri.authority}", error)
            false
        }
    }

    private fun inspectLocalImage(uri: Uri): Float? {
        val future = Glide.with(applicationContext)
            .asBitmap()
            .load(uri)
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
            Log.w(TAG, "Failed to decode image from ${uri.authority}", error)
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
            releaseAllNewUriPermissions()
            deleteAllNewPrivateAssets()
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.business_card_discard_title)
            .setMessage(R.string.business_card_discard_message)
            .setNegativeButton(R.string.business_card_cancel, null)
            .setPositiveButton(R.string.business_card_discard) { _, _ ->
                releaseAllNewUriPermissions()
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
        outState.putStringArrayList(STATE_VERIFIED_URIS, ArrayList(verifiedLocalUris))
        outState.putStringArrayList(STATE_NEW_URIS, ArrayList(newlyPersistedUris))
        outState.putStringArrayList(
            STATE_NEW_ASSET_PATHS,
            ArrayList(newlyCreatedAssetPaths)
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations && !keepUriPermissionsOnFinish) {
            releaseAllNewUriPermissions()
            deleteAllNewPrivateAssets()
        }
        super.onDestroy()
    }

    override fun finish() {
        if (suppressBaseRestoreFinish) return
        super.finish()
    }

    private fun releasePersistedUri(uriValue: String) {
        if (newlyPersistedUris.remove(uriValue)) {
            try {
                contentResolver.releasePersistableUriPermission(
                    Uri.parse(uriValue),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
        }
        verifiedLocalUris.remove(uriValue)
    }

    private fun hasPersistedReadPermission(uri: Uri): Boolean {
        return contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }
    }

    private fun releaseAllNewUriPermissions() {
        newlyPersistedUris.toList().forEach(::releasePersistedUri)
    }

    private fun deleteNewPrivateAsset(sourceValue: String) {
        val path = assetStore.pathForSource(sourceValue) ?: return
        if (newlyCreatedAssetPaths.remove(path)) {
            assetStore.deletePath(path)
        }
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
