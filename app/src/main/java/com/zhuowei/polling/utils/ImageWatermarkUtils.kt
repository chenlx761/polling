package com.zhuowei.polling.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

object ImageWatermarkUtils {

    /**
     * 外部调用：只传路径 + context
     */
    fun watermarkToCache(
        context: Context,
        inputPath: String,
        text: String
    ): String? {

        val bitmap = decodeSampledBitmap(inputPath, 1080, 1920) ?: return null

        val result = addTextWatermark(bitmap, text)

        val outFile = createCacheFile(context)

        return saveBitmap(result, outFile)
    }

    /**
     * 创建缓存文件（自动命名）
     */
    private fun createCacheFile(context: Context): File {
        val fileName = "wm_${System.currentTimeMillis()}.jpg"
        return File(context.cacheDir, fileName)
    }

    /**
     * 压缩加载
     */
    private fun decodeSampledBitmap(path: String, reqW: Int, reqH: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        options.inSampleSize = calculateInSampleSize(options, reqW, reqH)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        val (h, w) = options.outHeight to options.outWidth

        if (h > reqHeight || w > reqWidth) {
            val halfH = h / 2
            val halfW = w / 2

            while (halfH / inSampleSize >= reqHeight &&
                halfW / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 水印
     */
    private fun addTextWatermark(src: Bitmap, text: String): Bitmap {
        val width = src.width
        val height = src.height

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 原图
        canvas.drawBitmap(src, 0f, 0f, null)

        val margin = 20f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE          // 白色文字
            textSize = width / 40f
            alpha = 230
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66000000") // 半透明黑背景
        }

        val lines = text.split("\n")

        // 先计算整体高度（用于画背景）
        val lineHeight = paint.textSize + 10
        val totalHeight = lines.size * lineHeight

        // 找最长文本宽度
        val maxTextWidth = lines.maxOf { paint.measureText(it) }

        // 右下角起点（背景区域）
        val right = width - margin
        val bottom = height - margin

        val left = right - maxTextWidth - margin * 2
        val top = bottom - totalHeight - margin * 2

        // ===== 1. 先画背景块 =====
        canvas.drawRoundRect(
            left,
            top,
            right,
            bottom,
            12f, 12f,
            bgPaint
        )

        // ===== 2. 再画文字 =====
        var y = top + lineHeight

        for (line in lines) {
            val x = left + margin
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }

        return result
    }
    /**
     * 保存
     */
    private fun saveBitmap(bitmap: Bitmap, file: File): String? {
        return try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}