package com.chenming.httprequest

import android.util.Log


/**
 * Created by wuqx14 on 2020/12/28.
 */
class XLog private constructor() {
    companion object {
        var isDebug = true // 是否需要打印bug，可以在application的onCreate函数里面初始化
        private const val TAG = "XLOG----->"

        // 下面四个是默认tag的函数
        @JvmStatic
        fun i(msg: String) {
            if (isDebug) Log.i(TAG, msg)
        }

        @JvmStatic
        fun d(msg: String) {
            if (isDebug) Log.d(TAG, msg)
        }

        @JvmStatic
        fun e(msg: String) {
            if (isDebug) Log.e(TAG, msg)
        }

        @JvmStatic
        fun v(msg: String) {
            if (isDebug) Log.v(TAG, msg)
        }

        @JvmStatic
        fun w(msg: String) {
            if (isDebug) Log.w(TAG, msg)
        }

        // 下面是传入自定义tag的函数
        @JvmStatic
        fun i(tag: String, msg: String) {
            if (isDebug) Log.i(tag, msg)
        }

        @JvmStatic
        fun d(tag: String, msg: String) {
            if (isDebug) Log.d(tag, msg)
        }

        @JvmStatic
        fun e(tag: String, msg: String) {
            if (isDebug) Log.e(tag, msg)
        }

        @JvmStatic
        fun v(tag: String, msg: String) {
            if (isDebug) Log.v(tag, msg)
        }

        @JvmStatic
        fun w(tag: String, msg: String) {
            if (isDebug) Log.w(tag, msg)
        }
    }

    /**
     * Log统一管理类
     */
    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}