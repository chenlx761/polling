package com.chenming.httprequest.http.exception;

/**
 * Created by wqx on 2018/9/7.
 */
public class ServerException extends RuntimeException {

    public int code;
    public String message;
}
