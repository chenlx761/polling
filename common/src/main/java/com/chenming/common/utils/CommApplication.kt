package com.chenming.common.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chenming.httprequest.XLog
import java.lang.ref.WeakReference

class CommApplication {

    companion object {
        private var instance: Application? = null

        @JvmStatic
        fun getInstance(): Application {
            return instance!!
        }

        @JvmStatic
        fun setInstance(application: Application) {
            this.instance = application


            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    if (activity is AppCompatActivity)
                        AppManager.getAppManager().addActivity(activity)
                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {
                    XLog.e("activity-->onActivityResumed:" + activity.javaClass.name)
                }

                override fun onActivityPaused(activity: Activity) {
                    XLog.e("activity-->onActivityPaused:" + activity.javaClass.name)
                }

                override fun onActivityStopped(activity: Activity) {

                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (activity is AppCompatActivity)
                        AppManager.getAppManager().removeActivity(activity)
                    XLog.e("activity-->onActivityDestroyed:" + activity.javaClass.name)
                }

            })
        }
    }
}