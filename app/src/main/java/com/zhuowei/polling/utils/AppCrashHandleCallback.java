package com.zhuowei.polling.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.chenming.common.utils.AppManager;
import com.chenming.httprequest.XLog;
import com.tencent.bugly.crashreport.CrashReport.CrashHandleCallback;
import com.zhuowei.polling.BuildConfig;
import com.zhuowei.polling.MainActivity;
import com.zhuowei.polling.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AppCrashHandleCallback extends CrashHandleCallback {


    public AppCrashHandleCallback() {
    }

    @Override
    public synchronized Map<String, String> onCrashHandleStart(int crashType, String s, String s1, String s2) {
        Log.e("CrashHandle", "======== onCrashHandleStart =========\ncrashType=" + crashType + "\n" + s + "\n" + s1 + "\n" + s2);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss\r\n");
        StringBuffer sb = new StringBuffer(sdf.format(new Date(System.currentTimeMillis())));
        sb.append("crashType:").append(crashType).append("\r\n").append("s:").append(s).append("\r\n").append("s1:").append(s1).append("\r\n").append("s2:").append(s2).append("\r\n").append("------------------------------------");
        XLog.e(sb.toString());

        // 写入本地文件
        writeCrashToFile(sb.toString());

        switch (crashType) {
            case CrashHandleCallback.CRASHTYPE_JAVA_CATCH:
                if (!BuildConfig.DEBUG)
                    restart();
                break;
            case CrashHandleCallback.CRASHTYPE_JAVA_CRASH:
            case CrashHandleCallback.CRASHTYPE_NATIVE:
            case CrashHandleCallback.CRASHTYPE_ANR:
                if (!BuildConfig.DEBUG)
                    restart();
                break;
            case CrashHandleCallback.CRASHTYPE_U3D:
                break;
            case CrashHandleCallback.CRASHTYPE_BLOCK:
                break;
            default:
                break;
        }
        return super.onCrashHandleStart(crashType, s, s1, s2);
    }

    private void restart() {
        try {
            String packageName = MyApplication.getInstance().getPackageName();
            String className = MainActivity.class.getName();
            //创建一个Intent来启动应用程序
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //启动Activity来重启应用程序

            AppManager.getAppManager().killAllActivity();
            MyApplication.getInstance().startActivity(intent);
        } catch (Exception e) {
            XLog.e(e.getMessage());
        }


    }

    private void writeCrashToFile(String crashInfo) {
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            // 获取应用私有目录
            File crashDir = new File(MyApplication.getInstance().getFilesDir(), "crash_logs");
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }
            // 生成文件名：crash_时间戳.log
            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "crash_" + fileSdf.format(new Date()) + ".log";
            File crashFile = new File(crashDir, fileName);
            fos = new FileOutputStream(crashFile, true);
            pw = new PrintWriter(fos);
            pw.println(crashInfo);
            pw.flush();
            Log.d("PTT-CrashHandle", "Crash log written to: " + crashFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("PTT-CrashHandle", "Failed to write crash log", e);
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("CrashHandle", "Failed to close fos", e);
                }
            }
        }
    }
}
