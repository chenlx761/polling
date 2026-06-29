package com.chenming.httprequest.http.exception;


import android.net.ParseException;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.chenming.httprequest.http.HttpManager;

import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;


/**
 * Created by wqx on 2018/9/7.
 */
public class ExceptionManager {

    //对应HTTP的状态码
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    public static MsgException handleException(Throwable e) {
        MsgException ex;
        if (e instanceof SocketTimeoutException) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpManager.clearAllRequest();
                }
            }).start();
            ex = new MsgException(e, Error.HTTP_ERROR);
            ex.message = "网络请求超时";

            return ex;
        } else if (e instanceof HttpException) {             //HTTP错误
            HttpException httpException = (HttpException) e;
            ex = new MsgException(e, Error.HTTP_ERROR);
            switch (httpException.code()) {
                case BAD_REQUEST:
                    ex.message = "parameter error";
                    break;
                case UNAUTHORIZED:
                case FORBIDDEN:
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex.message = "网络异常";  //均视为网络异常
                    break;
            }
            return ex;
        } else if (e instanceof ServerException) {    //服务器返回的异常
            ServerException resultException = (ServerException) e;
            ex = new MsgException(resultException, resultException.code);
            ex.message = resultException.message;
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            ex = new MsgException(e, Error.PARSE_ERROR);
            ex.message = "Analytic exception";            //均视为解析异常
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new MsgException(e, Error.NETWORK_ERROR);
            ex.message = "Connection exception";  //均视为网络异常
            return ex;
        } else if (e instanceof IOException) {
            ex = new MsgException(e, Error.NETWORK_ERROR);
            ex.message = e.getMessage();
            return ex;
        } else {
            ex = new MsgException(e, Error.UNKNOWN);
            if (TextUtils.isEmpty(e.getMessage()))
                ex.message = "Unknown exception";          //未知异常
            else
                ex.message = e.getMessage();
            return ex;
        }
    }
}
