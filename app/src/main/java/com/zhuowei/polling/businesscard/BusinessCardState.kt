package com.zhuowei.polling.businesscard

import com.google.gson.annotations.SerializedName

const val BUSINESS_CARD_SCHEMA_VERSION = 1
const val BUSINESS_CARD_LANDSCAPE_ASPECT_RATIO = 1.666667f
const val BUSINESS_CARD_PORTRAIT_ASPECT_RATIO = 0.6f

data class BusinessCardState(
    var schemaVersion: Int = BUSINESS_CARD_SCHEMA_VERSION,
    var canvas: BusinessCardCanvas = BusinessCardCanvas(),
    var elements: MutableList<BusinessCardElement> = mutableListOf()
)

data class BusinessCardCanvas(
    var orientation: BusinessCardOrientation = BusinessCardOrientation.LANDSCAPE,
    var aspectRatio: Float = BUSINESS_CARD_LANDSCAPE_ASPECT_RATIO,
    var backgroundColor: String = "#FFFFFFFF"
)

data class BusinessCardElement(
    var id: String,
    var type: BusinessCardElementType,
    var centerXRatio: Float,
    var centerYRatio: Float,
    var widthRatio: Float,
    var heightRatio: Float,
    var zIndex: Int,
    var text: BusinessCardText? = null,
    var image: BusinessCardImage? = null
)

data class BusinessCardText(
    var value: String,
    var fontSizeRatio: Float,
    var color: String = "#FF111111",
    var fontFamily: String = "sans-serif",
    var fontWeight: Int = 400,
    var alignment: BusinessCardTextAlignment = BusinessCardTextAlignment.START
)

data class BusinessCardImage(
    var sourceKind: BusinessCardImageSourceKind,
    var sourceValue: String,
    var intrinsicAspectRatio: Float,
    var contentScale: BusinessCardContentScale = BusinessCardContentScale.FIT
)

enum class BusinessCardOrientation(
    val wireValue: String,
    val expectedAspectRatio: Float
) {
    @SerializedName("landscape")
    LANDSCAPE("landscape", BUSINESS_CARD_LANDSCAPE_ASPECT_RATIO),

    @SerializedName("portrait")
    PORTRAIT("portrait", BUSINESS_CARD_PORTRAIT_ASPECT_RATIO);

    companion object {
        fun fromWireValue(value: String): BusinessCardOrientation? =
            values().firstOrNull { it.wireValue == value }
    }
}

enum class BusinessCardElementType(val wireValue: String) {
    @SerializedName("text")
    TEXT("text"),

    @SerializedName("image")
    IMAGE("image");

    companion object {
        fun fromWireValue(value: String): BusinessCardElementType? =
            values().firstOrNull { it.wireValue == value }
    }
}

enum class BusinessCardImageSourceKind(val wireValue: String) {
    @SerializedName("local_uri")
    LOCAL_URI("local_uri"),

    @SerializedName("remote_url")
    REMOTE_URL("remote_url");

    companion object {
        fun fromWireValue(value: String): BusinessCardImageSourceKind? =
            values().firstOrNull { it.wireValue == value }
    }
}

enum class BusinessCardContentScale(val wireValue: String) {
    @SerializedName("fit")
    FIT("fit");

    companion object {
        fun fromWireValue(value: String): BusinessCardContentScale? =
            values().firstOrNull { it.wireValue == value }
    }
}

enum class BusinessCardTextAlignment(val wireValue: String) {
    @SerializedName("start")
    START("start"),

    @SerializedName("center")
    CENTER("center"),

    @SerializedName("end")
    END("end");

    companion object {
        fun fromWireValue(value: String): BusinessCardTextAlignment? =
            values().firstOrNull { it.wireValue == value }
    }
}
