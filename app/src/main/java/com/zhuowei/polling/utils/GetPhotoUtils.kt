package com.zhuowei.polling.utils

import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.zhuowei.polling.MyApplication
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetPhotoUtils {
    companion object{

         fun getPathFromUri(uri: Uri): String? {
            return try {
                copyUriToCache(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                uri.toString()
            }
        }

         fun copyUriToCache(uri: Uri): String? {
            return try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val cacheDir = MyApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val originalName = queryDisplayName(uri)
                val safeName = originalName?.substringAfterLast('/')?.ifBlank { null }
                val extension = safeName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() } ?: "jpg"
                val baseName = safeName?.substringBeforeLast('.', "")?.ifBlank { "FILE_$timeStamp" }
                    ?: "IMG_$timeStamp"
                val destFile = File(cacheDir, "${baseName}_$timeStamp.$extension")

                MyApplication.getInstance().contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                uri.toString()
            }
        }

        private fun queryDisplayName(uri: Uri): String? {
            return MyApplication.getInstance().contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
        }
    }
}