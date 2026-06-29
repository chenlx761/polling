package com.chenming.httprequest.http;


import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by wqx on 2018/4/25.
 */

public interface ApiService {

    //同步Get请求
    @GET()
    Call<ResponseBody> executeGetSync(@Url String url, @QueryMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //同步POST请求
    @GET()
    Call<ResponseBody> executePostSync(@Url String url, @QueryMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //同步POST表单数据请求
    @FormUrlEncoded
    @GET()
    Call<ResponseBody> executePostFormSync(@Url String url, @FieldMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //同步PUT请求
    @GET()
    Call<ResponseBody> executePutSync(@Url String url, @QueryMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //同步Delete请求
    @GET()
    Call<ResponseBody> executeDeleteSync(@Url String url, @QueryMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //get方法
    @GET()
    Observable<ResponseBody> executeGet(@Url String url, @QueryMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //postJson方法
    @POST()
    Observable<ResponseBody> postJson(@Url String url, @Body Map<String, Object> map, @HeaderMap HashMap<String, String> headers);


    //putJson方法
    @PUT()
    Observable<ResponseBody> putJson(@Url String url, @Body Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    @FormUrlEncoded
    @PUT
    Observable<ResponseBody> putForm(@Url String url, @FieldMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //deleteJson方法
    @DELETE()
    Observable<ResponseBody> deleteJson(@Url String url, @Body Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //post表单方法
    @FormUrlEncoded
    @POST()
    Observable<ResponseBody> postForm(@Url String url, @FieldMap Map<String, Object> map, @HeaderMap HashMap<String, String> headers);

    //上传图片
    @POST()
    @Multipart
    Observable<ResponseBody> postFile(@Url String url, @Part MultipartBody.Part part, @HeaderMap HashMap<String, String> headers);

    //多文件上传
    @Multipart
    @POST()
    Observable<ResponseBody> postFiles(
            @Url String url, @PartMap Map<String, RequestBody> body, @HeaderMap HashMap<String, String> headers);


    //上传图片带参数的
    @POST()
    @Multipart
    Observable<ResponseBody> postFile(@Url String url, @PartMap Map<String, RequestBody> data, @Part MultipartBody.Part part, @HeaderMap HashMap<String, String> headers);

    //上传图片带参数的
    @POST()
    @Multipart
    Call<ResponseBody> postFileSync(@Url String url, @PartMap Map<String, RequestBody> data, @Part MultipartBody.Part part, @HeaderMap HashMap<String, String> headers);


    //多文件上传带参数的
    @Multipart
    @POST()
    Observable<ResponseBody> postFiles(
            @Url String url, @PartMap Map<String, RequestBody> data, @PartMap Map<String, RequestBody> body, @HeaderMap HashMap<String, String> headers);
}
