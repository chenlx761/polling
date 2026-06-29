package com.chenming.httprequest.http.bean;

import android.text.TextUtils;

import com.chenming.httprequest.http.HttpManager;
import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

/**
 * Created by Admin on 2018/3/9.
 */

public class BaseBean<T> {
    private HeaderBean header;
    protected String status;
    protected String code;
    protected String msg;
    protected T data;
    protected T value;

    public static class HeaderBean {
        @SerializedName("code")
        private String codeX;
        @SerializedName("msg")
        private String msgX;
        private String data_server_time;
        private String uptime;

        public String getCodeX() {
            return codeX;
        }

        public void setCodeX(String codeX) {
            this.codeX = codeX;
        }

        public String getMsgX() {
            return msgX;
        }

        public void setMsgX(String msgX) {
            this.msgX = msgX;
        }

        public String getData_server_time() {
            return data_server_time;
        }

        public void setData_server_time(String data_server_time) {
            this.data_server_time = data_server_time;
        }

        public String getUptime() {
            return uptime;
        }

        public void setUptime(String uptime) {
            this.uptime = uptime;
        }
    }

    public HeaderBean getHeader() {
        return header;
    }

    public void setHeader(HeaderBean header) {
        this.header = header;
    }

    public boolean isSuccess() {

        if (header != null)
            return HttpManager.getRequestSuccessCode().equals(header.codeX);

        if (!TextUtils.isEmpty(status)) {
            return HttpManager.getRequestSuccessCode().equals(status);
        }

        return HttpManager.getRequestSuccessCode().equals(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
