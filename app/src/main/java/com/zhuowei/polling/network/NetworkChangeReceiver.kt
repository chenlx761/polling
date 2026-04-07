package com.zhuowei.polling.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/**
 * 网络状态变化广播接收器
 * 监听网络连接状态的变化，如WiFi、Mobile数据的开启/关闭、切换等
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    private var listener: NetworkChangeListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION ||
            intent.action == "android.net.conn.CONNECTIVITY_CHANGE") {
            
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isConnected = networkInfo != null && networkInfo.isConnected
            
            if (isConnected) {
                val networkType = getNetworkType(networkInfo!!.type)
                listener?.onNetworkConnected(networkType)
            } else {
                listener?.onNetworkDisconnected()
            }
        }
    }

    /**
     * 获取网络类型
     */
    private fun getNetworkType(networkType: Int): NetworkType {
        return when (networkType) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            ConnectivityManager.TYPE_MOBILE -> NetworkType.MOBILE
            ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * 设置网络状态监听器
     */
    fun setNetworkChangeListener(listener: NetworkChangeListener) {
        this.listener = listener
    }

    /**
     * 移除监听器，防止内存泄漏
     */
    fun removeListener() {
        this.listener = null
    }

    /**
     * 网络状态变化监听接口
     */
    interface NetworkChangeListener {
        fun onNetworkConnected(type: NetworkType)
        fun onNetworkDisconnected()
    }

    /**
     * 网络类型枚举
     */
    enum class NetworkType {
        WIFI,
        MOBILE,
        ETHERNET,
        UNKNOWN
    }
}
