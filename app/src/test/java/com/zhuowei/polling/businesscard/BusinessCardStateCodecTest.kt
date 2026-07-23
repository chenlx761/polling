package com.zhuowei.polling.businesscard

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BusinessCardStateCodecTest {
    @Test
    fun roundTrip_normalizesColorsAndPreservesChineseAndElementKinds() {
        val decoded = BusinessCardStateCodec.fromJson(LANDSCAPE_TEMPLATE_JSON)

        assertEquals("张三", decoded.elements[0].text?.value)
        assertEquals("#FF112233", decoded.elements[0].text?.color)
        assertEquals("#FFFFFFFF", decoded.canvas.backgroundColor)
        assertEquals(BusinessCardImageSourceKind.LOCAL_PATH, decoded.elements[1].image?.sourceKind)

        val encoded = BusinessCardStateCodec.toJson(decoded)
        assertEquals(
            JsonParser().parse(EXPECTED_NORMALIZED_LANDSCAPE_JSON),
            JsonParser().parse(encoded)
        )
        assertEquals(decoded, BusinessCardStateCodec.decode(BusinessCardStateCodec.encode(decoded)))
    }

    @Test
    fun portraitAndRemoteImage_roundTrip() {
        val original = BusinessCardState(
            canvas = BusinessCardCanvas(
                orientation = BusinessCardOrientation.PORTRAIT,
                aspectRatio = BUSINESS_CARD_PORTRAIT_ASPECT_RATIO,
                backgroundColor = "#102030"
            ),
            elements = mutableListOf(
                imageElement(
                    id = "remote-image",
                    sourceKind = BusinessCardImageSourceKind.REMOTE_URL,
                    sourceValue = "https://example.com/assets/card.png?size=large"
                )
            )
        )

        val decoded = BusinessCardStateCodec.fromJson(BusinessCardStateCodec.toJson(original))

        assertEquals(BusinessCardOrientation.PORTRAIT, decoded.canvas.orientation)
        assertEquals("#FF102030", decoded.canvas.backgroundColor)
        assertEquals(original.elements.single().image?.sourceValue, decoded.elements.single().image?.sourceValue)
    }

    @Test
    fun backgroundImage_roundTripUsesCropAndIsIncludedInLocalAssets() {
        val background = BusinessCardImage(
            sourceKind = BusinessCardImageSourceKind.LOCAL_PATH,
            sourceValue = SECOND_LOCAL_IMAGE_PATH,
            intrinsicAspectRatio = 2f,
            contentScale = BusinessCardContentScale.CROP
        )
        val state = BusinessCardState(
            canvas = BusinessCardCanvas(backgroundImage = background),
            elements = mutableListOf(imageElement(sourceValue = LOCAL_IMAGE_PATH))
        )

        val decoded = BusinessCardStateCodec.decode(BusinessCardStateCodec.encode(state))

        assertEquals(background, decoded.canvas.backgroundImage)
        assertEquals(
            listOf(SECOND_LOCAL_IMAGE_PATH, LOCAL_IMAGE_PATH),
            BusinessCardStateCodec.localAssetPaths(decoded)
        )
    }

    @Test
    fun fromJson_httpSourceNormalizesDeclaredLocalPathToRemoteUrl() {
        val remoteJson = BusinessCardStateCodec.encode(
            BusinessCardState(
                elements = mutableListOf(
                    imageElement(
                        sourceKind = BusinessCardImageSourceKind.REMOTE_URL,
                        sourceValue = "http://example.com/assets/card.png"
                    )
                )
            )
        )
        val declaredLocalJson = remoteJson.replaceFirst(
            "\"sourceKind\":\"remote_url\"",
            "\"sourceKind\":\"local_path\""
        )

        val decoded = BusinessCardStateCodec.decode(declaredLocalJson)

        assertEquals(BusinessCardImageSourceKind.REMOTE_URL, decoded.elements.single().image?.sourceKind)
        assertEquals("http://example.com/assets/card.png", decoded.elements.single().image?.sourceValue)
    }

    @Test
    fun fromJson_migratesV1RemoteOnlyAndLegacyLocalUriHttpToV2() {
        val v2RemoteJson = BusinessCardStateCodec.encode(
            BusinessCardState(
                elements = mutableListOf(
                    imageElement(
                        sourceKind = BusinessCardImageSourceKind.REMOTE_URL,
                        sourceValue = "https://example.com/assets/legacy.png"
                    )
                )
            )
        )
        val v1RemoteJson = v2RemoteJson.replaceFirst(
            "\"schemaVersion\":2",
            "\"schemaVersion\":1"
        )
        val migratedRemote = BusinessCardStateCodec.decode(v1RemoteJson)
        assertEquals(BUSINESS_CARD_SCHEMA_VERSION, migratedRemote.schemaVersion)
        assertEquals(
            BusinessCardImageSourceKind.REMOTE_URL,
            migratedRemote.elements.single().image?.sourceKind
        )

        val staleKindJson = v1RemoteJson.replaceFirst(
            "\"sourceKind\":\"remote_url\"",
            "\"sourceKind\":\"local_uri\""
        )
        val migratedStaleKind = BusinessCardStateCodec.decode(staleKindJson)
        assertEquals(BUSINESS_CARD_SCHEMA_VERSION, migratedStaleKind.schemaVersion)
        assertEquals(
            BusinessCardImageSourceKind.REMOTE_URL,
            migratedStaleKind.elements.single().image?.sourceKind
        )

        val v1LocalJson = LANDSCAPE_TEMPLATE_JSON.replaceFirst(
            "\"schemaVersion\": 2",
            "\"schemaVersion\": 1"
        )
        assertInvalid { BusinessCardStateCodec.decode(v1LocalJson) }
    }

    @Test
    fun toJson_ordersElementsByContinuousZIndexWithoutMutatingInput() {
        val top = textElement(id = "top", zIndex = 1)
        val bottom = imageElement(id = "bottom", zIndex = 0)
        val state = BusinessCardState(elements = mutableListOf(top, bottom))

        val decoded = BusinessCardStateCodec.fromJson(BusinessCardStateCodec.toJson(state))

        assertEquals(listOf("bottom", "top"), decoded.elements.map { it.id })
        assertEquals(listOf("top", "bottom"), state.elements.map { it.id })
    }

    @Test
    fun localAssetPaths_returnsDistinctPathsInLayerOrder() {
        val first = imageElement("first", 0, sourceValue = LOCAL_IMAGE_PATH)
        val remote = imageElement(
            "remote",
            1,
            BusinessCardImageSourceKind.REMOTE_URL,
            "https://example.com/two.png"
        )
        val duplicate = imageElement("duplicate", 2, sourceValue = LOCAL_IMAGE_PATH)
        val second = imageElement("second", 3, sourceValue = SECOND_LOCAL_IMAGE_PATH)
        val state = BusinessCardState(elements = mutableListOf(second, duplicate, remote, first))

        assertEquals(
            listOf(LOCAL_IMAGE_PATH, SECOND_LOCAL_IMAGE_PATH),
            BusinessCardStateCodec.localAssetPaths(state)
        )
    }

    @Test
    fun normalizeColor_acceptsRgbAndArgbAndRejectsInvalidValues() {
        assertEquals("#FFA0B1C2", BusinessCardStateCodec.normalizeColor("#a0b1c2"))
        assertEquals("#80A0B1C2", BusinessCardStateCodec.normalizeColor("#80a0b1c2"))
        assertInvalid { BusinessCardStateCodec.normalizeColor("A0B1C2") }
        assertInvalid { BusinessCardStateCodec.normalizeColor("#XYZXYZ") }
    }

    @Test
    fun fromJson_rejectsMalformedUnsupportedAndUnknownFields() {
        assertInvalid { BusinessCardStateCodec.fromJson("not-json") }
        assertInvalid { BusinessCardStateCodec.fromJson("[]") }
        assertInvalid {
            BusinessCardStateCodec.fromJson(
                LANDSCAPE_TEMPLATE_JSON.replace("\"schemaVersion\": 2", "\"schemaVersion\": 3")
            )
        }
        assertInvalid {
            BusinessCardStateCodec.fromJson(
                LANDSCAPE_TEMPLATE_JSON.replace(
                    "\"schemaVersion\": 2,",
                    "\"schemaVersion\": 2, \"unexpected\": true,"
                )
            )
        }
    }

    @Test
    fun fromJson_rejectsLenientJsonSyntaxAndTrailingContent() {
        val valid = BusinessCardStateCodec.toJson(BusinessCardState())

        assertInvalid {
            BusinessCardStateCodec.fromJson(valid.replaceFirst("{", "{// comment\n"))
        }
        assertInvalid {
            BusinessCardStateCodec.fromJson(valid.replaceFirst("\"schemaVersion\"", "schemaVersion"))
        }
        assertInvalid {
            BusinessCardStateCodec.fromJson(valid.replaceFirst("\"landscape\"", "'landscape'"))
        }
        assertInvalid {
            BusinessCardStateCodec.fromJson("$valid true")
        }
    }

    @Test
    fun fromJson_rejectsInvalidCoordinatesDuplicateIdsAndNonContinuousLayers() {
        val outside = sampleState().apply {
            elements[0].centerXRatio = 0.01f
        }
        assertInvalid { BusinessCardStateCodec.toJson(outside) }

        val duplicate = sampleState().apply {
            elements[1].id = elements[0].id
        }
        assertInvalid { BusinessCardStateCodec.toJson(duplicate) }

        val layerGap = sampleState().apply {
            elements[1].zIndex = 2
        }
        assertInvalid { BusinessCardStateCodec.toJson(layerGap) }
    }

    @Test
    fun validation_rejectsWrongElementPayloadAndInvalidText() {
        val wrongPayload = textElement().apply {
            image = BusinessCardImage(
                BusinessCardImageSourceKind.LOCAL_PATH,
                LOCAL_IMAGE_PATH,
                1f
            )
        }
        assertInvalid { BusinessCardStateCodec.validate(BusinessCardState(elements = mutableListOf(wrongPayload))) }

        val blank = textElement().apply { text?.value = "   " }
        assertInvalid { BusinessCardStateCodec.validate(BusinessCardState(elements = mutableListOf(blank))) }

        val multiline = textElement().apply { text?.value = "line one\nline two" }
        assertInvalid { BusinessCardStateCodec.validate(BusinessCardState(elements = mutableListOf(multiline))) }

        val oversized = textElement().apply { text?.fontSizeRatio = 0.21f }
        assertInvalid { BusinessCardStateCodec.validate(BusinessCardState(elements = mutableListOf(oversized))) }
    }

    @Test
    fun validation_rejectsWrongContentScaleForBackgroundAndElementImages() {
        val fitBackground = BusinessCardState(
            canvas = BusinessCardCanvas(
                backgroundImage = BusinessCardImage(
                    BusinessCardImageSourceKind.LOCAL_PATH,
                    LOCAL_IMAGE_PATH,
                    1f,
                    BusinessCardContentScale.FIT
                )
            )
        )
        assertInvalid { BusinessCardStateCodec.validate(fitBackground) }

        val croppedElement = imageElement().apply {
            image?.contentScale = BusinessCardContentScale.CROP
        }
        assertInvalid {
            BusinessCardStateCodec.validate(
                BusinessCardState(elements = mutableListOf(croppedElement))
            )
        }
    }

    @Test
    fun validation_rejectsInvalidCanvasAndImageSources() {
        val mismatchedCanvas = BusinessCardState(
            canvas = BusinessCardCanvas(
                BusinessCardOrientation.PORTRAIT,
                BUSINESS_CARD_LANDSCAPE_ASPECT_RATIO
            )
        )
        assertInvalid { BusinessCardStateCodec.validate(mismatchedCanvas) }

        val remoteContent = imageElement(
            sourceKind = BusinessCardImageSourceKind.REMOTE_URL,
            sourceValue = "content://cards/image"
        )
        assertInvalid { BusinessCardStateCodec.validate(BusinessCardState(elements = mutableListOf(remoteContent))) }
    }

    @Test
    fun validation_rejectsContentFileAndRelativeLocalSources() {
        listOf(
            "content://cards/image",
            "file:///data/user/0/com.zhuowei.polling/files/business_card_assets/image.jpg",
            "business_card_assets/image.jpg"
        ).forEach { invalidSource ->
            val state = BusinessCardState(
                elements = mutableListOf(imageElement(sourceValue = invalidSource))
            )
            assertInvalid { BusinessCardStateCodec.validate(state) }
        }

        val legacyContentJson = LANDSCAPE_TEMPLATE_JSON
            .replace("\"sourceKind\": \"local_path\"", "\"sourceKind\": \"local_uri\"")
            .replace("\"sourceValue\": \"$GOLDEN_LOCAL_IMAGE_PATH\"", "\"sourceValue\": \"content://cards/image\"")
        assertInvalid { BusinessCardStateCodec.decode(legacyContentJson) }
    }

    @Test
    fun fromJson_rejectsUnknownKindEvenWhenSourceIsHttp() {
        val invalidJson = LANDSCAPE_TEMPLATE_JSON
            .replace("\"sourceKind\": \"local_path\"", "\"sourceKind\": \"unknown_kind\"")
            .replace(
                "\"sourceValue\": \"$GOLDEN_LOCAL_IMAGE_PATH\"",
                "\"sourceValue\": \"https://example.com/image.png\""
            )

        assertInvalid { BusinessCardStateCodec.decode(invalidJson) }
    }

    private fun sampleState(): BusinessCardState = BusinessCardStateCodec.fromJson(LANDSCAPE_TEMPLATE_JSON)

    private fun textElement(id: String = "text", zIndex: Int = 0): BusinessCardElement =
        BusinessCardElement(
            id = id,
            type = BusinessCardElementType.TEXT,
            centerXRatio = 0.25f,
            centerYRatio = 0.30f,
            widthRatio = 0.20f,
            heightRatio = 0.08f,
            zIndex = zIndex,
            text = BusinessCardText("文字", 0.08f)
        )

    private fun imageElement(
        id: String = "image",
        zIndex: Int = 0,
        sourceKind: BusinessCardImageSourceKind = BusinessCardImageSourceKind.LOCAL_PATH,
        sourceValue: String = LOCAL_IMAGE_PATH
    ): BusinessCardElement = BusinessCardElement(
        id = id,
        type = BusinessCardElementType.IMAGE,
        centerXRatio = 0.70f,
        centerYRatio = 0.50f,
        widthRatio = 0.25f,
        heightRatio = 0.22f,
        zIndex = zIndex,
        image = BusinessCardImage(sourceKind, sourceValue, 1.5f)
    )

    private fun assertInvalid(block: () -> Unit) {
        assertThrows(BusinessCardStateException::class.java, block)
    }

    companion object {
        private val LANDSCAPE_TEMPLATE_JSON = """
            {
              "schemaVersion": 2,
              "canvas": {
                "orientation": "landscape",
                "aspectRatio": 1.666667,
                "backgroundColor": "#ffffff"
              },
              "elements": [
                {
                  "id": "text-id",
                  "type": "text",
                  "centerXRatio": 0.25,
                  "centerYRatio": 0.30,
                  "widthRatio": 0.20,
                  "heightRatio": 0.08,
                  "zIndex": 0,
                  "text": {
                    "value": "张三",
                    "fontSizeRatio": 0.08,
                    "color": "#112233",
                    "fontFamily": "sans-serif",
                    "fontWeight": 400,
                    "alignment": "start"
                  }
                },
                {
                  "id": "image-id",
                  "type": "image",
                  "centerXRatio": 0.70,
                  "centerYRatio": 0.50,
                  "widthRatio": 0.25,
                  "heightRatio": 0.22,
                  "zIndex": 1,
                  "image": {
                    "sourceKind": "local_path",
                    "sourceValue": "/data/user/0/com.zhuowei.polling/files/business_card_assets/photo.jpg",
                    "intrinsicAspectRatio": 1.5,
                    "contentScale": "fit"
                  }
                }
              ]
            }
        """.trimIndent()

        private val EXPECTED_NORMALIZED_LANDSCAPE_JSON = """
            {
              "schemaVersion": 2,
              "canvas": {
                "orientation": "landscape",
                "aspectRatio": 1.666667,
                "backgroundColor": "#FFFFFFFF"
              },
              "elements": [
                {
                  "id": "text-id",
                  "type": "text",
                  "centerXRatio": 0.25,
                  "centerYRatio": 0.30,
                  "widthRatio": 0.20,
                  "heightRatio": 0.08,
                  "zIndex": 0,
                  "text": {
                    "value": "张三",
                    "fontSizeRatio": 0.08,
                    "color": "#FF112233",
                    "fontFamily": "sans-serif",
                    "fontWeight": 400,
                    "alignment": "start"
                  }
                },
                {
                  "id": "image-id",
                  "type": "image",
                  "centerXRatio": 0.70,
                  "centerYRatio": 0.50,
                  "widthRatio": 0.25,
                  "heightRatio": 0.22,
                  "zIndex": 1,
                  "image": {
                    "sourceKind": "local_path",
                    "sourceValue": "/data/user/0/com.zhuowei.polling/files/business_card_assets/photo.jpg",
                    "intrinsicAspectRatio": 1.5,
                    "contentScale": "fit"
                  }
                }
              ]
            }
        """.trimIndent()

        private const val LOCAL_IMAGE_PATH =
            "/data/user/0/com.zhuowei.polling/files/business_card_assets/image.jpg"
        private const val SECOND_LOCAL_IMAGE_PATH =
            "/data/user/0/com.zhuowei.polling/files/business_card_assets/second.png"
        private const val GOLDEN_LOCAL_IMAGE_PATH =
            "/data/user/0/com.zhuowei.polling/files/business_card_assets/photo.jpg"
    }
}
