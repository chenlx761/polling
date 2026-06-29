package com.chenming.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.chenming.httprequest.XLog;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.documentfile.provider.DocumentFile;

public class FileUtils {

    public static boolean insertMediaPic(Context context, String filePath, boolean isImg) {
        if (TextUtils.isEmpty(filePath))
            return false;
        File file = new File(filePath);
        //判断android Q  (10 ) 版本
        if (isAdndroidQ()) {
            if (file == null || !file.exists()) {
                return false;
            } else {
                try {
                    if (isImg) {
                        MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Video.Media.DATA, file.getAbsolutePath());
                        values.put(MediaStore.Video.Media.DISPLAY_NAME, file.getName());
                        values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
                        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                        values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
                        ContentResolver resolver = context.getContentResolver();
                        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {//老方法
            if (isImg) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis() + "");
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
            } else {
                ContentResolver localContentResolver = context.getContentResolver();
                ContentValues localContentValues = getVideoContentValues(new File(filePath), System.currentTimeMillis());
                Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
            }
            return true;
        }

    }

    public static boolean isAdndroidQ() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static ContentValues getVideoContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Video.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    public static String createCacheFileDependDate(String cacheDir, String fileFormat) {
        Date date = new Date();
        String ymd = new SimpleDateFormat("yyyyMMdd").format(date);
        String hms = new SimpleDateFormat("HHmmss").format(date);
        String saveDir = cacheDir + File.separator + ymd;
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("_");
        builder.append(ymd);
        //builder.append("-");
        builder.append(hms);
        builder.append(fileFormat);
        String fileName = builder.toString();
        File file = new File(saveDir, fileName);
        return file.getPath();
    }


    /*
     * Java文件操作 获取文件扩展名
     * */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 根据路径获取文件名
     *
     * @param path 路径参数
     * @return 文件名字符串
     */
    public static String splitVideoName(String path) {
        // 判空操作必须要有 , 处理方式不唯一 , 根据实际情况可选其一 。
        if (path == null) {
            return "";
        }


        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return path.substring(start + 1, end);//包含头不包含尾 , 故:头 + 1
        } else {
            return "";
            //            return null; 返回 null 还是 "" 根据情况抉择吧
        }
    }


    /**
     * 将Byte数组转换成文件
     *
     * @param bytes byte数组
     */
    public static String bytesToFile(String path, String fileName, byte[] bytes) {


        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {

            file = createFile(path, fileName);
            if (file == null)
                return "";

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if (file != null)
            return file.getPath();
        return "";
    }


    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String content, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        createFile(filePath, fileName);
        String mFilePath = filePath + "/" + fileName;
        try {
            File file = new File(mFilePath);
            FileOutputStream fw = new FileOutputStream(file, false);
            fw.write(content.getBytes(StandardCharsets.UTF_8));
            fw.close();
        } catch (IOException e) {
            XLog.e("写入错误: " + e.toString());
        }
    }

    public static File createFile(File dirFile) {
        try {
            if (!dirFile.exists()) {
                if (!createFileDir(dirFile)) {
                    XLog.e("createFile dirFile.mkdirs fail");
                    return null;
                }
            } else if (!dirFile.isDirectory()) {
                boolean delete = dirFile.delete();
                if (delete) {
                    return createFile(dirFile);
                } else {
                    XLog.e("createFile dirFile !isDirectory and delete fail");
                    return null;
                }
            }
            if (!dirFile.exists()) {
                if (!dirFile.createNewFile()) {
                    XLog.e("createFile createNewFile fail");
                    return null;
                }
            }
            return dirFile;
        } catch (Exception e) {
            XLog.e("createFile fail :" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static File createFile(String dirPath, String fileName) {
        try {
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                if (!createFileDir(dirFile)) {
                    XLog.e("createFile dirFile.mkdirs fail");
                    return null;
                }
            } else if (!dirFile.isDirectory()) {
                boolean delete = dirFile.delete();
                if (delete) {
                    return createFile(dirPath, fileName);
                } else {
                    XLog.e("createFile dirFile !isDirectory and delete fail");
                    return null;
                }
            }
            File file = new File(dirPath, fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    XLog.e("createFile createNewFile fail");
                    return null;
                }
            }
            return file;
        } catch (Exception e) {
            XLog.e("createFile fail :" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 创建文件夹---之所以要一层层创建，是因为一次性创建多层文件夹可能会失败！
     */
    public static boolean createFileDir(File dirFile) {
        if (dirFile == null)
            return true;
        if (dirFile.exists()) {
            return true;
        }
        File parentFile = dirFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            //父文件夹不存在，则先创建父文件夹，再创建自身文件夹
            return createFileDir(parentFile) && createFileDir(dirFile);
        } else {
            boolean mkdirs = dirFile.mkdirs();
            boolean isSuccess = mkdirs || dirFile.exists();
            if (!isSuccess) {
                Log.e("FileUtil", "createFileDir fail " + dirFile);
            }
            return isSuccess;
        }
    }


    public static byte[] assert2Byte(String name) {
        byte[] data = null;
        InputStream input = null;
        try {
            input = CommApplication.getInstance().getResources()
                    .getAssets().open(name);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    //文件到byte数组
    public static byte[] file2byte(String path) {
        byte[] data = null;
        FileInputStream input = null;
        try {
            input = new FileInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

            return data;
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return null;
    }

    //byte数组到16进制字符串
    public static String byte2string(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int buf[] = new int[data.length];
        //byte数组转化成十进制
        for (int k = 0; k < data.length; k++) {
            buf[k] = data[k] < 0 ? (data[k] + 256) : (data[k]);
        }
        //十进制转化成十六进制
        for (int k = 0; k < buf.length; k++) {
            if (buf[k] < 16)
                sb.append("0" + Integer.toHexString(buf[k]));
            else
                sb.append(Integer.toHexString(buf[k]));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     */
    public static String readFileByBytes(String fileName) {
        InputStream in = null;
        try {
            // 一次读多个字节
            byte[] tempbytes = new byte[2000];
            int byteread = 0;
            in = new FileInputStream(fileName);
            StringBuilder builder = new StringBuilder();
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                for (int i = 0; i < byteread; i++) {
                    int ascii = CommonBytesUtils.byte2ASCII(tempbytes[i]);
                    String s = CommonBytesUtils.numToHex(ascii);
                    builder.append(s);
                }
            }
            return builder.toString();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return "";
    }


    /**
     * 按照指定的路径和编码格式保存文件内容，这个方法因为用到了字符串作为载体，为了正确写入文件（不乱码），只能写入文本内容，安全方法
     *
     * @param data 将要写入到文件中的字节数据
     * @param path 文件路径,包含文件名
     * @return boolean
     * 当写入完毕时返回true;
     */
    public static boolean writeFile(byte data[], String path, String code) {
        boolean flag = true;
        OutputStreamWriter osw = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file = new File(file.getParent());
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
            if ("asci".equals(code)) {
                code = "GBK";
            }
            osw = new OutputStreamWriter(new FileOutputStream(path), code);
            osw.write(new String(data, code));
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        return flag;
    }

    /**
     * 获取sd卡目录
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }


    //file文件读取成byte[]
    public static byte[] readFile(File file) {
        RandomAccessFile rf = null;
        byte[] data = null;
        try {
            rf = new RandomAccessFile(file, "r");
            data = new byte[(int) rf.length()];
            rf.readFully(data);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeQuietly(rf);
        }
        return data;
    }


    public static String readTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                content = getString(instream);
            } catch (FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            }
        }
        return content;
    }

    public static String readBin(String strFilePath) {
        //打开文件
        File file = new File(strFilePath);
        try {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream, "ISO-8859-1");
            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer("");
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //关流
            br.close();
            isr.close();
            inputStream.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static void saveBitmap2file(Bitmap bmp, String fileName) {
        int quality = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        byte[] buffer = new byte[1024];
        int len = 0;
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e("FileUtils", e.toString());
            }
        } else {
            try {
                file.getParentFile().delete();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e("FileUtils", e.toString());
            }
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            while ((len = is.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            stream.flush();
        } catch (FileNotFoundException e) {
            Log.i("FileUtils", e.toString());
        } catch (IOException e) {
            Log.i("FileUtils", e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i("FileUtils", e.toString());
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.i("FileUtils", e.toString());
                }
            }
        }
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }

    public interface readBinListener {

        void onReadBinResult(String result);
    }

    public static void readBin(String strFilePath, readBinListener listener) {
        //打开文件
        RxJavaManager.getInstance().doTo(new RxJavaManager.OnNeedInNewThreadToDoListener<String>() {
            @Override
            public String inNewThreadToDo() throws IOException {
                XLog.Companion.e("开始时间:" + new Date());
                char[] binChar = new char[200000];
                File file = new File(strFilePath);
                try {
                    InputStream inputStream = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(inputStream, "ISO-8859-1");
                    BufferedReader br = new BufferedReader(isr);
                    int byteread = 0;
                    StringBuilder builder = new StringBuilder();
                    // 将一次读取的字节数赋给byteread
                    while ((byteread = br.read(binChar)) != -1) {
                        for (int i = 0; i < byteread; i++) {
                            int ascii = CommonBytesUtils.char2ASCII(binChar[i]);
                            String s = CommonBytesUtils.numToHex(ascii);
                            builder.append(s);
                        }
                    }

                    //关流
                    br.close();
                    isr.close();
                    inputStream.close();

                    XLog.Companion.e("结束时间:" + new Date());
                    String content = builder.toString();
                    binChar = null;

                    return content;
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            public void toDoFinish(String info) {
                listener.onReadBinResult(info);
            }

            @Override
            public void toDoFaild(Throwable throwable) {

            }
        });
    }

    //文件到byte数组
    public static String file2byteStr(String path) {
        byte[] data = null;
        FileInputStream input = null;
        try {
            input = new FileInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

            String s = byte2string(data);
            return s;
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return "";
    }


    //关闭读取file
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 获取文件名
     *
     * @param pathandname
     * @return
     */
    public static String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }


    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null)
            return "";
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null)
            return "";
        return documentFile.getName();
    }

    /**
     * 获取扩展名
     */
    public static String getExtension(String path) {
        if (path == null) {
            return "";
        }
        int dot = path.lastIndexOf(".");
        if (dot >= 0) {
            return path.substring(dot);
        } else {
            return "";
        }
    }

    public static String copyFile(Context context, String oldPath$Name, String newPath, String newName) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return "";
            }
            File appDir = new File(newPath);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            File newFile = new File(appDir, newName);
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();


            //deleteFile(oldPath$Name);
            return newFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface OnCopyFileProgressListener {
        void onCopyFileProgress(int progress);
    }

    public static String copyFileHasPro(Context context, InputStream is, String newPath, String newName, OnCopyFileProgressListener listener) {
        try {
            int pro = 0;
            long transferSize = 0;

            File appDir = new File(newPath);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            File newFile = new File(appDir, newName);
            long totalLength = newFile.length();
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024 * 1024];

            int byteRead;
            while (-1 != (byteRead = is.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
                transferSize += byteRead;
                pro = (int) (transferSize * 100 / totalLength);
                listener.onCopyFileProgress(pro);
            }
            is.close();
            fileOutputStream.flush();
            fileOutputStream.close();


            //deleteFile(oldPath$Name);
            return newFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String copyFile(Context context, InputStream is, String newPath, String newName) {
        try {

            File appDir = new File(newPath);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            File newFile = new File(appDir, newName);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = is.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            is.close();
            fileOutputStream.flush();
            fileOutputStream.close();


            //deleteFile(oldPath$Name);
            return newFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param activity 上下文对象
     * @param uri      图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Activity activity, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(activity, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) {
                // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(activity, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(activity, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }


    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "gbk");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * 获取图片uri
     */
    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // 如果图片不在手机的共享图片数据库，就先把它插入。
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取视频uri
     */
    public static Uri getVideoContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    //获取流并写入本地文件
    //ParcelFileDescriptor获取方式为：context.getContentResolver().openFileDescriptor(videoContentUri, "r")
    public static File getVideoGalleryFile(Context context, ParcelFileDescriptor fileDescriptor) {
        String storePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".mp4";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            FileInputStream fis = new FileInputStream(fileDescriptor.getFileDescriptor());

            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fis.read(buffer))) {
                fos.write(buffer, 0, byteRead);
            }
            fis.close();
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取bitmap并写入本地（其实获取流写入本地也行，类似于上面的方法，这里提供多一种可能）
    public static File getImageGalleryFile(Context context, Bitmap bmp) {
        String storePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            //不通知相册更新
            //MediaStore.Images.Media.insertImage(context.getContentResolver(),
            //                    bmp, fileName, null);
            if (isSuccess) {
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteFile(String file) {
        File data = new File(file);
        if (!data.exists())
            return false;
        data.delete();
        return true;
    }

    // 修改文件名
    public static boolean reName(String absolutePathName, String name) {
        File file = new File(absolutePathName);
        if (file == null)
            return false;
        if (!file.exists()) {
            System.out.println("file " + absolutePathName + " doesn't exits!");
            return false;
        } else {
            String parent = file.getParent();
            return file.renameTo(new File(parent + "/" + name));

        }
    }

    // 通过uri获取bitmap
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getFileSize(String path) {
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return f.length();
        }

        return 0;
    }

    //flie：要删除的文件夹的所在位置
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }


}
