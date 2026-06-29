package com.chenming.common.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.chenming.httprequest.XLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

/**
 * 作者：xiaoguoqing
 * 创建时间：2019-06-11 下午 6:09
 * 文件描述：
 */

public class StatusBarUtil {

    /**
     * 设置状态栏为透明
     *
     * @param activity
     */
    @TargetApi(19)
    private static void setTranslucentStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    public static void setTranslucentStatus(Activity activity, boolean isDark) {

        //当系统版本为4.4或者4.4以上时可以使用沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置字体为深色
            //4.4以上才可以改文字图标颜色
            if (OSUtil.isMIUI()) {
                //小米MIUI系统
                setMIUIStatusBarTextMode(activity, isDark);
            } else if (OSUtil.isFlyme()) {
                //魅族flyme系统
                setFlymeStatusBarTextMode(activity, isDark);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //6.0以上，调用系统方法
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                //4.4以上6.0以下的其他系统，暂时没有修改状态栏的文字图标颜色的方法，有可以加上

            }

        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param activity
     * @param colorId
     */
    private static void setStatusBarColor(Activity activity, int colorId) {

        //Android6.0（API 23）以上，系统方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(activity.getResources().getColor(colorId));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            /**
             * 这里不能使用 ApplicationContext.getColor(R.color.white);获取
             * 垃圾小米4.4系统更改状态栏颜色的API识别不了这个颜色，会导致崩溃
             * */
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            setTranslucentStatus(activity);
            //设置状态栏颜色
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(colorId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setStatusBarTextModes(Activity activity, boolean isTextDark) {
        if (isTextDark) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);//黑色
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);//白色
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void onlySetStatusBarTextModes(Activity activity, boolean isTextDark) {
        if (isTextDark) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    );//黑色
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE
                    );//白色
        }
    }

    /**
     * 设置状态栏模式
     *
     * @param activity
     * @param isTextDark 文字、图标是否为黑色 （false为默认的白色）
     * @param colorId    状态栏颜色
     * @return
     */
    public static void setStatusBarMode(Activity activity, boolean isTextDark, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (OSUtil.isMIUI()) {
                //修改状态栏颜色
                setStatusBarColor(activity, colorId);
                //小米MIUI系统
                setMIUIStatusBarTextMode(activity, isTextDark);
            } else if (OSUtil.isFlyme()) {
                //修改状态栏颜色
                setStatusBarColor(activity, colorId);
                //魅族flyme系统
                setFlymeStatusBarTextMode(activity, isTextDark);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //修改状态栏颜色
                setStatusBarColor(activity, colorId);
                //6.0以上，调用系统方法更改字体颜色
                View decorView = activity.getWindow().getDecorView();
                if (isTextDark) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
            }
        }

        setRootViewFitsSystemWindows(activity, true);
    }


    /**
     * 设置Flyme系统状态栏的文字图标颜色
     *
     * @param activity
     * @param isDark   状态栏文字及图标是否为深色
     * @return
     */
    private static boolean setFlymeStatusBarTextMode(Activity activity, boolean isDark) {
        Window window = activity.getWindow();
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 设置MIUI系统状态栏的文字图标颜色（MIUIV6以上）
     *
     * @param activity
     * @param isDark   状态栏文字及图标是否为深色
     * @return
     */
    private static boolean setMIUIStatusBarTextMode(Activity activity, boolean isDark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (isDark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (isDark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View
                                .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                                .SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View
                                .SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            } catch (Exception e) {

            }
        }
        return result;
    }

    private static void setRootViewFitsSystemWindows(Activity activity, boolean fitSystemWindows) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup winContent = (ViewGroup) activity.findViewById(android.R.id.content);
            if (winContent.getChildCount() > 0) {
                ViewGroup rootView = (ViewGroup) winContent.getChildAt(0);
                if (rootView != null) {
                    rootView.setFitsSystemWindows(fitSystemWindows);
                }
            }
        }

    }

    //获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isStatusBarVisible(Activity baseActivity) {
        if ((baseActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0) {
            XLog.Companion.d("videoLayoutParamsas", "status bar is visible");
            return true;
        } else {
            XLog.Companion.d("videoLayoutParamsas", "status bar is not visible");
            return false;
        }
    }

    public static void translucentStatusBar(Activity activity) {
        translucentStatusBar(activity, true);
    }

    private static final int COLOR_TRANSLUCENT = Color.parseColor("#00000000");
    public static final int DEFAULT_COLOR_ALPHA = 112;

    /**
     * change to full screen mode
     *
     * @param hideStatusBarBackground hide status bar alpha Background when SDK > 21, true if hide it
     */
    public static void translucentStatusBar(Activity activity, boolean hideStatusBarBackground) {
        Window window = activity.getWindow();
        ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        //set child View not fill the system window
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int statusBarHeight = getStatusBarHeight(activity);

            //First translucent status bar.
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //After LOLLIPOP just set LayoutParams.
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (hideStatusBarBackground) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(COLOR_TRANSLUCENT);
                } else {
                    window.setStatusBarColor(calculateStatusBarColor(COLOR_TRANSLUCENT, DEFAULT_COLOR_ALPHA));
                }
                //must call requestApplyInsets, otherwise it will have space in screen bottom
                if (mChildView != null) {
                    ViewCompat.requestApplyInsets(mChildView);
                }
            } else {
                ViewGroup mDecorView = (ViewGroup) window.getDecorView();
                if (mDecorView.getTag() != null && mDecorView.getTag() instanceof Boolean && (Boolean) mDecorView.getTag()) {
                    mChildView = mDecorView.getChildAt(0);
                    //remove fake status bar view.
                    mContentView.removeView(mChildView);
                    mChildView = mContentView.getChildAt(0);
                    if (mChildView != null) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
                        //cancel the margin top
                        if (lp != null && lp.topMargin >= statusBarHeight) {
                            lp.topMargin -= statusBarHeight;
                            mChildView.setLayoutParams(lp);
                        }
                    }
                    mDecorView.setTag(false);
                }
            }
        }
    }

    //Get alpha color
    private static int calculateStatusBarColor(int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }


}
