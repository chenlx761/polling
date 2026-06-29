package com.chenming.common.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Admin on 2018/4/9.
 */

public class AppManager {

    //private int appStatus= Constant.STATUS_FORCE_KILLED;//默认为被后台回收了
    private static final String TAG = "ActivityStackManager";

    //public int getAppStatus() {
    //        return appStatus;
    //    }

    //public void setAppStatus(int appStatus) {
    //   this.appStatus = appStatus;
    //}

    /**
     * Activity栈
     */
    private Stack<WeakReference<AppCompatActivity>> mActivityStack;
    private static AppManager activityStackManager = new AppManager();

    private AppManager() {
    }

    /***
     * 获得AppManager的实例
     *
     * @return AppManager实例
     */
    public static AppManager getAppManager() {
        if (activityStackManager == null) {
            activityStackManager = new AppManager();
        }
        return activityStackManager;
    }

    /***
     * 栈中Activity的数
     *
     * @return Activity的数
     */
    public int stackSize() {
        return mActivityStack.size();
    }

    /***
     * 获得Activity栈
     *
     * @return Activity栈
     */
    public Stack<WeakReference<AppCompatActivity>> getStack() {
        return mActivityStack;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(WeakReference<AppCompatActivity> activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(AppCompatActivity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new WeakReference<>(activity));
    }


    public boolean containActivity(String activityName) {
        if (mActivityStack == null) {
            return false;
        }
        for (WeakReference<AppCompatActivity> activity : mActivityStack) {
            if (activity.get().getClass().getName().contains(activityName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 删除ac
     *
     * @param activity 弱引用的ac
     */
    public void removeActivity(WeakReference<AppCompatActivity> activity) {
        if (mActivityStack != null) {
            mActivityStack.remove(activity);
        }
    }


    /**
     * 删除ac
     */
    public void removeActivity(AppCompatActivity activity) {
        try {
            Iterator<WeakReference<AppCompatActivity>> iterator = mActivityStack.iterator();
            while (iterator.hasNext()) {
                WeakReference<AppCompatActivity> stackActivity = iterator.next();
                if (stackActivity.get() == null) {
                    iterator.remove();
                    continue;
                }
                if (stackActivity.get().getClass().getName().equals(activity.getClass().getName())) {
                    iterator.remove();
                    stackActivity.get().finish();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /***
     * 获取栈顶Activity（堆栈中最后一个压入的）
     *
     * @return Activity
     */
    public AppCompatActivity getTopActivity() {
        AppCompatActivity activity = mActivityStack.lastElement().get();
        if (null == activity) {
            return null;
        } else {
            return mActivityStack.lastElement().get();
        }
    }

    /***
     * 通过class 获取栈顶Activity
     *
     * @param cls
     * @return Activity
     */
    public AppCompatActivity getActivityByClass(Class<?> cls) {
        AppCompatActivity return_activity = null;
        for (WeakReference<AppCompatActivity> activity : mActivityStack) {
            if (activity.get().getClass().equals(cls)) {
                return_activity = activity.get();
                break;
            }
        }
        return return_activity;
    }

    /**
     * 结束栈顶Activity（堆栈中最后一个压入的）
     */
    public void killTopActivity() {
        try {
            WeakReference<AppCompatActivity> activity = mActivityStack.lastElement();
            killActivity(activity);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /***
     * 结束指定的Activity
     *
     * @param activity
     */
    public void killActivity(WeakReference<AppCompatActivity> activity) {
        try {
            Iterator<WeakReference<AppCompatActivity>> iterator = mActivityStack.iterator();
            while (iterator.hasNext()) {
                WeakReference<AppCompatActivity> stackActivity = iterator.next();
                if (stackActivity.get() == null) {
                    iterator.remove();
                    continue;
                }
                if (stackActivity.get().getClass().getName().equals(activity.get().getClass().getName())) {
                    iterator.remove();
                    stackActivity.get().finish();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /***
     * 结束指定类名的Activity
     *
     * @param cls
     */
    public void killActivity(Class<?> cls) {
        try {
            ListIterator<WeakReference<AppCompatActivity>> listIterator = mActivityStack.listIterator();
            while (listIterator.hasNext()) {
                Activity activity = listIterator.next().get();
                if (activity == null) {
                    listIterator.remove();
                    continue;
                }
                if (activity.getClass() == cls) {
                    listIterator.remove();
                    if (activity != null) {
                        activity.finish();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 结束所有Activity
     */
    public void killAllActivity() {
        try {
            ListIterator<WeakReference<AppCompatActivity>> listIterator = mActivityStack.listIterator();
            while (listIterator.hasNext()) {
                Activity activity = listIterator.next().get();
                if (activity != null) {
                    activity.finish();
                }
                listIterator.remove();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 移除除了某个activity的其他所有activity
     *
     * @param cls 界面
     */
    public void killAllActivityExceptOne(Class cls) {
        try {
            for (int i = 0; i < mActivityStack.size(); i++) {
                WeakReference<AppCompatActivity> activity = mActivityStack.get(i);
                if (activity.get().getClass().equals(cls)) {
                    continue;
                }
                if (mActivityStack.get(i) != null) {
                    killActivity(activity);
                    i--;

                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        killAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
