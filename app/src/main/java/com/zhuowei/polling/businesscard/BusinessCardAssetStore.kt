package com.zhuowei.polling.businesscard

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.UUID

class BusinessCardAssetStore(context: Context) {
    data class ImportedAsset(
        val contentUri: Uri,
        val absolutePath: String
    )

    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val assetDirectory = File(appContext.filesDir, DIRECTORY_NAME)

    init {
        cleanupStaleTemporaryFiles()
    }

    @Throws(IOException::class)
    fun importImage(sourceUri: Uri): ImportedAsset {
        if (!assetDirectory.exists() && !assetDirectory.mkdirs()) {
            throw IOException("Unable to create the business card asset directory")
        }

        val extension = resolveExtension(sourceUri)
        val fileName = "${UUID.randomUUID()}.$extension"
        val temporary = File(assetDirectory, "$fileName.part")
        val destination = File(assetDirectory, fileName)
        try {
            val input = resolver.openInputStream(sourceUri)
                ?: throw IOException("Unable to open the selected image")
            input.use { source ->
                temporary.outputStream().buffered().use { output ->
                    copyWithLimit(source, output)
                }
            }
            if (!temporary.isFile || temporary.length() <= 0L) {
                throw IOException("The selected image is empty")
            }
            if (!temporary.renameTo(destination)) {
                throw IOException("Unable to finalize the selected image")
            }

            val contentUri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                destination
            )
            return ImportedAsset(contentUri, destination.canonicalPath)
        } catch (error: Exception) {
            temporary.delete()
            destination.delete()
            if (error is IOException) throw error
            throw IOException("Unable to save the selected image", error)
        }
    }

    fun pathForSource(sourceValue: String): String? {
        val uri = runCatching { Uri.parse(sourceValue) }.getOrNull() ?: return null
        if (uri.scheme != "content" ||
            uri.authority != "${appContext.packageName}.fileprovider"
        ) {
            return null
        }
        val segments = uri.pathSegments
        if (segments.size != 2 || segments[0] != PROVIDER_PATH_NAME) return null
        val file = File(assetDirectory, segments[1])
        return try {
            file.takeIf { isDirectChild(it) && it.isFile }?.canonicalPath
        } catch (_: IOException) {
            null
        }
    }

    fun deletePath(path: String): Boolean {
        val file = File(path)
        if (!isDirectChild(file)) return false
        return !file.exists() || file.delete()
    }

    private fun resolveExtension(uri: Uri): String {
        val mimeExtension = resolver.getType(uri)
            ?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
            ?.lowercase(Locale.US)
            ?.normalizeImageExtension()
        if (mimeExtension != null) return mimeExtension

        val displayExtension = queryDisplayName(uri)
            ?.substringAfterLast('.', "")
            ?.lowercase(Locale.US)
            ?.normalizeImageExtension()
        return displayExtension ?: DEFAULT_EXTENSION
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun String.normalizeImageExtension(): String? {
        val normalized = if (this == "jpeg" || this == "jpe") "jpg" else this
        return normalized.takeIf { it in SUPPORTED_EXTENSIONS }
    }

    private fun isDirectChild(file: File): Boolean {
        return try {
            file.canonicalFile.parentFile == assetDirectory.canonicalFile
        } catch (_: IOException) {
            false
        }
    }

    @Throws(IOException::class)
    private fun copyWithLimit(source: InputStream, output: OutputStream) {
        val buffer = ByteArray(COPY_BUFFER_SIZE)
        var totalBytes = 0L
        while (true) {
            val count = source.read(buffer)
            if (count < 0) return
            totalBytes += count
            if (totalBytes > MAX_IMAGE_BYTES) {
                throw IOException("The selected image exceeds the size limit")
            }
            output.write(buffer, 0, count)
        }
    }

    private fun cleanupStaleTemporaryFiles() {
        val staleBefore = System.currentTimeMillis() - STALE_TEMP_FILE_AGE_MILLIS
        assetDirectory.listFiles { file ->
            file.name.endsWith(TEMP_FILE_SUFFIX) && file.lastModified() < staleBefore
        }?.forEach(File::delete)
    }

    companion object {
        const val DIRECTORY_NAME = "business_card_assets"
        const val PROVIDER_PATH_NAME = "business_card_assets"

        private const val DEFAULT_EXTENSION = "jpg"
        private const val TEMP_FILE_SUFFIX = ".part"
        private const val COPY_BUFFER_SIZE = 32 * 1024
        private const val MAX_IMAGE_BYTES = 50L * 1024L * 1024L
        private const val STALE_TEMP_FILE_AGE_MILLIS = 60L * 60L * 1000L
        private val SUPPORTED_EXTENSIONS = setOf(
            "jpg",
            "png",
            "webp",
            "gif",
            "bmp",
            "heic",
            "heif",
            "avif"
        )
    }
}
