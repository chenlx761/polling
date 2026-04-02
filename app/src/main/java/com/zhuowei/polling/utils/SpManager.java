package com.zhuowei.polling.utils;

import com.chenming.common.utils.SpUtils;
import com.zhuowei.polling.MyApplication;

public class SpManager {
    public static final String SP_TOKEN_KEY = "acc_token";
    public static final String SP_REFRESH_TOKEN_KEY = "refresh_token";


    public static String getToken() {
        return (String) SpUtils.getParam(MyApplication.getInstance(), SP_TOKEN_KEY, "");
    }


    public static void setToken(String token) {
        if (token == null) {
            token = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_TOKEN_KEY, token);
    }


    public static String getRefreshToken() {
        return (String) SpUtils.getParam(MyApplication.getInstance(), SP_REFRESH_TOKEN_KEY, "");
    }


    public static void setRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            refreshToken = "";
        }
        SpUtils.setParam(MyApplication.getInstance(), SP_REFRESH_TOKEN_KEY, refreshToken);
    }

}
