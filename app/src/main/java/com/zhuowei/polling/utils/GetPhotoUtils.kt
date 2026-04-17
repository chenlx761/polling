package com.zhuowei.polling.utils

import android.net.Uri
import android.os.Environment
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
                val destFile = File(cacheDir, "IMG_${timeStamp}.jpg")

                MyApplication.getInstance().contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                // 最后尝试直接返回 URI 字符串
                uri.toString()
            }
        }
    }
}