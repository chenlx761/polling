package com.chenming.httprequest.http.listener;

/**
 * Created by wqx on 2018/4/25.
 */

public interface OnHttpCallBack<T> {

    void onSuccessful(T t);//成功了就回调这个方法,可以传递任何形式的数据给调用者

    void onDataError(String errorMsg, T t);//请求成功,但是后台告诉我失败了

    //请求失败的
    void onRequestError(String errorMsg, Throwable throwable);
}
