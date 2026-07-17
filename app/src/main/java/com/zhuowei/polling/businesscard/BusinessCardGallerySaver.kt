package com.zhuowei.polling.businesscard

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BusinessCardGallerySaver {
    fun savePng(context: Context, bitmap: Bitmap): Uri {
        val appContext = context.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(appContext, bitmap)
        } else {
            saveToPublicPictures(appContext, bitmap)
        }
    }

    private fun saveWithMediaStore(context: Context, bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, createFileName())
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_PNG)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$ALBUM_DIRECTORY"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create a gallery item")
        var published = false
        try {
            val compressed = resolver.openOutputStream(uri, "w")?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
            } ?: false
            if (!compressed) throw IOException("Unable to encode the business card image")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            if (resolver.update(uri, values, null, null) <= 0) {
                throw IOException("Unable to publish the business card image")
            }
            published = true
            return uri
        } finally {
            if (!published) resolver.delete(uri, null, null)
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToPublicPictures(context: Context, bitmap: Bitmap): Uri {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            throw IOException("External storage is unavailable")
        }
        val pictures = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val directory = File(pictures, ALBUM_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Unable to create the gallery directory")
        }

        val fileName = createFileName()
        val destination = File(directory, fileName)
        val temporary = File(directory, ".$fileName.part")
        var completed = false
        try {
            val compressed = temporary.outputStream().buffered().use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
            }
            if (!compressed) throw IOException("Unable to encode the business card image")
            if (!temporary.renameTo(destination)) {
                throw IOException("Unable to publish the business card image")
            }
            completed = true
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destination.absolutePath),
                arrayOf(MIME_TYPE_PNG),
                null
            )
            return Uri.fromFile(destination)
        } finally {
            temporary.delete()
            if (!completed) destination.delete()
        }
    }

    private fun createFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val suffix = UUID.randomUUID().toString().take(8)
        return "business_card_${timestamp}_$suffix.png"
    }

    private const val MIME_TYPE_PNG = "image/png"
    private const val PNG_QUALITY = 100
    private const val ALBUM_DIRECTORY = "SZBL"
}
