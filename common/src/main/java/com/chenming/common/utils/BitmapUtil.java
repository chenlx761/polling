package com.chenming.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * desc:
 * author: shcx
 * date: 2021/9/10 9:45
 */
public class BitmapUtil {

    /**
     * 质量压缩
     *
     * @param format  图片格式 jpeg,png,webp
     * @param quality 图片的质量,0-100,数值越小质量越差
     */
    public static String compress(Context context, Bitmap.CompressFormat format, int quality, String path) {
        File originFile = new File(path);
        Bitmap originBitmap = BitmapFactory.decodeFile(originFile.getAbsolutePath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        originBitmap.compress(format, quality, bos);
        try {
            String fileName =
                    DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))
                            .toString() + "_crop.jpg";
            File file = new File(context.getExternalCacheDir().getPath() + "/" + fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
