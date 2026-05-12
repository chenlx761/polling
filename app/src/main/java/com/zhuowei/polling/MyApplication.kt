package com.zhuowei.polling

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.multidex.MultiDex
import com.chenming.common.utils.CommApplication
import com.chenming.httprequest.http.HttpManager
import com.chenming.httprequest.http.bean.BaseBean
import com.google.gson.Gson
import com.zhuowei.hudun.HuDunApplication
import com.zhuowei.hudun.HuDunManager
import com.zhuowei.polling.constants.HttpConstants
import com.zhuowei.polling.ui.activitys.login.LoginActivity
import com.zhuowei.polling.utils.SpManager
import okhttp3.Interceptor
import okhttp3.Response


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



//        private fun refreshTokenSync(): String {
//
//            val postSync = RetrofitUtil.Builder(HttpConstants.GET_TS_ID_URL)
//                .addPara("refresh_token", SpManager.getRefreshToken())
//                .addPara("grant_type", "refresh_token").addHeader("Host", HttpConstants.BASE_HOST)
//                .build().postFormSync(LoginResult::class.java, HttpConstants.HD_BASE_URL)
//            val accessToken = postSync.data.access_token
//            SpManager.setToken(accessToken)
//            SpManager.setRefreshToken(postSync.data.refresh_token)
//            return accessToken
//        }
    }

    private fun navigateToLogin() {
        HuDunManager.instance.release()
        val intent = Intent(instance, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        instance?.startActivity(intent)
    }

    private fun isResponseCode401(response: Response): Boolean {
        return try {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            val baseBean = Gson().fromJson(responseBody, BaseBean::class.java)
            baseBean.code.toInt() == 401
        } catch (e: Exception) {
            false
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun isLocationTest(): Boolean {
        return BuildConfig.FLAVOR.equals("locationTest")
    }

    override fun onCreate() {
        super.onCreate()
        instance = this



        HuDunApplication.getInstance().application = this
        CommApplication.setInstance(this)
        HttpManager.setBaseUrl(HttpConstants.BASE_URL)
        //添加请求头拦截器
        HttpManager.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestPath = originalRequest.url.encodedPath
            if (requestPath.contains(HttpConstants.GET_TS_ID_URL)) {
                chain.proceed(originalRequest)
            } else {
                val token = SpManager.getToken()
                if (token.isNotEmpty()) {
                    val newRequest =
                        originalRequest.newBuilder().addHeader("Authorization", token).build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(originalRequest)
                }
            }
        }

        HttpManager.addInterceptor(Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401 || isResponseCode401(response)) {
                response.close()
                navigateToLogin()
                response
            } else {
                response
            }
        })
    }


}
