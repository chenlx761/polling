package com.zhuowei.polling

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.chenming.common.utils.CommApplication
import com.chenming.httprequest.http.HttpManager
import com.chenming.httprequest.http.RetrofitUtil
import com.zhuowei.hudun.HuDunApplication
import com.zhuowei.polling.beans.LoginResult
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.utils.SpManager
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by qixianWu on 2020/12/27.
 */
class MyApplication : Application() {


    companion object {
        private var instance: MyApplication? = null

        @JvmStatic
        fun getInstance(): MyApplication {
            return instance!!
        }

        private fun refreshTokenSync(): String {

            val postSync = RetrofitUtil.Builder(HttpConstants.GET_TS_ID_URL)
                .addPara("refresh_token", SpManager.getRefreshToken())
                .addPara("grant_type", "refresh_token").addHeader("Host", HttpConstants.BASE_HOST)
                .build().postFormSync(LoginResult::class.java, HttpConstants.HD_BASE_URL)
            val accessToken = postSync.data.access_token
            SpManager.setToken(accessToken)
            SpManager.setRefreshToken(postSync.data.refresh_token)
            return accessToken
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        HuDunApplication.getInstance().application = this
        CommApplication.setInstance(this)
       // HttpManager.setBaseUrl(HttpConstants.BASE_URL)
//        HttpManager.addInterceptor(Interceptor { chain ->
//            val originalRequest = chain.request()
//            val requestPath = originalRequest.url.encodedPath
//            if (requestPath.contains(HttpConstants.GET_TS_ID_URL)) {
//                chain.proceed(originalRequest)
//            } else {
//                val token = SpManager.getToken()
//                if (token.isNotEmpty()) {
//                    val newRequest =
//                        originalRequest.newBuilder().addHeader("Authorization", token).build()
//                    chain.proceed(newRequest)
//                } else {
//                    chain.proceed(originalRequest)
//                }
//            }
//        })

//        HttpManager.addInterceptor(Interceptor { chain ->
//            val response = chain.proceed(chain.request())
//            if (response.code == 401 && SpManager.getRefreshToken().isNotEmpty()) {
//                response.close()
//                val refreshedToken = refreshTokenSync()
//                if (refreshedToken.isNotEmpty()) {
//                    val newRequest =
//                        chain.request().newBuilder().addHeader("Authorization", refreshedToken)
//                            .build()
//                    chain.proceed(newRequest)
//                } else {
//                    response
//                }
//            } else {
//                response
//            }
//        })
    }


}
