package com.chenming.httprequest.http;


import android.text.TextUtils;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import com.chenming.httprequest.BuildConfig;
import com.chenming.httprequest.XLog;
import com.chenming.httprequest.http.constant.HttpBaseUrl;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wqx on 2018/4/25.
 */

public class HttpManager {

    private static String mRequestSuccessCode="200";
    private static ApiService apiService;
    private static OkHttpClient okHttpClient;
    private static UploadListener mUploadListener;
    private static List<Interceptor> mNetWorkInterceptors = new ArrayList<>();
    private static List<Interceptor> mInterceptors = new ArrayList<>();
    private static Map<String, ApiService> mOtherApiService = new HashMap<>();//如果有多个请求地址的时候
    private static Map<String, Object> mPublicParameters = new HashMap<>();
    private static Map<String, String> mPublicHeaders = new HashMap<>();
    private static String mCurBaseUrl;

    public static Map<String, Object> getPublicParameters() {
        return mPublicParameters;
    }

    public static void setPublicParameters(Map<String, Object> mPublicParameters) {
        HttpManager.mPublicParameters = mPublicParameters;
    }

    public static Map<String, String> getmPublicHeaders() {
        return mPublicHeaders;
    }

    public static void setmPublicHeaders(Map<String, String> mPublicHeaders) {
        HttpManager.mPublicHeaders = mPublicHeaders;
    }

    private HttpManager() {
        initOkHttp();
    }


    /**
     * 获取特定请求地址的ApiService
     *
     * @param baseUrls
     * @return
     */
    public static ApiService api(String... baseUrls) {
        if (baseUrls == null || baseUrls.length == 0) {
            return api();
        }
        String baseUrl = "";
        if (baseUrls.length > 1) {
            XLog.Companion.e("baseUrl只能传一个");
        }
        baseUrl = baseUrls[0];
        if (baseUrl.equals(HttpBaseUrl.Companion.getBASE_URL())) {
            return api();
        }

        if (mOtherApiService.containsKey(baseUrl)) {
            return mOtherApiService.get(baseUrl);
        } else {
            ApiService apiService = new HttpManager().setHttpHost(baseUrl);
            mOtherApiService.put(baseUrl, apiService);
            return apiService;
        }

    }

    /**
     * 添加多一个请求地址
     *
     * @param baseUrl
     */
    private static void addBaseUrl(String baseUrl) {
        if (!mOtherApiService.containsKey(baseUrl)) {
            ApiService apiService = new HttpManager().setHttpHost(baseUrl);
            mOtherApiService.put(baseUrl, apiService);
        }
    }

    public static String getBaseUrl() {
        return mCurBaseUrl;
    }

    /**
     * 在第一次调用api()前使用
     * 修改请求地址
     */
    public static void setBaseUrl(String baseUrl) {
        apiService = null;
        mCurBaseUrl = baseUrl;
        HttpBaseUrl.Companion.setBASE_URL(baseUrl);
    }


    public static String getRequestSuccessCode() {
        return mRequestSuccessCode;
    }

    public static void setRequestSuccessCode(String requestSuccessCode) {
        mRequestSuccessCode = requestSuccessCode;
    }

    /**
     * 在第一次调用api()前使用
     * 添加网络拦截器
     */
    public static void addNetWorkInterceptor(Interceptor interceptor) {
        if (mNetWorkInterceptors == null) {
            mNetWorkInterceptors = new ArrayList<>();
        }

        if (!mNetWorkInterceptors.contains(interceptor)) {
            mNetWorkInterceptors.add(interceptor);
        }
    }

    /**
     * 在第一次调用api()前使用
     * 添加拦截器
     */
    public static void addInterceptor(Interceptor interceptor) {
        if (mInterceptors == null) {
            mInterceptors = new ArrayList<>();
        }

        if (!mInterceptors.contains(interceptor)) {
            mInterceptors.add(interceptor);
        }
    }


    private static ApiService api() {
        if (apiService == null) {
            synchronized (HttpManager.class) {
                if (apiService == null) {
                    apiService = new HttpManager().setHttpHost();
                }
            }
        }
        return apiService;
    }


    public static ApiService upLoadApi(UploadListener listener) {
        mUploadListener = listener;
        return api(); //不初始化  爆异常
    }


    public static void clearAllRequest() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll();
            okHttpClient.connectionPool().evictAll();
        }
    }


    private void initOkHttp() {
        MyHttpLogInterceptor httpLoggingInterceptor = new MyHttpLogInterceptor(new MyHttpLogInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                XLog.Companion.e(message);
            }
        });
        httpLoggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(httpLoggingInterceptor)
                .writeTimeout(10, TimeUnit.SECONDS);
        //如果还是一会儿就timeout 就用下面这两句代码
        //.pingInterval(15, TimeUnit.SECONDS)
        //.retryOnConnectionFailure(false)
        for (Interceptor interceptor :
                mNetWorkInterceptors) {
            builder.addNetworkInterceptor(interceptor);
        }

        for (Interceptor interceptor :
                mInterceptors) {
            builder.addInterceptor(interceptor);
        }
        //        okHttpClient =
        //                ProgressManager.getInstance().with(builder).build();
        okHttpClient = builder.build();
    }


    private ApiService setHttpHost() {
        if (TextUtils.isEmpty(HttpBaseUrl.Companion.getBASE_URL())) {
            XLog.Companion.e("请先设置请求地址");
            return null;
        }
        return setHttpHost(HttpBaseUrl.Companion.getBASE_URL());
    }


    private ApiService setHttpHost(String baseUrl) {
        if (okHttpClient == null)
            initOkHttp();


        ///////////////////////////////////////////////////set host/////////////////////////////
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 添加RxJava2的适配器支持
                .build();
        return retrofit.create(ApiService.class);
    }


    //    public static class TokenAuthenticator implements Authenticator {
    //
    //        @Override
    //        public Request authenticate(Route route, Response response) throws IOException {
    //            //            取出本地的refreshToken
    //            //                        String refreshToken = "sssgr122222222";
    //            UserInfo currentUser = MyApplication.getInstance().getCurrentUser();
    //            if (currentUser == null) {
    //                throw new IOException("登录失效,请重新登录~");
    //            } else {
    //                // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
    //                HashMap<String, String> requestHashMap = CommonUtils.getRequestHashMap("gettoken");
    //                requestHashMap.put("user_id", currentUser.getUser().getId() + "");
    //                Call<BaseBean<CheckToken>> baseToken = HttpManager.api().getBaseToken(new Gson().toJson(requestHashMap));
    //                //要用retrofit的同步方式
    //                BaseBean<CheckToken> body = baseToken.execute().body();
    //                MyApplication.getInstance().setToken(body.getData().getToken());
    //                return response.request().newBuilder()
    //                        .header("AUTHTOKEN", body.getData().getToken())
    //                        .build();
    //            }
    //            //            String string = response.body().string();
    //            //            LoginActivity.newInstance(AppManager.getAppManager().getTopActivity());
    //            ////            throw new IOException("登录失效,请重新登录~");
    //        }
    //    }
    //
    //

}
