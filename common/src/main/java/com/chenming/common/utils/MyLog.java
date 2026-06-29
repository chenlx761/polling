package com.chenming.common.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * +----------------------------------------------------------------------
 * | com.runde
 * +----------------------------------------------------------------------
 * | 功能描述:
 * +----------------------------------------------------------------------
 * | 时　　间: 2021/9/20.
 * +----------------------------------------------------------------------
 * | 代码创建: chenmingdu
 * +----------------------------------------------------------------------
 **/
public class MyLog {
    private static MyLog mMyLog;

    /**
     * 日志保存路径
     */

    private static final String LOG_SAVE_PATH = CommApplication.Companion.getInstance().getExternalCacheDir().getPath() + "/log";

    /**
     * 日志开关
     */

    private static final boolean LOG_SWITCH = false;

    public static MyLog MyLogInstance() {
        if (mMyLog == null) {
            mMyLog = new MyLog();

        }

        return mMyLog;

    }

    /**
     * 插入日志
     */

    public synchronized void addLog(String logStr) {

        if (LOG_SWITCH) {
            File file = checkLogFileIsExist();

            if (file == null)

                return;

            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(file, true);

                fos.write((new Date().toLocaleString() + "" + logStr).getBytes("gbk"));

                fos.write("\r\n".getBytes("gbk"));

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (fos != null) {
                        fos.close();

                        fos = null;

                    }

                } catch (IOException e) {
                    e.printStackTrace();

                }

                fos = null;

                file = null;

            }

        }

    }

    /**
     * 检查日志文件是否存在
     */

    private File checkLogFileIsExist() {

        File file = new File(LOG_SAVE_PATH);

        if (!file.exists()) {
            file.mkdirs();

        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String dateStr = sdf.format(new Date());

        File file2 = new File(LOG_SAVE_PATH, dateStr + ".txt");

        if (!file2.exists()) {
            try {
                boolean newFile = file2.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();

            }

        }

        try {
            //删除其他日期的日志
            if (file.isDirectory()) {

                File[] childFiles = file.listFiles();
                if (childFiles != null)
                    for (int i = childFiles.length - 1; i >= 0; i--) {
                        File childFile = childFiles[i];
                        if (!childFile.getAbsolutePath().equals(file2.getAbsolutePath()))
                            childFile.delete();
                    }
            }

        } catch (Exception e) {

        }
        sdf = null;

        return file2;

    }

    /**
     * 检查当天日志文件是否存在
     *
     * @param file
     * @return
     */

    private boolean isLogExist(File file) {
        File tempFile = new File(LOG_SAVE_PATH);

        File[] files = tempFile.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[0].getName().trim().equalsIgnoreCase(file.getName())) {
                return true;

            }

        }

        return false;

    }

    /**
     * 打印异常堆栈信息
     *
     * @param e
     * @return
     */

    public static String getExceptionStackTrace(Throwable e) {
        if (e != null) {
            StringWriter sw = new StringWriter();

            PrintWriter pw = new PrintWriter(sw);

            e.printStackTrace(pw);

            return sw.toString();

        }

        return "";

    }
}
