package com.zhuowei.polling.location

/**
 * 定位回调接口
 */
interface LocationCallBack {
    /**
     * 定位成功回调
     * @param result 定位结果，包含坐标和地址信息
     */
    fun onLocationSuccess(result: LocationResult)

    /**
     * 定位失败回调
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    fun onLocationError(errorCode: Int, errorMessage: String)

    companion object {
        const val ERROR_NO_PERMISSION = 1001       // 权限不足
        const val ERROR_LOCATION_DISABLE = 1002    // 定位服务未开启
        const val ERROR_TIMEOUT = 1003            // 定位超时
        const val ERROR_UNKNOWN = 1099            // 未知错误
    }
}
