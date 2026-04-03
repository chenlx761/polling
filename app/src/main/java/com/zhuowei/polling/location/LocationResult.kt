package com.zhuowei.polling.location

/**
 * 定位结果数据类
 * 包含经纬度坐标和地址信息
 */
data class LocationResult(
    /** 纬度 */
    val latitude: Double,
    /** 经度 */
    val longitude: Double,
    /** 地址信息 */
    val address: String,
    /** 国家名称 */
    val country: String = "",
    /** 省份名称 */
    val province: String = "",
    /** 城市名称 */
    val city: String = "",
    /** 区/县名称 */
    val district: String = "",
    /** 街道名称 */
    val street: String = "",
    /** 详细地址 */
    val streetNumber: String = "",
    /** 定位精度(米) */
    val radius: Float = 0f,
    /** 定位时间(毫秒时间戳) */
    val time: Long = 0
) {
    /** 判断定位是否成功 */
    val isSuccess: Boolean
        get() = latitude != 0.0 && longitude != 0.0

    /** 拼接完整地址 */
    val fullAddress: String
        get() = "$province$city$district$street$streetNumber"
}
