package com.chenming.common.utils;

import android.content.Context;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableUtil {

    /**
     * 保存对象
     *
     * @param ser
     * @param file
     * @throws IOException
     */
    public static boolean saveObject(Serializable ser,
                                     String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = CommApplication.Companion.getInstance().openFileOutput(file, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Serializable readObject(String file) {
        if (!isExistDataCache(file))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = CommApplication.Companion.getInstance().openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = CommApplication.Companion.getInstance().getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean deleteObject(String file) {
        if (!isExistDataCache(file))
            return false;
        File data = CommApplication.Companion.getInstance().getFileStreamPath(file);
        data.delete();
        return true;
    }

    /**
     * 判断缓存是否存在
     *
     * @param cachefile
     * @return
     */
    public static boolean isExistDataCache(String cachefile) {
        boolean exist = false;
        File data = CommApplication.Companion.getInstance().getFileStreamPath(cachefile);
        if (data.exists())
            exist = true;
        return exist;
    }
}
