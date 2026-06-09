package com.zhuowei.polling.utils

/**
 * 坐标系转换工具类
 * 支持百度BD09、高德/腾讯GCJ02、WGS84坐标系之间的相互转换
 */
object MyCoordinateConverter {


    private const val X_PI = Math.PI * 3000.0 / 180.0

    /**
     * 百度坐标(BD09) -> 火星坐标(GCJ02)
     *
     * @param bdLat 百度纬度
     * @param bdLon 百度经度
     * @return Pair<lat, lon>
     */
    fun bd09ToGcj02(bdLat: Double, bdLon: Double): Pair<Double, Double> {
        val x = bdLon - 0.0065
        val y = bdLat - 0.006

        val z = Math.sqrt(x * x + y * y) -
                0.00002 * Math.sin(y * X_PI)

        val theta = Math.atan2(y, x) -
                0.000003 * Math.cos(x * X_PI)

        val gcjLon = z * Math.cos(theta)
        val gcjLat = z * Math.sin(theta)

        return Pair(gcjLat, gcjLon)
    }
}
