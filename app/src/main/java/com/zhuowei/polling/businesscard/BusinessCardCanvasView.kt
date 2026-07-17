package com.zhuowei.polling.businesscard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.UUID
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Renders and edits a [BusinessCardState] in one normalized coordinate space.
 *
 * The view owns a defensive copy of the supplied state. Every state exposed through the public
 * API is another copy so callers cannot accidentally bypass bounds checks or change callbacks.
 */
class BusinessCardCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface Listener {
        fun onSelectionChanged(element: BusinessCardElement?)

        fun onStateChanged(state: BusinessCardState)
    }

    private enum class TouchMode {
        NONE,
        DRAG,
        RESIZE
    }

    private data class ExportImageRequest(
        val key: String,
        val source: Any,
        val widthPx: Int,
        val heightPx: Int
    )

    private val density = resources.displayMetrics.density
    private val assetStore = BusinessCardAssetStore(context)
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SELECTION_COLOR
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val handleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val handleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SELECTION_COLOR
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val imagePlaceholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(235, 238, 242)
        style = Paint.Style.FILL
    }
    private val imagePlaceholderStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(165, 171, 180)
        style = Paint.Style.STROKE
        strokeWidth = density
    }

    private var cardState = BusinessCardState()
    private var listener: Listener? = null
    private var selectedElementId: String? = null
    private var editingEnabled = true

    private var touchMode = TouchMode.NONE
    private var activeElementId: String? = null
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var resizeAnchorX = 0f
    private var resizeAnchorY = 0f
    private var gestureStarted = false
    private var gestureChangedState = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val bitmaps = mutableMapOf<String, Bitmap>()
    private val bitmapTargets = mutableMapOf<String, CustomTarget<Bitmap>>()
    private val failedBitmapKeys = mutableSetOf<String>()

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setState(state: BusinessCardState) {
        cardState = state.deepCopy().also { copy ->
            copy.elements.sortBy { it.zIndex }
            copy.elements.forEachIndexed { index, element -> element.zIndex = index }
        }
        selectedElementId = null
        cancelObsoleteImageRequests()
        failedBitmapKeys.clear()
        requestLayout()
        invalidate()
        listener?.onSelectionChanged(null)
    }

    fun getState(): BusinessCardState = cardState.deepCopy()

    fun getSelectedElement(): BusinessCardElement? =
        selectedElement()?.deepCopy()

    fun setEditingEnabled(enabled: Boolean) {
        if (editingEnabled == enabled) return
        editingEnabled = enabled
        if (!enabled) {
            clearSelection()
        } else {
            invalidate()
        }
    }

    fun isEditingEnabled(): Boolean = editingEnabled

    suspend fun exportBitmap(
        targetWidthPx: Int,
        targetHeightPx: Int,
        cornerRadiusPx: Float = 0f
    ): Bitmap {
        require(targetWidthPx > 0 && targetHeightPx > 0) {
            "Export dimensions must be greater than zero"
        }
        require(cornerRadiusPx.isFinite() && cornerRadiusPx >= 0f) {
            "Export corner radius must be finite and non-negative"
        }
        require(targetWidthPx.toLong() * targetHeightPx <= MAX_EXPORT_PIXELS) {
            "Export dimensions are too large"
        }
        require(width > 0 && height > 0) { "Canvas must be laid out before export" }

        val stateSnapshot = getState().also(BusinessCardStateCodec::validate)
        val actualAspectRatio = targetWidthPx.toFloat() / targetHeightPx
        require(abs(actualAspectRatio - stateSnapshot.canvas.aspectRatio) <= EXPORT_ASPECT_TOLERANCE) {
            "Export dimensions do not match the canvas aspect ratio"
        }

        val requestsByKey = linkedMapOf<String, ExportImageRequest>()
        stateSnapshot.elements.forEach { element ->
            val image = element.image ?: return@forEach
            val source = resolveImageSource(image)
                ?: throw IllegalStateException("A local business card image is unavailable")
            val key = image.cacheKey()
            val requestedWidth = (element.widthRatio * targetWidthPx)
                .roundToInt()
                .coerceIn(1, MAX_IMAGE_DECODE_SIZE_PX)
            val requestedHeight = (element.heightRatio * targetHeightPx)
                .roundToInt()
                .coerceIn(1, MAX_IMAGE_DECODE_SIZE_PX)
            val previous = requestsByKey[key]
            requestsByKey[key] = ExportImageRequest(
                key = key,
                source = source,
                widthPx = max(previous?.widthPx ?: 1, requestedWidth),
                heightPx = max(previous?.heightPx ?: 1, requestedHeight)
            )
        }
        require(requestsByKey.size <= MAX_EXPORT_IMAGE_REQUESTS) {
            "The template contains too many unique images to export"
        }
        val requestedImagePixels = requestsByKey.values.fold(0L) { total, request ->
            total + request.widthPx.toLong() * request.heightPx
        }
        require(requestedImagePixels <= MAX_EXPORT_IMAGE_PIXELS) {
            "The template contains too many high-resolution images to export"
        }

        val requestManager = Glide.with(context.applicationContext)
        val futures = mutableListOf<Pair<ExportImageRequest, FutureTarget<Bitmap>>>()
        return try {
            val exportBitmaps = mutableMapOf<String, Bitmap>()
            withTimeout(EXPORT_IMAGE_BATCH_TIMEOUT_MILLIS) {
                withContext(Dispatchers.IO) {
                    requestsByKey.values.forEach { request ->
                        val future = requestManager
                            .asBitmap()
                            .apply(
                                RequestOptions()
                                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                                    .disallowHardwareConfig()
                            )
                            .load(request.source)
                            .override(request.widthPx, request.heightPx)
                            .submit()
                        futures.add(request to future)
                    }
                }
                futures.forEach { (request, future) ->
                    exportBitmaps[request.key] = runInterruptible(Dispatchers.IO) {
                        future.get()
                    }
                }
            }
            withContext(Dispatchers.Main.immediate) {
                val output = Bitmap.createBitmap(
                    targetWidthPx,
                    targetHeightPx,
                    Bitmap.Config.ARGB_8888
                )
                try {
                    val outputCanvas = Canvas(output)
                    if (cornerRadiusPx > 0f) {
                        val bounds = RectF(0f, 0f, targetWidthPx.toFloat(), targetHeightPx.toFloat())
                        val clipPath = Path().apply {
                            addRoundRect(bounds, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
                        }
                        outputCanvas.clipPath(clipPath)
                    }
                    drawState(
                        canvas = outputCanvas,
                        state = stateSnapshot,
                        availableBitmaps = exportBitmaps,
                        loadMissingImages = false,
                        renderWidth = targetWidthPx.toFloat(),
                        renderHeight = targetHeightPx.toFloat()
                    )
                    output
                } catch (error: Throwable) {
                    output.recycle()
                    throw error
                }
            }
        } finally {
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                futures.forEach { (_, future) -> requestManager.clear(future) }
            }
        }
    }

    fun addTextElement(
        value: String = DEFAULT_TEXT,
        fontSizeRatio: Float = DEFAULT_FONT_SIZE_RATIO,
        color: String = DEFAULT_TEXT_COLOR
    ): BusinessCardElement {
        val text = BusinessCardText(
            value = value,
            fontSizeRatio = fontSizeRatio.coerceIn(MIN_FONT_SIZE_RATIO, MAX_FONT_SIZE_RATIO),
            color = BusinessCardStateCodec.normalizeColor(color)
        )
        text.fontSizeRatio = fitFontSize(text, text.fontSizeRatio)
        val size = measureTextRatios(text)
        val element = BusinessCardElement(
            id = UUID.randomUUID().toString(),
            type = BusinessCardElementType.TEXT,
            centerXRatio = 0.5f,
            centerYRatio = 0.5f,
            widthRatio = size.first.coerceIn(MIN_ELEMENT_RATIO, 1f),
            heightRatio = size.second.coerceIn(MIN_ELEMENT_RATIO, 1f),
            zIndex = cardState.elements.size,
            text = text
        )
        clampElementToCanvas(element)
        cardState.elements.add(element)
        selectElement(element.id)
        dispatchStateChanged(selectionMayHaveChanged = false)
        return element.deepCopy()
    }

    fun addImageElement(
        sourceKind: BusinessCardImageSourceKind,
        sourceValue: String,
        intrinsicAspectRatio: Float
    ): BusinessCardElement {
        require(intrinsicAspectRatio.isFinite() && intrinsicAspectRatio > 0f) {
            "intrinsicAspectRatio must be finite and greater than zero"
        }
        val image = BusinessCardImage(
            sourceKind = sourceKind,
            sourceValue = sourceValue,
            intrinsicAspectRatio = intrinsicAspectRatio
        )
        val widthRatio = imageWidthRange(image).let { range ->
            DEFAULT_IMAGE_WIDTH_RATIO.coerceIn(range.start, range.endInclusive)
        }
        val heightRatio = imageHeightRatio(widthRatio, image)
        val element = BusinessCardElement(
            id = UUID.randomUUID().toString(),
            type = BusinessCardElementType.IMAGE,
            centerXRatio = 0.5f,
            centerYRatio = 0.5f,
            widthRatio = widthRatio,
            heightRatio = heightRatio,
            zIndex = cardState.elements.size,
            image = image
        )
        clampElementToCanvas(element)
        cardState.elements.add(element)
        failedBitmapKeys.remove(image.cacheKey())
        selectElement(element.id)
        dispatchStateChanged(selectionMayHaveChanged = false)
        invalidate()
        return element.deepCopy()
    }

    fun addImageElement(
        sourceKind: String,
        sourceValue: String,
        intrinsicAspectRatio: Float
    ): BusinessCardElement {
        val kind = BusinessCardImageSourceKind.fromWireValue(sourceKind)
            ?: throw IllegalArgumentException("Unsupported image source kind: $sourceKind")
        return addImageElement(kind, sourceValue, intrinsicAspectRatio)
    }

    fun updateSelectedText(value: String): Boolean {
        val element = selectedElement()?.takeIf { it.type == BusinessCardElementType.TEXT }
            ?: return false
        val text = element.text ?: return false
        if (text.value == value) return true
        text.value = value
        text.fontSizeRatio = fitFontSize(text, text.fontSizeRatio)
        updateTextGeometry(element)
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun updateSelectedFontSize(fontSizeRatio: Float): Boolean {
        val element = selectedElement()?.takeIf { it.type == BusinessCardElementType.TEXT }
            ?: return false
        val text = element.text ?: return false
        val requested = fontSizeRatio.coerceIn(MIN_FONT_SIZE_RATIO, MAX_FONT_SIZE_RATIO)
        val fitted = fitFontSize(text, requested)
        if (abs(text.fontSizeRatio - fitted) < FLOAT_EPSILON) return true
        text.fontSizeRatio = fitted
        updateTextGeometry(element)
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun getSelectedTextMaxFontSizeRatio(): Float? {
        val text = selectedElement()
            ?.takeIf { it.type == BusinessCardElementType.TEXT }
            ?.text
            ?: return null
        if (requiredTextWidthRatio(text, MIN_FONT_SIZE_RATIO) > 1f) {
            return MIN_FONT_SIZE_RATIO
        }
        return fitFontSize(text, MAX_FONT_SIZE_RATIO)
    }

    fun updateSelectedTextColor(color: String): Boolean {
        val element = selectedElement()?.takeIf { it.type == BusinessCardElementType.TEXT }
            ?: return false
        val text = element.text ?: return false
        val normalized = BusinessCardStateCodec.normalizeColor(color)
        if (text.color == normalized) return true
        text.color = normalized
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun updateSelectedImageWidthRatio(widthRatio: Float): Boolean {
        val element = selectedElement()?.takeIf { it.type == BusinessCardElementType.IMAGE }
            ?: return false
        val image = element.image ?: return false
        val range = imageWidthRange(image)
        element.widthRatio = widthRatio.coerceIn(range.start, range.endInclusive)
        element.heightRatio = imageHeightRatio(element.widthRatio, image)
        clampElementToCanvas(element)
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun getSelectedImageWidthRange(): ClosedFloatingPointRange<Float>? {
        val image = selectedElement()
            ?.takeIf { it.type == BusinessCardElementType.IMAGE }
            ?.image
            ?: return null
        return imageWidthRange(image)
    }

    fun deleteSelected(): Boolean {
        val selectedId = selectedElementId ?: return false
        val removed = cardState.elements.removeAll { it.id == selectedId }
        if (!removed) return false
        selectedElementId = null
        normalizeZIndexes()
        cancelObsoleteImageRequests()
        listener?.onSelectionChanged(null)
        dispatchStateChanged(selectionMayHaveChanged = false)
        return true
    }

    fun moveSelectedForward(): Boolean {
        val index = selectedElementIndex()
        if (index < 0 || index >= cardState.elements.lastIndex) return false
        val selected = cardState.elements.removeAt(index)
        cardState.elements.add(index + 1, selected)
        normalizeZIndexes()
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun moveSelectedBackward(): Boolean {
        val index = selectedElementIndex()
        if (index <= 0) return false
        val selected = cardState.elements.removeAt(index)
        cardState.elements.add(index - 1, selected)
        normalizeZIndexes()
        dispatchStateChanged(selectionMayHaveChanged = true)
        return true
    }

    fun setOrientation(orientation: BusinessCardOrientation) {
        if (cardState.canvas.orientation == orientation) return
        cardState.canvas.orientation = orientation
        cardState.canvas.aspectRatio = orientation.expectedAspectRatio
        cardState.elements.forEach { element ->
            when (element.type) {
                BusinessCardElementType.TEXT -> updateTextGeometry(element)
                BusinessCardElementType.IMAGE -> {
                    val image = element.image ?: return@forEach
                    val range = imageWidthRange(image)
                    element.widthRatio = element.widthRatio.coerceIn(range.start, range.endInclusive)
                    element.heightRatio = imageHeightRatio(element.widthRatio, image)
                    clampElementToCanvas(element)
                }
            }
        }
        requestLayout()
        dispatchStateChanged(selectionMayHaveChanged = true)
    }

    fun setOrientation(orientation: String) {
        val parsed = BusinessCardOrientation.fromWireValue(orientation)
            ?: throw IllegalArgumentException("Unsupported canvas orientation: $orientation")
        setOrientation(parsed)
    }

    fun setCanvasBackgroundColor(color: String) {
        val normalized = BusinessCardStateCodec.normalizeColor(color)
        if (cardState.canvas.backgroundColor == normalized) return
        cardState.canvas.backgroundColor = normalized
        dispatchStateChanged(selectionMayHaveChanged = false)
    }

    fun setCanvasBackgroundColor(color: Int) {
        setCanvasBackgroundColor(color.toArgbHex())
    }

    fun setBackgroundColor(color: String) {
        setCanvasBackgroundColor(color)
    }

    fun clearSelection() {
        if (selectedElementId == null) return
        selectedElementId = null
        activeElementId = null
        touchMode = TouchMode.NONE
        invalidate()
        listener?.onSelectionChanged(null)
    }

    /** Returns a user-facing validation error, or null when the state can be completed. */
    fun getValidationError(checkLocalImages: Boolean = false): String? {
        cardState.elements.forEach { element ->
            when (element.type) {
                BusinessCardElementType.TEXT -> {
                    val text = element.text
                    if (text == null || text.value.isBlank()) {
                        return "文字内容不能为空"
                    }
                    if ('\n' in text.value || '\r' in text.value) {
                        return "文字内容只能输入一行"
                    }
                    if (measureTextRatios(text).first > 1f + FLOAT_EPSILON) {
                        return "文字“${text.value.take(12)}”过长，请缩短内容"
                    }
                }

                BusinessCardElementType.IMAGE -> {
                    val image = element.image
                    if (image == null || image.sourceValue.isBlank()) {
                        return "图片资源无效，请重新选择"
                    }
                    if (checkLocalImages &&
                        image.sourceKind == BusinessCardImageSourceKind.LOCAL_PATH &&
                        !canDecodeLocalImage(image.sourceValue)
                    ) {
                        return "本地图片无法读取，请重新选择"
                    }
                }
            }
        }
        return try {
            BusinessCardStateCodec.validate(getState())
            null
        } catch (error: BusinessCardStateException) {
            "名片数据无效：${error.message.orEmpty()}"
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val aspectRatio = cardState.canvas.aspectRatio.coerceAtLeast(FLOAT_EPSILON)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var measuredWidth = when (widthMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> widthSize
            else -> max(suggestedMinimumWidth, (DEFAULT_CANVAS_WIDTH_DP * density).roundToInt())
        }
        var measuredHeight = (measuredWidth / aspectRatio).roundToInt()

        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize
            if (widthMode != MeasureSpec.EXACTLY) {
                measuredWidth = (measuredHeight * aspectRatio).roundToInt()
            }
        } else if (heightMode == MeasureSpec.AT_MOST && measuredHeight > heightSize) {
            measuredHeight = heightSize
            if (widthMode != MeasureSpec.EXACTLY) {
                measuredWidth = (measuredHeight * aspectRatio).roundToInt()
            }
        }

        measuredWidth = max(measuredWidth, suggestedMinimumWidth)
        measuredHeight = max(measuredHeight, suggestedMinimumHeight)
        setMeasuredDimension(
            resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
            resolveSizeAndState(measuredHeight, heightMeasureSpec, 0)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawState(
            canvas = canvas,
            state = cardState,
            availableBitmaps = bitmaps,
            loadMissingImages = true,
            renderWidth = width.toFloat(),
            renderHeight = height.toFloat()
        )
        if (editingEnabled) {
            selectedElement()?.let { drawSelection(canvas, it) }
        }
    }

    private fun drawState(
        canvas: Canvas,
        state: BusinessCardState,
        availableBitmaps: Map<String, Bitmap>,
        loadMissingImages: Boolean,
        renderWidth: Float,
        renderHeight: Float
    ) {
        canvas.drawColor(parseColor(state.canvas.backgroundColor, Color.WHITE))
        state.elements.sortedBy { it.zIndex }.forEach { element ->
            when (element.type) {
                BusinessCardElementType.TEXT -> drawTextElement(
                    canvas,
                    element,
                    renderWidth,
                    renderHeight
                )
                BusinessCardElementType.IMAGE -> drawImageElement(
                    canvas,
                    element,
                    availableBitmaps,
                    loadMissingImages,
                    renderWidth,
                    renderHeight
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!editingEnabled || !isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_MOVE -> handleTouchMove(event)
            MotionEvent.ACTION_UP -> handleTouchUp(cancelled = false)
            MotionEvent.ACTION_CANCEL -> handleTouchUp(cancelled = true)
            else -> return true
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        bitmapTargets.values.toList().forEach { target -> Glide.with(context).clear(target) }
        bitmapTargets.clear()
        super.onDetachedFromWindow()
    }

    private fun handleTouchDown(event: MotionEvent) {
        downX = event.x
        downY = event.y
        lastX = event.x
        lastY = event.y
        gestureStarted = false
        gestureChangedState = false

        val currentSelection = selectedElement()
        if (currentSelection?.type == BusinessCardElementType.IMAGE &&
            isOnResizeHandle(currentSelection, event.x, event.y)
        ) {
            activeElementId = currentSelection.id
            val bounds = elementBounds(currentSelection)
            resizeAnchorX = bounds.left
            resizeAnchorY = bounds.top
            touchMode = TouchMode.RESIZE
            parent?.requestDisallowInterceptTouchEvent(true)
            return
        }

        val hit = findTopElementAt(event.x, event.y)
        if (hit == null) {
            clearSelection()
            touchMode = TouchMode.NONE
            activeElementId = null
            return
        }

        selectElement(hit.id)
        activeElementId = hit.id
        touchMode = TouchMode.DRAG
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    private fun handleTouchMove(event: MotionEvent) {
        val element = activeElement() ?: return
        if (!gestureStarted) {
            if (hypot(event.x - downX, event.y - downY) <= touchSlop) return
            gestureStarted = true
        }
        when (touchMode) {
            TouchMode.DRAG -> {
                val oldCenterX = element.centerXRatio
                val oldCenterY = element.centerYRatio
                val deltaXRatio = if (width > 0) (event.x - lastX) / width else 0f
                val deltaYRatio = if (height > 0) (event.y - lastY) / height else 0f
                if (deltaXRatio != 0f || deltaYRatio != 0f) {
                    element.centerXRatio += deltaXRatio
                    element.centerYRatio += deltaYRatio
                    clampElementToCanvas(element)
                    if (abs(element.centerXRatio - oldCenterX) >= FLOAT_EPSILON ||
                        abs(element.centerYRatio - oldCenterY) >= FLOAT_EPSILON
                    ) {
                        gestureChangedState = true
                        invalidate()
                    }
                }
            }

            TouchMode.RESIZE -> {
                val image = element.image ?: return
                val oldCenterX = element.centerXRatio
                val oldCenterY = element.centerYRatio
                val oldWidth = element.widthRatio
                val oldHeight = element.heightRatio
                val imageAspectRatio = image.intrinsicAspectRatio
                val diagonalLength = hypot(imageAspectRatio, 1f)
                val unitX = imageAspectRatio / diagonalLength
                val unitY = 1f / diagonalLength
                val projectedLength = max(
                    0f,
                    (event.x - resizeAnchorX) * unitX +
                        (event.y - resizeAnchorY) * unitY
                )
                var widthPx = projectedLength * unitX
                val logicalRange = imageWidthRange(image)
                val minWidthPx = logicalRange.start * width
                val maxWidthPxByState = logicalRange.endInclusive * width
                val maxWidthPxByAnchor = min(
                    width - resizeAnchorX,
                    (height - resizeAnchorY) * imageAspectRatio
                )
                widthPx = widthPx.coerceIn(
                    minWidthPx.coerceAtMost(maxWidthPxByAnchor),
                    min(maxWidthPxByState, maxWidthPxByAnchor).coerceAtLeast(minWidthPx.coerceAtMost(maxWidthPxByAnchor))
                )
                val heightPx = widthPx / imageAspectRatio
                element.widthRatio = if (width > 0) widthPx / width else element.widthRatio
                element.heightRatio = if (height > 0) heightPx / height else element.heightRatio
                element.centerXRatio = if (width > 0) (resizeAnchorX + widthPx / 2f) / width else 0.5f
                element.centerYRatio = if (height > 0) (resizeAnchorY + heightPx / 2f) / height else 0.5f
                clampElementToCanvas(element)
                if (abs(element.centerXRatio - oldCenterX) >= FLOAT_EPSILON ||
                    abs(element.centerYRatio - oldCenterY) >= FLOAT_EPSILON ||
                    abs(element.widthRatio - oldWidth) >= FLOAT_EPSILON ||
                    abs(element.heightRatio - oldHeight) >= FLOAT_EPSILON
                ) {
                    gestureChangedState = true
                    invalidate()
                }
            }

            TouchMode.NONE -> Unit
        }
        lastX = event.x
        lastY = event.y
    }

    private fun handleTouchUp(cancelled: Boolean) {
        val stateChanged = gestureChangedState
        val wasGesture = gestureStarted
        touchMode = TouchMode.NONE
        activeElementId = null
        gestureStarted = false
        gestureChangedState = false
        parent?.requestDisallowInterceptTouchEvent(false)
        if (stateChanged) {
            dispatchStateChanged(selectionMayHaveChanged = true)
        } else if (!cancelled && !wasGesture) {
            performClick()
        }
    }

    private fun drawTextElement(
        canvas: Canvas,
        element: BusinessCardElement,
        renderWidth: Float,
        renderHeight: Float
    ) {
        val text = element.text ?: return
        configureTextPaint(text, min(renderWidth, renderHeight))
        val bounds = elementBounds(element, renderWidth, renderHeight)
        val fontMetrics = textPaint.fontMetrics
        val baseline = bounds.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        val inset = textPaint.textSize * TEXT_HORIZONTAL_PADDING_FACTOR
        textPaint.textAlign = when (text.alignment) {
            BusinessCardTextAlignment.START -> Paint.Align.LEFT
            BusinessCardTextAlignment.CENTER -> Paint.Align.CENTER
            BusinessCardTextAlignment.END -> Paint.Align.RIGHT
        }
        val textX = when (text.alignment) {
            BusinessCardTextAlignment.START -> bounds.left + inset
            BusinessCardTextAlignment.CENTER -> bounds.centerX()
            BusinessCardTextAlignment.END -> bounds.right - inset
        }
        canvas.save()
        canvas.clipRect(bounds)
        canvas.drawText(text.value.replace('\n', ' ').replace('\r', ' '), textX, baseline, textPaint)
        canvas.restore()
    }

    private fun drawImageElement(
        canvas: Canvas,
        element: BusinessCardElement,
        availableBitmaps: Map<String, Bitmap>,
        loadMissingImage: Boolean,
        renderWidth: Float,
        renderHeight: Float
    ) {
        val image = element.image ?: return
        val bounds = elementBounds(element, renderWidth, renderHeight)
        val key = image.cacheKey()
        val bitmap = availableBitmaps[key]
        if (bitmap == null || bitmap.isRecycled) {
            if (loadMissingImage) {
                drawImagePlaceholder(canvas, bounds)
                ensureBitmapLoaded(image, bounds)
            }
            return
        }

        val bitmapAspect = bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1)
        val targetAspect = bounds.width() / bounds.height().coerceAtLeast(1f)
        val destination = if (bitmapAspect > targetAspect) {
            val targetHeight = bounds.width() / bitmapAspect
            RectF(bounds.left, bounds.centerY() - targetHeight / 2f, bounds.right, bounds.centerY() + targetHeight / 2f)
        } else {
            val targetWidth = bounds.height() * bitmapAspect
            RectF(bounds.centerX() - targetWidth / 2f, bounds.top, bounds.centerX() + targetWidth / 2f, bounds.bottom)
        }
        canvas.drawBitmap(bitmap, null, destination, null)
    }

    private fun drawImagePlaceholder(canvas: Canvas, bounds: RectF) {
        canvas.drawRect(bounds, imagePlaceholderPaint)
        canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.bottom, imagePlaceholderStrokePaint)
        canvas.drawLine(bounds.right, bounds.top, bounds.left, bounds.bottom, imagePlaceholderStrokePaint)
    }

    private fun drawSelection(canvas: Canvas, element: BusinessCardElement) {
        val bounds = elementBounds(element)
        canvas.drawRect(bounds, selectionPaint)
        if (element.type == BusinessCardElementType.IMAGE) {
            val radius = RESIZE_HANDLE_RADIUS_DP * density
            canvas.drawCircle(bounds.right, bounds.bottom, radius, handleFillPaint)
            canvas.drawCircle(bounds.right, bounds.bottom, radius, handleStrokePaint)
        }
    }

    private fun configureTextPaint(text: BusinessCardText, shortSide: Float = min(width, height).toFloat()) {
        textPaint.textSize = text.fontSizeRatio * logicalShortSide(shortSide)
        textPaint.color = parseColor(text.color, Color.rgb(17, 17, 17))
        textPaint.typeface = createTypeface(text.fontFamily, text.fontWeight)
    }

    private fun createTypeface(fontFamily: String, fontWeight: Int): Typeface {
        val base = Typeface.create(fontFamily, Typeface.NORMAL)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(base, fontWeight.coerceIn(100, 900), false)
        } else {
            Typeface.create(base, if (fontWeight >= 600) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private fun updateTextGeometry(element: BusinessCardElement) {
        val text = element.text ?: return
        text.fontSizeRatio = fitFontSize(text, text.fontSizeRatio)
        val size = measureTextRatios(text)
        element.widthRatio = size.first.coerceIn(MIN_ELEMENT_RATIO, 1f)
        element.heightRatio = size.second.coerceIn(MIN_ELEMENT_RATIO, 1f)
        clampElementToCanvas(element)
    }

    private fun fitFontSize(text: BusinessCardText, requested: Float): Float {
        val clampedRequest = requested.coerceIn(MIN_FONT_SIZE_RATIO, MAX_FONT_SIZE_RATIO)
        if (requiredTextWidthRatio(text, clampedRequest) <= 1f) return clampedRequest
        if (requiredTextWidthRatio(text, MIN_FONT_SIZE_RATIO) > 1f) return MIN_FONT_SIZE_RATIO

        var low = MIN_FONT_SIZE_RATIO
        var high = clampedRequest
        repeat(16) {
            val middle = (low + high) / 2f
            if (requiredTextWidthRatio(text, middle) <= 1f) {
                low = middle
            } else {
                high = middle
            }
        }
        return low.coerceIn(MIN_FONT_SIZE_RATIO, MAX_FONT_SIZE_RATIO)
    }

    private fun requiredTextWidthRatio(text: BusinessCardText, fontSizeRatio: Float): Float {
        val copy = text.copy(fontSizeRatio = fontSizeRatio)
        return measureTextRatios(copy).first
    }

    private fun measureTextRatios(text: BusinessCardText): Pair<Float, Float> {
        val logicalSize = logicalCanvasSize()
        configureTextPaint(text, min(logicalSize.first, logicalSize.second))
        val displayValue = text.value.replace('\n', ' ').replace('\r', ' ')
        val horizontalPadding = textPaint.textSize * TEXT_HORIZONTAL_PADDING_FACTOR * 2f
        val verticalPadding = textPaint.textSize * TEXT_VERTICAL_PADDING_FACTOR * 2f
        val fontMetrics = textPaint.fontMetrics
        val measuredWidth = textPaint.measureText(displayValue) + horizontalPadding
        val measuredHeight = fontMetrics.descent - fontMetrics.ascent + verticalPadding
        return measuredWidth / logicalSize.first to measuredHeight / logicalSize.second
    }

    private fun logicalCanvasSize(): Pair<Float, Float> {
        val aspect = cardState.canvas.aspectRatio.coerceAtLeast(FLOAT_EPSILON)
        return if (aspect >= 1f) {
            LOGICAL_SHORT_SIDE * aspect to LOGICAL_SHORT_SIDE
        } else {
            LOGICAL_SHORT_SIDE to LOGICAL_SHORT_SIDE / aspect
        }
    }

    private fun logicalShortSide(candidate: Float): Float {
        return if (candidate > 0f) candidate else LOGICAL_SHORT_SIDE
    }

    private fun imageWidthRange(image: BusinessCardImage): ClosedFloatingPointRange<Float> {
        val canvasAspect = cardState.canvas.aspectRatio.coerceAtLeast(FLOAT_EPSILON)
        val minWidth = (MIN_IMAGE_SHORT_SIDE_RATIO * min(canvasAspect, 1f) / canvasAspect)
            .coerceIn(MIN_ELEMENT_RATIO, 1f)
        val maxWidth = min(1f, image.intrinsicAspectRatio / canvasAspect)
            .coerceIn(MIN_ELEMENT_RATIO, 1f)
        return min(minWidth, maxWidth)..maxWidth
    }

    private fun imageHeightRatio(widthRatio: Float, image: BusinessCardImage): Float =
        (widthRatio * cardState.canvas.aspectRatio / image.intrinsicAspectRatio)
            .coerceIn(MIN_ELEMENT_RATIO, 1f)

    private fun clampElementToCanvas(element: BusinessCardElement) {
        element.widthRatio = element.widthRatio.coerceIn(MIN_ELEMENT_RATIO, 1f)
        element.heightRatio = element.heightRatio.coerceIn(MIN_ELEMENT_RATIO, 1f)
        val halfWidth = element.widthRatio / 2f
        val halfHeight = element.heightRatio / 2f
        element.centerXRatio = element.centerXRatio.coerceIn(halfWidth, 1f - halfWidth)
        element.centerYRatio = element.centerYRatio.coerceIn(halfHeight, 1f - halfHeight)
    }

    private fun elementBounds(element: BusinessCardElement): RectF =
        elementBounds(element, width.toFloat(), height.toFloat())

    private fun elementBounds(
        element: BusinessCardElement,
        renderWidth: Float,
        renderHeight: Float
    ): RectF {
        val centerX = element.centerXRatio * renderWidth
        val centerY = element.centerYRatio * renderHeight
        val halfWidth = element.widthRatio * renderWidth / 2f
        val halfHeight = element.heightRatio * renderHeight / 2f
        return RectF(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight)
    }

    private fun findTopElementAt(x: Float, y: Float): BusinessCardElement? =
        cardState.elements.asReversed().firstOrNull { elementBounds(it).contains(x, y) }

    private fun isOnResizeHandle(element: BusinessCardElement, x: Float, y: Float): Boolean {
        val bounds = elementBounds(element)
        val hitRadius = RESIZE_HANDLE_HIT_RADIUS_DP * density
        return hypot(x - bounds.right, y - bounds.bottom) <= hitRadius
    }

    private fun selectElement(id: String) {
        if (selectedElementId == id) return
        selectedElementId = id
        invalidate()
        listener?.onSelectionChanged(getSelectedElement())
    }

    private fun selectedElement(): BusinessCardElement? =
        selectedElementId?.let { id -> cardState.elements.firstOrNull { it.id == id } }

    private fun activeElement(): BusinessCardElement? =
        activeElementId?.let { id -> cardState.elements.firstOrNull { it.id == id } }

    private fun selectedElementIndex(): Int =
        selectedElementId?.let { id -> cardState.elements.indexOfFirst { it.id == id } } ?: -1

    private fun normalizeZIndexes() {
        cardState.elements.forEachIndexed { index, element -> element.zIndex = index }
    }

    private fun dispatchStateChanged(selectionMayHaveChanged: Boolean) {
        invalidate()
        listener?.onStateChanged(getState())
        if (selectionMayHaveChanged) {
            listener?.onSelectionChanged(getSelectedElement())
        }
    }

    private fun ensureBitmapLoaded(image: BusinessCardImage, bounds: RectF) {
        val key = image.cacheKey()
        if (key in bitmaps || key in bitmapTargets || key in failedBitmapKeys || !isAttachedToWindow) {
            return
        }
        val source = resolveImageSource(image)
        if (source == null) {
            failedBitmapKeys.add(key)
            return
        }
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmaps[key] = resource
                invalidate()
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                bitmapTargets.remove(key)
                failedBitmapKeys.add(key)
                invalidate()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                bitmapTargets.remove(key)
                bitmaps.remove(key)
            }
        }
        bitmapTargets[key] = target
        val targetWidth = bounds.width().roundToInt().coerceIn(1, MAX_IMAGE_DECODE_SIZE_PX)
        val targetHeight = bounds.height().roundToInt().coerceIn(1, MAX_IMAGE_DECODE_SIZE_PX)
        Glide.with(this)
            .asBitmap()
            .apply(RequestOptions().downsample(DownsampleStrategy.CENTER_INSIDE))
            .load(source)
            .override(targetWidth, targetHeight)
            .into(target)
    }

    private fun cancelObsoleteImageRequests() {
        val activeKeys = cardState.elements.mapNotNull { it.image?.cacheKey() }.toSet()
        bitmapTargets.filterKeys { it !in activeKeys }.values.toList().forEach { target ->
            Glide.with(context).clear(target)
        }
        bitmapTargets.keys.retainAll(activeKeys)
        bitmaps.keys.retainAll(activeKeys)
        failedBitmapKeys.retainAll(activeKeys)
    }

    private fun canDecodeLocalImage(sourceValue: String): Boolean {
        return try {
            val path = assetStore.pathForSource(sourceValue) ?: return false
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            options.outWidth > 0 && options.outHeight > 0
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveImageSource(image: BusinessCardImage): Any? {
        return when (image.sourceKind) {
            BusinessCardImageSourceKind.LOCAL_PATH ->
                assetStore.pathForSource(image.sourceValue)?.let(::File)
            BusinessCardImageSourceKind.REMOTE_URL -> image.sourceValue
        }
    }

    private fun parseColor(value: String, fallback: Int): Int =
        try {
            Color.parseColor(value)
        } catch (_: IllegalArgumentException) {
            fallback
        }

    private fun Int.toArgbHex(): String = String.format("#%08X", this)

    private fun BusinessCardImage.cacheKey(): String = "$sourceKind|$sourceValue"

    private fun BusinessCardState.deepCopy(): BusinessCardState = copy(
        canvas = canvas.copy(),
        elements = elements.map { it.deepCopy() }.toMutableList()
    )

    private fun BusinessCardElement.deepCopy(): BusinessCardElement = copy(
        text = text?.copy(),
        image = image?.copy()
    )

    companion object {
        private const val DEFAULT_TEXT = "文字"
        private const val DEFAULT_TEXT_COLOR = "#FF111111"
        private const val DEFAULT_FONT_SIZE_RATIO = 0.08f
        private const val DEFAULT_IMAGE_WIDTH_RATIO = 0.30f
        private const val MIN_FONT_SIZE_RATIO = 0.03f
        private const val MAX_FONT_SIZE_RATIO = 0.20f
        private const val MIN_IMAGE_SHORT_SIDE_RATIO = 0.08f
        private const val MIN_ELEMENT_RATIO = 0.0001f
        private const val LOGICAL_SHORT_SIDE = 1000f
        private const val TEXT_HORIZONTAL_PADDING_FACTOR = 0.12f
        private const val TEXT_VERTICAL_PADDING_FACTOR = 0.05f
        private const val DEFAULT_CANVAS_WIDTH_DP = 320f
        private const val RESIZE_HANDLE_RADIUS_DP = 7f
        private const val RESIZE_HANDLE_HIT_RADIUS_DP = 24f
        private const val MAX_IMAGE_DECODE_SIZE_PX = 2048
        private const val MAX_EXPORT_PIXELS = 8_000_000L
        private const val MAX_EXPORT_IMAGE_REQUESTS = 64
        private const val MAX_EXPORT_IMAGE_PIXELS = 16_000_000L
        private const val EXPORT_IMAGE_BATCH_TIMEOUT_MILLIS = 30_000L
        private const val EXPORT_ASPECT_TOLERANCE = 0.001f
        private const val FLOAT_EPSILON = 0.00001f
        private const val SELECTION_COLOR = 0xFF1976D2.toInt()
    }
}
