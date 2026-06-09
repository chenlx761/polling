package com.zhuowei.polling.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption


/**
 * 百度地图定位管理器
 * 实现单次定位功能，返回坐标系和地址信息
 */
class BaiDuLocationManager private constructor() {

    private var locationClient: LocationClient? = null
    private var locationCallBack: LocationCallBack? = null
    private var isLocating = false

    companion object {
        @JvmStatic
        val instance: BaiDuLocationManager by lazy { BaiDuLocationManager() }

    }

    /**
     * 检查定位权限是否完整
     */
    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查定位服务是否开启
     */
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * 初始化定位客户端
     * @param context 上下文
     */
    private fun initLocationClient(context: Context) {
        if (locationClient == null) {
            LocationClient.setAgreePrivacy(true)
            locationClient = LocationClient(context.applicationContext)
        }
    }

    /**
     * 配置定位参数
     * 使用高精度模式，获取地址信息
     */
    private fun setLocationOption(): LocationClientOption {
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy // 高精度定位模式
        option.setCoorType("bd09ll") // 百度坐标系 (BD-09)
        option.setScanSpan(0) // 单次定位，扫描间隔为0
        option.setIsNeedAddress(true) // 需要地址信息
        option.setIsNeedLocationDescribe(true) // 需要位置描述
        option.setIsNeedLocationPoiList(true) // 需要POI列表
        option.setNeedDeviceDirect(false) // 不需要设备方向
        option.setIgnoreKillProcess(true) // 定位SDK内部优先级处理
        option.SetIgnoreCacheException(true) // 忽略缓存异常
        option.setWifiCacheTimeOut(5 * 60 * 1000) //  wifi缓存超时时间
        option.setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC)
        option.setEnableSimulateGps(false) // 不允许模拟定位
        return option
    }

    /**
     * 单次定位
     * @param context 上下文
     * @param callBack 定位结果回调
     */
    fun requestLocation(context: Context, callBack: LocationCallBack) {
        // 检查是否正在定位
        if (isLocating) {
            stopLocation()
        }

        // 检查权限
        if (!hasLocationPermission(context)) {
            callBack.onLocationError(
                LocationCallBack.ERROR_NO_PERMISSION,
                "缺少定位权限，请先授予定位权限"
            )
            return
        }

        // 检查定位服务是否开启
        if (!isLocationEnabled(context)) {
            callBack.onLocationError(
                LocationCallBack.ERROR_LOCATION_DISABLE,
                "定位服务未开启，请在设置中开启定位服务"
            )
            return
        }

        isLocating = true
        locationCallBack = callBack

        initLocationClient(context)

        // 设置定位参数
        locationClient?.locOption = setLocationOption()

        // 设置定位监听器
        locationClient?.registerLocationListener(object : BDAbstractLocationListener() {

            override fun onReceiveLocation(location: BDLocation?) {
                handleLocationResult(location)
            }
        })

        // 启动定位
        locationClient?.start()


    }

    /**
     * 处理定位结果
     */
    private fun handleLocationResult(location: BDLocation?) {
        if (!isLocating) return

        isLocating = false

        if (location == null) {
            locationCallBack?.onLocationError(
                LocationCallBack.ERROR_UNKNOWN,
                "定位结果为空"
            )
            return
        }

        // 检查定位结果是否有效
        val locType = location.locType
        if (locType == BDLocation.TypeGpsLocation ||
            locType == BDLocation.TypeNetWorkLocation ||
            locType == BDLocation.TypeOffLineLocation
        ) {

            // 构建定位结果
            val result = LocationResult(
                latitude = location.latitude,
                longitude = location.longitude,
                address = if (TextUtils.isEmpty(location.addrStr)) "" else location.addrStr,
                country = location.country ?: "",
                province = location.province ?: "",
                city = location.city ?: "",
                district = location.district ?: "",
                street = location.street ?: "",
                streetNumber = location.streetNumber ?: "",
                radius = location.radius,
                time = location.timeStamp
            )

//            val bd09ToGcj02 = MyCoordinateConverter.bd09ToGcj02(location.longitude, location.latitude)
//
//
//            //初始化左边转换工具类，指定源坐标类型和坐标数据
////sourceLatLng 待转换坐标
//            val converter = CoordinateConverter()
//                .from(CoordinateConverter.CoordType.COMMON)
//                .coord(LatLng(bd09ToGcj02.first, bd09ToGcj02.second))
//
//
////转换坐标
//            val desLatLng = converter.convert()

            locationCallBack?.onLocationSuccess(result)
        } else {
            val errorMsg = getLocationErrorMessage(locType)
            locationCallBack?.onLocationError(
                LocationCallBack.ERROR_UNKNOWN,
                errorMsg
            )
        }

        stopLocation()
    }

    /**
     * 根据错误码获取错误信息
     */
    private fun getLocationErrorMessage(locType: Int): String {
        return when (locType) {
            BDLocation.TypeNetWorkException -> "网络定位失败"
            BDLocation.TypeCriteriaException -> "无法获取有效定位依据"
            else -> "定位失败，错误码: $locType"
        }
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        if (locationClient?.isStarted == true) {
            locationClient?.stop()
        }
        isLocating = false
    }

    /**
     * 释放资源
     * 在不需要定位时调用
     */
    fun release() {
        stopLocation()
        locationClient?.unRegisterLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(p0: BDLocation?) {
            }
        })
        locationClient = null
        locationCallBack = null
    }

    /**
     * 检查是否正在定位
     */
    fun isLocating(): Boolean = isLocating
}
