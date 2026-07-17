package com.zhuowei.polling.businesscard

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BusinessCardAssetStoreTest {
    @Test
    fun importImage_copiesToPrivateDirectoryAndResolvesRealPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceDirectory = requireNotNull(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        val sourceFile = File(sourceDirectory, "card_source_${UUID.randomUUID()}.jpg")
        createJpeg(sourceFile)
        val store = BusinessCardAssetStore(context)

        try {
            val sourceUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sourceFile
            )
            val imported = store.importImage(sourceUri)
            val importedFile = File(imported.absolutePath)

            assertTrue(importedFile.isFile)
            assertTrue(
                importedFile.canonicalPath.startsWith(
                    File(context.filesDir, BusinessCardAssetStore.DIRECTORY_NAME).canonicalPath
                )
            )
            assertArrayEquals(sourceFile.readBytes(), importedFile.readBytes())
            assertEquals(
                importedFile.canonicalPath,
                store.pathForSource(imported.contentUri.toString())
            )
            assertFalse(store.deletePath(sourceFile.absolutePath))
            assertTrue(store.deletePath(imported.absolutePath))
            assertNull(store.pathForSource(imported.contentUri.toString()))
        } finally {
            sourceFile.delete()
        }
    }

    private fun createJpeg(file: File) {
        val bitmap = Bitmap.createBitmap(4, 2, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
        }
        try {
            file.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
            }
        } finally {
            bitmap.recycle()
        }
    }
}
