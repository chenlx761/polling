package com.chenming.common.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chenming.common.R;
import com.chenming.httprequest.http.utils.HttpGsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.core.content.FileProvider;

import static android.content.Context.VIBRATOR_SERVICE;

public class CommonUtils {


    private static MediaPlayer mMediaPlayer;

    static Ringtone mSmsRingtone;

    public static void playSmsSound() {
        if (mSmsRingtone == null)
            mSmsRingtone = RingtoneManager.getRingtone(CommApplication.getInstance(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mSmsRingtone.play();
    }


    /**
     * 将分钟和秒转换为总秒数
     *
     * @param min 分钟（如 3）
     * @param sec 秒（如 5）
     * @return 总秒数（如 3分5秒 → 185）
     */
    public static int minSecToSeconds(int min, int sec) {
        return min * 60 + sec;
    }

    public static int[] secondsToMinutesArray(int totalSeconds) {
        int[] result = new int[2];
        result[0] = totalSeconds / 60;  // 分钟
        result[1] = totalSeconds % 60;  // 秒
        return result;
    }


    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 打开assets下的音乐mp3文件
     */

    public static void openAssetMusics(Context context, String name) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        try {
            //播放 assets/a2.mp3 音乐文件
            AssetFileDescriptor fd = context.getAssets().openFd(name);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    /**
     * 判断app是否处于前台
     *
     * @return
     */
    public static boolean isAppOnForeground() {

        ActivityManager activityManager = (ActivityManager) CommApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = CommApplication.getInstance().getPackageName();
        /**
         * 获取Android设备中所有正在运行的App
         */
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    /**
     * 保留一位小数
     *
     * @param source
     * @return
     */
    public static String saveOneDecimals(double source) {
        return new DecimalFormat("0.0").format(source);

    }

    /**
     * 保留两位小数
     *
     * @param source
     * @return
     */
    public static String saveTwoDecimals(double source) {
        return new DecimalFormat("0.00").format(source);

    }

    /**
     * 保留多位喜爱哦书
     *
     * @param source
     * @return
     */
    public static String saveDecimals(float source, int decimalLength) {
        return new DecimalFormat("0." + "000000000".substring(0, decimalLength)).format(source);

    }

    /**
     * 保留一位小数
     *
     * @param source
     * @return
     */
    public static String saveOneDecimals(float source) {
        return new DecimalFormat("0.0").format(source);

    }

    public static <T> List<T> getRawList(Class<T> t, int resID) {
        return HttpGsonUtil.gson2List(CommonUtils.readRaw(resID), t);
    }

    public static String readRaw(int resId) {
        try {
            InputStreamReader inputReader = new InputStreamReader(CommApplication.getInstance().getResources().openRawResource(resId));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null)
                result += line;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 复制res/raw中的文件到指定目录
     *
     * @param context     上下文
     * @param id          资源ID
     * @param storagePath 目标文件夹的路径
     */

    public static void copyFilesFromRaw(Context context, int id, String storagePath) {
        InputStream inputStream = context.getResources().openRawResource(id);
        readInputStream(storagePath, inputStream);

    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */

    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);

                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void playWav(String path) {

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        Uri uri = Uri.fromFile(new File(path));
        try {
            //播放器载入资源
            mMediaPlayer = MediaPlayer.create(CommApplication.getInstance(), uri);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            });
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 震动
     *
     * @param context
     */
    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length, (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }


    public static void myEncrypt(Map<String, Object> params) {

        //组成参数
        params.put("sign", getMd5Sign(params));
    }


    public static String getMd5Sign(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder();
        try {
            Map<String, Object> signParams = new HashMap<>();
            for (String key : params.keySet()) {
                if (params.get(key) != null && !TextUtils.isEmpty(params.get(key).toString())) {
                    signParams.put(key, params.get(key));
                }
            }

            //按字典排序
            Set<String> allKeys = getAllKeys(signParams);
            List<String> list = new ArrayList<>(allKeys);
            Collections.sort(list);

            for (int i = 0; i < list.size(); i++) {

                builder.append(list.get(i));
                builder.append("=");
                builder.append(signParams.get(list.get(i)));
                if (i != list.size() - 1) {
                    builder.append("&");
                }

            }


            //md5加密
            String md5Value = getMd5Value(builder.toString()).toUpperCase();
            return md5Value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * 得到JSONObject里的所有key
     *
     * @return Set
     */
    private static Set<String> getAllKeys(Map<String, Object> map) {
        return map.keySet();
    }

    /**
     * 从JSON字符串里得到所有key
     *
     * @param jsonString json字符串
     * @return Set
     */
    private static Set<String> getAllKeys(String jsonString) {
        HashMap map = JSON.parseObject(jsonString, HashMap.class);
        return map.keySet();
    }


    public static String getMd5Value(String sSecret) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(sSecret.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();// 加密
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * MD5加密工具
     * Created by zmx
     *
     * @return
     */
    public static String string2MD5(String myinfo) {
        byte[] digesta = null;
        try {
            byte[] btInput = myinfo.getBytes("utf-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            digesta = mdInst.digest();
        } catch (Exception ex) {
            System.out.println("非法摘要算法");
        }
        return byte2hex(digesta);
    }

    private static String byte2hex(byte[] b) {
        StringBuilder md5Str = new StringBuilder();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                md5Str.append(0 + stmp);
            } else {
                md5Str.append(stmp);
            }
            if (n < b.length - 1) {
                md5Str.append("");
            }
        }
        return md5Str.toString();
    }
}
