package com.chenming.httprequest.http.exception;

/**
 * Created by wqx on 2018/9/7.
 */
public class MsgException extends Exception {

    public int code;
    public String message;

    public MsgException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;

    }
}
