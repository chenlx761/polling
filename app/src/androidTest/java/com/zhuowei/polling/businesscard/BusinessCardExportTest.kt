package com.zhuowei.polling.businesscard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BusinessCardExportTest {
    @Test
    fun exportBitmap_rendersLocalImageAtFixedLandscapeSize() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, BusinessCardAssetStore.DIRECTORY_NAME).apply {
            assertTrue(exists() || mkdirs())
        }
        val imageFile = File(directory, "export_${UUID.randomUUID()}.jpg")
        createBlueJpeg(imageFile)
        var exported: Bitmap? = null
        try {
            val state = BusinessCardState(
                elements = mutableListOf(
                    BusinessCardElement(
                        id = "export-image",
                        type = BusinessCardElementType.IMAGE,
                        centerXRatio = 0.5f,
                        centerYRatio = 0.5f,
                        widthRatio = 0.5f,
                        heightRatio = 0.41666675f,
                        zIndex = 0,
                        image = BusinessCardImage(
                            sourceKind = BusinessCardImageSourceKind.LOCAL_PATH,
                            sourceValue = imageFile.canonicalPath,
                            intrinsicAspectRatio = 2f
                        )
                    )
                )
            )
            val canvasView = withContext(Dispatchers.Main) {
                BusinessCardCanvasView(context).apply {
                    setEditingEnabled(false)
                    setState(state)
                    measure(
                        View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
                    )
                    layout(0, 0, 300, 500)
                }
            }

            val bitmap = canvasView.exportBitmap(1500, 900)
            exported = bitmap
            assertEquals(1500, bitmap.width)
            assertEquals(900, bitmap.height)
            val centerColor = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
            assertTrue(Color.blue(centerColor) > Color.red(centerColor) + 80)
            assertTrue(Color.blue(centerColor) > Color.green(centerColor) + 80)
            val upperImageColor = bitmap.getPixel(bitmap.width / 2, 300)
            assertTrue(Color.blue(upperImageColor) > Color.red(upperImageColor) + 80)
        } finally {
            exported?.recycle()
            imageFile.delete()
        }
    }

    @Test
    fun savePng_writesDecodableMediaStoreImage() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = Bitmap.createBitmap(20, 12, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.GREEN)
        }
        val uri = BusinessCardGallerySaver.savePng(context, bitmap)
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
            assertEquals(20, options.outWidth)
            assertEquals(12, options.outHeight)
        } finally {
            context.contentResolver.delete(uri, null, null)
            bitmap.recycle()
        }
    }

    private fun createBlueJpeg(file: File) {
        val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
        }
        try {
            file.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output))
            }
        } finally {
            bitmap.recycle()
        }
    }
}
