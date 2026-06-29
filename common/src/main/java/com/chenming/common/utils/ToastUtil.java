package com.chenming.common.utils;

import android.view.Gravity;
import android.widget.Toast;



/**
 * Created by Admin on 2018/3/12.
 */

public class ToastUtil {
    private static Toast toast;//实现不管我们触发多少次Toast调用，都只会持续一次Toast显示的时长
    private static Toast commonToast;

    /**
     * 短时间显示Toast【居下】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showShortToast(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_SHORT);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }


    /**
     * 短时间成功显示Toast【居中】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showShortToastCenter(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_SHORT);
                commonToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }

    /**
     * 短时间显示Toast【居上】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showShortToastTop(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_SHORT);
                commonToast.setGravity(Gravity.TOP, 0, 0);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }

    /**
     * 长时间显示Toast【居下】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showLongToast(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_LONG);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }

    /**
     * 长时间显示Toast【居中】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showLongToastCenter(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_LONG);
                commonToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }

    /**
     * 长时间显示Toast【居上】
     *
     * @param msg 显示的内容-字符串
     */
    public static void showLongToastTop(String msg) {
        if (CommApplication.Companion.getInstance() != null) {
            if (commonToast == null) {
                commonToast = Toast.makeText(CommApplication.Companion.getInstance(), msg, Toast.LENGTH_LONG);
                commonToast.setGravity(Gravity.TOP, 0, 0);
            } else {
                commonToast.setText(msg);
            }
            commonToast.show();
        }
    }

    /**
     * toast取消
     */
    public static void cancel() {

        if (toast != null) {
            toast.cancel();
            toast = null;
        }

        if (commonToast != null) {
            commonToast.cancel();
            commonToast = null;
        }

    }
}
