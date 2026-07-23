package com.zhuowei.polling.businesscard

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.IOException
import java.io.StringReader
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale
import kotlin.math.abs

class BusinessCardStateException(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)

typealias BusinessCardValidationException = BusinessCardStateException

object BusinessCardStateCodec {
    private const val LEGACY_SCHEMA_VERSION = 1
    private const val LEGACY_LOCAL_URI_WIRE_VALUE = "local_uri"
    private const val LOCAL_ASSET_DIRECTORY_NAME = "business_card_assets"
    private const val MIN_FONT_SIZE_RATIO = 0.03f
    private const val MAX_FONT_SIZE_RATIO = 0.20f
    private const val FLOAT_TOLERANCE = 0.00001f
    private const val ASPECT_RATIO_TOLERANCE = 0.001f
    private val colorPattern = Regex("^#(?:[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

    fun toJson(state: BusinessCardState): String = gson.toJson(normalizeAndValidate(state))

    fun encode(state: BusinessCardState): String = toJson(state)

    fun fromJson(json: String): BusinessCardState {
        if (json.isBlank()) {
            fail("Template JSON must not be blank")
        }

        val root = parseStrictJson(json)
        if (!root.isJsonObject) {
            fail("Template JSON root must be an object")
        }

        return normalizeAndValidate(parseState(root.asJsonObject))
    }

    fun decode(json: String): BusinessCardState = fromJson(json)

    fun validate(state: BusinessCardState) {
        normalizeAndValidate(state)
    }

    fun normalizeColor(color: String): String {
        if (!colorPattern.matches(color)) {
            fail("Color must use #RRGGBB or #AARRGGBB format")
        }
        val upperCase = color.uppercase(Locale.US)
        return if (upperCase.length == 7) "#FF${upperCase.substring(1)}" else upperCase
    }

    fun localAssetPaths(state: BusinessCardState): List<String> {
        val normalized = normalizeAndValidate(state)
        return sequenceOf(normalized.canvas.backgroundImage)
            .plus(normalized.elements.asSequence().mapNotNull { it.image })
            .filterNotNull()
            .filter { it.sourceKind == BusinessCardImageSourceKind.LOCAL_PATH }
            .map { it.sourceValue }
            .distinct()
            .toList()
    }

    private fun parseStrictJson(json: String): JsonElement {
        val reader = JsonReader(StringReader(json)).apply { isLenient = false }
        return try {
            val root = gson.getAdapter(JsonElement::class.java).read(reader)
                ?: fail("Template JSON must contain a value")
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                fail("Template JSON must contain exactly one root value")
            }
            root
        } catch (error: JsonParseException) {
            throw BusinessCardStateException("Template JSON is malformed", error)
        } catch (error: IOException) {
            throw BusinessCardStateException("Template JSON is malformed", error)
        } catch (error: IllegalStateException) {
            throw BusinessCardStateException("Template JSON is malformed", error)
        } catch (error: NumberFormatException) {
            throw BusinessCardStateException("Template JSON is malformed", error)
        }
    }

    private fun parseState(root: JsonObject): BusinessCardState {
        checkKeys(root, ROOT_KEYS, "root")
        val schemaVersion = requiredInt(root, "schemaVersion", "root.schemaVersion")
        val canvas = parseCanvas(requiredObject(root, "canvas", "root.canvas"))
        val elementsJson = required(root, "elements", "root.elements")
        if (!elementsJson.isJsonArray) {
            fail("root.elements must be an array")
        }
        val elements = elementsJson.asJsonArray.mapIndexed { index, value ->
            if (!value.isJsonObject) {
                fail("root.elements[$index] must be an object")
            }
            parseElement(value.asJsonObject, index)
        }.toMutableList()
        return BusinessCardState(schemaVersion, canvas, elements)
    }

    private fun parseCanvas(json: JsonObject): BusinessCardCanvas {
        checkKeys(json, CANVAS_KEYS, "root.canvas")
        val orientationValue = requiredString(json, "orientation", "root.canvas.orientation")
        val orientation = BusinessCardOrientation.fromWireValue(orientationValue)
            ?: fail("root.canvas.orientation is not supported")
        return BusinessCardCanvas(
            orientation = orientation,
            aspectRatio = requiredFloat(json, "aspectRatio", "root.canvas.aspectRatio"),
            backgroundColor = requiredString(
                json,
                "backgroundColor",
                "root.canvas.backgroundColor"
            ),
            backgroundImage = optionalObject(
                json,
                "backgroundImage",
                "root.canvas.backgroundImage"
            )?.let { parseImage(it, "root.canvas.backgroundImage") }
        )
    }

    private fun parseElement(json: JsonObject, index: Int): BusinessCardElement {
        val path = "root.elements[$index]"
        checkKeys(json, ELEMENT_KEYS, path)
        val typeValue = requiredString(json, "type", "$path.type")
        val type = BusinessCardElementType.fromWireValue(typeValue)
            ?: fail("$path.type is not supported")
        return BusinessCardElement(
            id = requiredString(json, "id", "$path.id"),
            type = type,
            centerXRatio = requiredFloat(json, "centerXRatio", "$path.centerXRatio"),
            centerYRatio = requiredFloat(json, "centerYRatio", "$path.centerYRatio"),
            widthRatio = requiredFloat(json, "widthRatio", "$path.widthRatio"),
            heightRatio = requiredFloat(json, "heightRatio", "$path.heightRatio"),
            zIndex = requiredInt(json, "zIndex", "$path.zIndex"),
            text = optionalObject(json, "text", "$path.text")?.let { parseText(it, "$path.text") },
            image = optionalObject(json, "image", "$path.image")?.let { parseImage(it, "$path.image") }
        )
    }

    private fun parseText(json: JsonObject, path: String): BusinessCardText {
        checkKeys(json, TEXT_KEYS, path)
        val alignmentValue = requiredString(json, "alignment", "$path.alignment")
        val alignment = BusinessCardTextAlignment.fromWireValue(alignmentValue)
            ?: fail("$path.alignment is not supported")
        return BusinessCardText(
            value = requiredString(json, "value", "$path.value"),
            fontSizeRatio = requiredFloat(json, "fontSizeRatio", "$path.fontSizeRatio"),
            color = requiredString(json, "color", "$path.color"),
            fontFamily = requiredString(json, "fontFamily", "$path.fontFamily"),
            fontWeight = requiredInt(json, "fontWeight", "$path.fontWeight"),
            alignment = alignment
        )
    }

    private fun parseImage(json: JsonObject, path: String): BusinessCardImage {
        checkKeys(json, IMAGE_KEYS, path)
        val sourceKindValue = requiredString(json, "sourceKind", "$path.sourceKind")
        val sourceValue = requiredString(json, "sourceValue", "$path.sourceValue")
        val declaredSourceKind = BusinessCardImageSourceKind.fromWireValue(sourceKindValue)
        if (declaredSourceKind == null &&
            !(sourceKindValue == LEGACY_LOCAL_URI_WIRE_VALUE && isHttpSource(sourceValue))
        ) {
            fail("$path.sourceKind is not supported")
        }
        val sourceKind = inferSourceKind(declaredSourceKind, sourceValue)
            ?: BusinessCardImageSourceKind.REMOTE_URL
        val contentScaleValue = requiredString(json, "contentScale", "$path.contentScale")
        val contentScale = BusinessCardContentScale.fromWireValue(contentScaleValue)
            ?: fail("$path.contentScale is not supported")
        return BusinessCardImage(
            sourceKind = sourceKind,
            sourceValue = sourceValue,
            intrinsicAspectRatio = requiredFloat(
                json,
                "intrinsicAspectRatio",
                "$path.intrinsicAspectRatio"
            ),
            contentScale = contentScale
        )
    }

    private fun normalizeAndValidate(state: BusinessCardState): BusinessCardState {
        val canMigrateRemoteOnlyV1 = state.schemaVersion == LEGACY_SCHEMA_VERSION &&
            state.canvas.backgroundImage == null &&
            state.elements.all { element ->
                element.type != BusinessCardElementType.IMAGE ||
                    element.image?.sourceKind == BusinessCardImageSourceKind.REMOTE_URL
            }
        if (state.schemaVersion != BUSINESS_CARD_SCHEMA_VERSION && !canMigrateRemoteOnlyV1) {
            fail("Unsupported schemaVersion: ${state.schemaVersion}")
        }

        val canvas = normalizeCanvas(state.canvas)
        val ids = HashSet<String>()
        val zIndexes = HashSet<Int>()
        val elements = state.elements.mapIndexed { index, element ->
            normalizeElement(element, index).also { normalized ->
                if (!ids.add(normalized.id)) {
                    fail("Element id must be unique: ${normalized.id}")
                }
                if (!zIndexes.add(normalized.zIndex)) {
                    fail("Element zIndex must be unique: ${normalized.zIndex}")
                }
            }
        }

        if (zIndexes.sorted() != elements.indices.toList()) {
            fail("Element zIndex values must be continuous from 0")
        }

        return BusinessCardState(
            schemaVersion = BUSINESS_CARD_SCHEMA_VERSION,
            canvas = canvas,
            elements = elements.sortedBy { it.zIndex }.toMutableList()
        )
    }

    private fun normalizeCanvas(canvas: BusinessCardCanvas): BusinessCardCanvas {
        requireFinite(canvas.aspectRatio, "canvas.aspectRatio")
        if (canvas.aspectRatio <= 0f) {
            fail("canvas.aspectRatio must be greater than 0")
        }
        if (abs(canvas.aspectRatio - canvas.orientation.expectedAspectRatio) > ASPECT_RATIO_TOLERANCE) {
            fail("canvas.aspectRatio does not match canvas.orientation")
        }
        return canvas.copy(
            backgroundColor = normalizeColor(canvas.backgroundColor),
            backgroundImage = canvas.backgroundImage?.let {
                normalizeImage(
                    image = it,
                    path = "canvas.backgroundImage",
                    requiredContentScale = BusinessCardContentScale.CROP
                )
            }
        )
    }

    private fun normalizeElement(
        element: BusinessCardElement,
        index: Int
    ): BusinessCardElement {
        val path = "elements[$index]"
        if (element.id.isBlank()) {
            fail("$path.id must not be blank")
        }
        requireRatio(element.centerXRatio, "$path.centerXRatio", allowZero = true)
        requireRatio(element.centerYRatio, "$path.centerYRatio", allowZero = true)
        requireRatio(element.widthRatio, "$path.widthRatio", allowZero = false)
        requireRatio(element.heightRatio, "$path.heightRatio", allowZero = false)
        if (element.zIndex < 0) {
            fail("$path.zIndex must not be negative")
        }
        requireFullyInside(element, path)

        return when (element.type) {
            BusinessCardElementType.TEXT -> {
                if (element.image != null) {
                    fail("$path must not contain image data for a text element")
                }
                val text = element.text ?: fail("$path.text is required for a text element")
                element.copy(text = normalizeText(text, "$path.text"), image = null)
            }

            BusinessCardElementType.IMAGE -> {
                if (element.text != null) {
                    fail("$path must not contain text data for an image element")
                }
                val image = element.image ?: fail("$path.image is required for an image element")
                element.copy(
                    text = null,
                    image = normalizeImage(
                        image = image,
                        path = "$path.image",
                        requiredContentScale = BusinessCardContentScale.FIT
                    )
                )
            }
        }
    }

    private fun normalizeText(text: BusinessCardText, path: String): BusinessCardText {
        if (text.value.isBlank()) {
            fail("$path.value must not be blank")
        }
        if ('\n' in text.value || '\r' in text.value) {
            fail("$path.value must be a single line")
        }
        requireFinite(text.fontSizeRatio, "$path.fontSizeRatio")
        if (text.fontSizeRatio < MIN_FONT_SIZE_RATIO || text.fontSizeRatio > MAX_FONT_SIZE_RATIO) {
            fail("$path.fontSizeRatio must be between $MIN_FONT_SIZE_RATIO and $MAX_FONT_SIZE_RATIO")
        }
        if (text.fontFamily != "sans-serif") {
            fail("$path.fontFamily is not supported")
        }
        if (text.fontWeight !in 100..900) {
            fail("$path.fontWeight must be between 100 and 900")
        }
        return text.copy(color = normalizeColor(text.color))
    }

    private fun normalizeImage(
        image: BusinessCardImage,
        path: String,
        requiredContentScale: BusinessCardContentScale
    ): BusinessCardImage {
        if (image.sourceValue.isBlank()) {
            fail("$path.sourceValue must not be blank")
        }
        requireFinite(image.intrinsicAspectRatio, "$path.intrinsicAspectRatio")
        if (image.intrinsicAspectRatio <= 0f) {
            fail("$path.intrinsicAspectRatio must be greater than 0")
        }
        val normalized = image.copy(
            sourceKind = inferSourceKind(image.sourceKind, image.sourceValue)
                ?: fail("$path.sourceKind is not supported")
        )
        if (normalized.contentScale != requiredContentScale) {
            fail("$path.contentScale must be ${requiredContentScale.wireValue}")
        }
        validateImageLocation(normalized, path)
        return normalized
    }

    private fun validateImageLocation(image: BusinessCardImage, path: String) {
        when (image.sourceKind) {
            BusinessCardImageSourceKind.LOCAL_PATH -> {
                val segments = image.sourceValue.split('/').drop(1)
                val hasInvalidSegment = segments.any {
                    it.isBlank() || it == "." || it == ".."
                }
                val isBusinessCardAsset = segments.size >= 2 &&
                    segments[segments.lastIndex - 1] == LOCAL_ASSET_DIRECTORY_NAME
                if (!image.sourceValue.startsWith('/') ||
                    image.sourceValue.endsWith('/') ||
                    '\u0000' in image.sourceValue ||
                    hasInvalidSegment ||
                    !isBusinessCardAsset
                ) {
                    fail("$path.sourceValue must be an absolute business card asset path")
                }
            }

            BusinessCardImageSourceKind.REMOTE_URL -> {
                val uri = try {
                    URI(image.sourceValue)
                } catch (error: URISyntaxException) {
                    throw BusinessCardStateException("$path.sourceValue is not a valid URL", error)
                }
                val isHttp = uri.scheme.equals("http", ignoreCase = true) ||
                    uri.scheme.equals("https", ignoreCase = true)
                if (!isHttp || uri.host.isNullOrBlank()) {
                    fail("$path.sourceValue must be an HTTP(S) URL for remote_url")
                }
            }
        }
    }

    private fun inferSourceKind(
        declared: BusinessCardImageSourceKind?,
        sourceValue: String
    ): BusinessCardImageSourceKind? {
        return when {
            isHttpSource(sourceValue) -> BusinessCardImageSourceKind.REMOTE_URL
            else -> declared
        }
    }

    private fun isHttpSource(sourceValue: String): Boolean {
        val uri = try {
            URI(sourceValue)
        } catch (_: URISyntaxException) {
            return false
        }
        val isHttp = uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)
        return isHttp && !uri.host.isNullOrBlank()
    }

    private fun requireFullyInside(element: BusinessCardElement, path: String) {
        val halfWidth = element.widthRatio / 2f
        val halfHeight = element.heightRatio / 2f
        if (element.centerXRatio - halfWidth < -FLOAT_TOLERANCE ||
            element.centerXRatio + halfWidth > 1f + FLOAT_TOLERANCE ||
            element.centerYRatio - halfHeight < -FLOAT_TOLERANCE ||
            element.centerYRatio + halfHeight > 1f + FLOAT_TOLERANCE
        ) {
            fail("$path must be fully inside the canvas")
        }
    }

    private fun requireRatio(value: Float, path: String, allowZero: Boolean) {
        requireFinite(value, path)
        val valid = if (allowZero) value in 0f..1f else value > 0f && value <= 1f
        if (!valid) {
            fail("$path must be ${if (allowZero) "between 0 and 1" else "greater than 0 and at most 1"}")
        }
    }

    private fun requireFinite(value: Float, path: String) {
        if (!value.isFinite()) {
            fail("$path must be finite")
        }
    }

    private fun required(json: JsonObject, name: String, path: String): JsonElement {
        val value = json.get(name)
        if (value == null || value.isJsonNull) {
            fail("$path is required")
        }
        return value
    }

    private fun requiredObject(json: JsonObject, name: String, path: String): JsonObject {
        val value = required(json, name, path)
        if (!value.isJsonObject) {
            fail("$path must be an object")
        }
        return value.asJsonObject
    }

    private fun optionalObject(json: JsonObject, name: String, path: String): JsonObject? {
        val value = json.get(name) ?: return null
        if (value.isJsonNull) return null
        if (!value.isJsonObject) {
            fail("$path must be an object or null")
        }
        return value.asJsonObject
    }

    private fun requiredString(json: JsonObject, name: String, path: String): String {
        val value = required(json, name, path)
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isString) {
            fail("$path must be a string")
        }
        return value.asString
    }

    private fun requiredFloat(json: JsonObject, name: String, path: String): Float {
        val value = required(json, name, path)
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isNumber) {
            fail("$path must be a number")
        }
        val number = try {
            value.asDouble
        } catch (error: NumberFormatException) {
            throw BusinessCardStateException("$path must be a number", error)
        }
        if (!number.isFinite() || abs(number) > Float.MAX_VALUE) {
            fail("$path must be a finite 32-bit number")
        }
        return number.toFloat()
    }

    private fun requiredInt(json: JsonObject, name: String, path: String): Int {
        val value = required(json, name, path)
        if (!value.isJsonPrimitive || !value.asJsonPrimitive.isNumber) {
            fail("$path must be an integer")
        }
        val decimal = try {
            value.asBigDecimal
        } catch (error: NumberFormatException) {
            throw BusinessCardStateException("$path must be an integer", error)
        }
        val normalized = decimal.stripTrailingZeros()
        if (normalized.scale() > 0 || decimal < Int.MIN_VALUE.toBigDecimal() ||
            decimal > Int.MAX_VALUE.toBigDecimal()
        ) {
            fail("$path must be a 32-bit integer")
        }
        return decimal.toInt()
    }

    private fun checkKeys(json: JsonObject, allowed: Set<String>, path: String) {
        val unknown = json.entrySet().map { it.key }.filterNot { it in allowed }
        if (unknown.isNotEmpty()) {
            fail("$path contains unsupported field(s): ${unknown.joinToString()}")
        }
    }

    private fun fail(message: String): Nothing = throw BusinessCardStateException(message)

    private val ROOT_KEYS = setOf("schemaVersion", "canvas", "elements")
    private val CANVAS_KEYS = setOf(
        "orientation",
        "aspectRatio",
        "backgroundColor",
        "backgroundImage"
    )
    private val ELEMENT_KEYS = setOf(
        "id",
        "type",
        "centerXRatio",
        "centerYRatio",
        "widthRatio",
        "heightRatio",
        "zIndex",
        "text",
        "image"
    )
    private val TEXT_KEYS = setOf(
        "value",
        "fontSizeRatio",
        "color",
        "fontFamily",
        "fontWeight",
        "alignment"
    )
    private val IMAGE_KEYS = setOf(
        "sourceKind",
        "sourceValue",
        "intrinsicAspectRatio",
        "contentScale"
    )
}
