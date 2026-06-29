package com.chenming.httprequest.http;

import com.chenming.httprequest.http.bean.BaseBean;
import com.chenming.httprequest.http.constant.HttpBaseUrl;
import com.chenming.httprequest.http.exception.ExceptionManager;
import com.chenming.httprequest.http.listener.OnHttpCallBack;
import com.chenming.httprequest.http.utils.GsonUtil;
import com.chenming.httprequest.http.utils.HttpGsonUtil;
import com.chenming.httprequest.http.utils.ProgressRequestBody;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class RetrofitUtil {
    private String mUrl;
    private Map<String, Object> mRequestMap;
    private HashMap<String, String> mHeaderMap;
    private String mFilePath;
    private List<String> mFilePaths;
    private String mMediaType;
    private String mFileKey;
    private String mFilesKey;

    public static class Builder {

        private final RetrofitUtil mRetrofitUpdateUtil;

        public Builder(String url) {
            mRetrofitUpdateUtil = new RetrofitUtil();
            mRetrofitUpdateUtil.mUrl = url;

            mRetrofitUpdateUtil.mHeaderMap = new HashMap<>();
            mRetrofitUpdateUtil.mHeaderMap.putAll(HttpManager.getmPublicHeaders());

            mRetrofitUpdateUtil.mRequestMap = new HashMap<>();
            mRetrofitUpdateUtil.mRequestMap.putAll(HttpManager.getPublicParameters());
        }

        public Builder setFile(String filePath) {
            return setFile(filePath, "image/jpg", "file");
        }

        public Builder setFiles(List<String> filePaths) {
            return setFile(filePaths, "image/jpg", "file[]\"; filename=\"");
        }

        public Builder setFile(String filePath, String mediaType, String fileKey) {
            mRetrofitUpdateUtil.mFilePath = filePath;
            mRetrofitUpdateUtil.mMediaType = mediaType;
            mRetrofitUpdateUtil.mFileKey = fileKey;
            return this;
        }

        public Builder setFile(List<String> filePaths, String mediaType, String fileKey) {
            mRetrofitUpdateUtil.mFilePaths = filePaths;
            mRetrofitUpdateUtil.mMediaType = mediaType;
            mRetrofitUpdateUtil.mFilesKey = fileKey;
            return this;
        }


        public Builder addPara(String key, Object value) {
            if (mRetrofitUpdateUtil.mRequestMap == null) {
                mRetrofitUpdateUtil.mRequestMap = new HashMap<>();
            }
            mRetrofitUpdateUtil.mRequestMap.put(key, value);
            return this;
        }

        public Builder addPara(Map<String, Object> requestMap) {
            if (mRetrofitUpdateUtil.mRequestMap == null) {
                mRetrofitUpdateUtil.mRequestMap = new HashMap<>();
            }
            mRetrofitUpdateUtil.mRequestMap.putAll(requestMap);
            return this;
        }

        public Builder addPara(Object object) {
            if (mRetrofitUpdateUtil.mRequestMap == null) {
                mRetrofitUpdateUtil.mRequestMap = new HashMap<>();
            }
            HashMap hashMap = GsonUtil.fromJson(GsonUtil.toJson(object), HashMap.class);
            mRetrofitUpdateUtil.mRequestMap.putAll(hashMap);
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (mRetrofitUpdateUtil.mHeaderMap == null) {
                mRetrofitUpdateUtil.mHeaderMap = new HashMap<>();
            }
            mRetrofitUpdateUtil.mHeaderMap.put(key, value);
            return this;
        }

        public Builder addHeader(Map<String, String> headerMap) {
            if (mRetrofitUpdateUtil.mHeaderMap == null) {
                mRetrofitUpdateUtil.mHeaderMap = new HashMap<>();
            }
            mRetrofitUpdateUtil.mHeaderMap.putAll(headerMap);
            return this;
        }

        public RetrofitUtil build() {
            return mRetrofitUpdateUtil;
        }
    }

    private RetrofitUtil() {

    }


    /**
     * get同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<T> getSync(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executeGetSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToBean(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * get同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<List<T>> getSyncGetList(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executeGetSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToList(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }


    /**
     * post同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<List<T>> postSyncGetList(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePostSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToList(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * post表单同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<List<T>> postFormSyncGetList(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePostFormSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToList(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * post同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<T> postSync(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePostSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToBean(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * post表单数据同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<T> postFormSync(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePostFormSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToBean(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }


    /**
     * put同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<T> putSync(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePutSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToBean(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * put同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<List<T>> putSyncGetList(Class<T> t, String... baseurl) {
        try {
            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executePutSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToList(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * delete同步请求
     *
     * @param t   需要返回的类型
     * @param <T>
     * @return
     */
    public <T> BaseBean<T> deleteSync(Class<T> t, String... baseurl) {

        try {

            Call<ResponseBody> responseBody = HttpManager.api(baseurl).executeDeleteSync(mUrl, mRequestMap, mHeaderMap);
            return handlerRequestBodyToBean(responseBody.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }


    //get请求
    public <T> Disposable get(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //get请求列表
    public <T> Disposable getList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap
                )
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    public interface doToNextListener<T> {
        void doToNext(BaseBean<T> bean);
    }


    //get请求需要doDoNext的
    public <T> Disposable getNeedToDoNext(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, final doToNextListener<T> doToNextListener
            , String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(Schedulers.newThread()) //指定doOnNext执行线程是新线程
                .doOnNext(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> bean) throws Exception {
                        doToNextListener.doToNext(bean);


                        //  Schedulers.io().createWorker().schedule(new Runnable() {
                        //      @Override
                        //      public void run() {
                        //          //如果耗时操作写在这里,就不会影响subscribe了,因为会同步操作
                        //      }
                        //  });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //get请求List需要doDoNext的
    public <T> Disposable getListNeedToDoNext(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, final doToNextListener<List<T>> doToNextListener
            , String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(Schedulers.newThread()) //指定doOnNext执行线程是新线程
                .doOnNext(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> bean) throws Exception {
                        doToNextListener.doToNext(bean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //上传json数据
    public <T> Disposable postJson(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).postJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //deletejson数据
    public <T> Disposable deleteJson(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).deleteJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //putjson数据获取列表
    public <T> Disposable deleteJsonGetList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).deleteJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //putjson数据
    public <T> Disposable putJson(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).putJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //putjson数据获取列表
    public <T> Disposable putJsonGetList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).putJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //上传json数据获取列表
    public <T> Disposable postJsonGetList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).postJson(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //上传form表单数据
    public <T> Disposable postForm(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).postForm(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }

    //putform表单数据
    public <T> Disposable putForm(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).putForm(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //上传form表单数据获取列表
    public <T> Disposable postFormGetList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).postForm(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }

    //putform表单数据获取列表
    public <T> Disposable putFormGetList(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {

        return HttpManager.api(baseurl).putForm(mUrl, mRequestMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行

    }


    //上传图片带参数
    public <T> BaseBean<T> postFileSync(Class<T> t, String... baseurl) {
        Map<String, RequestBody> dataMap = new WeakHashMap<>();
        //遍历map中所有参数到builder
        if (mRequestMap != null) {
            for (String key : mRequestMap.keySet()) {
                dataMap.put(key, RequestBody.create(null, (String) mRequestMap.get(key)));
            }
        }
        MultipartBody.Part file;
        if (mUploadListener != null) {
            File uploadFile = new File(mFilePath);
            ProgressRequestBody requestFile = new ProgressRequestBody(uploadFile, mMediaType, mUploadListener);
            file = MultipartBody.Part.createFormData(mFileKey, uploadFile.getName(), requestFile);
        } else {

            file = prepareFilePart(mFileKey, mFilePath, mMediaType);
        }

        Call<ResponseBody> call = HttpManager.api(baseurl).postFileSync(mUrl, dataMap, file, mHeaderMap);
        try {
            return handlerRequestBodyToBean(call.execute().body().string(), t);
        } catch (Exception e) {
            return null;
        }

    }

    //上传图片带参数
    public <T> Disposable postFile(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, String... baseurl) {
        Map<String, RequestBody> dataMap = new WeakHashMap<>();
        //遍历map中所有参数到builder
        if (mRequestMap != null) {
            for (String key : mRequestMap.keySet()) {
                dataMap.put(key, RequestBody.create(null, (String) mRequestMap.get(key)));
            }
        }
        MultipartBody.Part file;
        if (mUploadListener != null) {
            File uploadFile = new File(mFilePath);
            ProgressRequestBody requestFile = new ProgressRequestBody(uploadFile, mMediaType, mUploadListener);
            file = MultipartBody.Part.createFormData(mFileKey, uploadFile.getName(), requestFile);
        } else {

            file = prepareFilePart(mFileKey, mFilePath, mMediaType);
        }
        return HttpManager.api(baseurl).postFile(mUrl, dataMap, file, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<T>>() {
                    @Override
                    public void accept(BaseBean<T> info) throws Exception {
                        mUploadListener = null;
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mUploadListener = null;
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行
    }


    private UploadListener mUploadListener;

    //上传有进度的
    public <T> Disposable postFileNeedProgress(Class<T> t
            , final OnHttpCallBack<BaseBean<T>> callBack, UploadListener listener, String... baseurl) {

        mUploadListener = listener;
        //        String url = new String(HttpBaseUrl.Companion.getBASE_URL() + mUrl);
        //        ProgressManager.getInstance().addRequestListener(
        //                url, new ProgressListener() {
        //                    @Override
        //                    public void onProgress(ProgressInfo progressInfo) {
        //                        listener.onRequestProgress(progressInfo.getCurrentbytes(), progressInfo.getContentLength());
        //                    }
        //
        //                    @Override
        //                    public void onError(long id, Exception e) {
        //
        //                    }
        //                });

        return postFile(t, callBack, baseurl);
    }

    //上传多张图片带参数
    public <T> Disposable postFiles(Class<T> t
            , final OnHttpCallBack<BaseBean<List<T>>> callBack, String... baseurl) {
        Map<String, RequestBody> photoMap = new HashMap<>();
        for (int i = 0; i < mFilePaths.size(); i++) {
            File file = new File(mFilePaths.get(i));
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse(mMediaType), file);
            photoMap.put(mFilesKey + file.getName(), requestFile);
        }

        Map<String, RequestBody> dataMap = new WeakHashMap<>();
        //遍历map中所有参数到builder
        if (mRequestMap != null) {
            for (String key : mRequestMap.keySet()) {
                dataMap.put(key, RequestBody.create(null, (String) mRequestMap.get(key)));
            }
        }
        return HttpManager.api(baseurl).postFiles(mUrl, dataMap, photoMap, mHeaderMap)
                .subscribeOn(Schedulers.io())//在新线程中执行请求
                .map(getListMap(t))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<List<T>>>() {
                    @Override
                    public void accept(BaseBean<List<T>> info) throws Exception {
                        if (info.isSuccess()) {
                            callBack.onSuccessful(info);
                        } else
                            callBack.onDataError(info.getMsg(), info);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callBack.onRequestError(ExceptionManager.handleException(throwable).message, throwable);
                    }
                });//在主线程中执行
    }


    //获取get的压缩请求对象
    public <T> Observable<BaseBean<T>> getGetZip(Class<T> t,
                                                 String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap).map(getMap(t));
    }


    //获取postJson的压缩请求对象
    public <T> Observable<BaseBean<T>> getPostJsonZip(Class<T> t
            , String... baseurl) {
        return HttpManager.api(baseurl).postJson(mUrl, mRequestMap, mHeaderMap).map(getMap(t));
    }


    //获取getList的压缩请求对象
    public <T> Observable<BaseBean<List<T>>> getListGetZip(Class<T> t
            , String... baseurl) {

        return HttpManager.api(baseurl).executeGet(mUrl, mRequestMap, mHeaderMap).map(getListMap(t));
    }


    private <T> Function<ResponseBody, BaseBean<T>> getMap(final Class<T> t) {
        return new Function<ResponseBody, BaseBean<T>>() {
            @Override
            public BaseBean<T> apply(ResponseBody responseBody) throws Exception {
                String string = responseBody.string();
                return handlerRequestBodyToBean(string, t);
            }
        };
    }

    /**
     * 把requestBody处理成BaseBean
     *
     * @param t
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> BaseBean<T> handlerRequestBodyToBean(String content, Class<T> t) throws Exception {

        //如果要转换的类是BaseBean的子类,那就直接转就好了
        if (BaseBean.class.isAssignableFrom(t)) {
            T t2 = new Gson().fromJson(content, t);
            BaseBean<T> info = new BaseBean<>();
            if (((BaseBean<?>) t2).getCode() == null) {
                info.setCode("1");
            } else {
                info.setCode(((BaseBean<?>) t2).getCode());
            }
            info.setMsg(((BaseBean<?>) t2).getMsg());
            info.setData(t2);
            return info;
        }


        Type type = new TypeToken<BaseBean<T>>() {
        }.getType();
        BaseBean<T> info = new Gson().fromJson(content, type);
        if (info == null) {
            info = new BaseBean<>();
            info.setCode("-999");
            info.setMsg("请求数据出错");
        }

        if (info.isSuccess()) {
            if (info.getData() == null && info.getValue() == null) {
                return info;
            }
            String jsonString = new Gson().toJson(info.getData() == null ? info.getValue() : info.getData());
            T t1 = new Gson().fromJson(jsonString, t);
            info.setData(t1);

        }
        return info;
    }

    private <T> Function<ResponseBody, BaseBean<List<T>>> getListMap(final Class<T> t) {
        return new Function<ResponseBody, BaseBean<List<T>>>() {
            @Override
            public BaseBean<List<T>> apply(ResponseBody responseBody) throws Exception {
                return handlerRequestBodyToList(responseBody.string(), t);
            }
        };
    }


    /**
     * 把requestBody处理成List<BaseBean>
     *
     * @param content
     * @param t
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> BaseBean<List<T>> handlerRequestBodyToList(String content, Class<T> t) throws Exception {

        //如果要转换的类是BaseBean的子类,那就直接报错,因为直接转bean就够用了
        if (BaseBean.class.isAssignableFrom(t)) {
            throw new Exception("要转换的类是BaseBean的子类,不支持 XXXGetList的方法");
        }

        Type type = new TypeToken<BaseBean<List<T>>>() {
        }.getType();
        BaseBean<List<T>> info = new Gson().fromJson(content, type);
        if (info == null) {
            info = new BaseBean<>();
            info.setCode("-999");
            info.setMsg("请求数据出错");
        }
        if (info.isSuccess()) {
            if (info.getData() == null && info.getValue() == null) {
                return info;
            }
            String jsonString = new Gson().toJson(info.getData() == null ? info.getValue() : info.getData());
            List<T> ts = HttpGsonUtil.gson2List(jsonString, t);
            info.setData(ts);

        }

        return info;
    }


    private MultipartBody.Part prepareFilePart(String partName, String path, String mediaType) {
        File file = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse(mediaType), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

}
